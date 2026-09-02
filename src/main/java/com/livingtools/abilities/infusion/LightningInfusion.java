package com.livingtools.abilities.infusion;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Particle;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LightningInfusion extends Ability {

    public LightningInfusion() {
        super("lightning_infusion", "Infusión de Rayo", "Probabilidad de invocar un rayo", 30, AbilityType.INFUSION);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, com.livingtools.data.LivingTool tool) {
        if (Math.random() < 0.2) { // 20% chance
            event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
        }
    }

    @Override
    public void onHold(Player player) {
        if (Math.random() < 0.1) {
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 1, 0.2, 0.2,
                    0.2, 0.01);
        }
    }
}
