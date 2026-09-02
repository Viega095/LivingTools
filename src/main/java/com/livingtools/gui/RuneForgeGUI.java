package com.livingtools.gui;

import com.livingtools.runes.RuneManager;
import com.livingtools.runes.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class RuneForgeGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "Forja Rúnica";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // Background
        ItemStack bg = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, bg);
        }

        // Mode 1: Geode Cracking (Left Side)
        ItemStack crackIcon = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta crackMeta = crackIcon.getItemMeta();
        crackMeta.setDisplayName(ChatColor.GOLD + "Romper Geoda");
        crackMeta.setLore(Arrays.asList(ChatColor.GRAY + "Coloca una Geoda en la ranura izquierda",
                ChatColor.GRAY + "y haz click aquí."));
        crackIcon.setItemMeta(crackMeta);
        gui.setItem(10, crackIcon); // Button
        gui.setItem(11, new ItemStack(Material.AIR)); // Input Slot

        // Mode 2: Fusion (Right Side)
        ItemStack fuseIcon = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta fuseMeta = fuseIcon.getItemMeta();
        fuseMeta.setDisplayName(ChatColor.RED + "Fusión Rúnica");
        fuseMeta.setLore(Arrays.asList(ChatColor.GRAY + "3 Runas Iguales + Esencia de Sangre",
                ChatColor.GRAY + "= Runa de Tier Superior"));
        fuseIcon.setItemMeta(fuseMeta);
        gui.setItem(16, fuseIcon); // Button

        // Input Slots for Fusion
        gui.setItem(13, new ItemStack(Material.AIR)); // Rune 1
        gui.setItem(14, new ItemStack(Material.AIR)); // Rune 2
        gui.setItem(15, new ItemStack(Material.AIR)); // Rune 3
        gui.setItem(22, new ItemStack(Material.AIR)); // Catalyst (Blood Essence)

        player.openInventory(gui);
    }

    public static void handleEvent(InventoryClickEvent event) {
        event.setCancelled(true); // Default: Cancel everything

        Player player = (Player) event.getWhoClicked();
        Inventory gui = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getRawSlot();

        // 1. Handle Player Inventory Clicks (Bottom Inventory)
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
            if (event.isShiftClick()) {
                // Handle Shift-Click into GUI
                if (clicked == null || clicked.getType() == Material.AIR)
                    return;

                if (RuneManager.isGeode(clicked)) {
                    // Try Slot 11
                    if (isSlotEmpty(gui, 11)) {
                        gui.setItem(11, clicked.clone());
                        event.setCurrentItem(null); // Remove from player
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
                    }
                } else if (RuneManager.isRune(clicked)) {
                    // Try Slots 13, 14, 15
                    int[] runeSlots = { 13, 14, 15 };
                    for (int s : runeSlots) {
                        if (isSlotEmpty(gui, s)) {
                            gui.setItem(s, clicked.clone());
                            event.setCurrentItem(null);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
                            return;
                        }
                    }
                } else if (isBloodEssence(clicked)) {
                    // Try Slot 22
                    if (isSlotEmpty(gui, 22)) {
                        gui.setItem(22, clicked.clone());
                        event.setCurrentItem(null);
                        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1, 1);
                    }
                }
            } else {
                event.setCancelled(false); // Allow normal inventory management
            }
            return;
        }

        // 2. Handle GUI Clicks (Top Inventory)
        if (slot == 11) { // Geode Slot
            handleSlotInteraction(event, item -> RuneManager.isGeode(item));
        } else if (slot == 13 || slot == 14 || slot == 15) { // Rune Slots
            handleSlotInteraction(event, item -> RuneManager.isRune(item));
        } else if (slot == 22) { // Catalyst Slot
            handleSlotInteraction(event, RuneForgeGUI::isBloodEssence);
        } else if (slot == 10) { // Crack Button
            handleCrackGeode(player, gui);
        } else if (slot == 16) { // Fuse Button
            handleFusion(player, gui);
        }
    }

    private static void handleSlotInteraction(InventoryClickEvent event,
            java.util.function.Predicate<ItemStack> validator) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Allow picking up items
        if (current != null && current.getType() != Material.AIR
                && (cursor == null || cursor.getType() == Material.AIR)) {
            event.setCancelled(false);
            return;
        }

        // Allow placing valid items
        if (cursor != null && cursor.getType() != Material.AIR && validator.test(cursor)) {
            // If slot is empty, allow place
            if (current == null || current.getType() == Material.AIR) {
                event.setCancelled(false);
                return;
            }
            // If slot has item, allow swap if valid (handled by default if not cancelled?
            // No, need to be careful)
            // Actually, standard swap is fine if both are valid, but let's keep it simple:
            // Only allow place if empty, or swap if we validate.
            // For simplicity, let's allow standard interaction if cursor is valid.
            event.setCancelled(false);
        }
    }

    private static void handleCrackGeode(Player player, Inventory gui) {
        ItemStack input = gui.getItem(11);
        if (input != null && RuneManager.isGeode(input)) {
            // Consume Geode
            if (input.getAmount() > 1) {
                input.setAmount(input.getAmount() - 1);
                gui.setItem(11, input);
            } else {
                gui.setItem(11, null);
            }

            // Give random rune
            RuneType randomType = RuneType.values()[new Random().nextInt(RuneType.values().length)];
            ItemStack rune = RuneManager.createRuneItem(randomType, RuneManager.RuneTier.COMMON);

            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(rune);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), rune);
            }

            // Visuals
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1, 1);
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
            player.getWorld().spawnParticle(org.bukkit.Particle.CRIT_MAGIC, player.getLocation().add(0, 1, 0), 20, 0.5,
                    0.5, 0.5, 0.1);
            player.sendMessage(
                    ChatColor.GREEN + "¡Geoda abierta! Has obtenido: " + rune.getItemMeta().getDisplayName());
        } else {
            player.sendMessage(ChatColor.RED + "Necesitas colocar una Geoda Rúnica en la ranura.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
        }
    }

    private static void handleFusion(Player player, Inventory gui) {
        ItemStack r1 = gui.getItem(13);
        ItemStack r2 = gui.getItem(14);
        ItemStack r3 = gui.getItem(15);
        ItemStack catalyst = gui.getItem(22);

        if (isValidFusion(r1, r2, r3, catalyst)) {
            // Consume items
            consumeItem(gui, 13);
            consumeItem(gui, 14);
            consumeItem(gui, 15);
            consumeItem(gui, 22);

            // Create Result
            RuneType type = RuneManager.getRuneType(r1);
            RuneManager.RuneTier currentTier = RuneManager.getRuneTier(r1);
            RuneManager.RuneTier nextTier = getNextTier(currentTier);

            ItemStack result = RuneManager.createRuneItem(type, nextTier);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(result);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), result);
            }

            // Visuals
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            player.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 30,
                    0.5, 0.5, 0.5, 0.05);
            player.sendMessage(
                    ChatColor.GOLD + "¡Fusión Exitosa! Has creado: " + result.getItemMeta().getDisplayName());
        } else {
            player.sendMessage(ChatColor.RED + "Fusión inválida. Necesitas 3 Runas iguales y Esencia de Sangre.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
        }
    }

    private static boolean isSlotEmpty(Inventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        return item == null || item.getType() == Material.AIR;
    }

    private static boolean isBloodEssence(ItemStack item) {
        return item != null && item.getType() == Material.REDSTONE && item.hasItemMeta()
                && item.getItemMeta().getDisplayName().contains("Esencia de Sangre");
    }

    private static void consumeItem(Inventory gui, int slot) {
        ItemStack item = gui.getItem(slot);
        if (item != null) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                gui.setItem(slot, item);
            } else {
                gui.setItem(slot, null);
            }
        }
    }

    private static boolean isValidFusion(ItemStack r1, ItemStack r2, ItemStack r3, ItemStack catalyst) {
        if (!RuneManager.isRune(r1) || !RuneManager.isRune(r2) || !RuneManager.isRune(r3))
            return false;

        // Check types match
        RuneType t1 = RuneManager.getRuneType(r1);
        if (t1 != RuneManager.getRuneType(r2) || t1 != RuneManager.getRuneType(r3))
            return false;

        // Check tiers match
        RuneManager.RuneTier tier1 = RuneManager.getRuneTier(r1);
        if (tier1 != RuneManager.getRuneTier(r2) || tier1 != RuneManager.getRuneTier(r3))
            return false;

        // Check max tier
        if (tier1 == RuneManager.RuneTier.MYTHIC)
            return false;

        // Check Catalyst (Blood Essence)
        if (!isBloodEssence(catalyst))
            return false;

        return true;
    }

    private static RuneManager.RuneTier getNextTier(RuneManager.RuneTier current) {
        if (current == RuneManager.RuneTier.COMMON)
            return RuneManager.RuneTier.RARE;
        if (current == RuneManager.RuneTier.RARE)
            return RuneManager.RuneTier.MYTHIC;
        return RuneManager.RuneTier.MYTHIC;
    }
}
