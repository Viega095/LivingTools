package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AbyssalManager {

    public static final NamespacedKey KEY_VOID_ESSENCE = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "void_essence");
    public static final NamespacedKey KEY_IS_ABYSSAL = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "is_abyssal");

    public static ItemStack createVoidEssence() {
        ItemStack item = new ItemStack(Material.SCULK_SHRIEKER); // Placeholder material
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&5&lEsencia del Vacío"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Una sustancia oscura y pulsante.");
        lore.add(ChatColor.GRAY + "Se usa para ascender herramientas al Abismo.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(KEY_VOID_ESSENCE, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isVoidEssence(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_VOID_ESSENCE, PersistentDataType.BYTE);
    }

    public static void upgradeToAbyssal(Player player, LivingTool tool) {
        if (isAbyssal(tool)) {
            player.sendMessage(ChatColor.RED + "Esta herramienta ya pertenece al Abismo.");
            return;
        }

        if (tool.getData().getLevel() < 200) {
            player.sendMessage(ChatColor.RED + "La herramienta debe ser Nivel 200 para soportar el Vacío.");
            return;
        }

        // Apply Upgrade
        ItemMeta meta = tool.getItem().getItemMeta();
        meta.getPersistentDataContainer().set(KEY_IS_ABYSSAL, PersistentDataType.BYTE, (byte) 1);

        // Visuals
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.add(0, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "★ ABISAL ★");
        }
        meta.setLore(lore);
        tool.getItem().setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1, 0.5f);
        player.spawnParticle(org.bukkit.Particle.SCULK_SOUL, player.getLocation(), 50);
        player.sendMessage(MessageUtils.color("&5&l¡TU HERRAMIENTA HA SIDO CORROMPIDA POR EL VACÍO!"));

        // Trigger Corruption System (to be implemented)
        com.livingtools.mechanics.CorruptionSystem.increaseCorruption(player, 10);
    }

    public static boolean isAbyssal(LivingTool tool) {
        return tool.getItem().getItemMeta().getPersistentDataContainer().has(KEY_IS_ABYSSAL, PersistentDataType.BYTE);
    }
}
