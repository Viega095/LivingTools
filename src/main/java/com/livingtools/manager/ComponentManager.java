package com.livingtools.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ComponentManager {

    public enum ComponentType {
        HEAD, BINDING, HANDLE
    }

    public static ItemStack createComponent(ComponentType type, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Componente de Herramienta Viviente");
            lore.add(ChatColor.DARK_GRAY + "Tipo: " + type.name());
            meta.setLore(lore);
            // Could add PDC here to identify it strictly
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getBlazeHandle() {
        return createComponent(ComponentType.HANDLE, Material.BLAZE_ROD, "Mango de Fuego");
    }

    public static ItemStack getNetheriteHead() {
        return createComponent(ComponentType.HEAD, Material.NETHERITE_INGOT, "Cabeza de Netherite");
    }

    public static ItemStack getLeatherBinding() {
        return createComponent(ComponentType.BINDING, Material.LEATHER, "Unión de Cuero");
    }

    // Helper to check if an item is a specific component
    public static boolean isComponent(ItemStack item, ComponentType type) {
        if (item == null || !item.hasItemMeta())
            return false;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.size() < 2)
            return false;
        return lore.get(1).contains(type.name());
    }
}
