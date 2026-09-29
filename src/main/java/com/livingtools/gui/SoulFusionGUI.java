package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.SoulFusionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * SoulFusionGUI — Interfaz gráfica del Crisol de Fusión de Almas.
 */
public class SoulFusionGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Crisol de Fusión de Almas ✦";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, TITLE);

        // Borde exterior
        ItemStack border = GUIBuilder.createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 36; i++) {
            gui.setItem(i, border);
        }

        // Slots interactivos abiertos (aire)
        gui.setItem(11, new ItemStack(Material.AIR)); // Slot Primario
        gui.setItem(15, new ItemStack(Material.AIR)); // Slot Sacrificio

        // Indicador del Slot Primario (Slot 2)
        ItemStack primaryHeader = GUIBuilder.createItem(Material.ITEM_FRAME,
                ChatColor.AQUA + "▼ Herramienta Principal ▼",
                ChatColor.GRAY + "Coloca aquí el arma que recibirá la fusión.");
        gui.setItem(2, primaryHeader);

        // Indicador del Slot Sacrificio (Slot 6)
        ItemStack sacrificeHeader = GUIBuilder.createItem(Material.WITHER_SKELETON_SKULL,
                ChatColor.RED + "▼ Alma a Sacrificar ▼",
                ChatColor.GRAY + "Coloca aquí el arma secundaria.",
                ChatColor.DARK_RED + "⚠ ESTA HERRAMIENTA SERÁ CONSUMIDA.");
        gui.setItem(6, sacrificeHeader);

        // Botón Central de Fusión (Slot 13)
        ItemStack fuseBtn = GUIBuilder.createGlowingItem(Material.NETHER_STAR,
                ChatColor.GOLD + "" + ChatColor.BOLD + "⚡ INICIAR FUSIÓN DE ALMAS ⚡",
                ChatColor.GRAY + "Coloca ambas herramientas vivientes en sus",
                ChatColor.GRAY + "respectivas ranuras para calcular el resultado.",
                "",
                ChatColor.YELLOW + "► Click aquí para fusionar");
        gui.setItem(13, fuseBtn);

        // Footer Informativo (Slot 31)
        ItemStack info = GUIBuilder.createItem(Material.BOOK,
                ChatColor.LIGHT_PURPLE + "✦ Información de la Fusión ✦",
                ChatColor.GRAY + "• La herramienta principal adquiere una Personalidad Híbrida.",
                ChatColor.GRAY + "• Se hereda el 50% de la XP de la secundaria.",
                ChatColor.GRAY + "• Se desatan sinergias de combate duales.");
        gui.setItem(31, info);

        player.openInventory(gui);
    }
}
