package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionalRiftAbility extends ActiveAbility {

    private static final Map<UUID, PortalPair> activePortals = new HashMap<>();
    private static final Map<UUID, Location> pendingPortals = new HashMap<>();

    private static class PortalPair {
        Location portal1;
        Location portal2;
        long expiryTime;

        PortalPair(Location p1, Location p2) {
            this.portal1 = p1;
            this.portal2 = p2;
            this.expiryTime = System.currentTimeMillis() + 30000; // 30 seconds
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public DimensionalRiftAbility() {
        super("dimensionalrift", "Dimensional Rift",
                "Click Derecho 2 veces: Crea portales enlazados.", 45000); // 45s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        UUID uuid = player.getUniqueId();

        // Check if player has pending portal
        if (pendingPortals.containsKey(uuid)) {
            // Create second portal
            Location portal2 = player.getLocation();
            Location portal1 = pendingPortals.remove(uuid);

            // Check distance
            if (portal1.distance(portal2) < 3) {
                player.sendMessage(ChatColor.RED + "¡Los portales están demasiado cerca!");
                return;
            }

            if (portal1.distance(portal2) > 50) {
                player.sendMessage(ChatColor.RED + "¡Los portales están demasiado lejos! (máximo 50 bloques)");
                return;
            }

            // Create portal pair
            createPortalPair(player, portal1, portal2);

            // Apply cooldown only on second portal
            if (!checkCooldown(player))
                return;

        } else {
            // Create first portal
            pendingPortals.put(uuid, player.getLocation());
            player.sendMessage(
                    ChatColor.LIGHT_PURPLE + "✦ Portal 1 creado. Usa la habilidad de nuevo para el Portal 2.");

            // Visual feedback
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 1);
            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1, 1.5f);
        }
    }

    private void createPortalPair(Player player, Location loc1, Location loc2) {
        UUID uuid = player.getUniqueId();

        // Remove old portals if exists
        if (activePortals.containsKey(uuid)) {
            player.sendMessage(ChatColor.YELLOW + "Portales anteriores reemplazados.");
        }

        // Create new portal pair
        PortalPair pair = new PortalPair(loc1, loc2);
        activePortals.put(uuid, pair);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ ¡Portales dimensionales creados! (30s duración)");

        // Start portal effects and teleportation check
        startPortalEffects(player, pair);
    }

    private void startPortalEffects(Player owner, PortalPair pair) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (pair.isExpired()) {
                    activePortals.remove(owner.getUniqueId());
                    owner.sendMessage(ChatColor.GRAY + "Los portales dimensionales se han cerrado.");
                    this.cancel();
                    return;
                }

                // Portal particles
                pair.portal1.getWorld().spawnParticle(Particle.PORTAL, pair.portal1, 15, 0.5, 1, 0.5, 0.5);
                pair.portal2.getWorld().spawnParticle(Particle.PORTAL, pair.portal2, 15, 0.5, 1, 0.5, 0.5);
                pair.portal1.getWorld().spawnParticle(Particle.REVERSE_PORTAL, pair.portal1, 10, 0.3, 0.5, 0.3, 0);
                pair.portal2.getWorld().spawnParticle(Particle.REVERSE_PORTAL, pair.portal2, 10, 0.3, 0.5, 0.3, 0);

                // Check for entities near portals
                if (ticks % 10 == 0) { // Check every 0.5s
                    checkTeleportation(pair.portal1, pair.portal2);
                    checkTeleportation(pair.portal2, pair.portal1);
                }

                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
    }

    private void checkTeleportation(Location from, Location to) {
        for (Entity entity : from.getWorld().getNearbyEntities(from, 1.5, 2, 1.5)) {
            if (entity instanceof Player || entity instanceof org.bukkit.entity.LivingEntity) {
                // Teleport
                entity.teleport(to);

                // Effects
                to.getWorld().spawnParticle(Particle.PORTAL, to, 30, 0.5, 1, 0.5, 0.5);
                to.getWorld().playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.5f);

                if (entity instanceof Player) {
                    ((Player) entity)
                            .sendMessage(ChatColor.LIGHT_PURPLE + "✦ Teletransportado por portal dimensional!");
                }
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return true; // Universal dimensional magic
    }
}
