package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.RhythmicForgeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * RhythmicForgeGUI — Interfaz interactiva de la forja rítmica.
 */
public class RhythmicForgeGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Forja Rítmica de Almas ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // Slot 4: Herramienta
        gui.setItem(4, tool.getItem().clone());

        // Slot 22: Botón de Martillear
        ItemStack strikeBtn = GUIBuilder.createGlowingItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                ChatColor.RED + "" + ChatColor.BOLD + "🔨 ¡MARTILLEAR AHORA! 🔨",
                ChatColor.GRAY + "Haz click cuando el martillo pase por la",
                ChatColor.GREEN + "zona verde central " + ChatColor.GRAY + "para obtener calidad " + ChatColor.GOLD + "Obra Maestra.");
        gui.setItem(22, strikeBtn);

        player.openInventory(gui);
        RhythmicForgeManager.startGame(player, tool, gui);
    }
}
