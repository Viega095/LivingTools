package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionAbility extends Ability {

    public NightVisionAbility() {
        super("nightvision", "Visión Nocturna", "Te permite ver en la oscuridad.", 15);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HELMET");
    }

    @Override
    public void onHold(Player player) {
        // This is called by the passive task.
        // For armor, we need to ensure the task checks armor slots too.
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 220, 0, false, false));
    }
}
