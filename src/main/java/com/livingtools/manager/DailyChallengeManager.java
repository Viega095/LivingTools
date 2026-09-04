package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Sistema de Retos Diarios.
 * Cada día se asignan 3 retos aleatorios por herramienta.
 * Completarlos da XP bonus y progresión de reto.
 *
 * Los retos se resetean a medianoche del servidor.
 * Los datos de progreso se guardan en el PDC del item.
 */
public class DailyChallengeManager {

    private static final NamespacedKey KEY_CHALLENGES =
            new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "daily_challenges");
    private static final NamespacedKey KEY_CHALLENGE_DAY =
            new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "challenge_day");
    private static final NamespacedKey KEY_CHALLENGE_PROGRESS =
            new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "challenge_progress");

    // -----------------------------------------------------------------------
    // Tipos de retos
    // -----------------------------------------------------------------------
    public enum ChallengeType {
        MINE_STONE       ("Extractor", "Mina {n} bloques de piedra o deepslate", 50, 200),
        MINE_ORE         ("Minero Experto", "Mina {n} minerales", 10, 200),
        MINE_DIAMOND     ("Cazador de Diamantes", "Mina {n} diamantes", 3, 500),
        KILL_MOBS        ("Cazador", "Mata {n} mobs hostiles", 15, 150),
        KILL_UNDEAD      ("Purificador", "Mata {n} mobs no-muertos (zombies/esqueletos)", 10, 200),
        KILL_BOSS        ("Defensor", "Mata {n} mini-boss o boss", 1, 1000),
        KILL_PLAYERS     ("Guerrero", "Mata {n} jugadores en PvP", 3, 400),
        FEED_TOOL        ("Chef", "Alimenta tu herramienta {n} veces", 5, 100),
        GAIN_XP          ("Estudioso", "Gana {n} XP con esta herramienta", 500, 150),
        SURVIVE_NIGHT    ("Vigilante", "Sobrevive {n} noches (usa la herramienta de noche)", 1, 200),
        USE_ABILITIES    ("Maestro", "Usa habilidades especiales {n} veces", 5, 250);

        private final String title;
        private final String template;
        private final int target;
        private final long xpReward;

        ChallengeType(String title, String template, int target, long xpReward) {
            this.title = title;
            this.template = template;
            this.target = target;
            this.xpReward = xpReward;
        }

        public String getTitle() { return title; }
        public String getDescription() { return template.replace("{n}", String.valueOf(target)); }
        public int getTarget() { return target; }
        public long getXpReward() { return xpReward; }
    }

    // -----------------------------------------------------------------------
    // API pública — llamar desde listeners
    // -----------------------------------------------------------------------

    /** Llamar desde ExperienceListener al minar un bloque */
    public static void onBlockMined(Player player, LivingTool tool, Material blockType) {
        String name = blockType.name();
        if (name.contains("ORE")) {
            progress(player, tool, ChallengeType.MINE_ORE, 1);
            if (name.contains("DIAMOND")) {
                progress(player, tool, ChallengeType.MINE_DIAMOND, 1);
            }
        }
        if (name.contains("STONE") || name.contains("DEEPSLATE") || name.contains("COBBLESTONE")) {
            progress(player, tool, ChallengeType.MINE_STONE, 1);
        }
    }

    /** Llamar desde ExperienceListener al matar una entidad */
    public static void onKill(Player player, LivingTool tool, org.bukkit.entity.EntityType type) {
        progress(player, tool, ChallengeType.KILL_MOBS, 1);
        if (type == org.bukkit.entity.EntityType.ZOMBIE
                || type == org.bukkit.entity.EntityType.SKELETON
                || type == org.bukkit.entity.EntityType.ZOMBIE_VILLAGER
                || type == org.bukkit.entity.EntityType.WITHER_SKELETON
                || type == org.bukkit.entity.EntityType.DROWNED
                || type == org.bukkit.entity.EntityType.PHANTOM) {
            progress(player, tool, ChallengeType.KILL_UNDEAD, 1);
        }
    }

    public static void onPlayerKill(Player player, LivingTool tool) {
        progress(player, tool, ChallengeType.KILL_PLAYERS, 1);
    }

    public static void onFeed(Player player, LivingTool tool) {
        progress(player, tool, ChallengeType.FEED_TOOL, 1);
    }

    public static void onXPGained(Player player, LivingTool tool, long xp) {
        progress(player, tool, ChallengeType.GAIN_XP, (int) Math.min(xp, Integer.MAX_VALUE));
    }

    public static void onAbilityUsed(Player player, LivingTool tool) {
        progress(player, tool, ChallengeType.USE_ABILITIES, 1);
    }

    // -----------------------------------------------------------------------
    // Core logic
    // -----------------------------------------------------------------------

    /** Muestra los retos activos del día en el chat */
    public static void showChallenges(Player player, LivingTool tool) {
        List<ChallengeType> challenges = getTodayChallenges(tool);
        Map<ChallengeType, Integer> progress = getProgress(tool);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "═══ Retos Diarios ═══");
        for (ChallengeType c : challenges) {
            int prog = progress.getOrDefault(c, 0);
            int target = c.getTarget();
            boolean done = prog >= target;
            String bar = buildMiniBar(prog, target);
            String mark = done ? ChatColor.GREEN + "✔ " : ChatColor.YELLOW + "◆ ";
            player.sendMessage(mark + ChatColor.WHITE + c.getTitle()
                    + ChatColor.GRAY + " — " + c.getDescription());
            player.sendMessage("  " + bar + " " + ChatColor.GRAY + prog + "/" + target
                    + (done ? ChatColor.GREEN + " ✔ ¡Completado!" : "") );
            if (done) player.sendMessage("  " + ChatColor.GREEN + "+" + c.getXpReward() + " XP ya otorgados");
        }
        player.sendMessage("");
    }

    private static void progress(Player player, LivingTool tool, ChallengeType type, int amount) {
        List<ChallengeType> today = getTodayChallenges(tool);
        if (!today.contains(type)) return;

        Map<ChallengeType, Integer> prog = getProgress(tool);
        int current = prog.getOrDefault(type, 0);
        if (current >= type.getTarget()) return; // ya completado

        int newVal = current + amount;
        prog.put(type, newVal);
        saveProgress(tool, prog);

        if (newVal >= type.getTarget()) {
            // Completar reto
            tool.addXP(player, type.getXpReward());
            player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡Reto Completado! "
                    + ChatColor.YELLOW + type.getTitle());
            player.sendMessage(ChatColor.GREEN + "  +" + type.getXpReward() + " XP de bonus");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0);
        }
    }

    // -----------------------------------------------------------------------
    // Persistencia
    // -----------------------------------------------------------------------

    private static List<ChallengeType> getTodayChallenges(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return generateAndSave(tool);
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        int today = getDayOfYear();
        int savedDay = meta.getPersistentDataContainer().getOrDefault(KEY_CHALLENGE_DAY, PersistentDataType.INTEGER, -1);
        if (savedDay != today) {
            // Nuevo día — generar nuevos retos y resetear progreso
            return generateAndSave(tool);
        }
        String raw = meta.getPersistentDataContainer().getOrDefault(KEY_CHALLENGES, PersistentDataType.STRING, "");
        if (raw.isEmpty()) return generateAndSave(tool);
        List<ChallengeType> list = new ArrayList<>();
        for (String s : raw.split(",")) {
            try { list.add(ChallengeType.valueOf(s)); } catch (Exception ignored) {}
        }
        return list.isEmpty() ? generateAndSave(tool) : list;
    }

    private static List<ChallengeType> generateAndSave(LivingTool tool) {
        ChallengeType[] all = ChallengeType.values();
        List<ChallengeType> shuffled = new ArrayList<>(Arrays.asList(all));
        Collections.shuffle(shuffled);
        List<ChallengeType> selected = shuffled.subList(0, Math.min(3, shuffled.size()));

        if (!tool.getItem().hasItemMeta()) return selected;
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        StringBuilder sb = new StringBuilder();
        for (ChallengeType c : selected) {
            if (sb.length() > 0) sb.append(",");
            sb.append(c.name());
        }
        meta.getPersistentDataContainer().set(KEY_CHALLENGES, PersistentDataType.STRING, sb.toString());
        meta.getPersistentDataContainer().set(KEY_CHALLENGE_DAY, PersistentDataType.INTEGER, getDayOfYear());
        // Reset progress for new day
        meta.getPersistentDataContainer().set(KEY_CHALLENGE_PROGRESS, PersistentDataType.STRING, "");
        tool.getItem().setItemMeta(meta);
        return selected;
    }

    private static Map<ChallengeType, Integer> getProgress(LivingTool tool) {
        Map<ChallengeType, Integer> map = new HashMap<>();
        if (!tool.getItem().hasItemMeta()) return map;
        String raw = tool.getItem().getItemMeta().getPersistentDataContainer()
                .getOrDefault(KEY_CHALLENGE_PROGRESS, PersistentDataType.STRING, "");
        if (raw.isEmpty()) return map;
        for (String pair : raw.split(";")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try { map.put(ChallengeType.valueOf(kv[0]), Integer.parseInt(kv[1])); }
                catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static void saveProgress(LivingTool tool, Map<ChallengeType, Integer> prog) {
        if (!tool.getItem().hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ChallengeType, Integer> e : prog.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(e.getKey().name()).append("=").append(e.getValue());
        }
        meta.getPersistentDataContainer().set(KEY_CHALLENGE_PROGRESS, PersistentDataType.STRING, sb.toString());
        tool.getItem().setItemMeta(meta);
    }

    private static int getDayOfYear() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_YEAR) + c.get(Calendar.YEAR) * 1000;
    }

    private static String buildMiniBar(int current, int target) {
        int filled = target > 0 ? Math.min(10, (current * 10) / target) : 10;
        StringBuilder sb = new StringBuilder(ChatColor.GRAY + "  [");
        for (int i = 0; i < 10; i++) {
            sb.append(i < filled ? ChatColor.YELLOW + "▌" : ChatColor.DARK_GRAY + "▌");
        }
        sb.append(ChatColor.GRAY + "]");
        return sb.toString();
    }
}
