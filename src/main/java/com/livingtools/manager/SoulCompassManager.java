package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * SoulCompassManager — Brújula de Almas y Generador Cinemático de Eventos de Meteoritos Celestiales.
 */
public class SoulCompassManager {

    private static Location currentMeteorLocation = null;
    private static BukkitTask meteorEventTask = null;
    private static final Random random = new Random();

    public static void startEventScheduler() {
        if (meteorEventTask != null) meteorEventTask.cancel();

        // Chequear y lanzar evento cada 30 minutos (36000 ticks)
        meteorEventTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;
                Player targetPlayer = new ArrayList<>(Bukkit.getOnlinePlayers()).get(random.nextInt(Bukkit.getOnlinePlayers().size()));
                spawnMeteorEvent(targetPlayer.getLocation());
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 1200L, 36000L);
    }

    public static void spawnMeteorEvent(Location nearLoc) {
        World world = nearLoc.getWorld();
        if (world == null) return;

        double ox = (random.nextDouble() - 0.5) * 400.0;
        double oz = (random.nextDouble() - 0.5) * 400.0;
        Location impactLoc = nearLoc.clone().add(ox, 0, oz);
        impactLoc.setY(world.getHighestBlockYAt(impactLoc));

        currentMeteorLocation = impactLoc;

        // Anuncio global
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "☄ " + ChatColor.BOLD + "¡UN METEORITO CELESTIAL HA CAÍDO DEL CIELO! " + ChatColor.GOLD + "☄");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Coordenadas aproximadas: " + ChatColor.WHITE + "X: " + impactLoc.getBlockX() + " | Z: " + impactLoc.getBlockZ());
        Bukkit.broadcastMessage(ChatColor.GRAY + "Usa una " + ChatColor.AQUA + "Brújula de Almas" + ChatColor.GRAY + " para rastrear el impacto y extraer minerales ancestrales.");
        Bukkit.broadcastMessage("");

        // Animación cinemática de caída (3 segundos)
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step >= 30) {
                    cancel();

                    // Impacto en el suelo
                    world.strikeLightningEffect(impactLoc);
                    world.spawnParticle(Particle.EXPLOSION_HUGE, impactLoc, 5, 1, 1, 1, 0.1);
                    world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                    world.playSound(impactLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 0.8f);

                    // Generar cráter con minerales celestiales
                    generateMeteorCrater(impactLoc);
                    return;
                }

                double progress = (double) step / 30.0;
                double y = 160.0 - (progress * (160.0 - impactLoc.getY()));
                Location fallLoc = new Location(world, impactLoc.getX(), y, impactLoc.getZ());

                world.spawnParticle(Particle.FLAME, fallLoc, 40, 0.6, 0.6, 0.6, 0.05);
                world.spawnParticle(Particle.LAVA, fallLoc, 15, 0.3, 0.3, 0.3, 0.1);
                world.playSound(fallLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 0.5f);

                step++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 2L);
    }

    private static void generateMeteorCrater(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        Material[] rareBlocks = {Material.CRYING_OBSIDIAN, Material.AMETHYST_BLOCK, Material.ANCIENT_DEBRIS, Material.RAW_GOLD_BLOCK};

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (Math.abs(x) + Math.abs(z) <= 3) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        if (x == 0 && z == 0 && y == 0) {
                            b.setType(Material.ANCIENT_DEBRIS);
                        } else if (random.nextDouble() < 0.6) {
                            b.setType(rareBlocks[random.nextInt(rareBlocks.length)]);
                        }
                    }
                }
            }
        }
    }

    public static ItemStack createSoulCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "🧭 Brújula de Almas");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Sintonizada con las perturbaciones energéticas.");
            lore.add(ChatColor.GRAY + "Apunta hacia meteoritos caídos y eventos cósmicos.");
            lore.add("");
            lore.add(ChatColor.YELLOW + "► Click derecho para sintonizar posición");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(LivingToolsPlugin.getInstance(), "is_soul_compass"),
                    PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isSoulCompass(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(LivingToolsPlugin.getInstance(), "is_soul_compass"),
                PersistentDataType.BYTE);
    }

    public static Location getCurrentMeteorLocation() {
        return currentMeteorLocation;
    }
}
