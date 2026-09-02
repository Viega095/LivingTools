package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AdrenalineAbility extends Ability {

    public AdrenalineAbility() {
        super("adrenaline", "Adrenalina", "Otorga Velocidad II cuando tienes menos del 20% de vida.", 40,
                AbilityType.PASSIVE);
    }

    @Override
    public void onHold(Player player) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (player.getHealth() < maxHealth * 0.2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_BOOTS") || material.name().endsWith("_LEGGINGS");
    }
}
