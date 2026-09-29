package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * ToolGuardianCompanion — Espíritu Guardián flotante manifestado por el alma de la herramienta.
 *
 * Requisito: Nivel 50+ o Prestigio 1+.
 * Funcionalidades:
 *  - Radar Espiritual: Detecta Diamantes y Ancient Debris ocultos a 8 bloques y guía con partículas.
 *  - Magnetismo de XP: Atrae orbes de experiencia cercanos hacia el jugador.
 *  - Luz Guía: Otorga Visión Nocturna en cuevas oscuras.
 */
public class ToolGuardianCompanion implements Listener {

    private static final Set<UUID> enabledCompanions = new HashSet<>();
    private static final Map<UUID, Long> lastRadarScan = new HashMap<>();
    private static BukkitTask companionTask = null;

    public static void startLoop() {
        if (companionTask != null) companionTask.cancel();

        companionTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!enabledCompanions.contains(player.getUniqueId())) continue;

                    ItemStack held = player.getInventory().getItemInMainHand();
                    if (!LivingTool.isLivingTool(held)) continue;

                    LivingTool tool = new LivingTool(held);
                    ToolData data = tool.getData();
                    if (data.getLevel() < 50 && data.getPrestige() < 1) continue;

                    // Posición flotante sobre el hombro derecho del jugador
                    Location eye = player.getEyeLocation();
                    Vector right = eye.getDirection().crossProduct(new Vector(0, 1, 0)).normalize();
                    Location companionLoc = eye.clone().add(right.multiply(0.8)).add(0, 0.3, 0);

                    // Renderizar Wisp Espiritual
                    String personality = data.getPersonality() != null ? data.getPersonality() : "WISE";
                    renderWisp(companionLoc, personality);

                    // Magnetismo de XP
                    for (org.bukkit.entity.Entity e : player.getWorld().getNearbyEntities(companionLoc, 5, 5, 5)) {
                        if (e instanceof ExperienceOrb) {
                            Vector pull = player.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.3);
                            e.setVelocity(pull);
                        }
                    }

                    // Radar de Minerales (cada 6 segundos)
                    scanNearbyOres(player, tool, companionLoc);
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 20L, 4L); // cada 4 ticks (~5 FPS para animaciones suaves)
    }

    private static void renderWisp(Location loc, String personality) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle coreParticle;
        Color color;
        switch (personality.toUpperCase()) {
            case "AGGRESSIVE":
                coreParticle = Particle.FLAME;
                color = Color.fromRGB(255, 60, 0);
                break;
            case "LAZY":
                coreParticle = Particle.SNOWFLAKE;
                color = Color.fromRGB(120, 220, 255);
                break;
            case "CHEERFUL":
                coreParticle = Particle.ELECTRIC_SPARK;
                color = Color.fromRGB(255, 220, 0);
                break;
            default: // WISE
                coreParticle = Particle.END_ROD;
                color = Color.fromRGB(150, 80, 255);
                break;
        }

        world.spawnParticle(Particle.REDSTONE, loc, 2, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(color, 1.2f));
        world.spawnParticle(coreParticle, loc, 1, 0.05, 0.05, 0.05, 0.01);
    }

    private static void scanNearbyOres(Player player, LivingTool tool, Location companionLoc) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - lastRadarScan.getOrDefault(uuid, 0L) < 6000L) return;
        lastRadarScan.put(uuid, now);

        Location pLoc = player.getLocation();
        int radius = 8;
        Block targetOre = null;

        searchLoop:
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = pLoc.clone().add(x, y, z).getBlock();
                    Material mat = b.getType();
                    if (mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE || mat == Material.ANCIENT_DEBRIS) {
                        targetOre = b;
                        break searchLoop;
                    }
                }
            }
        }

        if (targetOre != null) {
            Location oreLoc = targetOre.getLocation().add(0.5, 0.5, 0.5);
            ParticleOptimizer.spawnLine(companionLoc, oreLoc, Particle.SPELL_WITCH, 0.6, null);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.8f, 1.8f);
        }
    }

    public static boolean toggleCompanion(Player player) {
        UUID uuid = player.getUniqueId();
        if (enabledCompanions.contains(uuid)) {
            enabledCompanions.remove(uuid);
            return false;
        } else {
            enabledCompanions.add(uuid);
            return true;
        }
    }

    public static boolean isEnabled(Player player) {
        return enabledCompanions.contains(player.getUniqueId());
    }

    public static void cleanup(UUID uuid) {
        enabledCompanions.remove(uuid);
        lastRadarScan.remove(uuid);
    }
}
