package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EternalBreathAbility extends Ability {

    public EternalBreathAbility() {
        super("eternal_breath", "Respiración Eterna",
                "Oxígeno infinito bajo el agua y visión mejorada",
                20, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Restaurar aire si está bajo el agua
        if (player.isInWater() || player.getLocation().getBlock().getType() == Material.WATER) {
            player.setRemainingAir(player.getMaximumAir());

            // Dar visión nocturna bajo el agua
            if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.NIGHT_VISION, 220, 0, false, false));
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("HELMET");
    }
}
