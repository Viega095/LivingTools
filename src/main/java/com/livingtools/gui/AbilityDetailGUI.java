package com.livingtools.gui;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AbilityDetailGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_PURPLE + "Habilidad: ";

    public static void open(Player player, LivingTool tool, String abilityId, int page) {
        Ability ability = AbilityRegistry.getAbility(abilityId);
        if (ability == null)
            return;

        Inventory gui = Bukkit.createInventory(null, 54, TITLE_PREFIX + ability.getName());

        // Header (0-8)
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, bg);
        }

        // Back Button (0)
        ItemStack back = createItem(Material.ARROW, ChatColor.YELLOW + "Volver");
        gui.setItem(0, back);

        // Ability Icon (4)
        ItemStack icon = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + ability.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + ability.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Nivel Actual: " + ChatColor.WHITE + tool.getData().getAbilityLevel(abilityId));
        lore.add(ChatColor.YELLOW + "XP: " + ChatColor.WHITE + tool.getData().getAbilityXP(abilityId));
        meta.setLore(lore);

        // Store Metadata in Icon
        org.bukkit.NamespacedKey keyId = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "gui_ability_id");
        org.bukkit.NamespacedKey keyPage = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "gui_page");
        meta.getPersistentDataContainer().set(keyId, PersistentDataType.STRING, abilityId);
        meta.getPersistentDataContainer().set(keyPage, PersistentDataType.INTEGER, page);

        icon.setItemMeta(meta);
        gui.setItem(4, icon);

        // Toggle Button (8)
        boolean active = tool.getData().isAbilityActive(abilityId);
        ItemStack toggle = new ItemStack(active ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta tMeta = toggle.getItemMeta();
        tMeta.setDisplayName(active ? ChatColor.GREEN + "ACTIVO" : ChatColor.RED + "INACTIVO");
        tMeta.setLore(List.of(ChatColor.GRAY + "Click para alternar."));
        toggle.setItemMeta(tMeta);
        gui.setItem(8, toggle);

        // Levels (9-44) -> 36 slots per page. Max level 50.
        // Page 0: Levels 1-36
        // Page 1: Levels 37-50
        int startLevel = page * 36 + 1;
        int endLevel = Math.min(startLevel + 35, 50);

        int currentLevel = tool.getData().getAbilityLevel(abilityId);

        for (int i = 0; i < 36; i++) {
            int level = startLevel + i;
            if (level > 50)
                break;

            int slot = 9 + i;
            ItemStack levelItem;

            if (level <= currentLevel) {
                // Unlocked
                levelItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta lMeta = levelItem.getItemMeta();
                lMeta.setDisplayName(ChatColor.GREEN + "Nivel " + level);
                lMeta.setLore(List.of(ChatColor.GRAY + "Desbloqueado"));
                levelItem.setItemMeta(lMeta);
            } else {
                // Locked
                levelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta lMeta = levelItem.getItemMeta();
                lMeta.setDisplayName(ChatColor.RED + "Nivel " + level);
                lMeta.setLore(List.of(ChatColor.GRAY + "Bloqueado"));
                levelItem.setItemMeta(lMeta);
            }
            gui.setItem(slot, levelItem);
        }

        // Footer (45-53)
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, bg);
        }

        // Pagination
        if (page > 0) {
            ItemStack prev = createItem(Material.ARROW, ChatColor.YELLOW + "Página Anterior");
            gui.setItem(45, prev);
        }
        if (endLevel < 50) {
            ItemStack next = createItem(Material.ARROW, ChatColor.YELLOW + "Página Siguiente");
            gui.setItem(53, next);
        }

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        // STRICT SECURITY: Cancel everything immediately
        event.setCancelled(true);

        // Prevent any shift-clicking or dragging
        if (event.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                event.getAction() == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP ||
                event.getAction() == org.bukkit.event.inventory.InventoryAction.HOTBAR_MOVE_AND_READD ||
                event.getAction() == org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // If clicked outside or null, just return
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        // Block interactions with player inventory
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        Inventory gui = event.getInventory();
        ItemStack icon = gui.getItem(4);
        if (icon == null || !icon.hasItemMeta())
            return;

        org.bukkit.NamespacedKey keyId = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "gui_ability_id");
        org.bukkit.NamespacedKey keyPage = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "gui_page");

        if (!icon.getItemMeta().getPersistentDataContainer().has(keyId, PersistentDataType.STRING))
            return;

        String abilityId = icon.getItemMeta().getPersistentDataContainer().get(keyId, PersistentDataType.STRING);
        Integer page = icon.getItemMeta().getPersistentDataContainer().get(keyPage, PersistentDataType.INTEGER);

        if (abilityId == null || page == null)
            return;

        LivingTool tool = new LivingTool(player.getInventory().getItemInMainHand());
        if (!LivingTool.isLivingTool(tool.getItem())) {
            player.closeInventory();
            return;
        }

        if (event.getSlot() == 0) {
            // Back
            com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        } else if (event.getSlot() == 8) {
            // Toggle
            boolean current = tool.getData().isAbilityActive(abilityId);
            tool.getData().setAbilityActive(abilityId, !current);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            open(player, tool, abilityId, page); // Refresh
        } else if (event.getSlot() == 45 && page > 0) {
            // Prev Page
            open(player, tool, abilityId, page - 1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        } else if (event.getSlot() == 53) {
            // Next Page (Check if valid)
            open(player, tool, abilityId, page + 1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
    }

    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
