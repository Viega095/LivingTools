package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.EnhancedDashboardGUI;
import com.livingtools.gui.XPUpgradeGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener para las nuevas GUIs mejoradas
 */
public class EnhancedGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();

        // GUI de mejora con XP
        if (title.contains("Mejorar con XP")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null)
                return;

            // Botón de mejorar
            if (event.getCurrentItem().getType() == Material.ANVIL) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (LivingTool.isLivingTool(item)) {
                    LivingTool tool = new LivingTool(item);
                    if (XPUpgradeGUI.processUpgrade(player, tool)) {
                        player.closeInventory();
                        player.sendMessage(ChatColor.GREEN + "✔ ¡Mejora exitosa!");
                    }
                }
            }

            // Botón de cerrar
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                player.closeInventory();
            }
        }

        // Dashboard mejorado
        if (title.contains("Herramienta Viviente") || title.contains("Armadura Viviente")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null)
                return;

            // Botón de mejora con XP
            if (event.getCurrentItem().getType() == Material.ANVIL) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (LivingTool.isLivingTool(item)) {
                    LivingTool tool = new LivingTool(item);
                    player.closeInventory();
                    XPUpgradeGUI.open(player, tool);
                }
            }

            // Botón de cerrar
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                player.closeInventory();
            }
        }
    }
}
