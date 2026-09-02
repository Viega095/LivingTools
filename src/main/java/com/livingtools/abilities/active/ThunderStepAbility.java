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

public class ThunderStepAbility extends ActiveAbility {

    public ThunderStepAbility() {
        super("thunderstep", "Thunder Step",
                "Click Derecho: Blink con rayos devastadores en el camino.", 18000); // 18s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();
        double maxRange = 10.0;

        // Find destination
        Location target = findValidDestination(start, direction, maxRange);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "¡Destino no válido!");
            return;
        }

        // Create lightning trail
        createLightningTrail(start, target, player);

        // Teleport
        player.teleport(target.setDirection(start.getDirection()));

        // Effects
        player.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1.2f);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target, 50, 0.5, 1, 0.5, 0.15);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.YELLOW + "⚡ Thunder Step ⚡"));
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
        for (int i = 0; i < 4; i++) {
            Block below = loc.clone().subtract(0, i, 0).getBlock();
            if (below.getType().isSolid()) {
                return true;
            }
        }

        return false;
    }

    private void createLightningTrail(Location start, Location end, Player player) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = start.distance(end);

        // Strike lightning at 3 points along path
        int strikes = 3;
        for (int i = 0; i <= strikes; i++) {
            double fraction = (double) i / strikes;
            Location strikePoint = start.clone().add(direction.clone().multiply(fraction));

            // Visual lightning
            strikePoint.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, strikePoint, 30, 0.3, 0.5, 0.3, 0.1);
            strikePoint.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, strikePoint, 20, 0.2, 0.5, 0.2, 0.15);
            strikePoint.getWorld().playSound(strikePoint, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7f, 1.5f);

            // Damage nearby entities
            for (Entity entity : strikePoint.getWorld().getNearbyEntities(strikePoint, 2, 2, 2)) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(3.0, player);
                    entity.getWorld().spawnParticle(Particle.CRIT,
                            entity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
                }
            }
        }

        // Charging particle trail
        for (double i = 0; i < distance; i += 0.3) {
            Location point = start.clone().add(direction.clone().normalize().multiply(i));
            point.getWorld().spawnParticle(Particle.REDSTONE, point, 2, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 255, 100), 1.0f));
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
