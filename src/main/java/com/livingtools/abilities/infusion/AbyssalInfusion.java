package com.livingtools.abilities.infusion;

import com.livingtools.abilities.InfusionAbility;
import com.livingtools.manager.BiomeManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbyssalInfusion extends InfusionAbility {

    public AbyssalInfusion() {
        super("infusion_ocean", "Infusión Abisal", "Poder del Océano. Respiración acuática y agilidad.",
                BiomeManager.BiomeCategory.OCEAN, 500);
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
    }

    @Override
    public void onActiveTrigger(Player player) {
        player.setVelocity(player.getLocation().getDirection().multiply(2));
        player.sendMessage("¡Impulso Abisal!");
    }
}
