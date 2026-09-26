package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.CosmeticTrailGUI;
import com.livingtools.manager.CosmeticTrailManager;
import com.livingtools.manager.CosmeticTrailManager.TrailStyle;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * CosmeticTrailListener — procesa las selecciones de auras y estelas cosméticas.
 */
public class CosmeticTrailListener implements Listener {

    @EventHandler
    public void onTrailClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(CosmeticTrailGUI.TITLE)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            player.sendMessage(ChatColor.RED + "Debes sostener tu herramienta viviente.");
            player.closeInventory();
            return;
        }

        LivingTool tool = new LivingTool(held);
        int slot = event.getSlot();

        TrailStyle selected = null;
        if (slot == 10) selected = TrailStyle.DEFAULT;
        if (slot == 11) selected = TrailStyle.SOLAR_FLAME;
        if (slot == 12) selected = TrailStyle.AMETHYST_MIST;
        if (slot == 14) selected = TrailStyle.VOID_VORTEX;
        if (slot == 15) selected = TrailStyle.LIGHTNING_AURA;
        if (slot == 16) selected = TrailStyle.CELESTIAL_DUST;

        if (selected != null) {
            if (!selected.isUnlocked(tool)) {
                player.sendMessage(ChatColor.RED + "No cumples con el nivel o prestigio requerido para esta estela.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
                return;
            }

            CosmeticTrailManager.setActiveTrail(tool, selected);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.6f);
            player.sendMessage(ChatColor.GREEN + "✦ ¡Estela cosmética equipada! " + ChatColor.GOLD + selected.getDisplayName());
            CosmeticTrailGUI.open(player, tool);
            return;
        }

        if (slot == 22) {
            player.closeInventory();
        }
    }
}
