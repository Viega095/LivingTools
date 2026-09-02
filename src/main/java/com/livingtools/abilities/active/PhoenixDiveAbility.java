package com.livingtools.abilities.active;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class PhoenixDiveAbility extends Ability {

    public PhoenixDiveAbility() {
        super("phoenixdive", "Salto del Fénix", "Click derecho + Shift para lanzarte y aplastar enemigos.", 50);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("RIGHT_CLICK") && event.getPlayer().isSneaking()) {
            Player player = event.getPlayer();

            if (isOnCooldown(player))
                return;

            if (!((org.bukkit.entity.Entity) player).isOnGround()) {
                // Fallback for newer versions or just check block below
                if (player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN).getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "Debes estar en el suelo para saltar.");
                    return;
                }
            }

            int level = getLevel(tool);
            double jumpForce = 1.0 + (level * 0.2); // Lvl 1: 1.2, Lvl 5: 2.0

            // Launch
            player.setVelocity(new Vector(0, jumpForce, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 20);

            // Schedule Slam
            org.bukkit.Bukkit.getScheduler().runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(),
                    () -> {
                        // Check if player is falling or landed (simple approximation: wait 1s or check
                        // velocity)
                        // Better: Task timer checking for ground impact
                        new PhoenixSlamTask(player, level, tool)
                                .runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 5L, 2L);
                    }, 10L);

            addCooldown(player, 20000); // 20s cooldown
        }
    }

    private class PhoenixSlamTask extends org.bukkit.scheduler.BukkitRunnable {
        private final Player player;
        private final int level;
        private final LivingTool tool;
        private int ticks = 0;

        public PhoenixSlamTask(Player player, int level, LivingTool tool) {
            this.player = player;
            this.level = level;
            this.tool = tool;
        }

        @Override
        public void run() {
            ticks++;
            if (ticks > 40) { // Timeout
                this.cancel();
                return;
            }

            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 5);

            if (player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN).getType().isSolid()) {
                // Slam!
                double radius = 3.0 + (level * 0.5);
                double damage = 6.0 + (level * 2.0);

                player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 1);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

                int count = 0;
                for (Entity e : player.getNearbyEntities(radius, 2, radius)) {
                    if (e instanceof LivingEntity && e != player) {
                        ((LivingEntity) e).damage(damage, player);
                        e.setFireTicks(60 + (level * 20));
                        count++;
                    }
                }

                if (count > 0) {
                    player.sendMessage(ChatColor.GOLD + "¡Impacto de Fénix! (" + count + " enemigos)");
                    addXP(player, tool, count * 20);
                }

                this.cancel();
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
