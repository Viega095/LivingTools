package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SoulEchoVisualizer {

    public static void playEffect(Player player, Location sourceLoc) {
        new BukkitRunnable() {
            Location current = sourceLoc.clone().add(0, 0.5, 0);
            int steps = 0;
            final int maxSteps = 20; // 1 second approx

            @Override
            public void run() {
                if (!player.isOnline() || steps >= maxSteps) {
                    this.cancel();
                    return;
                }

                Location target = player.getLocation().add(0, 1, 0);
                Vector direction = target.toVector().subtract(current.toVector()).normalize().multiply(0.5);

                // Add some curve/noise
                if (steps < maxSteps / 2) {
                    direction.add(
                            new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(0.2));
                }

                current.add(direction);

                // Particle based on "Soul" theme (Soul Fire Flame or Sculk Soul)
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, current, 1, 0, 0, 0, 0);

                // Trail
                player.getWorld().spawnParticle(Particle.REDSTONE, current, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.AQUA, 0.5f));

                if (current.distanceSquared(target) < 1) {
                    // Reached player
                    player.getWorld().spawnParticle(Particle.SOUL, target, 5, 0.2, 0.2, 0.2, 0.05);
                    player.playSound(target, org.bukkit.Sound.PARTICLE_SOUL_ESCAPE, 0.5f, 2.0f);
                    this.cancel();
                }

                steps++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);
    }
}
