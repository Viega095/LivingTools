package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.ForgeManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;

public class ForgeListener implements Listener {

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item && event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            Item item = (Item) event.getEntity();
            if (LivingTool.isLivingTool(item.getItemStack())) {
                // Check if in Forge
                if (ForgeManager.isForge(item.getLocation())) {
                    event.setCancelled(true); // Prevent burning

                    // Check for other items nearby
                    Collection<Entity> nearby = item.getWorld().getNearbyEntities(item.getLocation(), 1, 1, 1);
                    for (Entity e : nearby) {
                        if (e instanceof Item && !e.equals(item)) {
                            Item other = (Item) e;
                            if (LivingTool.isLivingTool(other.getItemStack())) {
                                // Found pair!
                                ForgeManager.attemptFusion(item, other);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
