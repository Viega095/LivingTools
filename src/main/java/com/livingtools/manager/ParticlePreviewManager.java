package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shows particle preview at blink destination while sneaking
 */
public class ParticlePreviewManager {

    private static final Set<UUID> activePreviewers = new HashSet<>();
    private static BukkitRunnable previewTask;

    /**
     * Start the preview task (called once on plugin enable)
     */
    public static void start() {
        if (previewTask != null) {
            previewTask.cancel();
        }

        previewTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isSneaking()) {
                        showPreview(player);
                    }
                }
            }
        };

        previewTask.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 5); // Every 5 ticks
    }

    /**
     * Stop the preview task
     */
    public static void stop() {
        if (previewTask != null) {
            previewTask.cancel();
            previewTask = null;
        }
    }

    /**
     * Show preview for a player
     */
    private static void showPreview(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item)) {
            return;
        }

        LivingTool tool = new LivingTool(item);

        // Only show for blink-like abilities
        if (!tool.hasAbility("blink") && !tool.hasAbility("shadowstep") &&
                !tool.hasAbility("blinkstrike") && !tool.hasAbility("thunderstep")) {
            return;
        }

        // Calculate destination
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();
        double maxRange = 10.0; // Approximate

        Location dest = findDestination(start, direction, maxRange);

        if (dest == null) {
            return;
        }

        // Check if safe
        boolean safe = isValidDestination(dest);

        // Show particles
        Particle particle = safe ? Particle.VILLAGER_HAPPY : Particle.REDSTONE;

        if (safe) {
            player.spawnParticle(particle, dest, 5, 0.3, 0.3, 0.3, 0);
        } else {
            player.spawnParticle(particle, dest, 5, 0.3, 0.3, 0.3, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0f));
        }
    }

    private static Location findDestination(Location start, Vector direction, double maxRange) {
        Location target = start.clone();
        Location lastValid = start.clone();

        for (double i = 0; i < maxRange; i += 0.5) {
            Location next = target.clone().add(direction.clone().multiply(0.5));

            if (next.getBlock().getType().isSolid()) {
                break;
            }

            lastValid = next.clone();
            target = next;
        }

        if (lastValid.distance(start) > 1.0) {
            return lastValid;
        }

        return null;
    }

    private static boolean isValidDestination(Location loc) {
        org.bukkit.block.Block feet = loc.getBlock();
        org.bukkit.block.Block head = loc.clone().add(0, 1, 0).getBlock();

        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }

        // Check ground
        for (int i = 0; i < 4; i++) {
            org.bukkit.block.Block below = loc.clone().subtract(0, i, 0).getBlock();
            if (below.getType().isSolid() && below.getType() != Material.LAVA) {
                return true;
            }
        }

        return false;
    }
}
