package com.livingtools.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.livingtools.LivingToolsPlugin;

/**
 * Tracks detailed statistics for each ability per player
 */
public class AbilityStatisticsManager {

    /**
     * Record ability use
     */
    public static void recordUse(Player player, String abilityId) {
        incrementStat(player, abilityId, "uses");
        AbilityMasteryManager.addXP(player, abilityId, 1); // Link to mastery
    }

    /**
     * Record damage dealt by ability
     */
    public static void recordDamage(Player player, String abilityId, double damage) {
        addToStat(player, abilityId, "damage", damage);
    }

    /**
     * Record enemy killed with ability
     */
    public static void recordKill(Player player, String abilityId) {
        incrementStat(player, abilityId, "kills");
    }

    /**
     * Record distance traveled with ability
     */
    public static void recordDistance(Player player, String abilityId, double distance) {
        addToStat(player, abilityId, "distance", distance);
    }

    /**
     * Record successful combo
     */
    public static void recordCombo(Player player, String abilityId) {
        incrementStat(player, abilityId, "combos");
    }

    /**
     * Get total uses
     */
    public static int getTotalUses(Player player, String abilityId) {
        return getStat(player, abilityId, "uses");
    }

    /**
     * Get total damage
     */
    public static double getTotalDamage(Player player, String abilityId) {
        return getDoubleStat(player, abilityId, "damage");
    }

    /**
     * Get total kills
     */
    public static int getTotalKills(Player player, String abilityId) {
        return getStat(player, abilityId, "kills");
    }

    /**
     * Get total distance
     */
    public static double getTotalDistance(Player player, String abilityId) {
        return getDoubleStat(player, abilityId, "distance");
    }

    /**
     * Get total combos
     */
    public static int getTotalCombos(Player player, String abilityId) {
        return getStat(player, abilityId, "combos");
    }

    /**
     * Get most used ability
     */
    public static String getTopAbility(Player player) {
        // Simple implementation - can be expanded
        String topAbility = "none";
        int maxUses = 0;

        for (String abilityId : new String[] { "blink", "shadowstep", "recall", "swap", "blinkstrike",
                "vortex", "thunderstep", "phaseshift", "infernoleap" }) {
            int uses = getTotalUses(player, abilityId);
            if (uses > maxUses) {
                maxUses = uses;
                topAbility = abilityId;
            }
        }

        return topAbility;
    }

    // Helper methods
    private static void incrementStat(Player player, String abilityId, String stat) {
        int current = getStat(player, abilityId, stat);
        setStat(player, abilityId, stat, current + 1);
    }

    private static void addToStat(Player player, String abilityId, String stat, double value) {
        double current = getDoubleStat(player, abilityId, stat);
        setDoubleStat(player, abilityId, stat, current + value);
    }

    private static int getStat(Player player, String abilityId, String stat) {
        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(),
                "ability_stat_" + abilityId + "_" + stat);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Integer value = pdc.get(key, PersistentDataType.INTEGER);
        return value != null ? value : 0;
    }

    private static void setStat(Player player, String abilityId, String stat, int value) {
        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(),
                "ability_stat_" + abilityId + "_" + stat);
        player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
    }

    private static double getDoubleStat(Player player, String abilityId, String stat) {
        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(),
                "ability_stat_" + abilityId + "_" + stat);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Double value = pdc.get(key, PersistentDataType.DOUBLE);
        return value != null ? value : 0.0;
    }

    private static void setDoubleStat(Player player, String abilityId, String stat, double value) {
        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(),
                "ability_stat_" + abilityId + "_" + stat);
        player.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
    }

    /**
     * Clear all stats for an ability (admin/debug)
     */
    public static void clearStats(Player player, String abilityId) {
        for (String stat : new String[] { "uses", "damage", "kills", "distance", "combos" }) {
            setStat(player, abilityId, stat, 0);
            setDoubleStat(player, abilityId, stat, 0.0);
        }
    }
}
