package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Fábrica de ítems crafteados en la Forja de Jefes (sin recetas vanilla).
 */
public final class BossCraftingManager {

    private BossCraftingManager() {
    }

    public static ItemStack createSocketExpander() {
        ItemStack item = new ItemStack(Material.CONDUIT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&b&lExpansor de Zócalos"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Añade 1 ranura de runa a tu herramienta."));
        lore.add(MessageUtils.color("&7Máximo 5 ranuras."));
        meta.setLore(lore);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_socket_expander"),
                PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createRepairKit() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&c&lKit de Reparación Viva"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Repara instantáneamente el 50% de durabilidad."));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_repair_kit"),
                PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createAngelWings() {
        ItemStack item = new ItemStack(Material.ELYTRA);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&f&lAlas de Ángel"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Indestructibles."));
        lore.add(MessageUtils.color("&7Dejan un rastro de partículas."));
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_angel_wings"),
                PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createSeraphimHalo() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&6&lHalo de Serafín"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Otorga Visión Nocturna y Caída Lenta."));
        meta.setLore(lore);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_seraphim_halo"),
                PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTitanRune() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&5&lRuna del Titán"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Aplícala a una armadura para Fuerza II y Resistencia I."));
        meta.setLore(lore);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_titan_rune"),
                PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
}
