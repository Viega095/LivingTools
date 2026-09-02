package com.livingtools.gui;

import com.livingtools.manager.TradeManager;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TradeGUI {

    private final Player p1;
    private final Player p2;
    private final Inventory inv;
    private boolean p1Accepted = false;
    private boolean p2Accepted = false;
    private boolean finished = false;

    public TradeGUI(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.inv = Bukkit.createInventory(null, 54,
                MessageUtils.color("&0Comercio: " + p1.getName() + " <-> " + p2.getName()));
        setupGUI();
    }

    private void setupGUI() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        // Divider (Column 4, index 4, 13, 22, 31, 40, 49)
        for (int i = 4; i < 54; i += 9) {
            inv.setItem(i, glass);
        }

        updateStatus();
    }

    private void updateStatus() {
        ItemStack accept1 = new ItemStack(
                p1Accepted ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta m1 = accept1.getItemMeta();
        m1.setDisplayName(MessageUtils
                .color(p1Accepted ? "&a" + p1.getName() + " (Listo)" : "&c" + p1.getName() + " (Esperando)"));
        accept1.setItemMeta(m1);
        inv.setItem(45, accept1);
        inv.setItem(46, accept1);
        inv.setItem(47, accept1);

        ItemStack accept2 = new ItemStack(
                p2Accepted ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta m2 = accept2.getItemMeta();
        m2.setDisplayName(MessageUtils
                .color(p2Accepted ? "&a" + p2.getName() + " (Listo)" : "&c" + p2.getName() + " (Esperando)"));
        accept2.setItemMeta(m2);
        inv.setItem(51, accept2);
        inv.setItem(52, accept2);
        inv.setItem(53, accept2);

        if (p1Accepted && p2Accepted) {
            finishTrade();
        }
    }

    public void open() {
        p1.openInventory(inv);
        p2.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent event) {
        if (finished)
            return;

        Player clicker = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Status Buttons (Accept)
        if (slot >= 45 && slot <= 47 && clicker.equals(p1)) {
            p1Accepted = !p1Accepted;
            updateStatus();
            event.setCancelled(true);
        } else if (slot >= 51 && slot <= 53 && clicker.equals(p2)) {
            p2Accepted = !p2Accepted;
            updateStatus();
            event.setCancelled(true);
        }

        // Prevent modifying other player's side or divider
        if (slot < 54) {
            if (slot % 9 == 4) { // Divider
                event.setCancelled(true);
            } else if (clicker.equals(p1) && (slot % 9 > 4)) { // P1 clicked P2 side
                event.setCancelled(true);
            } else if (clicker.equals(p2) && (slot % 9 < 4)) { // P2 clicked P1 side
                event.setCancelled(true);
            }
        }

        // Reset acceptance on modification
        if (!event.isCancelled() && slot < 54) {
            p1Accepted = false;
            p2Accepted = false;
            updateStatus();
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        if (finished)
            return;
        cancelTrade();
    }

    private void finishTrade() {
        finished = true;
        p1.closeInventory();
        p2.closeInventory();

        // Give items
        for (int i = 0; i < 54; i++) {
            if (i % 9 < 4) { // P1 items -> Give to P2
                ItemStack item = inv.getItem(i);
                if (item != null)
                    p2.getInventory().addItem(item);
            } else if (i % 9 > 4) { // P2 items -> Give to P1
                ItemStack item = inv.getItem(i);
                if (item != null)
                    p1.getInventory().addItem(item);
            }
        }

        p1.sendMessage(MessageUtils.color("&aIntercambio completado."));
        p2.sendMessage(MessageUtils.color("&aIntercambio completado."));
        TradeManager.endTrade(p1, p2);
    }

    private void cancelTrade() {
        finished = true;
        // Return items
        for (int i = 0; i < 54; i++) {
            if (i % 9 < 4) { // P1 items -> Return to P1
                ItemStack item = inv.getItem(i);
                if (item != null)
                    p1.getInventory().addItem(item);
            } else if (i % 9 > 4) { // P2 items -> Return to P2
                ItemStack item = inv.getItem(i);
                if (item != null)
                    p2.getInventory().addItem(item);
            }
        }

        p1.sendMessage(MessageUtils.color("&cIntercambio cancelado."));
        p2.sendMessage(MessageUtils.color("&cIntercambio cancelado."));
        TradeManager.endTrade(p1, p2);

        if (p1.getOpenInventory().getTopInventory().equals(inv))
            p1.closeInventory();
        if (p2.getOpenInventory().getTopInventory().equals(inv))
            p2.closeInventory();
    }
}
