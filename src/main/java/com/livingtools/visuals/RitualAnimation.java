package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RitualAnimation {

    public enum RitualTheme {
        LIGHT, DARK
    }

    public static void play(Location center, ItemStack resultItem, Player player, RitualTheme theme,
            java.util.function.Consumer<Location> onComplete) {
        // Spawn Item Display
        ItemDisplay display = center.getWorld().spawn(center.clone().add(0.5, 1, 0.5), ItemDisplay.class);
        display.setItemStack(resultItem);
        display.setBillboard(ItemDisplay.Billboard.FIXED); // Fixed rotation initially

        // Initial Scale/Rotation
        Transformation transform = display.getTransformation();
        transform.getScale().set(0.5f);
        display.setTransformation(transform);

        // Apply Darkness Effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 120, 0, false, false, false));

        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                ticks++;
                angle += 0.2;

                // 1. Float Up & Rotate
                if (ticks < 60) {
                    Location loc = display.getLocation().add(0, 0.02, 0);
                    display.teleport(loc);

                    // Rotate
                    Transformation t = display.getTransformation();
                    t.getLeftRotation().set(new AxisAngle4f((float) angle, 0, 1, 0));
                    display.setTransformation(t);
                }

                // 2. Particles (Spiral & Atmosphere)
                double radius = 1.0;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);

                Particle spiralParticle;
                Particle secondaryParticle;

                if (theme == RitualTheme.DARK) {
                    spiralParticle = Particle.SCULK_SOUL;
                    secondaryParticle = Particle.SPELL_WITCH;
                } else {
                    spiralParticle = Particle.END_ROD;
                    secondaryParticle = Particle.FIREWORKS_SPARK;
                }

                // Spiral
                center.getWorld().spawnParticle(spiralParticle,
                        display.getLocation().clone().add(x, 0, z), 1, 0, 0, 0, 0);
                center.getWorld().spawnParticle(spiralParticle,
                        display.getLocation().clone().add(-x, 0, -z), 1, 0, 0, 0, 0);

                // Secondary Atmosphere
                if (ticks % 5 == 0) {
                    center.getWorld().spawnParticle(secondaryParticle,
                            display.getLocation().clone().add(0, 0.5, 0), 3, 0.3, 0.3, 0.3, 0.05);
                }

                // 3. Sound Buildup (Heartbeat)
                if (ticks % 20 == 0) {
                    // Accelerate heartbeat
                    float pitch = 0.5f + (ticks / 100f);
                    center.getWorld().playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1, pitch);
                }

                // 4. Climax & Drop
                if (ticks >= 100) {
                    // Explosion Effect
                    if (theme == RitualTheme.DARK) {
                        center.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, display.getLocation(), 1);
                        center.getWorld().spawnParticle(Particle.SCULK_SOUL, display.getLocation(), 50, 0.5, 0.5, 0.5,
                                0.1);
                        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, display.getLocation(), 30, 0.5, 0.5,
                                0.5, 0.1);
                        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1);
                        center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 0.5f);
                    } else {
                        center.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, display.getLocation(), 1);
                        center.getWorld().spawnParticle(Particle.FLASH, display.getLocation(), 1);
                        center.getWorld().spawnParticle(Particle.TOTEM, display.getLocation(), 30, 0.5, 0.5, 0.5, 0.5);
                        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                        center.getWorld().playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                    }

                    // Drop Item
                    center.getWorld().dropItemNaturally(display.getLocation(), resultItem);

                    // Cleanup
                    display.remove();
                    this.cancel();

                    // Callback
                    if (onComplete != null) {
                        onComplete.accept(display.getLocation());
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);
    }
}
