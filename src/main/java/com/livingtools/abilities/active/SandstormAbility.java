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

public class SandstormAbility extends Ability {

    public SandstormAbility() {
        super("sandstorm", "Tormenta de Arena", "Click derecho para cegar a enemigos cercanos.", 45);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();

            if (isOnCooldown(player))
                return;

            player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.getLocation(), 100, 3, 1, 3, 0.1);
            player.playSound(player.getLocation(), Sound.BLOCK_SAND_BREAK, 2, 0.5f);
            player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1, 0.5f);

            // Mastery Scaling
            int level = getLevel(tool);
            int radius = 3 + level; // Lvl 1: 4, Lvl 5: 8
            int blindDuration = 60 + (level * 20); // Lvl 1: 4s, Lvl 5: 8s
            int slowDuration = 40 + (level * 10); // Lvl 1: 2.5s, Lvl 5: 4.5s

            int count = 0;
            for (Entity e : player.getNearbyEntities(radius, 3, radius)) {
                if (e instanceof LivingEntity && e != player) {
                    LivingEntity victim = (LivingEntity) e;
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindDuration, 0));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, 1));
                    count++;
                }
            }

            // Add XP
            if (count > 0) {
                addXP(player, tool, count * 10);
            }

            if (count > 0) {
                player.sendMessage(ChatColor.YELLOW + "¡Has cegado a " + count + " enemigos!");
            } else {
                player.sendMessage(ChatColor.GRAY + "No hay enemigos cerca.");
            }

            addCooldown(player, 15000); // 15 seconds
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SHOVEL");
    }
}
