package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Sistema de Hitos — recompensa puntos clave del progreso de la herramienta.
 * Cada hito otorga bonus de XP, mensajes especiales y efectos visuales.
 */
public class MilestoneManager {

    private static final org.bukkit.NamespacedKey KEY_MILESTONES =
            new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "completed_milestones");

    // -----------------------------------------------------------------------
    // Definición de hitos
    // -----------------------------------------------------------------------

    public enum Milestone {
        // Niveles
        LEVEL_10      ("primer_nivel10",   "Aprendiz Despierto",      "¡Nivel 10 alcanzado!",            500,  "10 niveles"),
        LEVEL_25      ("primer_nivel25",   "Guerrero en Formación",   "¡Nivel 25 alcanzado!",           1500,  "25 niveles"),
        LEVEL_50      ("primer_nivel50",   "Veterano Forjado",        "¡Nivel 50 alcanzado!",           5000,  "50 niveles"),
        LEVEL_100     ("primer_nivel100",  "Maestro del Acero",       "¡Nivel 100 alcanzado!",         15000,  "100 niveles"),
        LEVEL_200     ("primer_nivel200",  "Leyenda Viviente",        "¡Nivel 200 — listo para ascender!", 50000, "200 niveles"),

        // Mobs
        MOBS_10       ("mobs_10",          "Primer Combate",          "¡10 mobs eliminados!",             100,  "10 mobs"),
        MOBS_50       ("mobs_50",          "Cazador Novato",          "¡50 mobs eliminados!",             500,  "50 mobs"),
        MOBS_250      ("mobs_250",         "Cazador Experto",         "¡250 mobs eliminados!",           2500,  "250 mobs"),
        MOBS_1000     ("mobs_1000",        "Exterminador",            "¡1000 mobs eliminados!",         10000,  "1000 mobs"),
        MOBS_5000     ("mobs_5000",        "El Apocalipsis",          "¡5000 mobs eliminados!",         50000,  "5000 mobs"),

        // Bloques minados
        BLOCKS_100    ("blocks_100",       "Minero Aficionado",       "¡100 bloques minados!",            200,  "100 bloques"),
        BLOCKS_1000   ("blocks_1000",      "Minero Profesional",      "¡1000 bloques minados!",          2000,  "1000 bloques"),
        BLOCKS_10000  ("blocks_10000",     "Excavador Incansable",    "¡10000 bloques minados!",        20000,  "10000 bloques"),

        // Prestige
        PRESTIGE_1    ("prestige_1",       "El Renacido",             "¡Primer Prestige completado!",   25000,  "Prestige I"),
        PRESTIGE_3    ("prestige_3",       "El Inmortal",             "¡Tercer Prestige completado!",   75000,  "Prestige III"),

        // PvP
        PVP_1         ("pvp_1",            "Primera Sangre",          "¡Primera victoria PvP!",           300,  "1 jugador"),
        PVP_10        ("pvp_10",           "El Implacable",           "¡10 jugadores eliminados!",       5000,  "10 jugadores"),

        // Acciones pacíficas
        PEACEFUL_10   ("peaceful_10",      "Alma Bondadosa",          "¡10 acciones pacíficas!",          500,  "10 acciones");

        private final String id;
        private final String title;
        private final String description;
        private final long xpBonus;
        private final String requirement;

        Milestone(String id, String title, String description, long xpBonus, String requirement) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.xpBonus = xpBonus;
            this.requirement = requirement;
        }

        public String getId()          { return id; }
        public String getTitle()       { return title; }
        public String getDescription() { return description; }
        public long getXpBonus()       { return xpBonus; }
        public String getRequirement() { return requirement; }
    }

    // -----------------------------------------------------------------------
    // Verificación principal — llamar después de cada acción relevante
    // -----------------------------------------------------------------------

    public static void checkAll(Player player, LivingTool tool) {
        ToolData data = tool.getData();
        Set<String> done = getCompleted(tool);

        for (Milestone m : Milestone.values()) {
            if (done.contains(m.getId())) continue;
            if (meetsCondition(m, data)) {
                complete(player, tool, m, done);
            }
        }
    }

    private static boolean meetsCondition(Milestone m, ToolData data) {
        switch (m) {
            case LEVEL_10:   return data.getLevel() >= 10;
            case LEVEL_25:   return data.getLevel() >= 25;
            case LEVEL_50:   return data.getLevel() >= 50;
            case LEVEL_100:  return data.getLevel() >= 100;
            case LEVEL_200:  return data.getLevel() >= 200;
            case MOBS_10:    return data.getMobKills() >= 10;
            case MOBS_50:    return data.getMobKills() >= 50;
            case MOBS_250:   return data.getMobKills() >= 250;
            case MOBS_1000:  return data.getMobKills() >= 1000;
            case MOBS_5000:  return data.getMobKills() >= 5000;
            case BLOCKS_100: return data.getBlocksMined() >= 100;
            case BLOCKS_1000:return data.getBlocksMined() >= 1000;
            case BLOCKS_10000:return data.getBlocksMined() >= 10000;
            case PRESTIGE_1: return data.getPrestige() >= 1;
            case PRESTIGE_3: return data.getPrestige() >= 3;
            case PVP_1:      return data.getPlayerKills() >= 1;
            case PVP_10:     return data.getPlayerKills() >= 10;
            case PEACEFUL_10:return data.getPeacefulActions() >= 10;
            default: return false;
        }
    }

    private static void complete(Player player, LivingTool tool, Milestone m, Set<String> done) {
        done.add(m.getId());
        saveCompleted(tool, done);

        // Give XP bonus
        tool.addXP(player, m.getXpBonus());

        // Announce
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡HITO DESBLOQUEADO!" + ChatColor.GOLD + " ✦");
        player.sendMessage(ChatColor.YELLOW + "  " + ChatColor.BOLD + m.getTitle());
        player.sendMessage(ChatColor.GRAY + "  " + m.getDescription());
        player.sendMessage(ChatColor.GREEN + "  +" + formatXP(m.getXpBonus()) + " XP de Bonus");
        player.sendMessage("");

        // Title on screen
        player.sendTitle(
                ChatColor.GOLD + "" + ChatColor.BOLD + "¡Hito!",
                ChatColor.YELLOW + m.getTitle(),
                10, 70, 20);

        // Sound + particles
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 20, 1, 1, 1, 0);

        // Server-wide broadcast for major milestones
        AnnouncementManager.announceMilestone(player, tool, m);
    }

    // -----------------------------------------------------------------------
    // Persistencia — guarda IDs de hitos completados en el PDC del item
    // -----------------------------------------------------------------------

    private static Set<String> getCompleted(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return new HashSet<>();
        String raw = tool.getItem().getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(KEY_MILESTONES, PersistentDataType.STRING, "");
        if (raw.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(raw.split(",")));
    }

    private static void saveCompleted(LivingTool tool, Set<String> done) {
        if (!tool.getItem().hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        meta.getPersistentDataContainer().set(KEY_MILESTONES, PersistentDataType.STRING,
                String.join(",", done));
        tool.getItem().setItemMeta(meta);
    }

    /** Devuelve cuántos hitos ha completado esta herramienta. */
    public static int countCompleted(LivingTool tool) {
        return getCompleted(tool).size();
    }

    /** Comprueba si un hito específico está completado. */
    public static boolean isCompleted(LivingTool tool, Milestone m) {
        return getCompleted(tool).contains(m.getId());
    }

    private static String formatXP(long xp) {
        if (xp >= 1_000_000) return String.format("%.1fM", xp / 1_000_000.0);
        if (xp >= 1_000)     return String.format("%.1fK", xp / 1_000.0);
        return String.valueOf(xp);
    }
}
