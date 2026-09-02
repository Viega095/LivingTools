package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.LoreManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class TomeListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.WRITTEN_BOOK)
            return;

        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null || !meta.hasLore())
            return;

        List<String> lore = meta.getLore();
        String idLine = null;
        for (String line : lore) {
            if (line.contains("ID: ")) {
                idLine = line;
                break;
            }
        }

        if (idLine == null)
            return;

        String id = ChatColor.stripColor(idLine).replace("ID: ", "").trim();
        LoreManager.TomeType type = LoreManager.TomeType.fromId(id);

        if (type == null)
            return;

        // Check for Living Tool in Offhand
        // If book is in main hand, check offhand for tool.
        // If book is in offhand, check main hand for tool.

        ItemStack otherItem = null;
        if (event.getHand() == EquipmentSlot.HAND) {
            otherItem = player.getInventory().getItemInOffHand();
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            otherItem = player.getInventory().getItemInMainHand();
        }

        if (LivingTool.isLivingTool(otherItem)) {
            LivingTool tool = new LivingTool(otherItem);
            LoreManager.unlockTome(player, tool, type);

            // Consume Tome
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                item.setAmount(0); // Or set to air/null
            }
        } else {
            // No tool found
            // player.sendMessage(ChatColor.GRAY + "Necesitas sostener una Herramienta
            // Viviente en la otra mano para que aprenda de este tomo.");
        }
    }
}
