package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * SoulCompassManager — Brújula de Almas y Generador Cinemático Espectacular de Meteoritos Celestiales.
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
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Coordenadas del impacto: " + ChatColor.WHITE + "X: " + impactLoc.getBlockX() + " | Y: " + impactLoc.getBlockY() + " | Z: " + impactLoc.getBlockZ());
        Bukkit.broadcastMessage(ChatColor.GRAY + "Usa una " + ChatColor.AQUA + "Brújula de Almas" + ChatColor.GRAY + " para rastrear el cráter y extraer minerales celestiales.");
        Bukkit.broadcastMessage("");

        playMeteorFallCinematic(impactLoc);
    }

    /**
     * Dispara la animación cinemática completa de meteorito en una ubicación específica (para testing o eventos).
     */
    public static void playMeteorFallCinematic(Location impactLoc) {
        World world = impactLoc.getWorld();
        if (world == null) return;

        currentMeteorLocation = impactLoc;

        // Punto de origen alto en el cielo con ángulo de caída diagonal
        double startY = Math.min(250.0, impactLoc.getY() + 90.0);
        double startX = impactLoc.getX() - 25.0;
        double startZ = impactLoc.getZ() - 25.0;
        Location startLoc = new Location(world, startX, startY, startZ);

        final int totalSteps = 35; // ~1.75 segundos de caída cinematográfica

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step >= totalSteps) {
                    cancel();

                    // === IMPACTO CINEMÁTICO: MINI-EXPLOSIÓN Y ONDA EXPANSIVA ===
                    world.strikeLightningEffect(impactLoc);
                    world.spawnParticle(Particle.EXPLOSION_HUGE, impactLoc.clone().add(0, 1, 0), 4, 1.2, 1.2, 1.2, 0.1);
                    world.spawnParticle(Particle.FLASH, impactLoc.clone().add(0, 1.5, 0), 6, 0.5, 0.5, 0.5, 0.0);
                    world.spawnParticle(Particle.LAVA, impactLoc.clone().add(0, 1, 0), 50, 1.5, 1.5, 1.5, 0.2);

                    // Sonidos de impacto masivo
                    world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.6f);
                    world.playSound(impactLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3.5f, 0.7f);
                    world.playSound(impactLoc, Sound.ITEM_TRIDENT_THUNDER, 3.0f, 1.0f);
                    world.playSound(impactLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);

                    // Onda expansiva en anillo horizontal
                    new BukkitRunnable() {
                        double shockRadius = 1.0;
                        @Override
                        public void run() {
                            if (shockRadius > 8.0) {
                                cancel();
                                return;
                            }
                            for (int d = 0; d < 360; d += 15) {
                                double rad = Math.toRadians(d);
                                double rx = Math.cos(rad) * shockRadius;
                                double rz = Math.sin(rad) * shockRadius;
                                Location ringLoc = impactLoc.clone().add(rx, 0.3, rz);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, ringLoc, 2, 0.1, 0.1, 0.1, 0.02);
                                world.spawnParticle(Particle.SMOKE_LARGE, ringLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                            shockRadius += 1.2;
                        }
                    }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);

                    // Empuje físico radial a entidades cercanas
                    for (Entity e : world.getNearbyEntities(impactLoc, 8.0, 5.0, 8.0)) {
                        if (e instanceof LivingEntity) {
                            Vector push = e.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(0.8);
                            push.setY(0.4);
                            e.setVelocity(push);
                            if (e instanceof Player) {
                                ((Player) e).playSound(e.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.2f, 1.5f);
                            }
                        }
                    }

                    // Generar cráter con minerales cósmicos
                    generateMeteorCrater(impactLoc);
                    return;
                }

                // Interpolación de trayectoria 3D
                double progress = (double) step / totalSteps;
                double curX = startLoc.getX() + (impactLoc.getX() - startLoc.getX()) * progress;
                double curY = startLoc.getY() + (impactLoc.getY() - startLoc.getY()) * progress;
                double curZ = startLoc.getZ() + (impactLoc.getZ() - startLoc.getZ()) * progress;
                Location fallLoc = new Location(world, curX, curY, curZ);

                // Estela masiva visible desde gran distancia
                world.spawnParticle(Particle.FLAME, fallLoc, 60, 0.8, 0.8, 0.8, 0.08);
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, fallLoc, 20, 0.5, 0.5, 0.5, 0.04);
                world.spawnParticle(Particle.LAVA, fallLoc, 15, 0.4, 0.4, 0.4, 0.1);
                world.spawnParticle(Particle.END_ROD, fallLoc, 12, 0.3, 0.3, 0.3, 0.05);

                if (step % 4 == 0) {
                    world.spawnParticle(Particle.FLASH, fallLoc, 2, 0.2, 0.2, 0.2, 0.0);
                    world.playSound(fallLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 3.5f, 0.5f);
                    world.playSound(fallLoc, Sound.ITEM_TRIDENT_RIPTIDE_2, 3.0f, 0.7f);
                }

                step++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);
    }

    private static void generateMeteorCrater(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        Material[] rareBlocks = {
                Material.CRYING_OBSIDIAN,
                Material.AMETHYST_BLOCK,
                Material.ANCIENT_DEBRIS,
                Material.RAW_GOLD_BLOCK,
                Material.GILDED_BLACKSTONE,
                Material.MAGMA_BLOCK
        };

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (Math.abs(x) + Math.abs(z) <= 3) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        if (x == 0 && z == 0 && y == 0) {
                            b.setType(Material.ANCIENT_DEBRIS);
                        } else if (random.nextDouble() < 0.65) {
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
