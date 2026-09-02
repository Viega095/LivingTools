package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BleedAbility extends Ability {

    public BleedAbility() {
        super("bleed", "Sangrado", "25% de probabilidad de aplicar Wither II.", 30, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            if (Math.random() < 0.25) { // 25% Chance
                LivingEntity target = (LivingEntity) event.getEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1)); // 3s Wither II
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE");
    }
}
