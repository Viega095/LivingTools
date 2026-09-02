package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SonicSpeedAbility extends Ability {

    public SonicSpeedAbility() {
        super("sonic_speed", "Velocidad Sónica",
                "Otorga Speed III permanente mientras usas las botas",
                25, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(
                    new PotionEffect(PotionEffectType.SPEED, 220, 2, false, false));
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("BOOTS");
    }
}
