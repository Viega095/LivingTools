package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EarthSetBonusAbility extends Ability {

    public EarthSetBonusAbility() {
        super("earth_set_bonus", "Set de Tierra",
                "+20% vida máxima + regeneración en tierra + resistencia a knockback",
                1, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Regeneración en tierra (cuando está en el suelo)
        if (player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, false, false));
            }

            // Absorción para simular +20% vida
            if (!player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, false));
            }

            // Partículas de tierra
            if (Math.random() < 0.1) {
                player.getWorld().spawnParticle(
                        org.bukkit.Particle.BLOCK_CRACK,
                        player.getLocation(),
                        5,
                        0.3, 0.1, 0.3,
                        Material.DIRT.createBlockData());
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
