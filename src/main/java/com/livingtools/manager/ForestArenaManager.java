package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Arena forestal para la Dríade Corrupta.
 * Suelo de tierra/pasto, árboles de roble en bordes, radio 18.
 */
public class ForestArenaManager {

    private static final int RADIUS = 18;

    public static void spawnArena(Location center) {
        spawnArena(center, true);
    }

    public static void spawnArena(Location center, boolean spawnBoss) {
        center = center.clone();
        center.setY(center.getWorld().getHighestBlockYAt(center));

        final Location finalCenter = center;
        final List<Location> blocks = new ArrayList<>();

        // Clear area above ground
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    for (int y = 0; y < 12; y++) {
                        finalCenter.clone().add(x, y, z).getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        // Floor: grass / dirt / moss
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    Location loc = finalCenter.clone().add(x, -1, z);
                    double rnd = Math.random();
                    if (rnd < 0.6) {
                        loc.getBlock().setType(Material.GRASS_BLOCK);
                    } else if (rnd < 0.85) {
                        loc.getBlock().setType(Material.DIRT);
                    } else {
                        loc.getBlock().setType(Material.MOSS_BLOCK);
                    }
                    blocks.add(loc);
                }
            }
        }

        // Oak trees at the perimeter
        for (int i = 0; i < 12; i++) {
            double angle = i * (Math.PI * 2 / 12);
            int tx = (int) (Math.cos(angle) * (RADIUS - 2));
            int tz = (int) (Math.sin(angle) * (RADIUS - 2));
            Location treeLoc = finalCenter.clone().add(tx, 0, tz);
            placeOakTree(treeLoc, blocks);
        }

        // Random ferns/flowers inside
        for (int x = -RADIUS + 2; x <= RADIUS - 2; x++) {
            for (int z = -RADIUS + 2; z <= RADIUS - 2; z++) {
                if (x * x + z * z <= (RADIUS - 3) * (RADIUS - 3) && Math.random() < 0.08) {
                    Location flowerLoc = finalCenter.clone().add(x, 0, z);
                    Material[] plants = {Material.FERN, Material.SHORT_GRASS, Material.DANDELION, Material.POPPY};
                    flowerLoc.getBlock().setType(plants[(int) (Math.random() * plants.length)]);
                    blocks.add(flowerLoc);
                }
            }
        }

        Bukkit.broadcastMessage(MessageUtils.color("&2&l🌳 ¡El Bosque Corrupto ha despertado! 🌳"));

        if (spawnBoss) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    org.bukkit.entity.LivingEntity boss =
                            BossAbilityManager.spawnDryad(finalCenter.clone().add(0, 1, 0));
                    if (boss != null) {
                        BossAbilityManager.registerArena(boss.getUniqueId(), blocks);
                    }
                }
            }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 40L);
        }
    }

    private static void placeOakTree(Location base, List<Location> blocks) {
        // Trunk (4 blocks)
        for (int y = 0; y < 4; y++) {
            Location log = base.clone().add(0, y, 0);
            log.getBlock().setType(Material.OAK_LOG);
            blocks.add(log);
        }
        // Leaf canopy
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 2; y <= 5; y++) {
                    if (Math.abs(x) + Math.abs(z) + (y == 5 ? 1 : 0) <= 3) {
                        Location leaf = base.clone().add(x, y, z);
                        if (leaf.getBlock().getType() == Material.AIR) {
                            leaf.getBlock().setType(Material.OAK_LEAVES);
                            blocks.add(leaf);
                        }
                    }
                }
            }
        }
    }
}
