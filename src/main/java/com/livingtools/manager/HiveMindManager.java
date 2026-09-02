package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HiveMindManager {

    private static final double RADIUS = 15.0;
    private static final int MIN_PLAYERS = 2;
    // Store active buffs to avoid spamming messages
    private static final Map<UUID, Boolean> activeBuffs = new HashMap<>();

    public static void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkProximity(player);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 100L); // Check every 5 seconds
    }

    private static void checkProximity(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            removeBuff(player);
            return;
        }

        int nearbyTools = 0;
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof Player) {
                Player nearby = (Player) entity;
                ItemStack nearbyItem = nearby.getInventory().getItemInMainHand();
                if (LivingTool.isLivingTool(nearbyItem)) {
                    nearbyTools++;
                }
            }
        }

        if (nearbyTools >= (MIN_PLAYERS - 1)) { // -1 because we don't count ourselves in nearbyEntities
            applyBuff(player);
        } else {
            removeBuff(player);
        }
    }

    private static void applyBuff(Player player) {
        if (!activeBuffs.getOrDefault(player.getUniqueId(), false)) {
            activeBuffs.put(player.getUniqueId(), true);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ ¡Mente Colmena Activada! XP compartida aumentada.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 1);
        }
        // Apply actual buff logic here (e.g., potion effect or just a flag for XP
        // listener)
        // For now, let's give a small Luck effect as a visual/gameplay bonus
        player.addPotionEffect(
                new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.LUCK, 120, 0, true, false, true));
    }

    private static void removeBuff(Player player) {
        if (activeBuffs.getOrDefault(player.getUniqueId(), false)) {
            activeBuffs.put(player.getUniqueId(), false);
            player.sendMessage(ChatColor.GRAY + "La conexión de la colmena se desvanece...");
        }
    }

    public static boolean hasHiveMindBuff(Player player) {
        return activeBuffs.getOrDefault(player.getUniqueId(), false);
    }
}
