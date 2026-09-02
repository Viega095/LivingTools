package com.livingtools.gui.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilidad para crear GUIs más lindas y consistentes
 */
public class GUIBuilder {

    // Colores del tema
    public static final ChatColor PRIMARY = ChatColor.GOLD;
    public static final ChatColor SECONDARY = ChatColor.YELLOW;
    public static final ChatColor ACCENT = ChatColor.AQUA;
    public static final ChatColor SUCCESS = ChatColor.GREEN;
    public static final ChatColor ERROR = ChatColor.RED;
    public static final ChatColor INFO = ChatColor.GRAY;

    /**
     * Crea un item decorativo con brillo
     */
    public static ItemStack createGlowingItem(Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Crea un item básico con nombre y lore
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ChatColor.GRAY + line);
            }
            meta.setLore(loreList);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Crea un item con lore personalizado (lista)
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Crea un panel de vidrio decorativo
     */
    public static ItemStack createGlassPane(Material glassType) {
        ItemStack item = new ItemStack(glassType);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Crea un borde animado para GUIs de 27 slots
     */
    public static void setBorder27(org.bukkit.inventory.Inventory inv, Material borderMaterial) {
        ItemStack border = createGlassPane(borderMaterial);
        int[] borderSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 };
        for (int slot : borderSlots) {
            inv.setItem(slot, border);
        }
    }

    /**
     * Crea un borde para GUIs de 54 slots
     */
    public static void setBorder54(org.bukkit.inventory.Inventory inv, Material borderMaterial) {
        ItemStack border = createGlassPane(borderMaterial);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }
    }

    /**
     * Crea un botón de "Volver"
     */
    public static ItemStack createBackButton() {
        return createGlowingItem(
                Material.ARROW,
                PRIMARY + "« Volver",
                "Click para regresar");
    }

    /**
     * Crea un botón de "Cerrar"
     */
    public static ItemStack createCloseButton() {
        return createItem(
                Material.BARRIER,
                ERROR + "✖ Cerrar",
                "Click para cerrar");
    }

    /**
     * Crea un botón de "Confirmar"
     */
    public static ItemStack createConfirmButton() {
        return createGlowingItem(
                Material.LIME_DYE,
                SUCCESS + "✔ Confirmar",
                "Click para confirmar");
    }

    /**
     * Crea un botón de "Cancelar"
     */
    public static ItemStack createCancelButton() {
        return createItem(
                Material.RED_DYE,
                ERROR + "✖ Cancelar",
                "Click para cancelar");
    }

    /**
     * Crea una barra de progreso visual
     */
    public static List<String> createProgressBar(int current, int max, int barLength) {
        List<String> lore = new ArrayList<>();

        double percentage = (double) current / max;
        int filled = (int) (percentage * barLength);

        StringBuilder bar = new StringBuilder();
        bar.append(SUCCESS).append("[");

        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        bar.append(SUCCESS).append("]");

        lore.add(bar.toString());
        lore.add(SECONDARY + "" + current + INFO + "/" + SECONDARY + max + INFO + " (" +
                String.format("%.1f", percentage * 100) + "%)");

        return lore;
    }

    /**
     * Formatea un número grande con separadores
     */
    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }

    /**
     * Crea un separador visual
     */
    public static String createSeparator() {
        return ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "                    ";
    }

    /**
     * Añade un efecto de brillo a un item existente
     */
    public static void addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    /**
     * Crea un item de información
     */
    public static ItemStack createInfoItem(String title, String... info) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (String line : info) {
            lore.add(INFO + line);
        }
        return createItem(Material.BOOK, ACCENT + "ℹ " + title, lore);
    }

    /**
     * Crea un título con formato especial
     */
    public static String createTitle(String text) {
        return PRIMARY + "✦ " + ChatColor.BOLD + text + PRIMARY + " ✦";
    }
}
