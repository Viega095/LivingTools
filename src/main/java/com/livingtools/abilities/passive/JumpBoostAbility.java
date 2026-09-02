package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JumpBoostAbility extends Ability {

    public JumpBoostAbility() {
        super("jumpboost", "Salto Mejorado", "Te permite saltar más alto.", 10);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_BOOTS");
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, 1, false, false));
    }
}
