package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BiomeManager {

    public enum BiomeCategory {
        FOREST, DESERT, PLAINS, MOUNTAIN, OCEAN, ICE, NETHER, END, SKY, NONE
    }

    public static void startBiomeTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkBiome(player);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 100L); // Check every 5 seconds
    }

    private static void checkBiome(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return;

        Biome biome = player.getLocation().getBlock().getBiome();
        applyBiomeBonus(player, biome);
    }

    private static void applyBiomeBonus(Player player, Biome biome) {
        String biomeName = biome.name();
        ChatColor color = ChatColor.WHITE;

        if (biomeName.contains("FOREST") || biomeName.contains("JUNGLE")) {
            // Speed I
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0, true, false, true));
            color = ChatColor.GREEN;
        } else if (biomeName.contains("DESERT") || biomeName.contains("BADLANDS") || biomeName.contains("SAVANNA")) {
            // Fire Resistance
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0, true, false, true));
            color = ChatColor.GOLD;
        } else if (biomeName.contains("OCEAN") || biomeName.contains("RIVER") || biomeName.contains("BEACH")) {
            // Water Breathing
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 120, 0, true, false, true));
            color = ChatColor.AQUA;
        } else if (biomeName.contains("SNOW") || biomeName.contains("ICE") || biomeName.contains("FROZEN")) {
            // Resistance I
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0, true, false, true));
            color = ChatColor.WHITE;
        } else if (biomeName.contains("NETHER")) {
            // Strength I
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0, true, false, true));
            color = ChatColor.RED;
        } else if (biomeName.contains("END")) {
            // Slow Falling
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 0, true, false, true));
            color = ChatColor.LIGHT_PURPLE;
        }

        // Update Tool Name Color
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            // String currentName = tool.getItem().getItemMeta().getDisplayName();

            // Avoid constant updates if color matches
            if (!tool.getData().getBiomeColor().equals(color.name())) {
                tool.getData().setBiomeColor(color.name());
                tool.updateLore();
            }
        }
    }

    public static BiomeCategory getCategory(Biome biome) {
        String name = biome.name();
        if (name.contains("FOREST") || name.contains("JUNGLE") || name.contains("TAIGA"))
            return BiomeCategory.FOREST;
        if (name.contains("DESERT") || name.contains("BADLANDS") || name.contains("SAVANNA"))
            return BiomeCategory.DESERT;
        if (name.contains("OCEAN") || name.contains("RIVER") || name.contains("BEACH"))
            return BiomeCategory.OCEAN;
        if (name.contains("SNOW") || name.contains("ICE") || name.contains("FROZEN"))
            return BiomeCategory.ICE;
        if (name.contains("NETHER") || name.contains("CRIMSON") || name.contains("WARPED") || name.contains("SOUL"))
            return BiomeCategory.NETHER;
        if (name.contains("END"))
            return BiomeCategory.END;
        if (name.contains("MOUNTAIN") || name.contains("PEAKS") || name.contains("HILLS"))
            return BiomeCategory.MOUNTAIN;
        if (name.contains("PLAINS") || name.contains("MEADOW"))
            return BiomeCategory.PLAINS;
        return BiomeCategory.NONE;
    }

}
