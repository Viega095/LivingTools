package com.livingtools.manager;

import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Consumer;

/**
 * BossCinematicManager — Motor de cinemáticas, animaciones de spawn,
 * telegrafía de ataques (skill-shots) y muerte épica para los Bosses.
 */
public class BossCinematicManager {

    /**
     * Inicia una secuencia de invocación cinemática de 3 segundos antes de spawnear al Boss.
     */
    public static void playSpawnCinematic(Location loc, String bossName, Runnable onSpawn) {
        World world = loc.getWorld();
        if (world == null) {
            onSpawn.run();
            return;
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick == 0) {
                    // Terremoto inicial
                    world.playSound(loc, Sound.ENTITY_WARDEN_DIG, 1.5f, 0.6f);
                    world.spawnParticle(Particle.BLOCK_CRACK, loc, 60, 2, 0.2, 2, 0.1, Material.OBSIDIAN.createBlockData());
                }

                // Círculo rúnico creciente (Fase 0s a 3s)
                double currentRadius = Math.min(5.0, 1.0 + (tick * 0.15));
                ParticleOptimizer.spawnCircle(loc, currentRadius, Particle.FLAME, 24, null);
                ParticleOptimizer.spawnCircle(loc, currentRadius * 0.6, Particle.SOUL_FIRE_FLAME, 18, null);

                if (tick % 10 == 0) {
                    world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 0.8f + (tick * 0.02f));
                }

                tick++;

                // Final de la cinemática a los 3 segundos (60 ticks)
                if (tick >= 60) {
                    cancel();

                    // Rayo celestial estético y explosión
                    world.strikeLightningEffect(loc);
                    world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 3, 0.5, 0.5, 0.5, 0);
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
                    world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 0.7f);

                    // Onda expansiva empuja a jugadores cercanos
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distanceSquared(loc) <= 225) { // 15 bloques
                            Vector diff = p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.8).setY(0.4);
                            p.setVelocity(diff);

                            p.sendTitle(
                                    ChatColor.DARK_RED + "⚔ " + ChatColor.BOLD + bossName + ChatColor.DARK_RED + " ⚔",
                                    ChatColor.GRAY + "¡El poder de la criatura despierta!",
                                    10, 50, 20
                            );
                        }
                    }

                    // Invocar la entidad real
                    onSpawn.run();
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 1L);
    }

    /**
     * Dibuja un círculo de peligro en el suelo que se contrae antes de un ataque de área masivo.
     */
    public static void playTelegraphedAttack(Location center, double radius, int durationTicks, Runnable onAttack) {
        World world = center.getWorld();
        if (world == null) {
            onAttack.run();
            return;
        }

        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed >= durationTicks) {
                    cancel();
                    // Impacto del ataque
                    world.spawnParticle(Particle.EXPLOSION_LARGE, center, 5, 0.5, 0.5, 0.5, 0.1);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.2f);
                    onAttack.run();
                    return;
                }

                // Círculo contraído indicando tiempo límite
                double progress = 1.0 - ((double) elapsed / durationTicks);
                double currentR = Math.max(0.5, radius * progress);

                ParticleOptimizer.spawnCircle(center, currentR, Particle.REDSTONE, 24,
                        new Particle.DustOptions(Color.fromRGB(255, 30, 30), 1.5f));
                ParticleOptimizer.spawnCircle(center, radius, Particle.SPELL_WITCH, 16, null);

                if (elapsed % 5 == 0) {
                    float pitch = 0.8f + (1.2f * ((float) elapsed / durationTicks));
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, pitch);
                }

                elapsed++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 1L);
    }

    /**
     * Secuencia de muerte del Boss con implosión y explosión cinemática de loot.
     */
    public static void playDeathCinematic(LivingEntity boss, List<ItemStack> drops) {
        if (boss == null || !boss.isValid()) return;
        Location loc = boss.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        // Anuncio de victoria
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 0.5f);
        world.spawnParticle(Particle.TOTEM, loc.add(0, 1.5, 0), 80, 1.0, 1.5, 1.0, 0.2);

        // Dispersión cinemática de drops
        if (drops != null) {
            for (ItemStack drop : drops) {
                if (drop == null || drop.getType() == Material.AIR) continue;
                org.bukkit.entity.Item droppedItem = world.dropItemNaturally(loc, drop);
                double vx = (Math.random() - 0.5) * 0.6;
                double vy = 0.5 + Math.random() * 0.4;
                double vz = (Math.random() - 0.5) * 0.6;
                droppedItem.setVelocity(new Vector(vx, vy, vz));
            }
        }
    }
}
