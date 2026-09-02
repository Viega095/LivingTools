package com.livingtools.abilities.elemental;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Arrays;
import java.util.List;

public class ThunderAbility extends Ability {

    public ThunderAbility() {
        super("thunder", "Trueno", "Probabilidad de invocar un rayo.", 20, 10000);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().contains("SWORD") || type.name().contains("AXE");
    }

    @Override
    public List<String> getIncompatibleAbilities() {
        return Arrays.asList("inferno", "glacial");
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            if (Math.random() < 0.25) { // 25% Chance
                LivingEntity target = (LivingEntity) event.getEntity();
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.damage(4.0); // Extra damage
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
            }
        }
    }
}
