package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SmugglerGUI {

    private final Player player;
    private final Inventory inventory;

    public SmugglerGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Mercado Negro");
        initializeItems();
    }

    private void initializeItems() {
        // Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Sell XP (Buy with Emeralds)
        ItemStack buyXP = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = buyXP.getItemMeta();
        xpMeta.setDisplayName(ChatColor.GREEN + "Comprar 1000 XP de Herramienta");
        List<String> xpLore = new ArrayList<>();
        xpLore.add(ChatColor.GRAY + "Coste: " + ChatColor.GREEN + "10 Esmeraldas");
        xpMeta.setLore(xpLore);
        buyXP.setItemMeta(xpMeta);
        inventory.setItem(11, buyXP);

        // Buy Illegal Upgrade (Forbidden Scroll)
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        upgradeMeta.setDisplayName(ChatColor.DARK_RED + "Pergamino Prohibido");
        List<String> upgradeLore = new ArrayList<>();
        upgradeLore.add(ChatColor.GRAY + "Otorga " + ChatColor.RED + "Sharpness VI");
        upgradeLore.add(ChatColor.GRAY + "pero aplica " + ChatColor.DARK_RED + "Maldición: Fragilidad");
        upgradeLore.add(ChatColor.GRAY + "(Doble pérdida de durabilidad)");
        upgradeLore.add(ChatColor.GRAY + "Coste: " + ChatColor.AQUA + "5 Diamantes");
        upgradeMeta.setLore(upgradeLore);
        upgrade.setItemMeta(upgradeMeta);
        inventory.setItem(15, upgrade);
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static void handleClick(Player player, int slot, ItemStack clicked, Inventory inv) {
        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) {
            player.sendMessage(ChatColor.RED + "Debes sostener una Herramienta Viviente.");
            player.closeInventory();
            return;
        }
        LivingTool tool = new LivingTool(hand);

        if (slot == 11) { // Buy XP
            if (player.getInventory().contains(Material.EMERALD, 10)) {
                player.getInventory().removeItem(new ItemStack(Material.EMERALD, 10));
                tool.addXP(player, 1000);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.sendMessage(ChatColor.GREEN + "Has comprado 1000 XP.");
            } else {
                player.sendMessage(ChatColor.RED + "No tienes suficientes esmeraldas.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
        } else if (slot == 15) { // Buy Forbidden Scroll (Sharpness VI + Fragility)
            if (player.getInventory().contains(Material.DIAMOND, 5)) { // Increased cost to 5 Diamonds
                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 5));

                // Apply Sharpness VI
                ItemMeta meta = tool.getItem().getItemMeta();
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 6, true);

                // Apply Curse of Fragility (Custom Lore Tag)
                List<String> lore = meta.getLore();
                if (lore == null)
                    lore = new ArrayList<>();
                lore.add(ChatColor.RED + "Maldición: Fragilidad");
                meta.setLore(lore);
                tool.getItem().setItemMeta(meta);

                tool.getData().addCorruption(25); // +25% Corruption
                tool.updateLore(); // This might reset lore, need to ensure curse persists or is handled in
                                   // updateLore

                // Re-apply curse if updateLore wiped it (since updateLore rebuilds lore from
                // data)
                // Better approach: Store curse in PersistentDataContainer
                ItemMeta newMeta = tool.getItem().getItemMeta();
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                        com.livingtools.LivingToolsPlugin.getInstance(), "curse_fragility");
                newMeta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                tool.getItem().setItemMeta(newMeta);
                tool.updateLore(); // Update lore to show the curse based on PDC

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 0.5f);
                player.sendMessage(ChatColor.DARK_RED
                        + "¡Has aceptado el poder prohibido! Tu herramienta es más fuerte, pero su alma se quiebra...");
            } else {
                player.sendMessage(ChatColor.RED + "Necesitas 5 Diamantes para este oscuro trato.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
        }
    }
}
