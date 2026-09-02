package com.livingtools.gui;

import com.livingtools.manager.LoreManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LibraryGUI {

    public static final String TITLE = ChatColor.DARK_BLUE + "Biblioteca Ancestral";

    public static void open(Player player, com.livingtools.data.LivingTool tool) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Fill background
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Get unlocked tomes
        List<LoreManager.TomeType> unlocked = LoreManager.getUnlockedTomes(tool);

        // Display Tomes
        int slot = 10;
        for (LoreManager.TomeType type : LoreManager.TomeType.values()) {
            if (slot % 9 == 8)
                slot += 2; // Skip borders
            if (slot >= 44)
                break;

            ItemStack item;
            if (unlocked.contains(type)) {
                item = new ItemStack(Material.WRITTEN_BOOK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + type.getTitle());
                meta.setLore(List.of(
                        ChatColor.GREEN + "DESBLOQUEADO",
                        ChatColor.GRAY + "Click para leer.",
                        ChatColor.DARK_GRAY + "ID: " + type.getId()));
                item.setItemMeta(meta);
            } else {
                item = new ItemStack(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "???");
                meta.setLore(List.of(ChatColor.GRAY + "Bloqueado. Encuentra este tomo en el mundo."));
                item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            slot++;
        }

        player.openInventory(inv);
    }
}
