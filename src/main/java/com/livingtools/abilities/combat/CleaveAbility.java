package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CleaveAbility extends Ability {

    public CleaveAbility() {
        super("cleave", "Hendidura", "Daña a enemigos cercanos.", 45, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            double damage = event.getDamage() * 0.5; // 50% Damage Cleave

            for (Entity entity : target.getNearbyEntities(3, 2, 3)) {
                if (entity instanceof LivingEntity && entity != event.getDamager()) {
                    ((LivingEntity) entity).damage(damage, (Entity) event.getDamager());
                }
            }

            // Visuals
            target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }
}
