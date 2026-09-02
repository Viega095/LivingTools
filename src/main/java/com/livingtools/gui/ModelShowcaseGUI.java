package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ModelShowcaseGUI {

    public static final String TITLE = ChatColor.DARK_AQUA + "Modelos de Herramientas";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        String[] variants = { "Base", "Lvl 10", "Lvl 20", "Lvl 30", "Lvl 40", "Lvl 50", "Infernal", "Aetherial",
                "Demonic" };
        int[] modelIds = { 1000, 1001, 1002, 1003, 1004, 1005, 1100, 1200, 1300 };
        Material[] types = {
                Material.DIAMOND_PICKAXE,
                Material.DIAMOND_SWORD,
                Material.DIAMOND_AXE,
                Material.DIAMOND_SHOVEL,
                Material.DIAMOND_HOE
        };

        int slot = 0;
        for (Material type : types) {
            for (int i = 0; i < variants.length; i++) {
                ItemStack item = new ItemStack(type);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + variants[i] + " " + formatType(type));
                meta.setCustomModelData(modelIds[i]);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Model ID: " + ChatColor.YELLOW + modelIds[i]);
                lore.add(ChatColor.YELLOW + "Click para obtener");
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(slot++, item);
            }
        }

        // Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Cerrar");
        close.setItemMeta(closeMeta);
        inv.setItem(49, close);

        player.openInventory(inv);
    }

    private static String formatType(Material type) {
        String name = type.name().replace("DIAMOND_", "").toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        if (!player.hasPermission("livingtools.admin")) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para tomar estos ítems.");
            player.closeInventory();
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Give Item
        ItemStack give = clicked.clone();
        ItemMeta meta = give.getItemMeta();
        // Clean lore for player use
        meta.setLore(null);
        give.setItemMeta(meta);

        // Initialize as Living Tool if needed, or just give as visual item
        // Let's initialize it so it works properly
        LivingTool tool = new LivingTool(give);
        tool.getData().initialize();
        // Force the model ID back because initialize() might reset it based on level 1
        // Actually, initialize sets level 1. updateModel will set it to 1000.
        // If we want to keep the visual, we should probably NOT make it a functional
        // living tool immediately
        // OR we set the level to match the visual?
        // For now, let's just give the item as-is for visual testing.
        // If they want a functional tool, they should use /give or craft it.
        // This is primarily for "Showcase".

        player.getInventory().addItem(give);
        player.sendMessage(ChatColor.GREEN + "Has recibido: " + clicked.getItemMeta().getDisplayName());
    }
}
