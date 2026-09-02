package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Arena desértica para el Wyrm de las Arenas.
 * Suelo de arena/arenisca, columnas de arenisca roja, radio 18.
 */
public class DesertArenaManager {

    private static final int RADIUS = 18;

    public static void spawnArena(Location center) {
        spawnArena(center, true);
    }

    public static void spawnArena(Location center, boolean spawnBoss) {
        center = center.clone();
        center.setY(center.getWorld().getHighestBlockYAt(center));

        final Location finalCenter = center;
        final List<Location> blocks = new ArrayList<>();

        // Clear area above
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    for (int y = 0; y < 12; y++) {
                        finalCenter.clone().add(x, y, z).getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        // Floor: sand / red sand / sandstone
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    Location loc = finalCenter.clone().add(x, -1, z);
                    double rnd = Math.random();
                    if (rnd < 0.55) {
                        loc.getBlock().setType(Material.SAND);
                    } else if (rnd < 0.8) {
                        loc.getBlock().setType(Material.RED_SAND);
                    } else {
                        loc.getBlock().setType(Material.SANDSTONE);
                    }
                    blocks.add(loc);
                }
            }
        }

        // Red sandstone pillars at corners
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4);
            int px = (int) (Math.cos(angle) * (RADIUS - 3));
            int pz = (int) (Math.sin(angle) * (RADIUS - 3));
            Location pillar = finalCenter.clone().add(px, 0, pz);
            for (int y = 0; y < 5; y++) {
                Location p = pillar.clone().add(0, y, 0);
                p.getBlock().setType(y == 4 ? Material.RED_SANDSTONE_SLAB : Material.RED_SANDSTONE);
                blocks.add(p);
            }
        }

        // Dead bushes scattered
        for (int x = -RADIUS + 2; x <= RADIUS - 2; x++) {
            for (int z = -RADIUS + 2; z <= RADIUS - 2; z++) {
                if (x * x + z * z <= (RADIUS - 3) * (RADIUS - 3) && Math.random() < 0.04) {
                    Location bush = finalCenter.clone().add(x, 0, z);
                    if (bush.getBlock().getType() == Material.AIR
                            || bush.getBlock().getType() == Material.SAND
                            || bush.getBlock().getType() == Material.RED_SAND) {
                        bush.getBlock().setType(Material.DEAD_BUSH);
                        blocks.add(bush);
                    }
                }
            }
        }

        Bukkit.broadcastMessage(MessageUtils.color("&6&l☀ ¡Las Arenas del Wyrm se alzan! ☀"));

        if (spawnBoss) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    org.bukkit.entity.LivingEntity boss =
                            BossAbilityManager.spawnWyrm(finalCenter.clone().add(0, 1, 0));
                    if (boss != null) {
                        BossAbilityManager.registerArena(boss.getUniqueId(), blocks);
                    }
                }
            }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 40L);
        }
    }
}
