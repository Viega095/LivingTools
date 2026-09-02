package com.livingtools.gui.category;

import com.livingtools.gui.BossForgeGUI;
import com.livingtools.gui.RecipeGUI;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Recetas craftables en la Forja de Jefes (estructura en cruz).
 */
public class BossForgeRecipesGUI {

    public static final String TITLE = GUIBuilder.createTitle("Categoría: Forja de Jefes");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        GUIBuilder.setBorder54(gui, Material.RED_STAINED_GLASS_PANE);

        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.NETHER_STAR,
                GUIBuilder.PRIMARY + "✦ Forja de Jefes",
                "",
                GUIBuilder.INFO + "Recetas legendarias en estructura de cruz",
                GUIBuilder.INFO + "Requieren drops únicos de bosses clásicos"));

        gui.setItem(10, entry(Material.CONDUIT, "Expansor de Zócalos", "SocketExpander"));
        gui.setItem(12, entry(Material.ANVIL, "Kit de Reparación Viva", "RepairKit"));
        gui.setItem(14, entry(Material.ELYTRA, "Alas de Ángel", "AngelWings"));
        gui.setItem(16, entry(Material.GOLDEN_HELMET, "Halo de Serafín", "SeraphimHalo"));
        gui.setItem(22, entry(Material.PAPER, "Runa del Titán", "TitanRune"));

        gui.setItem(30, GUIBuilder.createGlowingItem(
                Material.SMITHING_TABLE,
                GUIBuilder.SUCCESS + "⚒ Abrir Forja GUI",
                "",
                GUIBuilder.SECONDARY + "Click para abrir la interfaz de forja"));
        gui.setItem(32, GUIBuilder.createGlowingItem(
                Material.BOOK,
                GUIBuilder.ACCENT + "Guía de Estructura",
                "",
                GUIBuilder.INFO + "Ver partículas de la forja en cruz",
                GUIBuilder.SECONDARY + "Click para activar guía"));

        gui.setItem(45, GUIBuilder.createBackButton());
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack entry(Material material, String name, String recipeKey) {
        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + name,
                "",
                GUIBuilder.INFO + "Crafteo en Forja de Jefes",
                "",
                GUIBuilder.SECONDARY + "Click para ver receta");
    }

    public static void openRecipe(Player player, Material clicked) {
        switch (clicked) {
            case CONDUIT:
                RecipeGUI.openRecipeView(player, "SocketExpander", "FORGE");
                break;
            case ANVIL:
                RecipeGUI.openRecipeView(player, "RepairKit", "FORGE");
                break;
            case ELYTRA:
                RecipeGUI.openRecipeView(player, "AngelWings", "FORGE");
                break;
            case GOLDEN_HELMET:
                RecipeGUI.openRecipeView(player, "SeraphimHalo", "FORGE");
                break;
            case PAPER:
                RecipeGUI.openRecipeView(player, "TitanRune", "FORGE");
                break;
            default:
                break;
        }
    }
}
