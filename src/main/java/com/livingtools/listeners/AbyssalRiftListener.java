package com.livingtools.listeners;

import com.livingtools.gui.AbyssalRiftGUI;
import com.livingtools.manager.AbyssalRiftEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * AbyssalRiftListener — Protege y procesa la interacción en el GUI de la Grieta Abisal.
 */
public class AbyssalRiftListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(AbyssalRiftGUI.TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        // Slot 13: Iniciar Grieta Abisal
        if (slot == 13 && clicked.getType() == Material.ENDER_EYE) {
            player.closeInventory();
            AbyssalRiftEngine.startRift(player);
            return;
        }

        // Slot 22 o Barrier: Cerrar
        if ((slot == 22 || clicked.getType() == Material.BARRIER)) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(AbyssalRiftGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
