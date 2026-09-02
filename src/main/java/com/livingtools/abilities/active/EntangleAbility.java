package com.livingtools.abilities.active;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntangleAbility extends Ability {

    public EntangleAbility() {
        super("entangle", "Enredadera", "Click derecho para atrapar enemigos cercanos.", 25);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();

            if (isOnCooldown(player))
                return;

            int level = getLevel(tool);
            double radius = 3.0 + (level * 0.5); // Lvl 1: 3.5, Lvl 5: 5.5
            int duration = 60 + (level * 20); // Lvl 1: 4s, Lvl 5: 8s
            boolean root = (level >= 5);

            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 20, radius, 0.5, radius, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 0.5f);

            int count = 0;
            for (Entity e : player.getNearbyEntities(radius, 2, radius)) {
                if (e instanceof LivingEntity && e != player) {
                    LivingEntity victim = (LivingEntity) e;
                    if (root) {
                        // Slowness 10 makes them unable to move
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 10));
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration, 250)); // Prevent
                                                                                                        // jumping
                    } else {
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 2));
                    }
                    count++;
                }
            }

            if (count > 0) {
                player.sendMessage(ChatColor.GREEN + "Has enredado a " + count + " enemigos.");
                addXP(player, tool, count * 10);
            } else {
                player.sendMessage(ChatColor.GRAY + "No hay enemigos cerca.");
            }

            addCooldown(player, 12000); // 12s cooldown
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HOE");
    }
}
