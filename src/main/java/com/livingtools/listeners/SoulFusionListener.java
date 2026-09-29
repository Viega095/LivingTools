package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.SoulFusionGUI;
import com.livingtools.manager.SoulFusionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * SoulFusionListener — Gestiona los clics en el GUI de SoulFusion y previene la pérdida de ítems.
 */
public class SoulFusionListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(SoulFusionGUI.TITLE)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory top = event.getView().getTopInventory();
        int slot = event.getRawSlot();

        // Permitir interacción normal en slots 11 y 15 (slots de inserción)
        if (slot == 11 || slot == 15 || slot >= 36) {
            // Permitir mover ítems en el inventario del jugador o slots abiertos
            return;
        }

        event.setCancelled(true);

        // Click en Botón Central de Fusión (Slot 13)
        if (slot == 13) {
            ItemStack primaryItem = top.getItem(11);
            ItemStack sacrificeItem = top.getItem(15);

            if (primaryItem == null || primaryItem.getType() == Material.AIR
                    || sacrificeItem == null || sacrificeItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Debes colocar dos herramientas vivientes en las ranuras 11 y 15.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            if (!LivingTool.isLivingTool(primaryItem) || !LivingTool.isLivingTool(sacrificeItem)) {
                player.sendMessage(ChatColor.RED + "Ambos objetos deben ser Herramientas Vivientes.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            if (primaryItem.equals(sacrificeItem)) {
                player.sendMessage(ChatColor.RED + "No puedes fusionar un arma consigo misma.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            LivingTool primaryTool = new LivingTool(primaryItem);
            LivingTool sacrificeTool = new LivingTool(sacrificeItem);

            // Vaciar slot de sacrificio para que no sea devuelto
            top.setItem(15, new ItemStack(Material.AIR));

            // Ejecutar fusión
            boolean success = SoulFusionManager.executeFusion(player, primaryTool, sacrificeTool);
            if (success) {
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "Error al procesar la fusión de almas.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(SoulFusionGUI.TITLE)) return;

        Player player = (Player) event.getPlayer();
        Inventory top = event.getView().getTopInventory();

        // Devolver ítems restantes en slots 11 y 15 si quedaron
        ItemStack item11 = top.getItem(11);
        ItemStack item15 = top.getItem(15);

        if (item11 != null && item11.getType() != Material.AIR) {
            for (ItemStack left : player.getInventory().addItem(item11).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            top.setItem(11, new ItemStack(Material.AIR));
        }

        if (item15 != null && item15.getType() != Material.AIR) {
            for (ItemStack left : player.getInventory().addItem(item15).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            top.setItem(15, new ItemStack(Material.AIR));
        }
    }
}
