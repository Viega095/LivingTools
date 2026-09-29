package com.livingtools.listeners;

import com.livingtools.gui.RhythmicForgeGUI;
import com.livingtools.manager.RhythmicForgeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * RhythmicForgeListener — Gestiona el golpe rítmico en la forja y la limpieza de sesión.
 */
public class RhythmicForgeListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(RhythmicForgeGUI.TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Slot 22: Botón de Martillear
        if (slot == 22) {
            RhythmicForgeManager.handleStrike(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(RhythmicForgeGUI.TITLE)) return;

        Player player = (Player) event.getPlayer();
        RhythmicForgeManager.cleanup(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(RhythmicForgeGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
