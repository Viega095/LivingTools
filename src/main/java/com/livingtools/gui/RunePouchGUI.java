package com.livingtools.gui;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RunePouchGUI {

    private static final NamespacedKey KEY_POUCH_CONTENTS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "pouch_contents");

    public static void open(Player player, ItemStack pouch) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Bolsa de Runas");

        // Load contents from PDC
        ItemMeta meta = pouch.getItemMeta();
        if (meta.getPersistentDataContainer().has(KEY_POUCH_CONTENTS, PersistentDataType.STRING)) {
            String serialized = meta.getPersistentDataContainer().get(KEY_POUCH_CONTENTS, PersistentDataType.STRING);
            ItemStack[] contents = deserialize(serialized);
            gui.setContents(contents);
        }

        player.openInventory(gui);
    }

    public static void save(ItemStack pouch, Inventory inventory) {
        ItemMeta meta = pouch.getItemMeta();
        String serialized = serialize(inventory.getContents());
        meta.getPersistentDataContainer().set(KEY_POUCH_CONTENTS, PersistentDataType.STRING, serialized);

        // Update Lore with count
        List<String> lore = meta.getLore();
        if (lore == null)
            lore = new ArrayList<>();

        // Remove old count line if exists
        lore.removeIf(line -> line.startsWith(ChatColor.GRAY + "Runas:"));

        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                count += item.getAmount();
            }
        }
        lore.add(ChatColor.GRAY + "Runas: " + ChatColor.WHITE + count);
        meta.setLore(lore);

        pouch.setItemMeta(meta);
    }

    private static String serialize(ItemStack[] contents) {
        return com.livingtools.utils.ItemSerializer.toBase64(contents);
    }

    private static ItemStack[] deserialize(String data) {
        try {
            return com.livingtools.utils.ItemSerializer.fromBase64(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack[9];
        }
    }
}
