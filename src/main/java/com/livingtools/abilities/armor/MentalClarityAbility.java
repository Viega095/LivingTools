package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class MentalClarityAbility extends Ability {

    public MentalClarityAbility() {
        super("mental_clarity", "Claridad Mental",
                "Inmunidad a ceguera, náusea y efectos mentales negativos",
                25, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Remover efectos negativos mentales
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (player.hasPotionEffect(PotionEffectType.CONFUSION)) {
            player.removePotionEffect(PotionEffectType.CONFUSION);
        }
        if (player.hasPotionEffect(PotionEffectType.DARKNESS)) {
            player.removePotionEffect(PotionEffectType.DARKNESS);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("HELMET");
    }

    @Override
    public int getRequiredLevel() {
        return 25;
    }
}
