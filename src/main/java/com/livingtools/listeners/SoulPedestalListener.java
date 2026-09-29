package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.SoulPedestalManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SoulPedestalListener — Maneja la colocación y retiro de herramientas en Pedestales de Almas.
 */
public class SoulPedestalListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPedestalInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location blockLoc = block.getLocation();
        Player player = event.getPlayer();

        // 1. Si ya es un pedestal activo -> intentar retirar la herramienta
        if (SoulPedestalManager.isPedestal(blockLoc)) {
            event.setCancelled(true);
            SoulPedestalManager.retrieveToolFromPedestal(player, blockLoc);
            return;
        }

        // 2. Si el bloque es un pedestal compatible (LODESTONE o END_PORTAL_FRAME) y el jugador se agacha con una LivingTool
        if (block.getType() == Material.LODESTONE || block.getType() == Material.END_PORTAL_FRAME) {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (player.isSneaking() && LivingTool.isLivingTool(held)) {
                event.setCancelled(true);
                SoulPedestalManager.placeToolOnPedestal(player, blockLoc);
            }
        }
    }
}
