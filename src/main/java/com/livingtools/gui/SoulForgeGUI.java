package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.SoulForgeManager;
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

public class SoulForgeGUI {

    public static final String TITLE = MessageUtils.color("&5&lForja de Almas");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE);

        // Background
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            gui.setItem(i, bg);
        }

        // Slots
        // 13: Tool Input
        // 29: Soul Gem Input
        // 33: Catalyst (Nether Star) Input
        // 22: Forge Button (Anvil)

        gui.setItem(13, new ItemStack(Material.AIR)); // Tool
        gui.setItem(29, new ItemStack(Material.AIR)); // Soul Gem
        gui.setItem(33, new ItemStack(Material.AIR)); // Catalyst

        // Placeholders
        gui.setItem(13, createItem(Material.BARRIER, "&cColoca tu Herramienta aquí"));
        gui.setItem(29, createItem(Material.PURPLE_STAINED_GLASS_PANE, "&dColoca la Gema de Alma aquí"));
        gui.setItem(33, createItem(Material.YELLOW_STAINED_GLASS_PANE, "&eColoca una Estrella del Nether"));

        // Forge Button
        ItemStack forgeBtn = createItem(Material.ANVIL, "&5&lFORJAR ALMA");
        ItemMeta meta = forgeBtn.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Combina una herramienta con un alma"));
        lore.add(MessageUtils.color("&7para otorgarle nuevos poderes."));
        lore.add("");
        lore.add(MessageUtils.color("&cCoste: 50 Niveles de XP"));
        meta.setLore(lore);
        forgeBtn.setItemMeta(meta);
        gui.setItem(22, forgeBtn);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        Inventory inv = event.getInventory();

        // Allow player inventory interaction
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
            event.setCancelled(false);
            return;
        }

        // Input Slots Logic (Simplified for now, ideally handled in Listener with drag
        // support)
        if (slot == 13 || slot == 29 || slot == 33) {
            event.setCancelled(false); // Allow placing items
            return;
        }

        // Forge Button
        if (slot == 22) {
            ItemStack toolItem = inv.getItem(13);
            ItemStack soulGem = inv.getItem(29);
            ItemStack catalyst = inv.getItem(33);

            if (!LivingTool.isLivingTool(toolItem)) {
                player.sendMessage(MessageUtils.color("&cFalta la Herramienta Viviente."));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                return;
            }

            if (!SoulForgeManager.isSoulGem(soulGem)) {
                player.sendMessage(MessageUtils.color("&cFalta la Gema de Alma."));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                return;
            }

            if (catalyst == null || catalyst.getType() != Material.NETHER_STAR) {
                player.sendMessage(MessageUtils.color("&cFalta el catalizador (Estrella del Nether)."));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                return;
            }

            if (player.getLevel() < 50) {
                player.sendMessage(MessageUtils.color("&cNo tienes suficiente experiencia (50 Niveles)."));
                return;
            }

            // Perform Forging
            LivingTool tool = new LivingTool(toolItem);
            SoulForgeManager.SoulType type = SoulForgeManager.getSoulType(soulGem);

            // Apply Trait (Logic to be added in LivingTool or AbilityRegistry)
            // For now, just add lore/tag
            tool.getData().setPersonality(type.name()); // Placeholder: Set personality to soul type? Or add ability?
            // Let's add a lore line for now

            player.setLevel(player.getLevel() - 50);

            // Consume items
            inv.setItem(29, null);
            inv.setItem(33, null);

            player.sendMessage(MessageUtils.color("&5&l¡FORJA DE ALMA COMPLETADA!"));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0.5f);
            player.closeInventory();
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
