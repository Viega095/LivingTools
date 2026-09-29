package com.livingtools.gui;

import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.AbyssalRiftEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * AbyssalRiftGUI — Menú de entrada y estadísticas del Abismo Profundo.
 */
public class AbyssalRiftGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Grieta Abisal (Endless) ✦";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        int highScore = AbyssalRiftEngine.getHighScore(player.getUniqueId());

        // Slot 11: Récord Personal
        ItemStack record = GUIBuilder.createItem(Material.END_CRYSTAL,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "🏆 Tu Récord Personal",
                ChatColor.GRAY + "Mayor oleada alcanzada:",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Oleada " + highScore,
                "",
                ChatColor.YELLOW + "¡Supera la oleada 10 para recompensas míticas!");
        gui.setItem(11, record);

        // Slot 13: Iniciar Grieta
        ItemStack startBtn = GUIBuilder.createGlowingItem(Material.ENDER_EYE,
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "🌌 ENTRAR A LA GRIETA ABISAL 🌌",
                ChatColor.GRAY + "Inicia una serie de oleadas continuas con",
                ChatColor.GRAY + "modificadores de batalla aleatorios.",
                "",
                ChatColor.GREEN + "► Click aquí para descender al abismo");
        gui.setItem(13, startBtn);

        // Slot 15: Modificadores Posibles
        ItemStack mods = GUIBuilder.createItem(Material.ENCHANTED_BOOK,
                ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ Modificadores de Oleada",
                ChatColor.GRAY + "• Baja Gravedad",
                ChatColor.GRAY + "• Tormenta del Vacío",
                ChatColor.GRAY + "• Mobs Vampíricos",
                ChatColor.GRAY + "• Suelo Volátil");
        gui.setItem(15, mods);

        player.openInventory(gui);
    }
}
