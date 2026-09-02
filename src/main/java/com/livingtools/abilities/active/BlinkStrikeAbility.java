package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlinkStrikeAbility extends ActiveAbility {

    public BlinkStrikeAbility() {
        super("blinkstrike", "Blink Strike",
                "Click Derecho: Teletransporte + Explosión AoE.", 15000); // 15s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();
        double maxRange = ConfigManager.getDouble("abilities.blinkstrike.max-range");

        // Find valid destination
        Location target = findValidDestination(start, direction, maxRange);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "¡Destino no válido!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
            return;
        }

        // Charging particles along path
        createChargeTrail(start, target);

        // Teleport
        player.teleport(target.setDirection(player.getLocation().getDirection()));

        // Explosive arrival
        double damage = ConfigManager.getDouble("abilities.blinkstrike.damage");
        double radius = ConfigManager.getDouble("abilities.blinkstrike.radius");
        double knockback = ConfigManager.getDouble("abilities.blinkstrike.knockback-power");

        // Visual explosion
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target, 3, 0.1, 0.1, 0.1, 0);
        player.getWorld().spawnParticle(Particle.FLAME, target, 50, radius, 0.5, radius, 0.1);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, target, 30, radius, 0.5, radius, 0.05);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target, 5, radius, 0.1, radius, 0);

        // Sound effects
        player.playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);
        player.playSound(target, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
        player.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);

        // Apply damage and knockback
        int hitCount = 0;
        for (Entity entity : target.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Apply damage
                livingEntity.damage(damage, player);
                hitCount++;

                // Apply knockback
                Vector knockbackVector = entity.getLocation().toVector()
                        .subtract(target.toVector()).normalize().multiply(knockback);
                knockbackVector.setY(0.5); // Upward component
                entity.setVelocity(knockbackVector);

                // Visual hit effect
                entity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        entity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }

        // Feedback notification
        if (hitCount > 0) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "⚔ Blink Strike: " + hitCount +
                            " enemigo" + (hitCount > 1 ? "s" : "") + " golpeado" +
                            (hitCount > 1 ? "s" : "") + " ⚔"));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.YELLOW + "⚔ Blink Strike - Sin impactos ⚔"));
        }
    }

    private Location findValidDestination(Location start, Vector direction, double maxRange) {
        Location target = start.clone();
        Location lastValid = start.clone();

        for (double i = 0; i < maxRange; i += 0.5) {
            Location next = target.clone().add(direction.clone().multiply(0.5));

            if (next.getBlock().getType().isSolid()) {
                break;
            }

            if (isValidDestination(next)) {
                lastValid = next.clone();
                target = next;
            } else {
                break;
            }
        }

        if (isValidDestination(lastValid) && lastValid.distance(start) > 1.0) {
            return lastValid;
        }

        return null;
    }

    private boolean isValidDestination(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();

        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }

        // Check ground
        boolean hasGround = false;
        for (int i = 0; i < 4; i++) {
            Block below = loc.clone().subtract(0, i, 0).getBlock();
            if (below.getType().isSolid() && below.getType() != Material.LAVA) {
                hasGround = true;
                break;
            }
        }

        return hasGround;
    }

    private void createChargeTrail(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = start.distance(end);

        // Aggressive charging particles
        for (double i = 0; i < distance; i += 0.3) {
            Location point = start.clone().add(direction.clone().normalize().multiply(i));
            point.getWorld().spawnParticle(Particle.REDSTONE, point, 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 50, 50), 1.0f));
            point.getWorld().spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0.01);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        // Works with swords and axes for aggressive playstyle
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
