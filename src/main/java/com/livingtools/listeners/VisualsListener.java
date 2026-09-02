package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VisualsListener implements Listener {

    @EventHandler
    public void onSwing(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                tool.spawnSwingParticles(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            org.bukkit.entity.Player killer = event.getEntity().getKiller();
            ItemStack item = killer.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(item)) {
                // Soul Echoes
                org.bukkit.Location start = event.getEntity().getLocation().add(0, 0.5, 0);
                // Target is dynamic (killer location)

                new org.bukkit.scheduler.BukkitRunnable() {
                    org.bukkit.Location current = start.clone();
                    int steps = 0;

                    @Override
                    public void run() {
                        if (steps > 20 || killer.getLocation().distance(current) < 1) {
                            this.cancel();
                            return;
                        }

                        org.bukkit.util.Vector dir = killer.getLocation().add(0, 1, 0).toVector()
                                .subtract(current.toVector()).normalize().multiply(0.5);
                        current.add(dir);
                        current.getWorld().spawnParticle(org.bukkit.Particle.SOUL, current, 1, 0, 0, 0, 0);
                        steps++;
                    }
                }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
            }
        }
    }
}
