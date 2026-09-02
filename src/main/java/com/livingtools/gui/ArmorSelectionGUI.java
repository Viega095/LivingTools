package com.livingtools.gui;

import com.livingtools.data.LivingArmor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArmorSelectionGUI {

    public static final String TITLE = ChatColor.DARK_AQUA + "Selección de Armadura";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Fill background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Check slots
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        if (LivingArmor.isLivingArmor(helmet)) {
            inv.setItem(10, createIcon(helmet, "Casco Viviente"));
        }
        if (LivingArmor.isLivingArmor(chest)) {
            inv.setItem(12, createIcon(chest, "Pechera Viviente"));
        }
        if (LivingArmor.isLivingArmor(legs)) {
            inv.setItem(14, createIcon(legs, "Pantalones Vivientes"));
        }
        if (LivingArmor.isLivingArmor(boots)) {
            inv.setItem(16, createIcon(boots, "Botas Vivientes"));
        }

        // Back to Tool Button (if holding tool)
        ItemStack toolItem = player.getInventory().getItemInMainHand();
        if (com.livingtools.data.LivingTool.isLivingTool(toolItem)) {
            ItemStack toolIcon = new ItemStack(Material.DIAMOND_PICKAXE); // Generic icon
            ItemMeta meta = toolIcon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Volver a Herramienta");
            toolIcon.setItemMeta(meta);
            inv.setItem(22, toolIcon);
        }

        player.openInventory(inv);
    }

    private static ItemStack createIcon(ItemStack original, String title) {
        ItemStack icon = original.clone();
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + title);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click para ver estadísticas");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    public static void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE)
            return;

        if (clicked.getType().name().endsWith("_HELMET") ||
                clicked.getType().name().endsWith("_CHESTPLATE") ||
                clicked.getType().name().endsWith("_LEGGINGS") ||
                clicked.getType().name().endsWith("_BOOTS")) {

            // It's a copy, so we need to find the real one in player inventory to be safe,
            // or just trust the NBT if we passed it correctly.
            // Since we cloned it, it has the data.
            if (LivingArmor.isLivingArmor(clicked)) {
                ArmorGUI.open(player, new LivingArmor(clicked));
            }
        } else if (clicked.getType() == Material.DIAMOND_PICKAXE && event.getSlot() == 22) {
            ItemStack toolItem = player.getInventory().getItemInMainHand();
            if (com.livingtools.data.LivingTool.isLivingTool(toolItem)) {
                DashboardGUI.open(player, new com.livingtools.data.LivingTool(toolItem));
            }
        }
    }
}
