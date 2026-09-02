package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class FeatherWeightAbility extends Ability {

    public FeatherWeightAbility() {
        super("feather_weight", "Peso Pluma", "Anula el daño de caída.", 25, AbilityType.ARMOR);
    }

    @Override
    public void onDamageTaken(EntityDamageEvent event, Player victim) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);

            // Visuals
            victim.getWorld().spawnParticle(Particle.CLOUD, victim.getLocation(), 10, 0.5, 0.2, 0.5, 0.1);
            victim.playSound(victim.getLocation(), Sound.BLOCK_WOOL_FALL, 1, 1);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_BOOTS");
    }
}
