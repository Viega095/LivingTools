package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceSetBonusAbility extends Ability {

    public IceSetBonusAbility() {
        super("ice_set_bonus", "Set de Hielo",
                "Inmunidad a ralentización + congela enemigos + crea escarcha",
                1, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Inmunidad a ralentización
        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            player.removePotionEffect(PotionEffectType.SLOW);
        }

        // Crear escarcha bajo los pies
        if (player.isOnGround() && Math.random() < 0.2) {
            Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();
            if (blockBelow == Material.WATER) {
                player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.FROSTED_ICE);
            }

            // Partículas de hielo
            player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    player.getLocation(),
                    2,
                    0.3, 0.1, 0.3);
        }
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        // 20% chance de congelar al atacante
        if (Math.random() < 0.2 && event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 1));

            player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    attacker.getLocation(),
                    20,
                    0.5, 1, 0.5);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
