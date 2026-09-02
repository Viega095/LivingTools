package com.livingtools.manager;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import com.livingtools.LivingToolsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages ability usage tracking, mastery levels, and XP progression.
 * Players gain XP for each ability use and unlock bonuses at specific mastery
 * levels.
 */
public class AbilityMasteryManager {

    private static final NamespacedKey MASTERY_KEY_PREFIX = new NamespacedKey(
            LivingToolsPlugin.getInstance(), "ability_mastery_");

    // XP thresholds for each mastery level (1-10)
    private static final int[] XP_THRESHOLDS = {
            0, // Level 1 (starting)
            10, // Level 2
            50, // Level 3 - Cooldown reduction
            150, // Level 4
            300, // Level 5 - Effectiveness boost
            500, // Level 6
            800, // Level 7 - Enhanced particles
            1200, // Level 8
            1700, // Level 9
            2500, // Level 10 - Awakened
            5000 // Max (cap)
    };

    /**
     * Add XP to a player's ability mastery
     */
    public static void addXP(Player player, String abilityId, int amount) {
        int currentXP = getXP(player, abilityId);
        int newXP = currentXP + amount;

        int oldLevel = getLevel(player, abilityId);
        setXP(player, abilityId, newXP);
        int newLevel = getLevel(player, abilityId);

        // Level up notification
        if (newLevel > oldLevel) {
            onLevelUp(player, abilityId, newLevel);
        }
    }

    /**
     * Get current XP for an ability
     */
    public static int getXP(Player player, String abilityId) {
        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(),
                "ability_xp_" + abilityId);
        Integer xp = player.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return xp != null ? xp : 0;
    }

    /**
     * Set XP for an ability
     */
    private static void setXP(Player player, String abilityId, int xp) {
        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(),
                "ability_xp_" + abilityId);
        player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, xp);
    }

    /**
     * Get mastery level for an ability (1-10)
     */
    public static int getLevel(Player player, String abilityId) {
        int xp = getXP(player, abilityId);

        for (int i = XP_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= XP_THRESHOLDS[i]) {
                return i;
            }
        }
        return 1;
    }

    /**
     * Get XP required for next level
     */
    public static int getXPForNextLevel(Player player, String abilityId) {
        int currentLevel = getLevel(player, abilityId);
        if (currentLevel >= 10) {
            return XP_THRESHOLDS[10]; // Max level
        }
        return XP_THRESHOLDS[currentLevel + 1];
    }

    /**
     * Get cooldown reduction bonus (applies at level 3+)
     */
    public static double getCooldownReduction(Player player, String abilityId) {
        int level = getLevel(player, abilityId);

        if (level >= 10)
            return 0.30; // 30% at awakened
        if (level >= 7)
            return 0.20; // 20% at level 7
        if (level >= 3)
            return 0.10; // 10% at level 3
        return 0.0;
    }

    /**
     * Get effectiveness multiplier (applies at level 5+)
     */
    public static double getEffectivenessBonus(Player player, String abilityId) {
        int level = getLevel(player, abilityId);

        if (level >= 10)
            return 0.25; // 25% at awakened
        if (level >= 7)
            return 0.20; // 20% at level 7
        if (level >= 5)
            return 0.15; // 15% at level 5
        return 0.0;
    }

    /**
     * Check if ability is awakened (level 10)
     */
    public static boolean isAwakened(Player player, String abilityId) {
        return getLevel(player, abilityId) >= 10;
    }

    /**
     * Get enhanced particle flag (applies at level 7+)
     */
    public static boolean hasEnhancedParticles(Player player, String abilityId) {
        return getLevel(player, abilityId) >= 7;
    }

    /**
     * Called when player levels up ability mastery
     */
    private static void onLevelUp(Player player, String abilityId, int newLevel) {
        // Notification
        player.sendMessage(org.bukkit.ChatColor.GOLD + "✦ " +
                org.bukkit.ChatColor.YELLOW + "Maestría de " + abilityId.toUpperCase() +
                " aumentada a nivel " + newLevel + "!");

        // Sound and particles
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);

        // Bonus notifications
        if (newLevel == 3) {
            player.sendMessage(org.bukkit.ChatColor.GREEN + "  → -10% Cooldown desbloqueado");
        }
        if (newLevel == 5) {
            player.sendMessage(org.bukkit.ChatColor.GREEN + "  → +15% Efectividad desbloqueada");
        }
        if (newLevel == 7) {
            player.sendMessage(org.bukkit.ChatColor.GREEN + "  → Efectos de partículas mejorados");
        }
        if (newLevel == 10) {
            player.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "  ★ AWAKENED - Poder máximo alcanzado! ★");
        }

        // Fire event for other systems
        // Could trigger personality dialogue, achievements, etc.
    }

    /**
     * Reset mastery for an ability (admin use)
     */
    public static void resetMastery(Player player, String abilityId) {
        setXP(player, abilityId, 0);
        player.sendMessage(org.bukkit.ChatColor.YELLOW + "Maestría de " + abilityId + " restablecida.");
    }

    /**
     * Set mastery level directly (admin use)
     */
    public static void setLevel(Player player, String abilityId, int level) {
        if (level < 1)
            level = 1;
        if (level > 10)
            level = 10;

        int targetXP = XP_THRESHOLDS[level];
        setXP(player, abilityId, targetXP);
        player.sendMessage(org.bukkit.ChatColor.YELLOW + "Maestría de " + abilityId +
                " establecida en nivel " + level);
    }
}
