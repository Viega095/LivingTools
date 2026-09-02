package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Arena submarina para el Leviatán.
 * Fondo de prismarinas, columnas con linternas del mar, radio 18.
 */
public class OceanArenaManager {

    private static final int RADIUS = 18;

    public static void spawnArena(Location center) {
        spawnArena(center, true);
    }

    public static void spawnArena(Location center, boolean spawnBoss) {
        center = center.clone();
        // Place underwater — lower by 8 to simulate ocean floor
        center.setY(center.getWorld().getHighestBlockYAt(center) - 8);

        final Location finalCenter = center;
        final List<Location> blocks = new ArrayList<>();

        // Clear area
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    for (int y = -1; y < 12; y++) {
                        Material m = (y < 0) ? Material.WATER : Material.WATER;
                        finalCenter.clone().add(x, y, z).getBlock().setType(m);
                    }
                }
            }
        }

        // Floor: prismarine / dark prismarine / prismarine bricks
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    Location loc = finalCenter.clone().add(x, -1, z);
                    double rnd = Math.random();
                    if (rnd < 0.5) {
                        loc.getBlock().setType(Material.PRISMARINE);
                    } else if (rnd < 0.8) {
                        loc.getBlock().setType(Material.DARK_PRISMARINE);
                    } else {
                        loc.getBlock().setType(Material.PRISMARINE_BRICKS);
                    }
                    blocks.add(loc);
                }
            }
        }

        // Sea lantern pillars
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4);
            int px = (int) (Math.cos(angle) * (RADIUS - 4));
            int pz = (int) (Math.sin(angle) * (RADIUS - 4));
            Location pillar = finalCenter.clone().add(px, 0, pz);
            for (int y = 0; y < 6; y++) {
                Location p = pillar.clone().add(0, y, 0);
                p.getBlock().setType(y == 5 ? Material.SEA_LANTERN : Material.PRISMARINE_BRICKS);
                blocks.add(p);
            }
        }

        // Sea grass and coral scattered on floor
        for (int x = -RADIUS + 2; x <= RADIUS - 2; x++) {
            for (int z = -RADIUS + 2; z <= RADIUS - 2; z++) {
                if (x * x + z * z <= (RADIUS - 4) * (RADIUS - 4) && Math.random() < 0.1) {
                    Location coral = finalCenter.clone().add(x, 0, z);
                    Material[] decorations = {
                        Material.SEAGRASS, Material.BRAIN_CORAL, Material.BUBBLE_CORAL,
                        Material.FIRE_CORAL, Material.HORN_CORAL, Material.TUBE_CORAL
                    };
                    coral.getBlock().setType(decorations[(int) (Math.random() * decorations.length)]);
                    blocks.add(coral);
                }
            }
        }

        // Water ceiling above arena
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS * RADIUS) {
                    for (int y = 0; y < 10; y++) {
                        Location waterLoc = finalCenter.clone().add(x, y, z);
                        if (waterLoc.getBlock().getType() == Material.AIR) {
                            waterLoc.getBlock().setType(Material.WATER);
                        }
                    }
                }
            }
        }

        Bukkit.broadcastMessage(MessageUtils.color("&3&l🌊 ¡Las Profundidades del Leviatán se abren! 🌊"));

        if (spawnBoss) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    org.bukkit.entity.LivingEntity boss =
                            BossAbilityManager.spawnLeviathan(finalCenter.clone().add(0, 3, 0));
                    if (boss != null) {
                        BossAbilityManager.registerArena(boss.getUniqueId(), blocks);
                    }
                }
            }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 40L);
        }
    }
}
