package com.livingtools.gui;

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

public class RitualGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "Altar de Rituales";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Fill background
        ItemStack glass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Center Slot (13) - For Tool
        ItemStack placeholder = new ItemStack(Material.BARRIER);
        ItemMeta phMeta = placeholder.getItemMeta();
        phMeta.setDisplayName(ChatColor.RED + "Coloca tu Herramienta Aquí");
        placeholder.setItemMeta(phMeta);
        inv.setItem(13, placeholder);

        // Catalyst Slot (22)
        ItemStack catalystPh = new ItemStack(Material.GRAY_DYE);
        ItemMeta catMeta = catalystPh.getItemMeta();
        catMeta.setDisplayName(ChatColor.GRAY + "Catalizador (Opcional)");
        List<String> catLore = new ArrayList<>();
        catLore.add(ChatColor.DARK_GRAY + "Estrella del Nether -> Reencarnación");
        catLore.add(ChatColor.DARK_GRAY + "Tótem -> Desvinculación");
        catLore.add(ChatColor.DARK_GRAY + "Lágrima de Ghast -> Purificación");
        catMeta.setLore(catLore);
        catalystPh.setItemMeta(catMeta);
        inv.setItem(22, catalystPh);

        // Action Button (15)
        ItemStack action = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta actionMeta = action.getItemMeta();
        actionMeta.setDisplayName(ChatColor.GREEN + "Realizar Ritual");
        List<String> actionLore = new ArrayList<>();
        actionLore.add(ChatColor.GRAY + "Realiza el ritual basado en");
        actionLore.add(ChatColor.GRAY + "los ítems presentes.");
        actionMeta.setLore(actionLore);
        action.setItemMeta(actionMeta);
        inv.setItem(15, action);

        // Guide Book Button (11)
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName(ChatColor.GOLD + "Ver Rituales");
        book.setItemMeta(bookMeta);
        inv.setItem(11, book);

        player.openInventory(inv);
    }

    public static void updateGUI(Inventory inv) {
        ItemStack toolItem = inv.getItem(13);
        ItemStack action = inv.getItem(15);
        ItemMeta meta = action.getItemMeta();
        List<String> lore = new ArrayList<>();

        if (com.livingtools.manager.SleepingManager.isSleepingTool(toolItem)) {
            meta.setDisplayName(ChatColor.GOLD + "Despertar Herramienta");
            lore.add(ChatColor.GRAY + "Costo: 30 Niveles de XP");
            lore.add(ChatColor.YELLOW + "Click para despertar");
        } else {
            meta.setDisplayName(ChatColor.GREEN + "Realizar Ritual");
            lore.add(ChatColor.GRAY + "Realiza el ritual basado en");
            lore.add(ChatColor.GRAY + "los ítems presentes.");
        }

        meta.setLore(lore);
        action.setItemMeta(meta);
        inv.setItem(15, action);
    }

    public static void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.PURPLE_STAINED_GLASS_PANE)
            return;

        if (clicked.getType() == Material.BOOK) {
            com.livingtools.gui.RitualGuideGUI.open(player);
        } else if (clicked.getType() == Material.ENCHANTING_TABLE) {
            ItemStack toolItem = event.getInventory().getItem(13);

            // Awakening Logic
            if (com.livingtools.manager.SleepingManager.isSleepingTool(toolItem)) {
                // Extra check: If it's already a Living Tool (has level/xp), do NOT awaken
                // again.
                // Sleeping tools should NOT have the LivingTool PDC data fully initialized or
                // should be distinguishable.
                // Assuming isSleepingTool checks for specific lore or NBT.

                if (LivingTool.isLivingTool(toolItem)) {
                    player.sendMessage(ChatColor.RED + "Esta herramienta ya está despierta.");
                    return;
                }

                com.livingtools.manager.SleepingManager.tryAwaken(player, toolItem);

                // If successful (item changed), close inventory or update
                if (!com.livingtools.manager.SleepingManager.isSleepingTool(toolItem)) {
                    player.closeInventory();
                }
                return;
            }

            ItemStack catalystItem = event.getInventory().getItem(22);

            if (toolItem != null && LivingTool.isLivingTool(toolItem)) {
                LivingTool tool = new LivingTool(toolItem);
                String ritualType = "Restauración";

                // Catalyst Logic
                if (catalystItem != null) {
                    if (catalystItem.getType() == Material.NETHER_STAR) {
                        // Reincarnation (Reroll Personality)
                        com.livingtools.manager.PersonalityManager.assignRandomPersonality(tool);
                        ritualType = "Reencarnación";
                        player.sendMessage(
                                ChatColor.GOLD + "¡Tu herramienta ha reencarnado con una nueva personalidad!");
                    } else if (catalystItem.getType() == Material.TOTEM_OF_UNDYING) {
                        // Unbinding (Remove Owner)
                        tool.getData().setOwnerName("None");
                        ritualType = "Desvinculación";
                        player.sendMessage(ChatColor.AQUA + "¡Tu herramienta ha sido desvinculada de su dueño!");
                    } else if (catalystItem.getType() == Material.GHAST_TEAR) {
                        // Purification (Remove All Corruption)
                        if (player.getLevel() >= 50) {
                            tool.getData().setCorruption(0);
                            player.setLevel(player.getLevel() - 50);
                            ritualType = "Purificación";
                            player.sendMessage(ChatColor.WHITE + "¡Tu herramienta ha sido purificada completamente!");
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                        } else {
                            player.sendMessage(
                                    ChatColor.RED + "Necesitas 50 niveles de XP para purificar la herramienta.");
                            player.closeInventory();
                            return;
                        }
                    }
                }

                // Corruption Check (Phase 22)
                if (tool.getData().getCorruption() > 75) {
                    // Blood Moon Bonus: 100% Success Rate (Phase 33)
                    if (com.livingtools.manager.BloodMoonManager.isBloodMoonActive()) {
                        player.sendMessage(ChatColor.DARK_RED + "☠ La Luna de Sangre estabiliza la corrupción... ☠");
                    } else if (Math.random() < 0.5) { // 50% chance to fail
                        player.closeInventory();
                        player.sendMessage(
                                ChatColor.DARK_RED + "¡La corrupción de la herramienta ha interrumpido el ritual!");
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1,
                                0.5f);
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VEX_CHARGE, 1, 0.5f);

                        // Spawn Corrupt Spirit
                        org.bukkit.entity.Vex vex = (org.bukkit.entity.Vex) player.getWorld()
                                .spawnEntity(player.getLocation().add(0, 1, 0), org.bukkit.entity.EntityType.VEX);
                        vex.setCustomName(ChatColor.DARK_RED + "Espíritu Corrupto");
                        vex.setCustomNameVisible(true);
                        vex.setTarget(player);

                        // Consume items anyway (punishment)
                        event.getInventory().setItem(13, null);
                        event.getInventory().setItem(22, null);
                        return;
                    }
                }

                // Check if a valid ritual was triggered (Catalyst used)
                if (catalystItem == null) {
                    player.sendMessage(ChatColor.GRAY + "El altar permanece en silencio... Necesitas un catalizador.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH, 1, 0.5f);
                    return;
                }

                // Trigger Animation
                org.bukkit.block.Block target = player.getTargetBlockExact(5);
                org.bukkit.Location ritualLoc;

                if (target != null && target.getType() == Material.ENCHANTING_TABLE) {
                    ritualLoc = target.getLocation();
                } else {
                    ritualLoc = player.getLocation(); // Fallback
                }

                // Remove items so they don't drop/return
                event.getInventory().setItem(13, null);
                event.getInventory().setItem(22, null); // Consume catalyst

                player.closeInventory();

                // Determine Theme
                com.livingtools.visuals.RitualAnimation.RitualTheme theme = com.livingtools.visuals.RitualAnimation.RitualTheme.LIGHT;
                if (ritualType.equals("Reencarnación") || ritualType.equals("Desvinculación")) {
                    theme = com.livingtools.visuals.RitualAnimation.RitualTheme.DARK;
                }

                // Start Animation
                com.livingtools.visuals.RitualAnimation.play(ritualLoc, toolItem, player, theme, null);

            } else {
                player.sendMessage(ChatColor.RED + "Coloca una Herramienta Viviente en el centro.");
            }
        }
    }
}
