package com.livingtools.listeners;

import com.livingtools.entities.GenesisAvatarBoss;
import com.livingtools.gui.BestiaryGUI;
import com.livingtools.gui.MobDetailGUI;
import com.livingtools.gui.RecipeGUI;
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
 * BestiaryListener — Maneja la navegación, filtrado por categorías, paginación y vista detallada en el Bestiario.
 */
public class BestiaryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        String title = event.getView().getTitle();

        // 1. Manejo del Menú Principal del Bestiario
        if (title.equals(BestiaryGUI.TITLE)) {
            handleBestiaryMainMenuClick(event);
            return;
        }

        // 2. Manejo de la Vista Detallada de Mob (MobDetailGUI)
        if (title.startsWith(MobDetailGUI.TITLE_PREFIX) || title.contains("✦ Detalle:")) {
            handleMobDetailClick(event);
            return;
        }
    }

    private void handleBestiaryMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        // Categorías superiores (Slots 1, 2, 6, 7)
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

        // Determinar categoría y página actual desde el slot 4
        BestiaryGUI.BestiaryCategory currentCat = BestiaryGUI.BestiaryCategory.ALL;
        int currentPage = 0;
        ItemStack header = event.getInventory().getItem(4);
        if (header != null && header.hasItemMeta() && header.getItemMeta().hasLore()) {
            List<String> lore = header.getItemMeta().getLore();
            if (lore != null && lore.size() > 1) {
                String catLine = lore.get(0);
                for (BestiaryGUI.BestiaryCategory cat : BestiaryGUI.BestiaryCategory.values()) {
                    if (catLine.contains(cat.getDisplayName())) {
                        currentCat = cat;
                        break;
                    }
                }
                String pageLine = lore.get(1);
                currentPage = extractPageNumber(pageLine) - 1;
            }
        }

        // Click en Criatura (Slots de contenido: 10-16, 19-25, 28-34)
        if (isContentSlot(slot)) {
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String displayName = clicked.getItemMeta().getDisplayName();
                BestiaryGUI.BestiaryEntry entry = BestiaryGUI.getEntryByName(displayName);
                if (entry != null) {
                    MobDetailGUI.open(player, entry, currentCat, currentPage);
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1.2f);
                    return;
                }
            }
        }

        // Paginación (Slot 45 y 53)
        if (slot == 45 && clicked.getType() == Material.ARROW) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String name = ChatColor.stripColor(meta.getDisplayName());
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
                int targetPage = extractPageNumber(name) - 1;
                BestiaryGUI.open(player, Math.max(0, targetPage), currentCat);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            }
            return;
        }
    }

    private void handleMobDetailClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        // Slot 45: Invocar para Pruebas (Admin)
        if (slot == 45 && clicked.getType() == Material.NETHER_STAR && player.hasPermission("livingtools.admin")) {
            String title = event.getView().getTitle();
            player.closeInventory();

            if (title.contains("Smith")) {
                player.performCommand("livingtool admin spawnboss LIVING");
            } else if (title.contains("Serafín")) {
                player.performCommand("livingtool admin spawnboss SERAPHIM");
            } else if (title.contains("Titán")) {
                player.performCommand("livingtool admin spawnboss TITAN");
            } else if (title.contains("Dríada")) {
                player.performCommand("livingtool admin spawnarena DRYAD");
            } else if (title.contains("Wyrm")) {
                player.performCommand("livingtool admin spawnarena WYRM");
            } else if (title.contains("Leviatán")) {
                player.performCommand("livingtool admin spawnarena LEVIATHAN");
            } else if (title.contains("Génesis")) {
                GenesisAvatarBoss.spawn(player.getLocation());
            } else {
                player.sendMessage(ChatColor.GOLD + "✦ Criatura invocada para prueba.");
            }
            return;
        }

        // Slot 49: Volver al Bestiario
        if (slot == 49) {
            BestiaryGUI.open(player, 0, BestiaryGUI.BestiaryCategory.ALL);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        // Slot 53: Ver Crafteos
        if (slot == 53) {
            RecipeGUI.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }
    }

    private boolean isContentSlot(int slot) {
        int[] valid = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };
        for (int v : valid) {
            if (v == slot) return true;
        }
        return false;
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

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        String title = event.getView().getTitle();
        if (title.equals(BestiaryGUI.TITLE) || title.startsWith(MobDetailGUI.TITLE_PREFIX) || title.contains("✦ Detalle:")) {
            event.setCancelled(true);
        }
    }
}
