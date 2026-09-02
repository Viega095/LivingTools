package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.LivingArmor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {

    public static void registerRecipes() {
        registerToolRecipe(Material.DIAMOND_PICKAXE, "living_pickaxe");
        registerToolRecipe(Material.DIAMOND_SWORD, "living_sword");
        registerToolRecipe(Material.DIAMOND_AXE, "living_axe");
        registerToolRecipe(Material.DIAMOND_SHOVEL, "living_shovel");
        registerToolRecipe(Material.DIAMOND_HOE, "living_hoe");

        registerBasicRecipes();
        registerArmorRecipes();
        registerIronArmorRecipes();
        registerGoldArmorRecipes();
        registerNetheriteArmorRecipes();
    }

    private static void registerGoldArmorRecipes() {
        registerTier1Recipe(Material.GOLDEN_HELMET, "living_golden_helmet", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_CHESTPLATE, "living_golden_chestplate", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_LEGGINGS, "living_golden_leggings", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_BOOTS, "living_golden_boots", Material.GOLD_BLOCK);
    }

    private static void registerArmorRecipes() {
        registerToolRecipe(Material.DIAMOND_HELMET, "living_helmet");
        registerToolRecipe(Material.DIAMOND_CHESTPLATE, "living_chestplate");
        registerToolRecipe(Material.DIAMOND_LEGGINGS, "living_leggings");
        registerToolRecipe(Material.DIAMOND_BOOTS, "living_boots");
    }

    private static void registerIronArmorRecipes() {
        registerTier1Recipe(Material.IRON_HELMET, "living_iron_helmet", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_CHESTPLATE, "living_iron_chestplate", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_LEGGINGS, "living_iron_leggings", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_BOOTS, "living_iron_boots", Material.IRON_BLOCK);
    }

    private static void registerNetheriteArmorRecipes() {
        registerNetheriteRecipe(Material.NETHERITE_HELMET, "living_netherite_helmet");
        registerNetheriteRecipe(Material.NETHERITE_CHESTPLATE, "living_netherite_chestplate");
        registerNetheriteRecipe(Material.NETHERITE_LEGGINGS, "living_netherite_leggings");
        registerNetheriteRecipe(Material.NETHERITE_BOOTS, "living_netherite_boots");
    }

    private static void registerBasicRecipes() {
        registerBasicToolRecipe(Material.WOODEN_PICKAXE, "living_wooden_pickaxe");
        registerBasicToolRecipe(Material.WOODEN_SWORD, "living_wooden_sword");
        registerBasicToolRecipe(Material.WOODEN_AXE, "living_wooden_axe");
        registerBasicToolRecipe(Material.WOODEN_SHOVEL, "living_wooden_shovel");
        registerBasicToolRecipe(Material.WOODEN_HOE, "living_wooden_hoe");

        registerStoneRecipes();
        registerIronRecipes();
        registerGoldRecipes();
        registerNetheriteRecipes();
        registerSleepingRecipes();
        registerArtifactRecipes();
        registerGeodeRecipe();
    }

    private static void registerGeodeRecipe() {
        ItemStack result = com.livingtools.runes.RuneManager.createGeode();

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), "rune_geode");
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(" A ", "GSG", " A ");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        recipe.setIngredient('S', Material.STONE);

        Bukkit.addRecipe(recipe);
    }

    private static void registerArtifactRecipes() {
        registerArtifactRecipe(com.livingtools.manager.ArtifactManager.ArtifactType.LIVING_CHARM,
                "living_charm");
        registerArtifactRecipe(com.livingtools.manager.ArtifactManager.ArtifactType.SOUL_GEM, "soul_gem");
        registerArtifactRecipe(com.livingtools.manager.ArtifactManager.ArtifactType.RUNE_POUCH,
                "rune_pouch");
    }

    private static void registerArtifactRecipe(com.livingtools.manager.ArtifactManager.ArtifactType type,
            String keyName) {
        ItemStack result = com.livingtools.manager.ArtifactManager.createArtifact(type);

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        if (type == com.livingtools.manager.ArtifactManager.ArtifactType.LIVING_CHARM) {
            recipe.shape(" E ", " G ", " S ");
            recipe.setIngredient('E', Material.EMERALD);
            recipe.setIngredient('G', Material.GOLD_INGOT);
            recipe.setIngredient('S', Material.STRING);
        } else if (type == com.livingtools.manager.ArtifactManager.ArtifactType.SOUL_GEM) {
            recipe.shape(" A ", " S ", " G ");
            recipe.setIngredient('A', Material.AMETHYST_SHARD);
            recipe.setIngredient('S', Material.SOUL_SAND);
            recipe.setIngredient('G', Material.GLASS);
        } else if (type == com.livingtools.manager.ArtifactManager.ArtifactType.RUNE_POUCH) {
            recipe.shape(" L ", " S ", " G ");
            recipe.setIngredient('L', Material.LEATHER);
            recipe.setIngredient('S', Material.STRING);
            recipe.setIngredient('G', Material.GOLD_NUGGET);
        }

        Bukkit.addRecipe(recipe);
    }

    private static void registerSleepingRecipes() {
        registerSleepingRecipe(Material.DIAMOND_PICKAXE, "sleeping_pickaxe");
        registerSleepingRecipe(Material.DIAMOND_SWORD, "sleeping_sword");
        registerSleepingRecipe(Material.DIAMOND_AXE, "sleeping_axe");
        registerSleepingRecipe(Material.DIAMOND_SHOVEL, "sleeping_shovel");
        registerSleepingRecipe(Material.DIAMOND_HOE, "sleeping_hoe");
    }

    private static void registerSleepingRecipe(Material baseType, String keyName) {
        ItemStack result = com.livingtools.manager.SleepingManager.createSleepingTool(baseType);

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(" P ", "STS", " P ");
        recipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        recipe.setIngredient('S', Material.SOUL_SAND);
        recipe.setIngredient('T', baseType);

        Bukkit.addRecipe(recipe);
    }

    private static void registerStoneRecipes() {
        registerTier1Recipe(Material.STONE_PICKAXE, "living_stone_pickaxe", Material.COAL);
        registerTier1Recipe(Material.STONE_SWORD, "living_stone_sword", Material.COAL);
        registerTier1Recipe(Material.STONE_AXE, "living_stone_axe", Material.COAL);
        registerTier1Recipe(Material.STONE_SHOVEL, "living_stone_shovel", Material.COAL);
        registerTier1Recipe(Material.STONE_HOE, "living_stone_hoe", Material.COAL);
    }

    private static void registerIronRecipes() {
        registerTier1Recipe(Material.IRON_PICKAXE, "living_iron_pickaxe", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_SWORD, "living_iron_sword", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_AXE, "living_iron_axe", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_SHOVEL, "living_iron_shovel", Material.IRON_BLOCK);
        registerTier1Recipe(Material.IRON_HOE, "living_iron_hoe", Material.IRON_BLOCK);
    }

    private static void registerGoldRecipes() {
        registerTier1Recipe(Material.GOLDEN_PICKAXE, "living_golden_pickaxe", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_SWORD, "living_golden_sword", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_AXE, "living_golden_axe", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_SHOVEL, "living_golden_shovel", Material.GOLD_BLOCK);
        registerTier1Recipe(Material.GOLDEN_HOE, "living_golden_hoe", Material.GOLD_BLOCK);
    }

    private static void registerNetheriteRecipes() {
        registerNetheriteRecipe(Material.NETHERITE_PICKAXE, "living_netherite_pickaxe");
        registerNetheriteRecipe(Material.NETHERITE_SWORD, "living_netherite_sword");
        registerNetheriteRecipe(Material.NETHERITE_AXE, "living_netherite_axe");
        registerNetheriteRecipe(Material.NETHERITE_SHOVEL, "living_netherite_shovel");
        registerNetheriteRecipe(Material.NETHERITE_HOE, "living_netherite_hoe");
    }

    private static void initializeItem(ItemStack item) {
        Material type = item.getType();
        String name = type.name();
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            LivingArmor armor = new LivingArmor(item);
            armor.initialize();
        } else {
            LivingTool tool = new LivingTool(item);
            tool.getData().initialize();
            tool.updateLore();
        }
    }

    private static void registerTier1Recipe(Material baseType, String keyName, Material ingredient) {
        ItemStack result = new ItemStack(baseType);
        initializeItem(result);

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(" I ", " T ", "   ");
        recipe.setIngredient('I', ingredient);
        recipe.setIngredient('T', baseType);

        Bukkit.addRecipe(recipe);
    }

    private static void registerNetheriteRecipe(Material baseType, String keyName) {
        ItemStack result = new ItemStack(baseType);
        initializeItem(result);

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(" M ", "ITI", " M ");
        recipe.setIngredient('M', Material.MAGMA_CREAM);
        recipe.setIngredient('I', Material.NETHERITE_INGOT);
        recipe.setIngredient('T', baseType);

        Bukkit.addRecipe(recipe);
    }

    private static void registerToolRecipe(Material baseType, String keyName) {
        ItemStack result = new ItemStack(baseType);
        initializeItem(result);

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(" E ", "ATA", " E ");
        recipe.setIngredient('E', Material.EMERALD_BLOCK);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('T', baseType);

        Bukkit.addRecipe(recipe);
    }

    private static void registerBasicToolRecipe(Material baseType, String keyName) {
        ItemStack result = new ItemStack(baseType);
        initializeItem(result);

        NamespacedKey key = new NamespacedKey(LivingToolsPlugin.getInstance(), keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(" S ", " T ", "   ");
        recipe.setIngredient('S', Material.WHEAT_SEEDS);
        recipe.setIngredient('T', baseType);

        Bukkit.addRecipe(recipe);
    }
}
