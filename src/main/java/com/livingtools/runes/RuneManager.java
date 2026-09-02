package com.livingtools.runes;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RuneManager {

    public enum RuneTier {
        COMMON(ChatColor.WHITE),
        RARE(ChatColor.AQUA),
        MYTHIC(ChatColor.GOLD);

        private final ChatColor color;

        RuneTier(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }
    }

    private static final NamespacedKey KEY_RUNE_TYPE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_rune_type");
    private static final NamespacedKey KEY_RUNE_TIER = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_rune_tier");
    private static final NamespacedKey KEY_IS_GEODE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_is_geode");

    public static ItemStack createRuneItem(RuneType type, RuneTier tier) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(tier.getColor() + "Runa " + type.getName() + " " + getTierSymbol(tier));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + type.getDescription());
        lore.add(ChatColor.DARK_GRAY + "Tier: " + tier.name());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Arrastra sobre una ranura de runa");
        lore.add(ChatColor.YELLOW + "en el Árbol de Talentos para equipar.");
        lore.add(ChatColor.RED + "Solo para crafteo");
        meta.setLore(lore);

        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer().set(KEY_RUNE_TYPE, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(KEY_RUNE_TIER, PersistentDataType.STRING, tier.name());

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGeode() {
        ItemStack item = new ItemStack(org.bukkit.Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Geoda Rúnica");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Una piedra misteriosa que vibra con energía.");
        lore.add(ChatColor.GRAY + "Quizás la Forja Rúnica pueda abrirla...");
        lore.add(ChatColor.RED + "Solo para crafteo");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(KEY_IS_GEODE, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isRune(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_RUNE_TYPE, PersistentDataType.STRING);
    }

    public static boolean isGeode(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_IS_GEODE, PersistentDataType.BYTE);
    }

    public static RuneType getRuneType(ItemStack item) {
        if (!isRune(item))
            return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(KEY_RUNE_TYPE, PersistentDataType.STRING);
        try {
            return RuneType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static RuneTier getRuneTier(ItemStack item) {
        if (!isRune(item))
            return RuneTier.COMMON; // Default
        String tierName = item.getItemMeta().getPersistentDataContainer().get(KEY_RUNE_TIER, PersistentDataType.STRING);
        if (tierName == null)
            return RuneTier.COMMON;
        try {
            return RuneTier.valueOf(tierName);
        } catch (IllegalArgumentException e) {
            return RuneTier.COMMON;
        }
    }

    private static String getTierSymbol(RuneTier tier) {
        switch (tier) {
            case RARE:
                return "★★";
            case MYTHIC:
                return "★★★";
            default:
                return "★";
        }
    }
}
