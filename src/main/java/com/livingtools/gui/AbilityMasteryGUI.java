package com.livingtools.gui;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AbilityMasteryGUI {

    private static final String TITLE = ChatColor.DARK_PURPLE + "Maestría de Habilidades";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        // Background / Borders
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack purpleGlass = createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        ItemStack blueGlass = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");

        // Fill borders
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, blackGlass);
            } else {
                // Inner background (optional, or leave air)
                // gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
        }

        // Decorate corners
        gui.setItem(0, purpleGlass);
        gui.setItem(8, purpleGlass);
        gui.setItem(45, purpleGlass);
        gui.setItem(53, purpleGlass);

        // Decorate center top/bottom
        gui.setItem(4, blueGlass);
        gui.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "Cerrar"));

        List<String> abilities = tool.getAbilities();

        // Center area slots: 10-16, 19-25, 28-34, 37-43
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (String abilityId : abilities) {
            if (index >= slots.length)
                break;

            Ability ability = AbilityRegistry.getAbility(abilityId);
            if (ability != null) {
                int level = tool.getData().getAbilityLevel(abilityId);
                int xp = tool.getData().getAbilityXP(abilityId);
                int nextLevelXP = (level + 1) * (level + 1) * 100;
                if (level >= 5)
                    nextLevelXP = xp; // Cap

                ItemStack icon = new ItemStack(Material.ENCHANTED_BOOK);
                // Try to find a representative material if possible, otherwise book
                // For now, Book is classic.

                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + ability.getName());

                if (level >= 5) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_GRAY + "----------------------");
                lore.add(ChatColor.GRAY + ability.getDescription());
                lore.add("");

                // Level Bar
                lore.add(ChatColor.YELLOW + "Nivel: " + ChatColor.WHITE + level + ChatColor.GOLD + "/5");
                lore.add(getLevelProgressBar(level, 5));
                lore.add("");

                if (level < 5) {
                    lore.add(ChatColor.YELLOW + "Progreso XP:");
                    lore.add(getXPProgressBar(xp, nextLevelXP) + ChatColor.DARK_GRAY + " (" + xp + "/" + nextLevelXP
                            + ")");
                    lore.add("");
                    lore.add(ChatColor.AQUA + "Siguiente Nivel:");
                    lore.add(ChatColor.GRAY + "Mejora la efectividad de la habilidad.");
                } else {
                    lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "★ MAESTRÍA MÁXIMA ★");
                    lore.add(ChatColor.GRAY + "Has alcanzado el potencial máximo.");
                }
                lore.add(ChatColor.DARK_GRAY + "----------------------");

                meta.setLore(lore);
                icon.setItemMeta(meta);

                gui.setItem(slots[index++], icon);
            }
        }

        player.openInventory(gui);
    }

    private static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static String getLevelProgressBar(int current, int max) {
        StringBuilder bar = new StringBuilder(ChatColor.GRAY + "[");
        for (int i = 1; i <= max; i++) {
            if (i <= current) {
                bar.append(ChatColor.GOLD).append("★");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("☆");
            }
        }
        bar.append(ChatColor.GRAY + "]");
        return bar.toString();
    }

    private static String getXPProgressBar(int current, int max) {
        int totalBars = 20;
        int filledBars = (int) ((double) current / max * totalBars);
        if (filledBars > totalBars)
            filledBars = totalBars;

        StringBuilder bar = new StringBuilder(ChatColor.GRAY + "[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append(ChatColor.GREEN).append("|");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("|");
            }
        }
        bar.append(ChatColor.GRAY + "]");
        return bar.toString();
    }
}
