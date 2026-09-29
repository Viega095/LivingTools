package com.livingtools.visuals;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * DamageIndicatorManager — Indicadores flotantes efímeros de daño crítico, XP y tesoros.
 */
public class DamageIndicatorManager {

    public static void spawnIndicator(Location loc, String text) {
        if (loc.getWorld() == null) return;

        Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 0.6, 0.5, (Math.random() - 0.5) * 0.6);

        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setGravity(false);
        as.setSmall(true);
        as.setMarker(true);
        as.setCustomName(text);
        as.setCustomNameVisible(true);
        as.setCanPickupItems(false);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!as.isValid() || ticks >= 20) {
                    as.remove();
                    cancel();
                    return;
                }

                as.teleport(as.getLocation().add(0, 0.04, 0));
                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 1L, 1L);
    }
}
