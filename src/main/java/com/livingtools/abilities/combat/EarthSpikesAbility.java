package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EarthSpikesAbility extends Ability {

    public EarthSpikesAbility() {
        super("earth_spikes", "Púas Terrestres", "Probabilidad de invocar púas al atacar.", 30, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            if (Math.random() < 0.30) { // 30% Chance
                LivingEntity target = (LivingEntity) event.getEntity();
                target.getWorld().spawn(target.getLocation(), EvokerFangs.class);
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }
}
