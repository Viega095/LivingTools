package com.livingtools.manager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages boss bar cooldown visualizations for abilities
 */
public class CooldownBarManager {

    private static class CooldownBar {
        BossBar bar;
        long endTime;
        String abilityName;

        CooldownBar(BossBar bar, long end, String name) {
            this.bar = bar;
            this.endTime = end;
            this.abilityName = name;
        }
    }

    private static final Map<UUID, List<CooldownBar>> activeBars = new HashMap<>();
    private static final int MAX_BARS = 3;

    /**
     * Show cooldown bar for an ability
     */
    public static void showCooldown(Player player, String abilityName, long cooldownMillis) {
        UUID uuid = player.getUniqueId();

        // Get or create bar list
        List<CooldownBar> bars = activeBars.computeIfAbsent(uuid, k -> new ArrayList<>());

        // Remove if already exists for this ability
        bars.removeIf(cb -> cb.abilityName.equals(abilityName));

        // Check max limit
        if (bars.size() >= MAX_BARS) {
            // Remove oldest bar
            CooldownBar oldest = bars.remove(0);
            oldest.bar.removePlayer(player);
            oldest.bar.removeAll();
        }

        // Create new boss bar
        BarColor color = getColorForAbility(abilityName);
        BossBar bar = Bukkit.createBossBar(
                formatAbilityName(abilityName),
                color,
                BarStyle.SOLID);

        bar.addPlayer(player);
        bar.setProgress(1.0);
        bar.setVisible(true);

        long endTime = System.currentTimeMillis() + cooldownMillis;
        bars.add(new CooldownBar(bar, endTime, abilityName));

        // Start update task
        updateBars(player);
    }

    /**
     * Update all bars for a player
     */
    private static void updateBars(Player player) {
        Bukkit.getScheduler().runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
            UUID uuid = player.getUniqueId();
            List<CooldownBar> bars = activeBars.get(uuid);

            if (bars == null || bars.isEmpty()) {
                return;
            }

            long now = System.currentTimeMillis();
            Iterator<CooldownBar> iterator = bars.iterator();
            boolean hasActive = false;

            while (iterator.hasNext()) {
                CooldownBar cb = iterator.next();

                if (now >= cb.endTime) {
                    // Cooldown finished
                    cb.bar.removePlayer(player);
                    cb.bar.removeAll();
                    iterator.remove();
                } else {
                    // Update progress
                    long total = cb.endTime - (now - (cb.endTime - now));
                    long remaining = cb.endTime - now;
                    double progress = (double) remaining / (double) (cb.endTime - (now - remaining));

                    cb.bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

                    // Update title with remaining seconds
                    int seconds = (int) (remaining / 1000) + 1;
                    cb.bar.setTitle(formatAbilityName(cb.abilityName) + " - " + seconds + "s");

                    hasActive = true;
                }
            }

            // Continue updating if bars are still active
            if (hasActive && player.isOnline()) {
                updateBars(player);
            }
        }, 10L); // Update every 0.5s
    }

    /**
     * Clear all bars for a player
     */
    public static void clearBars(Player player) {
        UUID uuid = player.getUniqueId();
        List<CooldownBar> bars = activeBars.remove(uuid);

        if (bars != null) {
            for (CooldownBar cb : bars) {
                cb.bar.removePlayer(player);
                cb.bar.removeAll();
            }
        }
    }

    /**
     * Get bar color based on ability type
     */
    private static BarColor getColorForAbility(String abilityName) {
        if (abilityName.contains("blink") || abilityName.contains("shadow") || abilityName.contains("recall")) {
            return BarColor.PURPLE;
        } else if (abilityName.contains("fire") || abilityName.contains("inferno")) {
            return BarColor.RED;
        } else if (abilityName.contains("ice") || abilityName.contains("frost")) {
            return BarColor.BLUE;
        } else if (abilityName.contains("lightning") || abilityName.contains("thunder")) {
            return BarColor.YELLOW;
        } else if (abilityName.contains("vortex") || abilityName.contains("swap")) {
            return BarColor.PINK;
        }
        return BarColor.WHITE;
    }

    /**
     * Format ability name for display
     */
    private static String formatAbilityName(String abilityId) {
        // Convert ability_id to "Ability Name"
        String[] parts = abilityId.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                formatted.append(part.substring(1).toLowerCase());
            }
        }

        return formatted.toString();
    }
}
