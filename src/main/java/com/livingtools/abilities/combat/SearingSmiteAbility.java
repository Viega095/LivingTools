package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SearingSmiteAbility extends Ability {

    public SearingSmiteAbility() {
        super("searing_smite", "Golpe Abrasador", "Quema intensa y daño extra.", 20, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // Apply Fire
            target.setFireTicks(100); // 5 Seconds

            // Bonus Damage
            event.setDamage(event.getDamage() + 2.0); // +1 Heart

            // Visuals
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 10, 0.2, 0.5, 0.2, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }
}
