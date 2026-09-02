package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class VolcanicArenaManager {

    public static void spawnArena(Location center) {
        spawnArena(center, true);
    }

    public static void spawnArena(Location center, boolean spawnBoss) {
        center.setY(center.getWorld().getHighestBlockYAt(center));
        java.util.List<Location> blocks = new java.util.ArrayList<>();

        // Clear area above
        int radius = 15;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    for (int y = 0; y < 10; y++) {
                        center.clone().add(x, y, z).getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        // Generate floor
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location loc = center.clone().add(x, -1, z);
                    loc.getBlock().setType(Material.NETHER_BRICKS);
                    blocks.add(loc);

                    // Random magma blocks
                    if (Math.random() < 0.2) {
                        loc.getBlock().setType(Material.MAGMA_BLOCK);
                    }
                }
            }
        }

        // Generate pillars
        for (int i = 0; i < 4; i++) {
            double angle = i * (Math.PI / 2);
            int px = (int) (Math.cos(angle) * 10);
            int pz = (int) (Math.sin(angle) * 10);
            Location pillarLoc = center.clone().add(px, 0, pz);
            for (int y = 0; y < 4; y++) {
                Location pLoc = pillarLoc.clone().add(0, y, 0);
                pLoc.getBlock().setType(Material.NETHER_BRICK_FENCE);
                blocks.add(pLoc);
            }
            Location fireLoc = pillarLoc.clone().add(0, 4, 0);
            fireLoc.getBlock().setType(Material.FIRE);
            blocks.add(fireLoc);
        }

        org.bukkit.Bukkit.broadcastMessage(MessageUtils.color("&c&l¡Una Arena Volcánica ha surgido!"));

        if (spawnBoss) {
            // Spawn boss after a slight delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    org.bukkit.entity.LivingEntity boss = BossAbilityManager
                            .spawnLivingBoss(center.clone().add(0, 1, 0));
                    if (boss != null) {
                        BossAbilityManager.registerArena(boss.getUniqueId(), blocks);
                    }
                }
            }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 40);
        }
    }
}
