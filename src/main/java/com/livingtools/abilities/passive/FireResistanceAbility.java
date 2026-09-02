package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FireResistanceAbility extends Ability {

    public FireResistanceAbility() {
        super("fireresistance", "Piel de Magma", "Te hace inmune al fuego.", 25);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_CHESTPLATE") || type.name().endsWith("_LEGGINGS");
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 0, false, false));
    }
}
