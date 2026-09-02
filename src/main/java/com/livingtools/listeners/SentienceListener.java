package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.SentienceManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class SentienceListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            SentienceManager.onPlayerDeath(player, new LivingTool(item));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(item)) {
                SentienceManager.onKill(player, new LivingTool(item));
            }
        }
    }
}
