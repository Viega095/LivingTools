package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VortexAbility extends ActiveAbility {

    public VortexAbility() {
        super("vortex", "Vortex",
                "Click Derecho: Crea un vórtice gravitacional que atrae enemigos.", 30000); // 30s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        // Find target location
        Location target = player.getTargetBlock(null, 15).getLocation().add(0, 1, 0);

        double radius = 5.0;
        int duration = 100; // 5 seconds

        // Visual spawn
        player.getWorld().spawnParticle(Particle.PORTAL, target, 100, 0.5, 0.5, 0.5, 1.0);
        player.playSound(target, Sound.BLOCK_PORTAL_TRIGGER, 1.5f, 0.5f);

        // Action bar notification
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.DARK_PURPLE + "◉ Vórtice Activado ◉"));

        // Vortex effect task
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    // End vortex
                    player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, target, 3);
                    player.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1, 0.8f);
                    this.cancel();
                    return;
                }

                // Spiral particles
                for (int i = 0; i < 360; i += 20) {
                    double angle = Math.toRadians(i + ticks * 20);
                    double x = target.getX() + Math.cos(angle) * radius;
                    double z = target.getZ() + Math.sin(angle) * radius;
                    double y = target.getY() + Math.sin(ticks * 0.1) * 2;

                    Location particleLoc = new Location(target.getWorld(), x, y, z);
                    target.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 3, 0.1, 0.1, 0.1, 0);
                    target.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }

                // Pull entities
                for (Entity entity : target.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        Vector pull = target.toVector().subtract(entity.getLocation().toVector());
                        pull.normalize().multiply(0.3);
                        pull.setY(0.1); // Slight upward lift
                        entity.setVelocity(pull);
                    }
                    // Pull projectiles too
                    else if (entity instanceof Projectile) {
                        Vector pull = target.toVector().subtract(entity.getLocation().toVector());
                        pull.normalize().multiply(0.5);
                        entity.setVelocity(pull);
                    }
                }

                // Sound every 20 ticks
                if (ticks % 20 == 0) {
                    target.getWorld().playSound(target, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.5f);
                }

                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
    }

    @Override
    public boolean isCompatible(Material type) {
        // Works with special items or staffs (if you add them)
        return type.name().contains("STICK") || type.name().endsWith("_HOE");
    }
}
