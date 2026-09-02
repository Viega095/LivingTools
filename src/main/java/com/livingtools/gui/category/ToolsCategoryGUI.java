package com.livingtools.gui.category;

import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI de categoría para herramientas (Picos, Palas, Hachas, Azadas)
 */
public class ToolsCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Categoría: Herramientas");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        // Borde decorativo
        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Título de categoría
        ItemStack categoryTitle = GUIBuilder.createGlowingItem(
                Material.DIAMOND_PICKAXE,
                GUIBuilder.PRIMARY + "⛏ HERRAMIENTAS VIVIENTES",
                "",
                GUIBuilder.INFO + "Explora todas las herramientas disponibles",
                GUIBuilder.INFO + "Click en un item para ver su receta");
        gui.setItem(4, categoryTitle);

        // Picos (fila 2)
        addPickaxes(gui);

        // Palas (fila 3)
        addShovels(gui);

        // Hachas (fila 4)
        addAxes(gui);

        // Azadas (fila 5)
        addHoes(gui);

        // Botón de volver
        gui.setItem(45, GUIBuilder.createBackButton());

        // Botón de cerrar
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static void addPickaxes(Inventory gui) {
        // Pico de madera
        gui.setItem(11, createToolItem(Material.WOODEN_PICKAXE, "Pico de Madera Viviente", 1));

        // Pico de piedra
        gui.setItem(12, createToolItem(Material.STONE_PICKAXE, "Pico de Piedra Viviente", 5));

        // Pico de hierro
        gui.setItem(13, createToolItem(Material.IRON_PICKAXE, "Pico de Hierro Viviente", 10));

        // Pico de oro
        gui.setItem(14, createToolItem(Material.GOLDEN_PICKAXE, "Pico de Oro Viviente", 15));

        // Pico de diamante
        gui.setItem(15, createToolItem(Material.DIAMOND_PICKAXE, "Pico de Diamante Viviente", 20));

        // Pico de netherite
        gui.setItem(16, createToolItem(Material.NETHERITE_PICKAXE, "Pico de Netherite Viviente", 30));
    }

    private static void addShovels(Inventory gui) {
        gui.setItem(20, createToolItem(Material.WOODEN_SHOVEL, "Pala de Madera Viviente", 1));
        gui.setItem(21, createToolItem(Material.STONE_SHOVEL, "Pala de Piedra Viviente", 5));
        gui.setItem(22, createToolItem(Material.IRON_SHOVEL, "Pala de Hierro Viviente", 10));
        gui.setItem(23, createToolItem(Material.GOLDEN_SHOVEL, "Pala de Oro Viviente", 15));
        gui.setItem(24, createToolItem(Material.DIAMOND_SHOVEL, "Pala de Diamante Viviente", 20));
        gui.setItem(25, createToolItem(Material.NETHERITE_SHOVEL, "Pala de Netherite Viviente", 30));
    }

    private static void addAxes(Inventory gui) {
        gui.setItem(29, createToolItem(Material.WOODEN_AXE, "Hacha de Madera Viviente", 1));
        gui.setItem(30, createToolItem(Material.STONE_AXE, "Hacha de Piedra Viviente", 5));
        gui.setItem(31, createToolItem(Material.IRON_AXE, "Hacha de Hierro Viviente", 10));
        gui.setItem(32, createToolItem(Material.GOLDEN_AXE, "Hacha de Oro Viviente", 15));
        gui.setItem(33, createToolItem(Material.DIAMOND_AXE, "Hacha de Diamante Viviente", 20));
        gui.setItem(34, createToolItem(Material.NETHERITE_AXE, "Hacha de Netherite Viviente", 30));
    }

    private static void addHoes(Inventory gui) {
        gui.setItem(38, createToolItem(Material.WOODEN_HOE, "Azada de Madera Viviente", 1));
        gui.setItem(39, createToolItem(Material.STONE_HOE, "Azada de Piedra Viviente", 5));
        gui.setItem(40, createToolItem(Material.IRON_HOE, "Azada de Hierro Viviente", 10));
        gui.setItem(41, createToolItem(Material.GOLDEN_HOE, "Azada de Oro Viviente", 15));
        gui.setItem(42, createToolItem(Material.DIAMOND_HOE, "Azada de Diamante Viviente", 20));
        gui.setItem(43, createToolItem(Material.NETHERITE_HOE, "Azada de Netherite Viviente", 30));
    }

    private static ItemStack createToolItem(Material material, String name, int level) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(GUIBuilder.INFO + "Nivel requerido: " + GUIBuilder.PRIMARY + level);
        lore.add("");
        lore.add(GUIBuilder.SECONDARY + "Click para ver receta");

        ItemStack item = GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + name,
                lore.toArray(new String[0]));

        return item;
    }
}
