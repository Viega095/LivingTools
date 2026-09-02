package com.livingtools.manager;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Monitors server performance and adjusts ability settings dynamically
 */
public class PerformanceOptimizer {

    private static double currentTPS = 20.0;
    private static int particleDensityMultiplier = 100; // Percentage

    /**
     * Start monitoring TPS
     */
    public static void startMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                currentTPS = getCurrentTPS();
                adjustSettings();
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 100); // Every 5 seconds
    }

    private static double getCurrentTPS() {
        // Simplified TPS calculation
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            Object tpsField = server.getClass().getField("recentTps").get(server);
            double[] tps = (double[]) tpsField;
            return tps[0]; // 1 minute average
        } catch (Exception e) {
            return 20.0; // Default to perfect TPS if can't read
        }
    }

    private static void adjustSettings() {
        if (currentTPS < 12) {
            // Critical - minimal particles, disable bars
            particleDensityMultiplier = 0;
        } else if (currentTPS < 15) {
            // Poor - reduce particles significantly
            particleDensityMultiplier = 25;
        } else if (currentTPS < 18) {
            // Low - reduce particles moderately
            particleDensityMultiplier = 50;
        } else {
            // Good - full quality
            particleDensityMultiplier = 100;
        }
    }

    /**
     * Get adjusted particle count
     */
    public static int getAdjustedParticleCount(int baseCount) {
        return (baseCount * particleDensityMultiplier) / 100;
    }

    /**
     * Check if boss bars should be shown
     */
    public static boolean shouldShowBossBars() {
        return currentTPS >= 12;
    }

    /**
     * Get current TPS
     */
    public static double getTPS() {
        return currentTPS;
    }
}
