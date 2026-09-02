package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedAbility extends Ability {

    public SpeedAbility() {
        super("speed", "Velocidad", "Otorga Velocidad I al sostener la herramienta.", 10);
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false, true));
    }
}
