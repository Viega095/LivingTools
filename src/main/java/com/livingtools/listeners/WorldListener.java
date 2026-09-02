package com.livingtools.listeners;

import com.livingtools.data.LivingTool;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class WorldListener implements Listener {

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (LivingTool.isLivingTool(item)) {
                double damageMultiplier = 1.0;

                // Weather Synergy
                if (player.getWorld().hasStorm()) {
                    damageMultiplier += 0.15; // +15% Damage during storm
                }

                // Biome Adaptation
                Biome biome = player.getLocation().getBlock().getBiome();
                if (isHotBiome(biome)) {
                    event.getEntity().setFireTicks(60); // Fire Aspect in hot biomes
                } else if (isColdBiome(biome)) {
                    // Slow effect handled by ability usually, but we can add minor slow here
                    if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                        ((org.bukkit.entity.LivingEntity) event.getEntity()).addPotionEffect(
                                new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 40, 0));
                    }
                }

                if (damageMultiplier > 1.0) {
                    event.setDamage(event.getDamage() * damageMultiplier);
                }
            }
        }
    }

    private boolean isHotBiome(Biome biome) {
        return biome.name().contains("DESERT") || biome.name().contains("BADLANDS") || biome.name().contains("NETHER");
    }

    private boolean isColdBiome(Biome biome) {
        return biome.name().contains("SNOW") || biome.name().contains("ICE") || biome.name().contains("TAIGA");
    }
}
