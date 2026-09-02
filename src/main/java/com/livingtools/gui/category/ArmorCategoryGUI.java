package com.livingtools.gui.category;

import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI de categoría para armaduras
 */
public class ArmorCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Categoría: Armaduras");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        // Borde decorativo
        GUIBuilder.setBorder54(gui, Material.CYAN_STAINED_GLASS_PANE);

        // Título
        ItemStack categoryTitle = GUIBuilder.createGlowingItem(
                Material.DIAMOND_CHESTPLATE,
                GUIBuilder.PRIMARY + "🛡 ARMADURAS VIVIENTES",
                "",
                GUIBuilder.INFO + "Explora todas las armaduras disponibles",
                GUIBuilder.INFO + "Click en un item para ver su receta");
        gui.setItem(4, categoryTitle);

        // Cascos (fila 2) — cuero/malla pendientes de receta
        gui.setItem(13, createArmorItem(Material.IRON_HELMET, "Casco de Hierro Viviente", 10));
        gui.setItem(14, createArmorItem(Material.GOLDEN_HELMET, "Casco de Oro Viviente", 15));
        gui.setItem(15, createArmorItem(Material.DIAMOND_HELMET, "Casco de Diamante Viviente", 20));
        gui.setItem(16, createArmorItem(Material.NETHERITE_HELMET, "Casco de Netherite Viviente", 30));

        // Pecheras (fila 3)
        gui.setItem(22, createArmorItem(Material.IRON_CHESTPLATE, "Pechera de Hierro Viviente", 10));
        gui.setItem(23, createArmorItem(Material.GOLDEN_CHESTPLATE, "Pechera de Oro Viviente", 15));
        gui.setItem(24, createArmorItem(Material.DIAMOND_CHESTPLATE, "Pechera de Diamante Viviente", 20));
        gui.setItem(25, createArmorItem(Material.NETHERITE_CHESTPLATE, "Pechera de Netherite Viviente", 30));

        // Pantalones (fila 4)
        gui.setItem(31, createArmorItem(Material.IRON_LEGGINGS, "Pantalones de Hierro Vivientes", 10));
        gui.setItem(32, createArmorItem(Material.GOLDEN_LEGGINGS, "Pantalones de Oro Vivientes", 15));
        gui.setItem(33, createArmorItem(Material.DIAMOND_LEGGINGS, "Pantalones de Diamante Vivientes", 20));
        gui.setItem(34, createArmorItem(Material.NETHERITE_LEGGINGS, "Pantalones de Netherite Vivientes", 30));

        // Botas (fila 5)
        gui.setItem(40, createArmorItem(Material.IRON_BOOTS, "Botas de Hierro Vivientes", 10));
        gui.setItem(41, createArmorItem(Material.GOLDEN_BOOTS, "Botas de Oro Vivientes", 15));
        gui.setItem(42, createArmorItem(Material.DIAMOND_BOOTS, "Botas de Diamante Vivientes", 20));
        gui.setItem(43, createArmorItem(Material.NETHERITE_BOOTS, "Botas de Netherite Vivientes", 30));

        // Botones
        gui.setItem(45, GUIBuilder.createBackButton());
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createArmorItem(Material material, String name, int level) {
        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + name,
                "",
                GUIBuilder.INFO + "Nivel requerido: " + GUIBuilder.PRIMARY + level,
                "",
                GUIBuilder.SECONDARY + "Click para ver receta");
    }
}
