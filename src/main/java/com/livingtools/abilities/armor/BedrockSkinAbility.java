package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BedrockSkinAbility extends Ability {

    public BedrockSkinAbility() {
        super("bedrock_skin", "Piel de Bedrock", "Resistencia II, pero Lentitud I.", 40, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, false, true));
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_CHESTPLATE");
    }
}
