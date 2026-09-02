package com.livingtools.abilities.infusion;

import com.livingtools.abilities.InfusionAbility;
import com.livingtools.manager.BiomeManager;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InfernalInfusion extends InfusionAbility {

    public InfernalInfusion() {
        super("infusion_nether", "Infusión Infernal", "Poder del Nether. Resistencia al fuego y bolas de fuego.",
                BiomeManager.BiomeCategory.NETHER, 500);
    }

    @Override
    public void onHold(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));
    }

    @Override
    public void onActiveTrigger(Player player) {
        player.launchProjectile(Fireball.class);
        player.sendMessage("¡Fuego Infernal!");
    }
}
