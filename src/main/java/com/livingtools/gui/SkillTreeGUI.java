package com.livingtools.gui;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeGUI {

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 54,
                com.livingtools.manager.ConfigManager.getRawMessage("stats_title"));

        // Background - Gray Glass Pane (as in the image)
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, bg);
        }

        // --- Snake Path Implementation ---
        // Based on the image:
        // Row 1 (9-17): [ ][G][ ][G][ ][R][ ][R][ ]
        // Row 2 (18-26): [ ][G][ ][G][ ][R][ ][R][ ]
        // Row 3 (27-35): [ ][G][ ][R][ ][R][ ][R][ ]
        // Row 4 (36-44): [ ][G][ ][R][ ][R][ ][R][ ]
        // Tier 4: 12, 14

        // Let's place them.
        placeNode(gui, 49, Material.BEACON, "awakening", "Despertar", 1, tool);

        placeNode(gui, 40, Material.GOLDEN_PICKAXE, "haste", "Prisa Minera", 5, tool);
        placeNode(gui, 39, Material.FURNACE, "autosmelt", "Auto-Horno", 10, tool);
        placeNode(gui, 41, Material.IRON_INGOT, "magnet", "Imán", 5, tool);

        placeNode(gui, 31, Material.TNT, "veinminer", "Vena Minera", 20, tool);
        placeNode(gui, 30, Material.OAK_LOG, "timber", "Leñador", 25, tool);
        placeNode(gui, 32, Material.EXPERIENCE_BOTTLE, "xp_magnet", "Imán de XP", 25, tool);

        placeNode(gui, 22, Material.DIAMOND_SWORD, "execute", "Ejecutar", 35, tool);
        placeNode(gui, 21, Material.REDSTONE, "vampirism", "Vampirismo", 30, tool);
        placeNode(gui, 23, Material.NETHER_WART, "bleed", "Sangrado", 50, tool);

        placeNode(gui, 13, Material.END_ROD, "lightning", "Golpe de Rayo", 25, tool);
        placeNode(gui, 12, Material.IRON_AXE, "cleave", "Hendidura", 45, tool);
        placeNode(gui, 14, Material.TOTEM_OF_UNDYING, "soulbound", "Ligadura de Alma", 30, tool);

        // --- New Abilities (Filling Gaps) ---
        // Combat
        placeNode(gui, 10, Material.SHEARS, "backstab", "Puñalada", 40, tool);
        placeNode(gui, 11, Material.SHIELD, "parry", "Parada", 35, tool);
        placeNode(gui, 19, Material.NETHERITE_CHESTPLATE, "tank", "Tanque", 60, tool);

        // Elemental / Magic
        placeNode(gui, 20, Material.FIRE_CHARGE, "fire_nova", "Nova de Fuego", 55, tool);
        placeNode(gui, 29, Material.BLUE_ICE, "ice_freeze", "Congelación", 55, tool);
        placeNode(gui, 28, Material.FEATHER, "double_jump", "Doble Salto", 40, tool);

        // Utility
        placeNode(gui, 37, Material.ENDER_PEARL, "blink", "Parpadeo", 50, tool);
        placeNode(gui, 38, Material.GOLDEN_CARROT, "regeneration", "Regeneración", 45, tool);

        placeNode(gui, 4, Material.NETHER_STAR, "prestige", "Prestigio", 200, tool); // Placeholder for prestige logic

        // --- Prestige Branch (Requires Prestige > 0) ---
        if (tool.getData().getPrestige() > 0) {
            placeNode(gui, 5, Material.ENDER_EYE, "telepathy", "Telepatía", 1, tool);
            placeNode(gui, 6, Material.GOLDEN_HELMET, "night_vision", "Visión Nocturna", 10, tool);
            placeNode(gui, 7, Material.MAGMA_CREAM, "fire_resistance", "Piel de Magma", 20, tool);

            // Draw path
            drawHorizontalPath(gui, 4, 7, tool);
        }

        // Fill the gaps with colored glass panes to form the "Snake" body
        // Vertical connections
        drawVerticalPath(gui, 49, 40, tool);
        drawVerticalPath(gui, 40, 31, tool);
        drawVerticalPath(gui, 31, 22, tool);
        drawVerticalPath(gui, 22, 13, tool);
        drawVerticalPath(gui, 13, 4, tool);

        // Horizontal connections
        drawHorizontalPath(gui, 40, 39, tool);
        drawHorizontalPath(gui, 40, 41, tool);
        drawHorizontalPath(gui, 31, 30, tool);
        drawHorizontalPath(gui, 31, 32, tool);
        drawHorizontalPath(gui, 22, 21, tool);
        drawHorizontalPath(gui, 22, 23, tool);
        drawHorizontalPath(gui, 13, 12, tool);
        drawHorizontalPath(gui, 13, 14, tool);

        // Rune Slots (Bottom Corners)
        placeRuneSlots(gui, tool);

        player.openInventory(gui);
    }

    private static void drawVerticalPath(Inventory gui, int start, int end, LivingTool tool) {
        int min = Math.min(start, end);
        int max = Math.max(start, end);
        // Slots between min and max (exclusive)
        for (int i = min + 9; i < max; i += 9) {
            // Determine color: Green if start and end are unlocked?
            // Or if the path is available.
            // Let's simplify: Green if 'start' (lower node) is unlocked.
            // Red otherwise.
            // But wait, start is 49 (Awakening). If unlocked, path to 40 is Green?
            // Yes.

            // Get ability ID of start node?
            // It's hard to get from slot.
            // Let's assume if tool level >= requirement of END node, it's green.

            // We need to know the requirement of the END node.
            // This is getting complex.
            // Let's just use Green/Red Glass Panes based on if the user HAS the ability at
            // 'start'.

            ItemStack pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            // Logic: If tool has ability at 'start', path is green.
            // But we don't know ability at 'start' easily here without a map.
            // Let's just make it visually pleasing:
            // If the slot above/below has an unlocked item, make it green.

            ItemStack startItem = gui.getItem(start);
            boolean unlocked = startItem != null && startItem.getType() != Material.GRAY_DYE &&
                    (startItem.getItemMeta().getDisplayName().contains(ChatColor.GREEN.toString()) ||
                            startItem.getItemMeta().getDisplayName().contains(ChatColor.GOLD.toString()));

            if (unlocked) {
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName(" ");
                pane.setItemMeta(meta);
                gui.setItem(i, pane);
            } else {
                pane.setType(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName(" ");
                pane.setItemMeta(meta);
                gui.setItem(i, pane);
            }
        }
    }

    private static void drawHorizontalPath(Inventory gui, int start, int end, LivingTool tool) {
        int min = Math.min(start, end);
        int max = Math.max(start, end);
        for (int i = min + 1; i < max; i++) {
            ItemStack pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemStack startItem = gui.getItem(start);
            boolean unlocked = startItem != null &&
                    (startItem.getItemMeta().getDisplayName().contains(ChatColor.GREEN.toString()) ||
                            startItem.getItemMeta().getDisplayName().contains(ChatColor.GOLD.toString()));

            if (unlocked) {
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName(" ");
                pane.setItemMeta(meta);
                gui.setItem(i, pane);
            } else {
                pane.setType(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName(" ");
                pane.setItemMeta(meta);
                gui.setItem(i, pane);
            }
            ItemStack prestigeBtn = new ItemStack(Material.NETHER_STAR);
            ItemMeta pMeta = prestigeBtn.getItemMeta();
            pMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "ASCENSIÓN (Prestigio)");
            List<String> pLore = new ArrayList<>();
            pLore.add(ChatColor.GRAY + "Tu herramienta ha alcanzado su límite mortal.");
            pLore.add("");
            pLore.add(ChatColor.YELLOW + "Coste:");
            pLore.add(ChatColor.RED + "- Reinicio a Nivel 1");
            pLore.add(ChatColor.RED + "- Reinicio de XP");
            pLore.add("");
            pLore.add(ChatColor.YELLOW + "Recompensa:");
            pLore.add(ChatColor.GREEN + "+ Estrella en el nombre (✯)");
            pLore.add(ChatColor.GREEN + "+ 20% Multiplicador de XP (Permanente)");
            pLore.add("");
            pLore.add(ChatColor.GOLD + "Haz clic para ASCENDER");
            pMeta.setLore(pLore);

            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_action");
            pMeta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING,
                    "prestige");

            prestigeBtn.setItemMeta(pMeta);
            // Place in center (e.g., slot 40) or a specific slot
            int slot = 40;
            gui.setItem(slot, prestigeBtn);
        }
    }

    private static void placeNode(Inventory gui, int slot, Material icon, String abilityId, String name, int levelReq,
            LivingTool tool) {

        if (abilityId.equals("prestige")) {
            if (tool.getData().getLevel() >= 200) {
                ItemStack prestigeBtn = new ItemStack(Material.NETHER_STAR);
                ItemMeta pMeta = prestigeBtn.getItemMeta();
                pMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "ASCENSIÓN (Prestigio)");
                List<String> pLore = new ArrayList<>();
                pLore.add(ChatColor.GRAY + "Tu herramienta ha alcanzado su límite mortal.");
                pLore.add("");
                pLore.add(ChatColor.YELLOW + "Coste:");
                pLore.add(ChatColor.RED + "- Reinicio a Nivel 1");
                pLore.add(ChatColor.RED + "- Reinicio de XP");
                pLore.add("");
                pLore.add(ChatColor.YELLOW + "Recompensa:");
                pLore.add(ChatColor.GREEN + "+ Estrella en el nombre (✯)");
                pLore.add(ChatColor.GREEN + "+ 20% Multiplicador de XP (Permanente)");
                pLore.add("");
                pLore.add(ChatColor.GOLD + "Haz clic para ASCENDER");
                pMeta.setLore(pLore);

                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                        com.livingtools.LivingToolsPlugin.getInstance(), "gui_action");
                pMeta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING,
                        "prestige");

                prestigeBtn.setItemMeta(pMeta);
                gui.setItem(slot, prestigeBtn);
            }
            return;
        }

        // Filter Incompatible Abilities
        Ability ability = AbilityRegistry.getAbility(abilityId);
        if (ability != null && !ability.isCompatible(tool.getItem().getType())) {
            // Place a placeholder
            ItemStack item = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_GRAY + name + " (Incompatible)");
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            return;
        }

        boolean unlocked = tool.hasAbility(abilityId) || abilityId.equals("awakening");
        boolean canUnlock = tool.getData().getLevel() >= levelReq;

        String levelSuffix = "";
        if (unlocked && !abilityId.equals("awakening")) {
            int level = tool.getData().getAbilityLevel(abilityId);
            levelSuffix = ChatColor.GOLD + " [Nv. " + level + "]";
        }

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(
                (unlocked ? ChatColor.GREEN : (canUnlock ? ChatColor.YELLOW : ChatColor.RED)) + name + levelSuffix);

        List<String> lore = new ArrayList<>();
        if (ability != null) {
            lore.add(ChatColor.GRAY + ability.getDescription());
            if (unlocked) {
                int xp = tool.getData().getAbilityXP(abilityId);
                lore.add("");
                lore.add(ChatColor.AQUA + "XP: " + xp);
            }
        } else if (abilityId.equals("awakening")) {
            lore.add(com.livingtools.manager.ConfigManager.getMessage("awakening_desc"));
        }

        lore.add("");
        lore.add(com.livingtools.manager.ConfigManager.getMessage("level_required").replace("%level%",
                String.valueOf(levelReq)));

        if (unlocked) {
            lore.add(com.livingtools.manager.ConfigManager.getMessage("unlocked"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click para ver detalles y gestionar.");
        } else if (canUnlock) {
            lore.add(com.livingtools.manager.ConfigManager.getMessage("click_to_unlock"));
        } else {
            lore.add(com.livingtools.manager.ConfigManager.getMessage("locked"));
        }

        meta.setLore(lore);

        // Add Ability ID to PDC
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "gui_ability_id");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, abilityId);

        item.setItemMeta(meta);

        gui.setItem(slot, item);
    }

    private static void placeRuneSlots(Inventory gui, LivingTool tool) {
        List<com.livingtools.runes.RuneType> runes = tool.getData().getRunes();
        List<com.livingtools.runes.RuneManager.RuneTier> tiers = tool.getData().getRuneTiers();

        // Place at bottom corners: 45, 46, 52, 53?
        // Let's use 45, 46, 53.

        placeRuneSlot(gui, 45, tool.getData().getLevel() >= 25, runes.size() > 0 ? runes.get(0) : null,
                tiers.size() > 0 ? tiers.get(0) : com.livingtools.runes.RuneManager.RuneTier.COMMON, 0);

        placeRuneSlot(gui, 46, tool.getData().getLevel() >= 50, runes.size() > 1 ? runes.get(1) : null,
                tiers.size() > 1 ? tiers.get(1) : com.livingtools.runes.RuneManager.RuneTier.COMMON, 1);

        placeRuneSlot(gui, 53, tool.getData().getLevel() >= 75, runes.size() > 2 ? runes.get(2) : null,
                tiers.size() > 2 ? tiers.get(2) : com.livingtools.runes.RuneManager.RuneTier.COMMON, 2);
    }

    private static void placeRuneSlot(Inventory gui, int slot, boolean unlocked,
            com.livingtools.runes.RuneType rune,
            com.livingtools.runes.RuneManager.RuneTier tier, int index) {
        ItemStack item;
        if (!unlocked) {
            item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(com.livingtools.manager.ConfigManager.getMessage("rune_slot_locked"));
            meta.setLore(List.of(com.livingtools.manager.ConfigManager.getMessage("unlocks_at_level")));
            item.setItemMeta(meta);
        } else if (rune != null) {
            item = new ItemStack(rune.getMaterial());
            ItemMeta meta = item.getItemMeta();

            String tierSymbol = "★";
            if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                tierSymbol = "★★";
            if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                tierSymbol = "★★★";

            meta.setDisplayName(tier.getColor() + "Runa " + rune.getName() + " " + tierSymbol);
            meta.setLore(List.of(ChatColor.GRAY + rune.getDescription(),
                    ChatColor.DARK_GRAY + "Tier: " + tier.name(),
                    "",
                    com.livingtools.manager.ConfigManager.getMessage("click_to_remove")));

            // Add Rune Index to PDC
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_rune_index");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, index);

            item.setItemMeta(meta);
        } else {
            item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(com.livingtools.manager.ConfigManager.getMessage("rune_slot_empty"));
            meta.setLore(List.of(com.livingtools.manager.ConfigManager.getMessage("drag_rune_here")));

            // Add Rune Index to PDC
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_rune_index");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, index);

            item.setItemMeta(meta);
        }
        gui.setItem(slot, item);
    }

    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
