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

public class ArmorGUI {

    public static void open(Player player, LivingArmor armor) {
        Inventory gui = Bukkit.createInventory(null, 27,
                com.livingtools.manager.ConfigManager.getRawMessage("armor_title"));

        // Background
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, bg);
        }

        // Armor Info
        ItemStack info = createItem(Material.NETHER_STAR, ChatColor.AQUA + "Estadísticas");
        ItemMeta meta = info.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Nivel: " + ChatColor.YELLOW + armor.getLevel());
        lore.add(ChatColor.GRAY + "XP: " + ChatColor.YELLOW + armor.getXP());
        lore.add(ChatColor.GRAY + "Personalidad: " + ChatColor.LIGHT_PURPLE + armor.getPersonality());
        meta.setLore(lore);
        info.setItemMeta(meta);
        gui.setItem(13, info);

        // Set Bonus Indicator
        boolean fullSet = checkFullSet(player);
        ItemStack setBonus = createItem(fullSet ? Material.GOLDEN_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE,
                ChatColor.GOLD + "Set Bonus: Avatar");
        ItemMeta setMeta = setBonus.getItemMeta();
        List<String> setLore = new ArrayList<>();
        setLore.add(ChatColor.GRAY + "Estado: " + (fullSet ? ChatColor.GREEN + "ACTIVO" : ChatColor.RED + "INACTIVO"));
        setLore.add("");
        setLore.add(ChatColor.GRAY + "Efectos:");
        setLore.add(ChatColor.YELLOW + "- Regeneración II");
        setLore.add(ChatColor.YELLOW + "- Resistencia I");
        setLore.add(ChatColor.YELLOW + "- Rayos al golpear (5%)");
        setMeta.setLore(setLore);
        setBonus.setItemMeta(setMeta);
        gui.setItem(15, setBonus);

        // Back to Tool Button (if holding tool)
        ItemStack toolItem = player.getInventory().getItemInMainHand();
        if (com.livingtools.data.LivingTool.isLivingTool(toolItem)) {
            ItemStack toolIcon = new ItemStack(Material.DIAMOND_PICKAXE); // Generic icon
            ItemMeta toolMeta = toolIcon.getItemMeta();
            toolMeta.setDisplayName(ChatColor.GOLD + "Volver a Herramienta");
            toolIcon.setItemMeta(toolMeta);
            gui.setItem(22, toolIcon);
        }

        player.openInventory(gui);
    }

    private static boolean checkFullSet(Player player) {
        int livingPieces = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (LivingArmor.isLivingArmor(item)) {
                livingPieces++;
            }
        }
        return livingPieces == 4;
    }

    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
