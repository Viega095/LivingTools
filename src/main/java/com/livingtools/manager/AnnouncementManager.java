package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Broadcasts de servidor para eventos importantes de LivingTools.
 * - Prestige de un jugador
 * - Primera muerte de un jefe
 * - Hito extremo (nivel 200, 5000 mobs)
 * - Crafteo de reliquia
 */
public class AnnouncementManager {

    private static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "✦ LivingTools" + ChatColor.DARK_GRAY + "] ";

    // -----------------------------------------------------------------------
    // Anuncios
    // -----------------------------------------------------------------------

    /** Broadcast al hacer Prestige */
    public static void announcePrestige(Player player, LivingTool tool, int newPrestige) {
        String toolName = getToolName(tool);
        String roman = romanNumeral(newPrestige);
        String line;

        if (newPrestige == 1) {
            line = ChatColor.YELLOW + player.getName() + ChatColor.WHITE
                    + " ha ascendido con " + toolName + ChatColor.WHITE
                    + " — " + ChatColor.GOLD + "¡Prestige " + roman + "!";
        } else if (newPrestige >= 3) {
            line = ChatColor.GOLD + "" + ChatColor.BOLD + "★ " + player.getName()
                    + ChatColor.GOLD + " ha alcanzado Prestige " + roman + " con " + toolName
                    + ChatColor.GOLD + " — ¡Una leyenda!";
        } else {
            line = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " ha alcanzado Prestige "
                    + roman + " con " + toolName + ChatColor.WHITE + "!";
        }

        broadcastAll(line);
        // Sonido para todos los jugadores en línea
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.0f);
        }
    }

    /** Broadcast al matar un jefe del plugin */
    public static void announceBossKill(Player player, String bossName, LivingTool tool) {
        String toolName = getToolName(tool);
        String line = ChatColor.RED + "☠ " + ChatColor.WHITE + player.getName()
                + ChatColor.GRAY + " ha derrotado a "
                + ChatColor.RED + "" + ChatColor.BOLD + bossName
                + ChatColor.GRAY + " con " + toolName + ChatColor.GRAY + "!";
        broadcastAll(line);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.3f, 1.5f);
        }
    }

    /** Broadcast al craftear una reliquia */
    public static void announceRelicCraft(Player player, String relicName) {
        String line = ChatColor.LIGHT_PURPLE + "💎 " + ChatColor.WHITE + player.getName()
                + ChatColor.GRAY + " ha forjado la reliquia "
                + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + relicName + ChatColor.GRAY + "!";
        broadcastAll(line);
    }

    /** Broadcast al completar un hito extremo (nivel 200 / 5000 mobs / etc.) */
    public static void announceMilestone(Player player, LivingTool tool, MilestoneManager.Milestone milestone) {
        // Solo anunciar hitos "grandes"
        switch (milestone) {
            case LEVEL_200:
            case MOBS_5000:
            case PRESTIGE_3:
            case BLOCKS_10000:
                break;
            default:
                return; // No anunciar hitos menores
        }
        String toolName = getToolName(tool);
        String line = ChatColor.GOLD + "✦ " + ChatColor.WHITE + player.getName()
                + ChatColor.GRAY + " ha completado el hito "
                + ChatColor.GOLD + "" + ChatColor.BOLD + milestone.getTitle()
                + ChatColor.GRAY + " con " + toolName + ChatColor.GRAY + "!";
        broadcastAll(line);
    }

    // -----------------------------------------------------------------------

    private static void broadcastAll(String message) {
        Bukkit.broadcastMessage(PREFIX + message);
    }

    private static String getToolName(LivingTool tool) {
        if (tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName()) {
            return tool.getItem().getItemMeta().getDisplayName();
        }
        return ChatColor.GRAY + "una herramienta";
    }

    private static String romanNumeral(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(n);
        }
    }
}
