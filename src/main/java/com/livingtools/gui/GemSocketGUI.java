package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.GemSocketManager;
import com.livingtools.manager.GemSocketManager.GemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * GemSocketGUI — Interfaz de Engarce de Joyas y Gemas Celestiales.
 */
public class GemSocketGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Ranuras de Gemas Celestiales ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.ORANGE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // Slot 4: Info de la herramienta
        gui.setItem(4, tool.getItem().clone());

        int maxSockets = GemSocketManager.getMaxUnlockedSockets(tool);
        List<GemType> currentGems = GemSocketManager.getSocketedGems(tool);

        int[] socketSlots = {10, 12, 14, 16};
        int[] unlockLevels = {20, 50, 80, 100};

        for (int i = 0; i < 4; i++) {
            int slot = socketSlots[i];
            int reqLvl = unlockLevels[i];

            if (i < currentGems.size()) {
                // Ranura con Gema Engarzada
                GemType gem = currentGems.get(i);
                ItemStack gemItem = GUIBuilder.createGlowingItem(gem.getIcon(),
                        gem.getDisplayName(),
                        ChatColor.GRAY + gem.getDescription(),
                        "",
                        ChatColor.RED + "► Click para desengarzar gema");
                gui.setItem(slot, gemItem);
            } else if (i < maxSockets) {
                // Ranura Desbloqueada y Vacía
                ItemStack emptySocket = GUIBuilder.createItem(Material.HOPPER,
                        ChatColor.GREEN + "Ranura " + (i + 1) + ": " + ChatColor.BOLD + "DISPONIBLE",
                        ChatColor.GRAY + "Esta ranura está lista para recibir una gema.",
                        "",
                        ChatColor.YELLOW + "Ten una gema en tu inventario y haz click aquí");
                gui.setItem(slot, emptySocket);
            } else {
                // Ranura Bloqueada
                ItemStack locked = GUIBuilder.createItem(Material.BARRIER,
                        ChatColor.RED + "Ranura " + (i + 1) + ": " + ChatColor.DARK_RED + "BLOQUEADA",
                        ChatColor.GRAY + "Se desbloquea al alcanzar el " + ChatColor.GOLD + "Nivel " + reqLvl,
                        ChatColor.GRAY + "o mediante el sistema de Prestigio.");
                gui.setItem(slot, locked);
            }
        }

        // Footer
        ItemStack info = GUIBuilder.createItem(Material.BOOK,
                ChatColor.YELLOW + "✦ Joyería Celestial ✦",
                ChatColor.GRAY + "Las gemas otorgan pasivas incomparables a tu arma viviente.");
        gui.setItem(22, info);

        player.openInventory(gui);
    }
}
