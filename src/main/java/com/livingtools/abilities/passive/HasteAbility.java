package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HasteAbility extends Ability {

    public HasteAbility() {
        super("haste", "Prisa Minera", "Otorga Prisa I al sostener la herramienta.", 5);
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 0, false, false, true));
    }

    @Override
    public boolean isCompatible(org.bukkit.Material type) {
        String name = type.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }
}
