package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.manager.ConfigManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LightningStrikeAbility extends Ability {

    public LightningStrikeAbility() {
        super("lightning", "Golpe de Rayo", "Probabilidad de invocar un rayo al atacar.", 25);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, com.livingtools.data.LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            if (Math.random() * 100 < ConfigManager.getLightningChance()) {
                event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());

                // Mastery Scaling
                int level = getLevel(tool);
                double bonusDamage = level * 2.0; // Lvl 1: +2, Lvl 5: +10

                if (bonusDamage > 0) {
                    ((LivingEntity) event.getEntity()).damage(bonusDamage);
                }

                // Add XP
                addXP((org.bukkit.entity.Player) event.getDamager(), tool, 5);
            }
        }
    }

    @Override
    public boolean isCompatible(org.bukkit.Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }
}
