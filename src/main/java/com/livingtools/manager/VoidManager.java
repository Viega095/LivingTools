package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class VoidManager {

    private static final Random random = new Random();

    public static void checkVoidTouch(Player player, LivingTool tool) {
        if (tool.getData().getLevel() < 50)
            return;

        // Chance to become Void-Touched when in The End
        if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
            if (random.nextInt(1000) == 0) { // 0.1% chance per check (e.g., on kill)
                applyVoidTouch(player, tool);
            }
        }
    }

    public static void applyVoidTouch(Player player, LivingTool tool) {
        if (tool.getData().getPersonality().equals("VOID_TOUCHED"))
            return;

        tool.getData().setPersonality("VOID_TOUCHED");
        tool.updateLore();

        player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + "!!! " + ChatColor.RESET
                + ChatColor.DARK_PURPLE + " El Vacío ha reclamado tu herramienta... " + ChatColor.MAGIC + "!!!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.5f);
        player.spawnParticle(Particle.PORTAL, player.getLocation(), 100, 0.5, 1, 0.5);
    }

    public static void triggerVoidRift(Location location) {
        World world = location.getWorld();
        if (world == null)
            return;

        world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 50, 1, 1, 1);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    this.cancel();
                    return;
                }

                if (ticks % 20 == 0) {
                    world.spawnParticle(Particle.PORTAL, location, 20, 0.5, 0.5, 0.5);
                    // Damage nearby entities? Or drop special loot?
                    // For now, let's say it drops "Void Essence" (Lapis Lazuli renamed)
                    if (random.nextInt(5) == 0) {
                        ItemStack essence = new ItemStack(Material.LAPIS_LAZULI);
                        // Meta for name...
                        world.dropItemNaturally(location, essence);
                    }
                }
                ticks += 5;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 5L);
    }
}
