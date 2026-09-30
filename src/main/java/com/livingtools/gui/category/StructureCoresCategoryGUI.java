package com.livingtools.gui.category;

import com.livingtools.gui.RecipeGUI;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.StructureCoreManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * StructureCoresCategoryGUI — Menú interactivo que lista todos los Núcleos de Estructuras Desplegables,
 * sus dificultades y enlaces a sus recetas de crafteo.
 */
public class StructureCoresCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Núcleos de Estructuras");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);
        GUIBuilder.setBorder54(gui, Material.CYAN_STAINED_GLASS_PANE);

        // Header
        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.BEACON,
                ChatColor.AQUA + "" + ChatColor.BOLD + "✦ NÚCLEOS DE ESTRUCTURAS DESPLEGABLES ✦",
                "",
                ChatColor.GRAY + "¡Olvídate de construir bloque a bloque!",
                ChatColor.GRAY + "Craftea un núcleo y haz " + ChatColor.YELLOW + "Click Derecho" + ChatColor.GRAY + " en el suelo",
                ChatColor.GRAY + "para auto-construir la estructura con animación.",
                "",
                ChatColor.YELLOW + "Selecciona un núcleo para ver su crafteo:"));

        // Fila 2 & 3: Los 8 Núcleos Desplegables
        gui.setItem(10, createEntry(StructureCoreManager.StructureType.ASSEMBLY_TABLE, "AssemblyCore"));
        gui.setItem(12, createEntry(StructureCoreManager.StructureType.SOUL_PEDESTAL, "PedestalCore"));
        gui.setItem(14, createEntry(StructureCoreManager.StructureType.RUNE_FORGE, "RuneForgeCore"));
        gui.setItem(16, createEntry(StructureCoreManager.StructureType.RHYTHMIC_FORGE, "RhythmicForgeCore"));

        gui.setItem(28, createEntry(StructureCoreManager.StructureType.CURSED_FORGE, "CursedForgeCore"));
        gui.setItem(30, createEntry(StructureCoreManager.StructureType.SOUL_FUSION_CRUCIBLE, "FusionCrucibleCore"));
        gui.setItem(32, createEntry(StructureCoreManager.StructureType.BOSS_FORGE, "BossForgeCore"));
        gui.setItem(34, createEntry(StructureCoreManager.StructureType.RITUAL_ALTAR, "RitualAltarCore"));

        gui.setItem(45, GUIBuilder.createBackButton());
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createEntry(StructureCoreManager.StructureType type, String recipeKey) {
        return GUIBuilder.createGlowingItem(
                type.getIcon(),
                type.getColor() + "" + ChatColor.BOLD + "✦ " + type.getDisplayName() + " ✦",
                "",
                ChatColor.GRAY + type.getDescription(),
                "",
                ChatColor.YELLOW + "✦ Dificultad: " + ChatColor.WHITE + type.getDifficulty(),
                ChatColor.GREEN + "✦ Función:" + ChatColor.GRAY + " Auto-construcción con 1 click",
                "",
                ChatColor.AQUA + "► Click para ver receta de crafteo ◄"
        );
    }

    public static void openRecipe(Player player, Material clicked) {
        switch (clicked) {
            case CRAFTING_TABLE:
                RecipeGUI.openRecipeView(player, "AssemblyCore", "CORE");
                break;
            case END_PORTAL_FRAME:
                RecipeGUI.openRecipeView(player, "PedestalCore", "CORE");
                break;
            case AMETHYST_CLUSTER:
                RecipeGUI.openRecipeView(player, "RuneForgeCore", "CORE");
                break;
            case LODESTONE:
                RecipeGUI.openRecipeView(player, "RhythmicForgeCore", "CORE");
                break;
            case CRYING_OBSIDIAN:
                RecipeGUI.openRecipeView(player, "CursedForgeCore", "CORE");
                break;
            case CONDUIT:
                RecipeGUI.openRecipeView(player, "FusionCrucibleCore", "CORE");
                break;
            case NETHERITE_UPGRADE_SMITHING_TEMPLATE:
                RecipeGUI.openRecipeView(player, "BossForgeCore", "CORE");
                break;
            case BEACON:
                RecipeGUI.openRecipeView(player, "RitualAltarCore", "CORE");
                break;
            default:
                break;
        }
    }
}
