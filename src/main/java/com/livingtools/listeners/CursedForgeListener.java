package com.livingtools.listeners;

import com.livingtools.manager.CursedForgeManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CursedForgeListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (block.getType() == Material.ANVIL || block.getType() == Material.CHIPPED_ANVIL
                || block.getType() == Material.DAMAGED_ANVIL) {
            if (CursedForgeManager.isCursedForge(block)) {
                event.setCancelled(true);
                CursedForgeManager.openGUI(event.getPlayer());
            } else {
                event.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "La Forja Maldita está incompleta.");
                com.livingtools.visuals.CursedForgeVisualizer.sendMaterialLegend(event.getPlayer());
            }
        }
    }
}
