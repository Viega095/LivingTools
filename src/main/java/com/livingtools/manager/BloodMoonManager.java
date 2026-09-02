package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BloodMoonManager {

    private static boolean isBloodMoonActive = false;
    private static final int BLOOD_MOON_CHANCE = 10; // 10% chance per night

    public static void startCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorlds().get(0); // Main world
                if (world == null)
                    return;

                long time = world.getTime();

                // Check at sunset (around 13000 ticks)
                if (time >= 13000 && time < 13100) {
                    if (!isBloodMoonActive && Math.random() * 100 < BLOOD_MOON_CHANCE) {
                        triggerBloodMoon(world);
                    }
                }

                // End at sunrise (around 23000 ticks)
                if (time >= 23000 && time < 23100) {
                    if (isBloodMoonActive) {
                        endBloodMoon(world);
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 100L); // Check every 5 seconds
    }

    public static void triggerBloodMoon(World world) {
        isBloodMoonActive = true;

        for (Player player : world.getPlayers()) {
            player.sendTitle(ChatColor.DARK_RED + "☠ LA LUNA DE SANGRE ASCIENDE ☠",
                    ChatColor.RED + "La oscuridad se fortalece...", 10, 100, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);
        }

        Bukkit.broadcastMessage(ChatColor.DARK_RED + "[LivingTools] " + ChatColor.RED
                + "¡La Luna de Sangre ha comenzado! Los monstruos son más fuertes.");
    }

    public static void endBloodMoon(World world) {
        isBloodMoonActive = false;

        for (Player player : world.getPlayers()) {
            player.sendMessage(ChatColor.GREEN + "La Luna de Sangre se desvanece con el amanecer...");
        }
    }

    public static void forceStart() {
        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            world.setTime(13000); // Set to night
            triggerBloodMoon(world);
        }
    }

    public static void forceStop() {
        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            endBloodMoon(world);
        }
    }

    public static boolean isBloodMoonActive() {
        return isBloodMoonActive;
    }
}
