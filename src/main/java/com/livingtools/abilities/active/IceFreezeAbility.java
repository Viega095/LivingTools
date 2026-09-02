package com.livingtools.abilities.active;

import com.livingtools.abilities.ElementalAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceFreezeAbility extends ElementalAbility {

    public IceFreezeAbility() {
        super("ice_freeze", "Congelación", "Congela a los enemigos cercanos.", 35, 20000);
    }

    @Override
    protected boolean activate(Player player, LivingTool tool) {
        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(), 100, 4, 1, 4, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);

        for (Entity entity : player.getNearbyEntities(6, 2, 6)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10)); // 3s Slowness
                ((LivingEntity) entity).damage(2.0, player);
            }
        }
        return true;
    }

    @Override
    public boolean isCompatible(org.bukkit.Material material) {
        return material.name().endsWith("_SWORD") || material.name().endsWith("_AXE");
    }
}
