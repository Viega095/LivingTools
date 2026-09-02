package com.livingtools.abilities.infusion;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FireInfusion extends Ability {

    public FireInfusion() {
        super("fire_infusion", "Infusión de Fuego", "Quema a los enemigos y emite partículas de fuego", 30,
                AbilityType.INFUSION);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, com.livingtools.data.LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            target.setFireTicks(100); // 5 seconds of fire

            // Visuals
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
        }
    }

    @Override
    public void onHold(Player player) {
        if (Math.random() < 0.1) {
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 1, 0.2, 0.2, 0.2, 0.01);
        }
    }
}
