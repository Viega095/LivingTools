package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class InfernoLeapAbility extends ActiveAbility {

    public InfernoLeapAbility() {
        super("infernoleap", "Inferno Leap",
                "Click Derecho: Salto explosivo con aterrizaje de fuego.", 20000); // 20s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        // Launch player upward
        Vector launch = new Vector(0, 1.5, 0); // High jump
        launch.add(player.getLocation().getDirection().multiply(0.5)); // Slight forward momentum
        player.setVelocity(launch);

        // Launch effects
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 50, 0.5, 0.2, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 20, 0.3, 0.1, 0.3, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 0.8f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.RED + "🔥 Inferno Leap 🔥"));

        // Track landing
        new org.bukkit.scheduler.BukkitRunnable() {
            Location lastLocation = player.getLocation();
            int ticks = 0;
            boolean landed = false;

            @Override
            public void run() {
                if (!player.isOnline() || ticks > 100) { // 5 second max
                    this.cancel();
                    return;
                }

                // Trail particles while airborne
                if (!player.isOnGround()) {
                    player.getWorld().spawnParticle(Particle.FLAME,
                            player.getLocation(), 5, 0.2, 0.2, 0.2, 0.01);
                    lastLocation = player.getLocation();
                } else if (!landed) {
                    // Player landed!
                    landed = true;
                    onLanding(player, lastLocation);
                    this.cancel();
                }

                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
    }

    private void onLanding(Player player, Location landingLoc) {
        double radius = 4.0;
        double damage = 6.0;

        // Explosion effects
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, landingLoc, 5, 1, 0.1, 1, 0);
        player.getWorld().spawnParticle(Particle.FLAME, landingLoc, 100, radius, 0.5, radius, 0.1);
        player.getWorld().spawnParticle(Particle.LAVA, landingLoc, 50, radius, 0.5, radius, 0);

        player.playSound(landingLoc, Sound.ENTITY_GENERIC_EXPLODE, 2, 0.8f);
        player.playSound(landingLoc, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.5f);

        // Damage and ignite entities
        int hitCount = 0;
        for (Entity entity : landingLoc.getWorld().getNearbyEntities(landingLoc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.setFireTicks(100); // 5 seconds of fire
                hitCount++;

                // Knockback
                Vector knockback = entity.getLocation().toVector()
                        .subtract(landingLoc.toVector()).normalize().multiply(1.5);
                knockback.setY(0.5);
                entity.setVelocity(knockback);
            }
        }

        // Create temporary fire blocks
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.random() < 0.3) { // 30% chance per block
                    Location fireLoc = landingLoc.clone().add(x, 0, z);
                    if (fireLoc.getBlock().getType() == Material.AIR &&
                            fireLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {

                        fireLoc.getBlock().setType(Material.FIRE);

                        // Remove fire after 3 seconds
                        org.bukkit.Bukkit.getScheduler().runTaskLater(
                                com.livingtools.LivingToolsPlugin.getInstance(),
                                () -> {
                                    if (fireLoc.getBlock().getType() == Material.FIRE) {
                                        fireLoc.getBlock().setType(Material.AIR);
                                    }
                                }, 60L);
                    }
                }
            }
        }

        // Notification
        if (hitCount > 0) {
            player.sendMessage(ChatColor.RED + "🔥 Inferno Leap impactó " + hitCount +
                    " enemigo" + (hitCount > 1 ? "s" : "") + "!");
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        // Works with axes or can be used with boots as armor ability
        return type.name().endsWith("_AXE") || type.name().endsWith("_BOOTS");
    }
}
