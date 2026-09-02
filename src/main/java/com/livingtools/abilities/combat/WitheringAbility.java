package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WitheringAbility extends Ability {

    public WitheringAbility() {
        super("withering", "Marchitar", "Aplica efecto Wither a los enemigos.", 40, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // 25% Chance
            if (Math.random() < 0.25) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1)); // Wither II for 3s

                // Visuals
                target.getWorld().spawnParticle(Particle.SMOKE_LARGE, target.getLocation().add(0, 1, 0), 10, 0.3, 0.5,
                        0.3, 0.05);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 0.5f);
            }
        }
    }
}
