package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.DivineAscensionManager;
import com.livingtools.manager.DivineAscensionManager.AscensionPath;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * AscensionGUI — Interfaz de Consagración de la Ascensión Divina.
 */
public class AscensionGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Consagración Divina de Almas ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.YELLOW_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // Slot 4: Herramienta actual
        gui.setItem(4, tool.getItem().clone());

        boolean eligible = DivineAscensionManager.canAscend(tool);
        AscensionPath current = DivineAscensionManager.getAscensionPath(tool);

        // Slot 11: Soberano Celestial (Luz)
        ItemStack path1 = GUIBuilder.createGlowingItem(AscensionPath.CELESTIAL_SOVEREIGN.getIcon(),
                AscensionPath.CELESTIAL_SOVEREIGN.getDisplayName(),
                ChatColor.GRAY + AscensionPath.CELESTIAL_SOVEREIGN.getDescription(),
                "",
                (current == AscensionPath.CELESTIAL_SOVEREIGN ? ChatColor.GREEN + "✔ SENDA ACTUAL"
                        : eligible ? ChatColor.YELLOW + "► Click para consagrar" : ChatColor.RED + "Requiere Lv. 100 o Prestigio 2"));
        gui.setItem(11, path1);

        // Slot 13: Señor del Abismo (Vacío)
        ItemStack path2 = GUIBuilder.createGlowingItem(AscensionPath.ABYSSAL_LORD.getIcon(),
                AscensionPath.ABYSSAL_LORD.getDisplayName(),
                ChatColor.GRAY + AscensionPath.ABYSSAL_LORD.getDescription(),
                "",
                (current == AscensionPath.ABYSSAL_LORD ? ChatColor.GREEN + "✔ SENDA ACTUAL"
                        : eligible ? ChatColor.YELLOW + "► Click para consagrar" : ChatColor.RED + "Requiere Lv. 100 o Prestigio 2"));
        gui.setItem(13, path2);

        // Slot 15: Titán Primordial (Tierra)
        ItemStack path3 = GUIBuilder.createGlowingItem(AscensionPath.PRIMORDIAL_TITAN.getIcon(),
                AscensionPath.PRIMORDIAL_TITAN.getDisplayName(),
                ChatColor.GRAY + AscensionPath.PRIMORDIAL_TITAN.getDescription(),
                "",
                (current == AscensionPath.PRIMORDIAL_TITAN ? ChatColor.GREEN + "✔ SENDA ACTUAL"
                        : eligible ? ChatColor.YELLOW + "► Click para consagrar" : ChatColor.RED + "Requiere Lv. 100 o Prestigio 2"));
        gui.setItem(15, path3);

        player.openInventory(gui);
    }
}
