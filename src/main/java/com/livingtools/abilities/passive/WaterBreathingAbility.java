package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WaterBreathingAbility extends Ability {

    public WaterBreathingAbility() {
        super("waterbreathing", "Branquias", "Te permite respirar bajo el agua.", 20);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HELMET");
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 20, 0, false, false));
    }
}
