package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

public class FireSetBonusAbility extends Ability {

    public FireSetBonusAbility() {
        super("fire_set_bonus", "Set de Fuego",
                "Inmunidad al fuego + daño de fuego en ataques + trail de llamas",
                1, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Inmunidad al fuego
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }

        // Trail de llamas
        if (player.isOnGround() && Math.random() < 0.3) {
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation(),
                    3,
                    0.3, 0.1, 0.3,
                    0.01);
        }
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        // Quemar al atacante
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            attacker.setFireTicks(100); // 5 segundos
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
