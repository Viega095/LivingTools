package com.livingtools.listeners;

import com.livingtools.abilities.active.BlinkAbility;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BlinkBoostListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        // Check if player has blink damage boost
        if (!BlinkAbility.hasDamageBoost(player)) {
            return;
        }

        // Get the boost percentage
        double boostMultiplier = BlinkAbility.getDamageBoost(player);

        if (boostMultiplier > 0) {
            // Apply damage bonus
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * boostMultiplier;
            event.setDamage(originalDamage + bonusDamage);

            // Visual & sound feedback
            event.getEntity().getWorld().spawnParticle(
                    org.bukkit.Particle.CRIT_MAGIC,
                    event.getEntity().getLocation().add(0, 1, 0),
                    15, 0.3, 0.5, 0.3, 0.05);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.5f);

            // Consume the boost (one-time use)
            BlinkAbility.consumeDamageBoost(player);

            // Notification
            player.sendMessage(ChatColor.AQUA + "✦ " + ChatColor.YELLOW +
                    "Blink Boost activado! (+" + String.format("%.0f", boostMultiplier * 100) + "% daño)");
        }
    }
}
