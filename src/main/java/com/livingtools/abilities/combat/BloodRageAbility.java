package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BloodRageAbility extends Ability {

    public BloodRageAbility() {
        super("blood_rage", "Furia de Sangre", "Más daño cuanta menos vida tengas.", 35, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = player.getHealth();
            double missingHealth = maxHealth - currentHealth;

            // +1 Damage per 4 missing HP (2 hearts)
            double bonusDamage = missingHealth / 4.0;

            if (bonusDamage > 0) {
                event.setDamage(event.getDamage() + bonusDamage);
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD");
    }
}
