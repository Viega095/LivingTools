package com.livingtools.gui.category;

import com.livingtools.gui.RecipeGUI;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Recetas y objetos temáticos de los jefes Dryad, Wyrm y Leviatán.
 */
public class BossRelicsCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Categoría: Reliquias de Jefe");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        GUIBuilder.setBorder54(gui, Material.GREEN_STAINED_GLASS_PANE);

        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.NETHER_STAR,
                GUIBuilder.PRIMARY + "✦ Reliquias de Jefe",
                "",
                GUIBuilder.INFO + "Drops únicos y crafteos temáticos",
                GUIBuilder.INFO + "Requieren la Mesa de Ensamblaje"));

        gui.setItem(11, createEntry(Material.OAK_SAPLING, "&2Dríade Corrupta",
                "Corazón del Bosque → Relicario del Bosque"));
        gui.setItem(13, createEntry(Material.SAND, "&6Wyrm de las Arenas",
                "Núcleo de Arena → Sello del Desierto"));
        gui.setItem(15, createEntry(Material.PRISMARINE_CRYSTALS, "&3Leviatán Abisal",
                "Núcleo Abisal → Núcleo Abisal Forjado"));

        gui.setItem(29, createThemedEntry(Material.STICK, "&2Bastón del Bosque", "DryadStaff"));
        gui.setItem(31, createThemedEntry(Material.GOLDEN_SWORD, "&6Colmillo del Desierto", "SandFang"));
        gui.setItem(33, createThemedEntry(Material.TRIDENT, "&3Ancla Abisal", "AbyssAnchor"));

        gui.setItem(38, createThemedEntry(Material.STICK, "&2Bastón Ancestral", "VerdantAscension"));
        gui.setItem(40, createThemedEntry(Material.GOLDEN_SWORD, "&6Colmillo Real", "SandstormCrown"));
        gui.setItem(42, createThemedEntry(Material.TRIDENT, "&3Ancla del Abismo", "TidalDominion"));

        gui.setItem(45, GUIBuilder.createBackButton());
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createEntry(Material material, String bossName, String hint) {
        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + bossName.replace("&", "§"),
                "",
                GUIBuilder.INFO + hint,
                "",
                GUIBuilder.SECONDARY + "Click para ver receta de reliquia");
    }

    private static ItemStack createThemedEntry(Material material, String name, String recipeKey) {
        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.ACCENT + name.replace("&", "§"),
                "",
                GUIBuilder.INFO + "Usa el drop del jefe correspondiente",
                "",
                GUIBuilder.SECONDARY + "Click para ver receta");
    }

    public static void openRecipe(Player player, ItemStack clicked) {
        if (clicked == null) {
            return;
        }
        Material type = clicked.getType();
        String name = clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(clicked.getItemMeta().getDisplayName())
                : "";

        switch (type) {
            case OAK_SAPLING:
                RecipeGUI.openRecipeView(player, "DryadRelic", "BOSS");
                break;
            case SAND:
                RecipeGUI.openRecipeView(player, "WyrmRelic", "BOSS");
                break;
            case PRISMARINE_CRYSTALS:
                RecipeGUI.openRecipeView(player, "LeviathanRelic", "BOSS");
                break;
            case TRIDENT:
                if (name.contains("Abismo")) {
                    RecipeGUI.openRecipeView(player, "TidalDominion", "BOSS");
                } else {
                    RecipeGUI.openRecipeView(player, "AbyssAnchor", "BOSS");
                }
                break;
            case STICK:
                if (name.contains("Ancestral")) {
                    RecipeGUI.openRecipeView(player, "VerdantAscension", "BOSS");
                } else {
                    RecipeGUI.openRecipeView(player, "DryadStaff", "BOSS");
                }
                break;
            case GOLDEN_SWORD:
                if (name.contains("Real")) {
                    RecipeGUI.openRecipeView(player, "SandstormCrown", "BOSS");
                } else {
                    RecipeGUI.openRecipeView(player, "SandFang", "BOSS");
                }
                break;
            default:
                break;
        }
    }
}
