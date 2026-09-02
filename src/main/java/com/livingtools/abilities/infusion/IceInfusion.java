package com.livingtools.abilities.infusion;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceInfusion extends Ability {

    public IceInfusion() {
        super("ice_infusion", "Infusión de Hielo", "Congela a los enemigos y emite partículas de nieve", 30,
                AbilityType.INFUSION);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, com.livingtools.data.LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1)); // Slowness II for 3s

            // Visuals
            target.getWorld().spawnParticle(Particle.SNOWBALL, target.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
        }
    }

    @Override
    public void onHold(Player player) {
        if (Math.random() < 0.1) {
            player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 1, 0.2, 0.2, 0.2,
                    0.01);
        }
    }
}
