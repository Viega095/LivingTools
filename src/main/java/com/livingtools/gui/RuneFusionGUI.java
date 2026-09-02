package com.livingtools.gui;

import com.livingtools.manager.RuneFusionManager;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RuneFusionGUI {

    public static final String TITLE = MessageUtils.color("&5&lFusión de Runas");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE);

        // Background
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            gui.setItem(i, bg);
        }

        // Slots
        // 11, 13, 15: Input Runes
        // 31: Output Rune (Preview)
        // 22: Fuse Button

        gui.setItem(11, new ItemStack(Material.AIR));
        gui.setItem(13, new ItemStack(Material.AIR));
        gui.setItem(15, new ItemStack(Material.AIR));

        gui.setItem(11, createItem(Material.PURPLE_STAINED_GLASS_PANE, "&dRuna 1"));
        gui.setItem(13, createItem(Material.PURPLE_STAINED_GLASS_PANE, "&dRuna 2"));
        gui.setItem(15, createItem(Material.PURPLE_STAINED_GLASS_PANE, "&dRuna 3"));

        // Fuse Button
        ItemStack fuseBtn = createItem(Material.ENCHANTING_TABLE, "&5&lFUSIONAR");
        ItemMeta meta = fuseBtn.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Combina 3 runas del mismo tipo y tier"));
        lore.add(MessageUtils.color("&7para obtener una de tier superior."));
        meta.setLore(lore);
        fuseBtn.setItemMeta(meta);
        gui.setItem(22, fuseBtn);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        Inventory inv = event.getInventory();

        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
            event.setCancelled(false);
            return;
        }

        if (slot == 11 || slot == 13 || slot == 15) {
            event.setCancelled(false);
            return;
        }

        if (slot == 22) {
            ItemStack r1 = inv.getItem(11);
            ItemStack r2 = inv.getItem(13);
            ItemStack r3 = inv.getItem(15);

            if (r1 == null || r2 == null || r3 == null) {
                player.sendMessage(MessageUtils.color("&cFaltan runas."));
                return;
            }

            ItemStack result = RuneFusionManager.fuseRunes(r1, r2, r3);
            if (result != null) {
                inv.setItem(11, null);
                inv.setItem(13, null);
                inv.setItem(15, null);

                // Give result or place in output slot? Let's give to player for safety
                player.getInventory().addItem(result);

                player.sendMessage(MessageUtils.color("&5&l¡FUSIÓN EXITOSA!"));
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.color("&cRunas incompatibles (Deben ser iguales)."));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            }
        }
    }

    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color(name));
        item.setItemMeta(meta);
        return item;
    }
}
