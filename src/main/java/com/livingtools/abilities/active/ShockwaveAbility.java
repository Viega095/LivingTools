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
import org.bukkit.util.Vector;

public class ShockwaveAbility extends Ability {

    public ShockwaveAbility() {
        super("shockwave", "Onda de Choque", "Click derecho para empujar enemigos cercanos.", 40);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();

            if (isOnCooldown(player))
                return;

            int level = getLevel(tool);
            double radius = 4.0 + (level * 0.5); // Lvl 1: 4.5, Lvl 5: 6.5
            double force = 1.0 + (level * 0.2); // Lvl 1: 1.2, Lvl 5: 2.0

            player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 1);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

            int count = 0;
            for (Entity e : player.getNearbyEntities(radius, 2, radius)) {
                if (e instanceof LivingEntity && e != player) {
                    Vector dir = e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    dir.setY(0.5); // Upward lift
                    e.setVelocity(dir.multiply(force));
                    count++;
                }
            }

            if (count > 0) {
                player.sendMessage(ChatColor.GOLD + "¡Onda de Choque! (" + count + " enemigos)");
                addXP(player, tool, count * 15);
            } else {
                player.sendMessage(ChatColor.GRAY + "No hay enemigos cerca.");
            }

            addCooldown(player, 10000); // 10 seconds
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE");
    }
}
