package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class CorruptionListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;

        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item))
            return;

        LivingTool tool = new LivingTool(item);
        EntityType type = event.getEntityType();

        int corruptionGain = 0;
        if (type == EntityType.VILLAGER) {
            corruptionGain = 5;
        } else if (type == EntityType.PLAYER) {
            corruptionGain = 10;
        } else if (type == EntityType.IRON_GOLEM) {
            corruptionGain = 3;
        }

        if (corruptionGain > 0) {
            tool.getData().addCorruption(corruptionGain);
            player.sendMessage(ChatColor.DARK_PURPLE + "Tu herramienta se siente más oscura... (+" + corruptionGain
                    + "% Corrupción)");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 0.5f);
            tool.updateLore();

            // Guide AI Trigger
            com.livingtools.manager.GuideManager.triggerStep(player, tool,
                    com.livingtools.manager.GuideManager.TutorialStep.CORRUPTION_START);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item))
            return;

        LivingTool tool = new LivingTool(item);
        int corruption = tool.getData().getCorruption();

        if (corruption <= 0)
            return;

        // Power Mechanic: Critical Hit Chance
        // 1% chance per 1% corruption to deal extra damage
        if (random.nextInt(100) < corruption) {
            double multiplier = 1.0 + (corruption / 100.0); // Up to 2x damage at 100% corruption
            event.setDamage(event.getDamage() * multiplier);
            player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, event.getEntity().getLocation(), 10, 0.5,
                    0.5, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 0.5f);
        }

        // Betrayal Mechanic: Chance to damage user
        // Starts at 20% corruption. Chance = (Corruption - 20) / 2
        if (corruption > 20) {
            double betrayalChance = (corruption - 20) / 2.0;
            if (random.nextDouble() * 100 < betrayalChance) {
                event.setCancelled(true);
                player.damage(2.0); // 1 heart damage
                player.sendMessage(ChatColor.DARK_RED + "¡Tu herramienta te traiciona!");
                player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1, 0.5f);
            }
        }
    }
}
