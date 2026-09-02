package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DolphinsGraceAbility extends Ability {

    public DolphinsGraceAbility() {
        super("dolphins_grace", "Gracia de Delfín", "Velocidad y respiración bajo el agua.", 25, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        if (player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false, true));
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HELMET");
    }
}
