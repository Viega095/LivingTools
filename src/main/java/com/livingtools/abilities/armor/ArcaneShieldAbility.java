package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ArcaneShieldAbility extends Ability {

    public ArcaneShieldAbility() {
        super("arcane_shield", "Escudo Arcano", "20% de probabilidad de negar daño y alejar al atacante.", 40,
                AbilityType.ARMOR);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof Player) {
            if (Math.random() < 0.20) { // 20% Chance
                event.setCancelled(true);
                Player player = (Player) event.getEntity();

                // Visuals
                player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 20, 0.5,
                        0.5, 0.5, 0.5);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 2);

                // Teleport Attacker Away
                Entity attacker = event.getDamager();
                if (attacker instanceof LivingEntity) {
                    attacker.setVelocity(attacker.getLocation().getDirection().multiply(-2).setY(0.5));
                }
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_LEGGINGS");
    }
}
