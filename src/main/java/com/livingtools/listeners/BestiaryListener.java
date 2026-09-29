package com.livingtools.listeners;

import com.livingtools.gui.BestiaryGUI;
import com.livingtools.gui.category.MainCategoryGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * BestiaryListener — Maneja la navegación, filtrado por categorías y paginación en el Bestiario.
 */
public class BestiaryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(BestiaryGUI.TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        // Categorías superiores
        if (slot == 1) {
            BestiaryGUI.open(player, 0, BestiaryGUI.BestiaryCategory.ALL);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            return;
        }

        if (slot == 2) {
            BestiaryGUI.open(player, 0, BestiaryGUI.BestiaryCategory.BOSSES);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            return;
        }

        if (slot == 6) {
            BestiaryGUI.open(player, 0, BestiaryGUI.BestiaryCategory.MINIONS);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            return;
        }

        if (slot == 7) {
            BestiaryGUI.open(player, 0, BestiaryGUI.BestiaryCategory.RAID);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            return;
        }

        // Determinar categoría actual desde el slot 4
        BestiaryGUI.BestiaryCategory currentCat = BestiaryGUI.BestiaryCategory.ALL;
        ItemStack header = event.getInventory().getItem(4);
        if (header != null && header.hasItemMeta() && header.getItemMeta().hasLore()) {
            List<String> lore = header.getItemMeta().getLore();
            if (lore != null && lore.size() > 0) {
                String catLine = lore.get(0);
                for (BestiaryGUI.BestiaryCategory cat : BestiaryGUI.BestiaryCategory.values()) {
                    if (catLine.contains(cat.getDisplayName())) {
                        currentCat = cat;
                        break;
                    }
                }
            }
        }

        // Paginación
        if (slot == 45 && clicked.getType() == Material.ARROW) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String name = ChatColor.stripColor(meta.getDisplayName());
                // Formato: "« Página Anterior (1)" -> el número entre paréntesis es la página objetivo (1-indexed)
                int targetPage = extractPageNumber(name) - 1;
                BestiaryGUI.open(player, Math.max(0, targetPage), currentCat);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            }
            return;
        }

        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        if (slot == 53 && clicked.getType() == Material.ARROW) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String name = ChatColor.stripColor(meta.getDisplayName());
                // Formato: "Página Siguiente (2) »" -> el número entre paréntesis es la página objetivo (1-indexed)
                int targetPage = extractPageNumber(name) - 1;
                BestiaryGUI.open(player, Math.max(0, targetPage), currentCat);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            }
            return;
        }
    }

    private int extractPageNumber(String text) {
        try {
            int openParen = text.lastIndexOf('(');
            int closeParen = text.lastIndexOf(')');
            if (openParen != -1 && closeParen != -1 && closeParen > openParen) {
                String num = text.substring(openParen + 1, closeParen).trim();
                return Integer.parseInt(num);
            }
        } catch (Exception ignored) {}
        return 1;
    }
}
