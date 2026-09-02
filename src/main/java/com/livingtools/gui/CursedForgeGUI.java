package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.visuals.RitualAnimation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CursedForgeGUI {

    public static final String TITLE = ChatColor.DARK_RED + "Forja Maldita";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // Fill with background
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, bg);
        }

        // Slot 11: Input (Barrier placeholder)
        ItemStack placeholder = new ItemStack(Material.BARRIER);
        ItemMeta phMeta = placeholder.getItemMeta();
        phMeta.setDisplayName(ChatColor.RED + "Coloca tu Herramienta Viviente");
        placeholder.setItemMeta(phMeta);
        gui.setItem(11, placeholder);

        // Slot 15: Gamble Button
        ItemStack button = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta bMeta = button.getItemMeta();
        bMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "RITUAL DE LA AVARICIA");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "¿Te atreves a desafiar al destino?");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Coste:");
        lore.add(ChatColor.RED + "- 50 Niveles de XP (Herramienta)");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Resultados Posibles:");
        lore.add(ChatColor.GREEN + "50% - PODER ABSOLUTO (Doble Stats x1h)");
        lore.add(ChatColor.DARK_RED + "50% - MALDICIÓN (-10 Niveles)");
        bMeta.setLore(lore);
        button.setItemMeta(bMeta);
        gui.setItem(15, button);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        event.setCancelled(true); // Default cancel
        Player player = (Player) event.getWhoClicked();
        Inventory gui = event.getInventory();
        int slot = event.getRawSlot();

        // Handle Shift-Click from Player Inventory
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
            if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                if (LivingTool.isLivingTool(clicked)) {
                    ItemStack input = gui.getItem(11);
                    if (input == null || input.getType() == Material.BARRIER) {
                        gui.setItem(11, clicked);
                        event.setCurrentItem(null);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                    }
                }
            } else {
                event.setCancelled(false); // Allow normal inventory management
            }
            return;
        }

        if (slot == 11) {
            // Handle Input Slot
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();

            if (current != null && current.getType() == Material.BARRIER) {
                // Placing into placeholder
                if (LivingTool.isLivingTool(cursor)) {
                    gui.setItem(11, cursor);
                    player.setItemOnCursor(null);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                }
            } else {
                // Normal slot behavior (Taking out or Swapping)
                if (cursor == null || cursor.getType() == Material.AIR) {
                    // Taking out
                    event.setCancelled(false);
                    // Restore placeholder logic
                    org.bukkit.Bukkit.getScheduler()
                            .runTask(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
                                if (gui.getItem(11) == null || gui.getItem(11).getType() == Material.AIR) {
                                    ItemStack placeholder = new ItemStack(Material.BARRIER);
                                    ItemMeta phMeta = placeholder.getItemMeta();
                                    phMeta.setDisplayName(ChatColor.RED + "Coloca tu Herramienta Viviente");
                                    placeholder.setItemMeta(phMeta);
                                    gui.setItem(11, placeholder);
                                }
                            });
                } else if (LivingTool.isLivingTool(cursor)) {
                    // Swapping
                    event.setCancelled(false);
                }
            }
        } else if (slot == 15) {
            // Handle Gamble Button
            ItemStack input = gui.getItem(11);
            if (input == null || !LivingTool.isLivingTool(input)) {
                player.sendMessage(ChatColor.RED + "Debes colocar una Herramienta Viviente primero.");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                return;
            }

            LivingTool tool = new LivingTool(input);

            // Check Requirement
            if (tool.getData().getLevel() < 50) {
                player.sendMessage(ChatColor.RED + "Tu herramienta debe ser al menos Nivel 50 para este ritual.");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                return;
            }

            // Perform Ritual
            gui.setItem(11, null); // Remove from GUI to prevent drop on close
            player.closeInventory();

            // Determine Result
            boolean success = new Random().nextBoolean();
            if (!success) {
                // Apply Penalty immediately to the item reference
                int currentLevel = tool.getData().getLevel();
                int newLevel = Math.max(1, currentLevel - 10);
                tool.getData().setLevel(newLevel);
                tool.getData().setXP(100L * newLevel * newLevel);
                tool.updateLore();
            }

            // Animation (Pass the potentially modified item)
            RitualAnimation.play(player.getLocation(), input, player,
                    RitualAnimation.RitualTheme.DARK, (loc) -> {
                        // Callback when animation finishes
                        if (success) {
                            // SUCCESS
                            player.sendTitle(ChatColor.GOLD + "¡PODER ABSOLUTO!",
                                    ChatColor.YELLOW + "Tus estadísticas se han duplicado.", 10, 70, 20);
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0.5f);

                            // Reward: Haste II + Strength II for 1 hour
                            player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.FAST_DIGGING, 72000, 1));
                            player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.INCREASE_DAMAGE, 72000, 1));

                        } else {
                            // FAILURE
                            player.sendTitle(ChatColor.DARK_RED + "MALDICIÓN",
                                    ChatColor.RED + "La forja ha consumido tu poder (-10 Niveles).", 10, 70, 20);
                            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 0.5f);
                        }
                    });
        }
    }
}
