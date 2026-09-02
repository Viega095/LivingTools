package com.livingtools.gui.category;

import com.livingtools.gui.RecipeGUI;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Categoría de artefactos y objetos especiales.
 */
public class ArtifactsCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Categoría: Artefactos");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        GUIBuilder.setBorder54(gui, Material.YELLOW_STAINED_GLASS_PANE);

        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.ENCHANTED_BOOK,
                GUIBuilder.PRIMARY + "✦ Artefactos y Objetos",
                "",
                GUIBuilder.INFO + "Amuletos, gemas, runas y geodas",
                GUIBuilder.INFO + "Se craftean en la Mesa de Ensamblaje"));

        gui.setItem(11, createArtifactEntry(Material.EMERALD, "Amuleto Viviente", "Charm"));
        gui.setItem(13, createArtifactEntry(Material.AMETHYST_SHARD, "Gema de Almas", "SoulGem"));
        gui.setItem(15, createArtifactEntry(Material.BUNDLE, "Bolsa de Runas", "RunePouch"));
        gui.setItem(20, createArtifactEntry(Material.AMETHYST_CLUSTER, "Geoda Rúnica", "Geode"));

        gui.setItem(24, GUIBuilder.createInfoItem(
                "Mesa de Ensamblaje",
                "Estos objetos requieren la estructura",
                "de ensamblaje en el mundo.",
                "Usa /livingtool structure assembly",
                "para ver la guía visual."));

        gui.setItem(45, GUIBuilder.createBackButton());
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createArtifactEntry(Material material, String name, String recipeKey) {
        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + name,
                "",
                GUIBuilder.INFO + "Receta en mesa de ensamblaje",
                "",
                GUIBuilder.SECONDARY + "Click para ver receta");
    }

    public static void openRecipe(Player player, Material clicked) {
        switch (clicked) {
            case EMERALD:
                RecipeGUI.openRecipeView(player, "Charm", "ARTIFACT");
                break;
            case AMETHYST_SHARD:
                RecipeGUI.openRecipeView(player, "SoulGem", "ARTIFACT");
                break;
            case BUNDLE:
                RecipeGUI.openRecipeView(player, "RunePouch", "ARTIFACT");
                break;
            case AMETHYST_CLUSTER:
                RecipeGUI.openRecipeView(player, "Geode", "ARTIFACT");
                break;
            default:
                break;
        }
    }
}
