package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class BondManager {

    public static void bond(Player player, Player target) {
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemStack targetItem = target.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item) || !LivingTool.isLivingTool(targetItem)) {
            player.sendMessage(ChatColor.RED + "Ambos deben sostener una Herramienta Viviente.");
            return;
        }

        LivingTool tool = new LivingTool(item);
        LivingTool targetTool = new LivingTool(targetItem);

        // Store UUIDs
        tool.getData().setBondPartner(target.getUniqueId().toString());
        targetTool.getData().setBondPartner(player.getUniqueId().toString());

        tool.updateLore();
        targetTool.updateLore();

        player.sendMessage(ChatColor.GREEN + "¡Has formado un Pacto de Sangre con " + target.getName() + "!");
        target.sendMessage(ChatColor.GREEN + "¡Has formado un Pacto de Sangre con " + player.getName() + "!");

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10);
        target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, 1, 0), 10);
    }

    public static void startBondTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkBond(player);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 100L); // Every 5 seconds
    }

    private static void checkBond(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return;

        LivingTool tool = new LivingTool(item);
        String partnerUUIDStr = tool.getData().getBondPartner();

        if (partnerUUIDStr == null || partnerUUIDStr.isEmpty())
            return;

        try {
            UUID partnerUUID = UUID.fromString(partnerUUIDStr);
            Player partner = Bukkit.getPlayer(partnerUUID);

            if (partner != null && partner.isOnline() && partner.getWorld().equals(player.getWorld())) {
                double distance = player.getLocation().distance(partner.getLocation());
                if (distance < 15) {
                    // Apply Buffs
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0, true, false, true)); // Strength
                                                                                                            // I
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0, true, false, true)); // Resistance
                                                                                                              // I

                    // Visuals
                    if (Math.random() < 0.3) { // 30% chance per check to avoid spam
                        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 1);
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static UUID getBondPartner(LivingTool tool) {
        String uuidStr = tool.getData().getBondPartner();
        if (uuidStr == null || uuidStr.isEmpty())
            return null;
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
