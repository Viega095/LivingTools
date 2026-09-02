package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ThornsAbility extends Ability {

    public ThornsAbility() {
        super("thorns_living", "Espinas Vivientes", "Probabilidad de devolver daño y sangrado al atacante.", 50000,
                AbilityType.PASSIVE);
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        if (Math.random() < 0.25) { // 25% chance
            if (event.getDamager() instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) event.getDamager();
                double damage = event.getDamage() * 0.3; // Reflect 30% damage

                attacker.damage(damage, player);

                // Visuals
                player.getWorld().spawnParticle(Particle.CRIT, attacker.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.1);
                player.getWorld().playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1, 0.5f);

                player.sendMessage(org.bukkit.ChatColor.GREEN + "¡Tus Espinas Vivientes contraatacan!");
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_CHESTPLATE") || material.name().endsWith("_LEGGINGS");
    }
}
