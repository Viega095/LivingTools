package com.livingtools.listeners;

import com.livingtools.gui.BossForgeGUI;
import com.livingtools.gui.RecipeGUI;
import com.livingtools.gui.category.ArmorCategoryGUI;
import com.livingtools.gui.category.ArtifactsCategoryGUI;
import com.livingtools.gui.category.BossForgeRecipesGUI;
import com.livingtools.gui.category.BossRelicsCategoryGUI;
import com.livingtools.gui.category.MainCategoryGUI;
import com.livingtools.gui.category.RecipeViewHelper;
import com.livingtools.gui.category.ToolsCategoryGUI;
import com.livingtools.gui.category.WeaponsCategoryGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Navegación unificada para las GUIs de categorías y recetas.
 */
public class CategoryGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (isProtectedMenu(title) && isBlockedAction(event.getAction())) {
            event.setCancelled(true);
            return;
        }

        if (title.equals(MainCategoryGUI.TITLE)) {
            handleMainCategoryClick(player, event);
            return;
        }

        if (title.equals(ToolsCategoryGUI.TITLE)) {
            handleToolsCategoryClick(player, event);
            return;
        }

        if (title.equals(ArmorCategoryGUI.TITLE)) {
            handleArmorCategoryClick(player, event);
            return;
        }

        if (title.equals(WeaponsCategoryGUI.TITLE)) {
            handleWeaponsCategoryClick(player, event);
            return;
        }

        if (title.equals(ArtifactsCategoryGUI.TITLE)) {
            handleArtifactsCategoryClick(player, event);
            return;
        }

        if (title.equals(BossForgeRecipesGUI.TITLE)) {
            handleBossForgeRecipesClick(player, event);
            return;
        }

        if (title.equals(BossRelicsCategoryGUI.TITLE)) {
            handleBossRelicsCategoryClick(player, event);
            return;
        }

        if (title.startsWith(RecipeGUI.TITLE_RECIPE)) {
            RecipeGUI.handleRecipeViewClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isProtectedMenu(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }

    private void handleMainCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        switch (clicked.getType()) {
            case DIAMOND_PICKAXE:
                ToolsCategoryGUI.open(player);
                break;
            case DIAMOND_CHESTPLATE:
                ArmorCategoryGUI.open(player);
                break;
            case DIAMOND_SWORD:
                WeaponsCategoryGUI.open(player);
                break;
            case ENCHANTED_BOOK:
                ArtifactsCategoryGUI.open(player);
                break;
            case NETHER_STAR:
                BossForgeRecipesGUI.open(player);
                break;
            case CRAFTING_TABLE:
                player.closeInventory();
                player.performCommand("livingtool structure assembly");
                player.sendMessage(org.bukkit.ChatColor.GREEN + "Mostrando guía de la Mesa de Ensamblaje.");
                return;
            case OAK_SAPLING:
                BossRelicsCategoryGUI.open(player);
                break;
            case BARRIER:
                player.closeInventory();
                return;
            default:
                return;
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private void handleToolsCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.ARROW) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (isTool(clicked.getType())) {
            RecipeViewHelper.openForMaterial(player, clicked.getType());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
        }
    }

    private void handleArmorCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.ARROW) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (isArmor(clicked.getType())) {
            RecipeViewHelper.openForMaterial(player, clicked.getType());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
        }
    }

    private void handleWeaponsCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.ARROW) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (clicked.getType().name().endsWith("_SWORD")) {
            WeaponsCategoryGUI.openRecipe(player, clicked.getType());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
        }
    }

    private void handleArtifactsCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.ARROW) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (clicked.getType() == Material.BOOK) {
            player.closeInventory();
            player.performCommand("livingtool structure assembly");
            return;
        }

        ArtifactsCategoryGUI.openRecipe(player, clicked.getType());
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private void handleBossForgeRecipesClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.ARROW) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (clicked.getType() == Material.SMITHING_TABLE) {
            player.closeInventory();
            BossForgeGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (clicked.getType() == Material.BOOK) {
            player.closeInventory();
            player.performCommand("livingtool structure bossforge");
            return;
        }

        BossForgeRecipesGUI.openRecipe(player, clicked.getType());
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private void handleBossRelicsCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.ARROW) {
            MainCategoryGUI.open(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        BossRelicsCategoryGUI.openRecipe(player, clicked);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private boolean isProtectedMenu(String title) {
        return isCategoryMenu(title) || title.startsWith(RecipeGUI.TITLE_RECIPE);
    }

    private boolean isCategoryMenu(String title) {
        return title.equals(MainCategoryGUI.TITLE)
                || title.equals(ToolsCategoryGUI.TITLE)
                || title.equals(ArmorCategoryGUI.TITLE)
                || title.equals(WeaponsCategoryGUI.TITLE)
                || title.equals(ArtifactsCategoryGUI.TITLE)
                || title.equals(BossForgeRecipesGUI.TITLE)
                || title.equals(BossRelicsCategoryGUI.TITLE);
    }

    private boolean isBlockedAction(InventoryAction action) {
        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.HOTBAR_MOVE_AND_READD
                || action == InventoryAction.COLLECT_TO_CURSOR;
    }

    private boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")
                || (name.endsWith("_AXE") && !name.endsWith("_PICKAXE"));
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
