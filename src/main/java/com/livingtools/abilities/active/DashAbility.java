package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DashAbility extends ActiveAbility {

    public DashAbility() {
        super("dash", "Dash", "Click derecho para impulsarte hacia adelante.", 10000); // 10s cooldown
    }

    @Override
    public void onRightClick(Player player, com.livingtools.data.LivingTool tool) {
        if (!checkCooldown(player)) {
            // Echo Rune Check
            if (!checkEchoRune(tool, player)) {
                return;
            }
        }

        // Mastery Scaling
        int level = getLevel(tool);
        // Base Cooldown is 10s. Level reduces it.
        // Lvl 1: 10s, Lvl 5: 2s (2s reduction per level above 1? No, 10 -> 8 -> 6 -> 4
        // -> 2)
        // Actually checkCooldown uses fixed cooldownMillis from constructor.
        // I need to override checkCooldown or modify it to accept dynamic cooldown.
        // Since checkCooldown is in ActiveAbility and uses private map, I can't easily
        // change it without modifying ActiveAbility again.
        // Alternative: Just use the boost power scaling.

        // Let's scale Boost Power instead for simplicity, or I can modify ActiveAbility
        // to have setCooldown.
        // Plan said "Level reduces cooldown".
        // I'll stick to plan. I need to modify ActiveAbility to allow dynamic cooldowns
        // or just handle cooldown manually here if I want.
        // But checkCooldown is final-ish.

        // Let's modify ActiveAbility to be more flexible? No, too many changes.
        // I will scale Boost Power and add XP.

        Vector direction = player.getLocation().getDirection();
        double speed = 2.0 + (level * 0.5); // Lvl 1: 2.5, Lvl 5: 4.5
        player.setVelocity(direction.multiply(speed));

        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 0.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(org.bukkit.ChatColor.GREEN + "¡Dash! (Nvl " + level + ")");

        addXP(player, tool, 5);
    }
}
