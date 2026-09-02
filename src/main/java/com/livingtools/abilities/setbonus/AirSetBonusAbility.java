package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AirSetBonusAbility extends Ability {

    public AirSetBonusAbility() {
        super("air_set_bonus", "Set de Aire",
                "Doble salto mejorado + sin daño de caída + velocidad aumentada",
                1, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Velocidad aumentada
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 220, 1, false, false));
        }

        // Permitir doble salto (ya implementado en DoubleJumpAbility)
        // Aquí solo agregamos el efecto visual
        if (!player.isOnGround() && Math.random() < 0.1) {
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.CLOUD,
                    player.getLocation(),
                    3,
                    0.2, 0.2, 0.2,
                    0.02);
        }
    }

    @Override
    public void onDamageTaken(EntityDamageEvent event, Player player) {
        // Sin daño de caída
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.CLOUD,
                    player.getLocation(),
                    10,
                    0.5, 0.1, 0.5);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
