package com.livingtools.gui.category;

import com.livingtools.gui.RecipeGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Resuelve materiales de herramientas/armaduras hacia vistas de receta unificadas.
 */
public final class RecipeViewHelper {

    private RecipeViewHelper() {
    }

    public static void openForMaterial(Player player, Material material) {
        String name = material.name();

        if (name.endsWith("_SWORD")) {
            String recipeType = resolveTierRecipeType(name);
            if (recipeType != null) {
                RecipeGUI.openRecipeView(player, recipeType, "SWORD");
            }
            return;
        }

        if (isToolMaterial(name)) {
            String recipeType = resolveTierRecipeType(name);
            String toolType = resolveToolType(name);
            if (recipeType != null && toolType != null) {
                RecipeGUI.openRecipeView(player, recipeType, toolType);
            }
            return;
        }

        if (isArmorMaterial(name)) {
            String recipeType = resolveArmorRecipeType(name);
            if (recipeType != null) {
                RecipeGUI.openRecipeView(player, recipeType, resolveArmorSlot(name));
            }
        }
    }

    private static String resolveTierRecipeType(String materialName) {
        if (materialName.startsWith("WOODEN")) {
            return "Basic";
        }
        if (materialName.startsWith("STONE")) {
            return "Stone";
        }
        if (materialName.startsWith("IRON")) {
            return "Iron";
        }
        if (materialName.startsWith("GOLDEN")) {
            return "Gold";
        }
        if (materialName.startsWith("DIAMOND")) {
            return "Advanced";
        }
        if (materialName.startsWith("NETHERITE")) {
            return "Netherite";
        }
        return null;
    }

    private static String resolveToolType(String materialName) {
        if (materialName.endsWith("_PICKAXE")) {
            return "PICKAXE";
        }
        if (materialName.endsWith("_AXE")) {
            return "AXE";
        }
        if (materialName.endsWith("_SHOVEL")) {
            return "SHOVEL";
        }
        if (materialName.endsWith("_HOE")) {
            return "HOE";
        }
        return null;
    }

    private static String resolveArmorRecipeType(String materialName) {
        if (materialName.contains("IRON")) {
            return "IronArmor";
        }
        if (materialName.contains("GOLDEN")) {
            return "GoldArmor";
        }
        if (materialName.contains("DIAMOND")) {
            return "DiamondArmor";
        }
        if (materialName.contains("NETHERITE")) {
            return "NetheriteArmor";
        }
        if (materialName.contains("LEATHER") || materialName.contains("CHAINMAIL")) {
            return "IronArmor";
        }
        return null;
    }

    private static String resolveArmorSlot(String materialName) {
        if (materialName.endsWith("_HELMET")) {
            return "HELMET";
        }
        if (materialName.endsWith("_CHESTPLATE")) {
            return "CHESTPLATE";
        }
        if (materialName.endsWith("_LEGGINGS")) {
            return "LEGGINGS";
        }
        if (materialName.endsWith("_BOOTS")) {
            return "BOOTS";
        }
        return "CHESTPLATE";
    }

    private static boolean isToolMaterial(String name) {
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE")
                || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }

    private static boolean isArmorMaterial(String name) {
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
