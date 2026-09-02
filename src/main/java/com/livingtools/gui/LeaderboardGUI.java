package com.livingtools.gui;

import com.livingtools.manager.LeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardGUI {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Top 10 Herramientas Vivientes");

        List<LeaderboardManager.LeaderboardEntry> topEntries = LeaderboardManager.getTop(10);
        int slot = 0;

        for (LeaderboardManager.LeaderboardEntry entry : topEntries) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                // Use getOfflinePlayer(UUID) if possible, but entry likely has name.
                // Deprecated but functional for name.
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.playerName);
                meta.setOwningPlayer(offlinePlayer);
                meta.setDisplayName(ChatColor.GOLD + "#" + (slot + 1) + " " + ChatColor.YELLOW + entry.playerName);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Herramienta: " + ChatColor.AQUA + entry.toolName);
                lore.add(ChatColor.GRAY + "Nivel: " + ChatColor.GREEN + entry.level);
                lore.add(ChatColor.GRAY + "XP: " + ChatColor.YELLOW + entry.xp);
                if (entry.prestige > 0) {
                    lore.add(ChatColor.GRAY + "Prestigio: " + ChatColor.LIGHT_PURPLE
                            + com.livingtools.data.LivingTool.romanNumeral(entry.prestige) + " ✪");
                }
                meta.setLore(lore);

                head.setItemMeta(meta);
            }
            inv.setItem(slot, head);
            slot++;
        }

        // Fill empty slots with glass
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = slot; i < 27; i++) {
            inv.setItem(i, glass);
        }

        player.openInventory(inv);
    }
}
