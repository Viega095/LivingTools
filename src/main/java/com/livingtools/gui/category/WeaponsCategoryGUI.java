package com.livingtools.gui.category;

import com.livingtools.gui.RecipeGUI;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI de categoría para armas
 */
public class WeaponsCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Categoría: Armas");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        GUIBuilder.setBorder54(gui, Material.RED_STAINED_GLASS_PANE);

        ItemStack categoryTitle = GUIBuilder.createGlowingItem(
                Material.DIAMOND_SWORD,
                GUIBuilder.PRIMARY + "⚔ ARMAS VIVIENTES",
                "",
                GUIBuilder.INFO + "Explora las espadas del proyecto",
                GUIBuilder.INFO + "Click en un item para ver su receta");
        gui.setItem(4, categoryTitle);

        gui.setItem(11, createWeaponItem(Material.WOODEN_SWORD, "Espada de Madera Viviente", 1, "Basic"));
        gui.setItem(12, createWeaponItem(Material.STONE_SWORD, "Espada de Piedra Viviente", 5, "Stone"));
        gui.setItem(13, createWeaponItem(Material.IRON_SWORD, "Espada de Hierro Viviente", 10, "Iron"));
        gui.setItem(14, createWeaponItem(Material.GOLDEN_SWORD, "Espada de Oro Viviente", 15, "Gold"));
        gui.setItem(15, createWeaponItem(Material.DIAMOND_SWORD, "Espada de Diamante Viviente", 20, "Advanced"));
        gui.setItem(16, createWeaponItem(Material.NETHERITE_SWORD, "Espada de Netherite Viviente", 30, "Netherite"));

        gui.setItem(45, GUIBuilder.createBackButton());
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createWeaponItem(Material material, String name, int level, String recipeType) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(GUIBuilder.INFO + "Nivel requerido: " + GUIBuilder.PRIMARY + level);
        lore.add(GUIBuilder.INFO + "Ruta de receta: " + GUIBuilder.SECONDARY + recipeType);
        lore.add("");
        lore.add(GUIBuilder.SECONDARY + "Click para ver receta");

        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + name,
                lore.toArray(new String[0]));
    }

    public static String getRecipeType(Material material) {
        switch (material) {
            case WOODEN_SWORD:
                return "Basic";
            case STONE_SWORD:
                return "Stone";
            case IRON_SWORD:
                return "Iron";
            case GOLDEN_SWORD:
                return "Gold";
            case DIAMOND_SWORD:
                return "Advanced";
            case NETHERITE_SWORD:
                return "Netherite";
            default:
                return null;
        }
    }

    public static void openRecipe(Player player, Material material) {
        String recipeType = getRecipeType(material);
        if (recipeType == null) {
            return;
        }
        RecipeGUI.openRecipeView(player, recipeType, "SWORD");
    }
}
