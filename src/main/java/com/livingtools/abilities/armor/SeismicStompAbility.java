package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SeismicStompAbility extends Ability {

    public SeismicStompAbility() {
        super("seismic_stomp", "Pisotón Sísmico",
                "Al caer desde altura, causa daño AoE a enemigos cercanos",
                35, AbilityType.ARMOR);
    }

    @Override
    public void onDamageTaken(EntityDamageEvent event, Player player) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            double fallDistance = player.getFallDistance();

            if (fallDistance > 5.0) {
                // Reducir daño de caída en 50%
                event.setDamage(event.getDamage() * 0.5);

                // Calcular daño AoE basado en altura de caída
                double aoeDamage = Math.min(fallDistance * 0.5, 15.0);
                double radius = Math.min(fallDistance * 0.3, 8.0);

                // Efectos visuales y sonoros
                player.getWorld().spawnParticle(
                        Particle.EXPLOSION_LARGE,
                        player.getLocation(),
                        5,
                        radius / 2, 0.1, radius / 2);
                player.getWorld().playSound(
                        player.getLocation(),
                        Sound.ENTITY_GENERIC_EXPLODE,
                        2.0f,
                        0.8f);

                // Dañar entidades cercanas
                for (Entity entity : player.getNearbyEntities(radius, 3, radius)) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(aoeDamage, player);

                        // Knockback
                        target.setVelocity(
                                target.getLocation()
                                        .toVector()
                                        .subtract(player.getLocation().toVector())
                                        .normalize()
                                        .multiply(1.5)
                                        .setY(0.5));
                    }
                }

                player.sendMessage("§6⚡ ¡Pisotón Sísmico! §e" + (int) aoeDamage + " daño");
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("BOOTS");
    }
}
