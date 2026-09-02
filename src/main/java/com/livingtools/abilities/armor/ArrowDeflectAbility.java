package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ArrowDeflectAbility extends Ability {

    public ArrowDeflectAbility() {
        super("arrow_deflect", "Desvío", "25% de probabilidad de desviar flechas.", 35, AbilityType.ARMOR);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            if (Math.random() < 0.25) { // 25% Chance
                event.setCancelled(true);
                event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
                // Maybe bounce logic?
                event.getDamager().setVelocity(event.getDamager().getVelocity().multiply(-0.5));
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_CHESTPLATE");
    }
}
