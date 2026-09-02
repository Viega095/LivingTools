package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SacrificeAbility extends ActiveAbility {

    public SacrificeAbility() {
        super("sacrifice", "Sacrificio", "Click Derecho: Sacrifica vida por fuerza (30s CD).", 30000);
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (player.getHealth() > 4) {
            if (!checkCooldown(player))
                return;

            player.damage(4); // Take 2 hearts damage
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 2)); // 10s Strength III

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1, 0.5f);
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(org.bukkit.Color.RED, 2));
            player.sendMessage("§c¡Sacrificio de Sangre!");
        } else {
            player.sendMessage("§c¡No tienes suficiente vida para el sacrificio!");
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD");
    }
}
