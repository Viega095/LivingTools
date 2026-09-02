package com.livingtools.abilities.infusion;

import com.livingtools.abilities.InfusionAbility;
import com.livingtools.manager.BiomeManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AetherialInfusion extends InfusionAbility {

    public AetherialInfusion() {
        super("infusion_sky", "Infusión Etérea", "Poder del Cielo. Caída lenta y velocidad.",
                BiomeManager.BiomeCategory.SKY, 500);
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
    }

    @Override
    public void onActiveTrigger(Player player) {
        player.setVelocity(new org.bukkit.util.Vector(0, 1.5, 0));
        player.sendMessage("¡Salto Etéreo!");
    }
}
