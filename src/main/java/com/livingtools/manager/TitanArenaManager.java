package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class TitanArenaManager {

    public static void spawnArena(Location center) {
        spawnArena(center, true);
    }

    public static void spawnArena(Location center, boolean spawnBoss) {
        center.setY(center.getWorld().getHighestBlockYAt(center));
        java.util.List<Location> blocks = new java.util.ArrayList<>();

        // Clear area above
        int radius = 20;
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
                    loc.getBlock().setType(Material.OBSIDIAN);
                    blocks.add(loc);

                    // Random crying obsidian
                    if (Math.random() < 0.3) {
                        loc.getBlock().setType(Material.CRYING_OBSIDIAN);
                    }
                }
            }
        }

        // Generate walls
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > (radius - 1) * (radius - 1) && x * x + z * z <= radius * radius) {
                    for (int y = 0; y < 3; y++) {
                        Location wallLoc = center.clone().add(x, y, z);
                        wallLoc.getBlock().setType(Material.BLACKSTONE);
                        blocks.add(wallLoc);
                    }
                }
            }
        }

        org.bukkit.Bukkit.broadcastMessage(MessageUtils.color("&5&l¡La Prisión del Titán ha aparecido!"));

        if (spawnBoss) {
            // Spawn boss after a slight delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    org.bukkit.entity.LivingEntity boss = BossAbilityManager.spawnTitan(center.clone().add(0, 1, 0));
                    if (boss != null) {
                        BossAbilityManager.registerArena(boss.getUniqueId(), blocks);
                    }
                }
            }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 40);
        }
    }
}
