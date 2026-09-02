package com.livingtools.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles ability interactions with the world (doors, glass, torches, etc.)
 */
public class AbilityWorldInteractionListener implements Listener {

    private static final Set<UUID> recentBlinkers = new HashSet<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }

        Player player = event.getPlayer();

        // Track for blink-based interactions
        recentBlinkers.add(player.getUniqueId());

        // Schedule interaction check
        org.bukkit.Bukkit.getScheduler().runTaskLater(
                com.livingtools.LivingToolsPlugin.getInstance(), () -> {
                    if (recentBlinkers.remove(player.getUniqueId())) {
                        handleBlinkInteractions(player, event.getTo());
                    }
                }, 1L);
    }

    private void handleBlinkInteractions(Player player, org.bukkit.Location loc) {
        if (loc == null)
            return;

        // Open iron doors in 3-block radius
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();

                    if (block.getType() == Material.IRON_DOOR) {
                        org.bukkit.block.data.Openable door = (org.bukkit.block.data.Openable) block.getBlockData();
                        if (!door.isOpen()) {
                            door.setOpen(true);
                            block.setBlockData(door);
                            player.playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 0.5f, 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when BlinkStrike is used - breaks glass in radius
     */
    public static void handleBlinkStrikeImpact(org.bukkit.Location loc, double radius) {
        for (int x = (int) -radius; x <= radius; x++) {
            for (int y = (int) -radius; y <= radius; y++) {
                for (int z = (int) -radius; z <= radius; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();

                    if (block.getType().name().contains("GLASS") ||
                            block.getType().name().contains("PANE")) {
                        block.breakNaturally();
                    }
                }
            }
        }
    }

    /**
     * Called when ShadowStep is used - extinguish lights
     */
    public static void handleShadowStepArrival(org.bukkit.Location loc, double radius) {
        for (int x = (int) -radius; x <= radius; x++) {
            for (int y = (int) -radius; y <= radius; y++) {
                for (int z = (int) -radius; z <= radius; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();

                    if (block.getType() == Material.TORCH ||
                            block.getType() == Material.WALL_TORCH ||
                            block.getType() == Material.LANTERN ||
                            block.getType() == Material.SOUL_LANTERN) {
                        block.setType(Material.AIR);
                        block.getWorld().playSound(block.getLocation(),
                                Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 1);
                    }
                }
            }
        }
    }
}
