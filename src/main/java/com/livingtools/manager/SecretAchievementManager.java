package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * SecretAchievementManager — logros ocultos que se revelan al conseguirse.
 *
 * Los jugadores no saben de antemano qué logros existen. Cuando se cumplen
 * las condiciones, aparece el mensaje "¡Logro Secreto Desbloqueado!" con
 * el nombre y descripción del logro.
 *
 * Logros disponibles (almacenados en PDC de la herramienta):
 *
 *   FIRST_BLOOD       — Primera muerte de mob con la herramienta
 *   NIGHT_OWL         — Matar 50 mobs de noche
 *   DEEP_DIGGER       — Minar a Y < -50
 *   STREAK_HUNTER     — Conseguir racha de 15 kills
 *   SLEEPY_WARRIOR    — Usar bonus de sueño por primera vez
 *   RELIC_SEEKER      — Encontrar tu primer fragmento de reliquia
 *   TITLE_EARNED      — Conseguir tu primer título
 *   ANCIENT_DIGGER    — Minar Ancient Debris con la herramienta
 *   GRIND_MASTER      — Acumular 10,000 XP total en una sesión
 *   BIOME_TRAVELER    — Minar en 10 biomas diferentes
 *   TREASURE_HUNTER   — Encontrar tu primer tesoro con el encantamiento
 *   VEIN_KING         — Romper 64 bloques de un solo Rompevenas
 *   PRESTIGE_RUSH     — Hacer prestige antes del día 7 de la herramienta
 */
public class SecretAchievementManager implements Listener {

    public enum SecretAchievement {
        FIRST_BLOOD("¡Primera Sangre!", "Primera criatura eliminada con la herramienta."),
        NIGHT_OWL("Búho Nocturno", "50 criaturas eliminadas durante la noche."),
        DEEP_DIGGER("Minero de las Profundidades", "Minado a Y < -50 con la herramienta."),
        STREAK_HUNTER("Imparable", "Conseguida una racha de 15 kills consecutivos."),
        SLEEPY_WARRIOR("Guerrero Descansado", "Bonus de sueño activado por primera vez."),
        RELIC_SEEKER("Buscador de Reliquias", "Primer fragmento de reliquia obtenido."),
        TITLE_EARNED("El Título", "Primer título desbloqueado."),
        ANCIENT_DIGGER("Excavador Ancestral", "Ancient Debris minado con la herramienta."),
        GRIND_MASTER("Maestro del Grindeo", "10,000 XP ganados en una sola sesión."),
        BIOME_TRAVELER("Viajero de Biomas", "Minado en 10 biomas diferentes."),
        TREASURE_HUNTER("Cazatesoros", "Primer tesoro descubierto con el encantamiento."),
        VEIN_KING("Rey de las Venas", "64+ bloques rotos de un solo Rompevenas."),
        PRESTIGE_RUSH("Ascenso Relámpago", "Prestige realizado antes del día 7.");

        final String title;
        final String description;

        SecretAchievement(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    // PDC key — lazy
    private static NamespacedKey KEY_ACHIEVEMENTS;
    private static NamespacedKey keyAchievements() {
        if (KEY_ACHIEVEMENTS == null)
            KEY_ACHIEVEMENTS = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "secret_achievements");
        return KEY_ACHIEVEMENTS;
    }

    // Session XP tracking (in-memory)
    private static final Map<UUID, Long> sessionXP = new HashMap<>();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Otorga un logro si no fue obtenido ya. Devuelve true si es nuevo.
     */
    public static boolean grant(Player player, LivingTool tool, SecretAchievement achievement) {
        if (hasAchievement(tool, achievement)) return false;

        // Guardar en PDC
        ItemStack item = tool.getItem();
        if (!item.hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String current = meta.getPersistentDataContainer()
                .getOrDefault(keyAchievements(), org.bukkit.persistence.PersistentDataType.STRING, "");
        String updated = current.isEmpty() ? achievement.name() : current + "," + achievement.name();
        meta.getPersistentDataContainer().set(keyAchievements(),
                org.bukkit.persistence.PersistentDataType.STRING, updated);
        item.setItemMeta(meta);

        // Anunciar
        announceAchievement(player, achievement);
        return true;
    }

    public static boolean hasAchievement(LivingTool tool, SecretAchievement achievement) {
        if (!tool.getItem().hasItemMeta()) return false;
        String saved = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(keyAchievements(), org.bukkit.persistence.PersistentDataType.STRING);
        if (saved == null) return false;
        for (String s : saved.split(",")) {
            if (s.equals(achievement.name())) return true;
        }
        return false;
    }

    public static List<SecretAchievement> getAchievements(LivingTool tool) {
        List<SecretAchievement> list = new ArrayList<>();
        if (!tool.getItem().hasItemMeta()) return list;
        String saved = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(keyAchievements(), org.bukkit.persistence.PersistentDataType.STRING);
        if (saved == null || saved.isEmpty()) return list;
        for (String s : saved.split(",")) {
            try { list.add(SecretAchievement.valueOf(s)); } catch (Exception ignored) {}
        }
        return list;
    }

    public static int countAchievements(LivingTool tool) {
        return getAchievements(tool).size();
    }

    // -----------------------------------------------------------------------
    // Event listeners
    // -----------------------------------------------------------------------

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;
        LivingTool tool = new LivingTool(held);

        // FIRST_BLOOD
        grant(player, tool, SecretAchievement.FIRST_BLOOD);

        // NIGHT_OWL — 50 kills de noche
        if (player.getWorld().getTime() > 13000 || player.getWorld().getTime() < 500) {
            long kills = tool.getData().getMobKills();
            if (kills >= 50) grant(player, tool, SecretAchievement.NIGHT_OWL);
        }

        // STREAK_HUNTER — racha 15+
        if (KillStreakManager.getStreak(player) >= 15) {
            grant(player, tool, SecretAchievement.STREAK_HUNTER);
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;
        LivingTool tool = new LivingTool(held);

        // DEEP_DIGGER
        if (event.getBlock().getY() < -50) {
            grant(player, tool, SecretAchievement.DEEP_DIGGER);
        }

        // ANCIENT_DIGGER
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            grant(player, tool, SecretAchievement.ANCIENT_DIGGER);
        }

        // BIOME_TRAVELER — 10+ biomas
        int biomeCount = ToolMemoryManager.getBiomeHistory(tool).size();
        if (biomeCount >= 10) {
            grant(player, tool, SecretAchievement.BIOME_TRAVELER);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sessionXP.put(event.getPlayer().getUniqueId(), 0L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessionXP.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Llamado desde ExperienceListener al ganar XP.
     */
    public static void onXPGained(Player player, LivingTool tool, long amount) {
        UUID uuid = player.getUniqueId();
        long current = sessionXP.getOrDefault(uuid, 0L) + amount;
        sessionXP.put(uuid, current);

        // GRIND_MASTER — 10,000 XP en sesión
        if (current >= 10_000) {
            grant(player, tool, SecretAchievement.GRIND_MASTER);
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static void announceAchievement(Player player, SecretAchievement achievement) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ " + ChatColor.BOLD + "¡LOGRO SECRETO DESBLOQUEADO!"
                + ChatColor.DARK_PURPLE + " ✦");
        player.sendMessage(ChatColor.GOLD + "  " + achievement.title);
        player.sendMessage(ChatColor.GRAY + "  " + achievement.description);
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                player.getLocation().add(0, 2, 0), 30, 0.5, 0.5, 0.5, 0.1);
    }
}
