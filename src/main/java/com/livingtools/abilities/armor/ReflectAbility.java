package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReflectAbility extends Ability {

    public ReflectAbility() {
        super("reflect", "Reflejo", "Probabilidad de reflejar daño.", 10);
    }

    @Override
    public void onHold(Player player) {
        // Passive effect handled in event
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            if (Math.random() < 0.15) { // 15% chance
                double damage = event.getFinalDamage() * 0.30; // Reflect 30%
                attacker.damage(damage, player);
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1, 1);
                player.getWorld().spawnParticle(org.bukkit.Particle.CRIT, attacker.getLocation(), 10, 0.5, 0.5, 0.5,
                        0.1);
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_CHESTPLATE") || material.name().endsWith("_LEGGINGS");
    }
}
