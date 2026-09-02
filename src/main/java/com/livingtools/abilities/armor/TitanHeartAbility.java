package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class TitanHeartAbility extends Ability {

    public TitanHeartAbility() {
        super("titan_heart", "Corazón de Titán",
                "Aumenta la vida máxima en 10 corazones (20 HP)",
                60, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double baseHealth = 20.0; // Salud base de Minecraft
            double bonusHealth = 20.0; // +10 corazones

            if (maxHealth.getBaseValue() < baseHealth + bonusHealth) {
                maxHealth.setBaseValue(baseHealth + bonusHealth);
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("CHESTPLATE");
    }
}
