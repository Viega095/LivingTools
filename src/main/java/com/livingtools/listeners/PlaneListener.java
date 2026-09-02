package com.livingtools.listeners;

import com.livingtools.manager.PlaneManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PlaneListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (PlaneManager.isPlane(event.getPlayer().getWorld())) {
            if (!event.getPlayer().isOp()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "No puedes romper bloques aquí.");
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (PlaneManager.isPlane(event.getEntity().getWorld())) {
            if (event.getEntity().getKiller() != null) {
                // Drop Fire Essence
                ItemStack essence = new ItemStack(Material.BLAZE_POWDER);
                ItemMeta meta = essence.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Esencia de Fuego");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Poder concentrado del Plano de Fuego."));
                essence.setItemMeta(meta);

                event.getDrops().add(essence);
            }
        }
    }
}
