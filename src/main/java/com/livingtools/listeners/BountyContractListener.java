package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.BountyContractGUI;
import com.livingtools.manager.BountyContractManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * BountyContractListener — Escucha progreso de cacería/minería y gestiona recompensas en el GUI.
 */
public class BountyContractListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(held)) {
            BountyContractManager.onMobKill(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(held)) {
            BountyContractManager.onOreMine(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(BountyContractGUI.TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        int[] contractSlots = {11, 13, 15};
        int clickedIdx = -1;
        for (int i = 0; i < contractSlots.length; i++) {
            if (contractSlots[i] == slot) {
                clickedIdx = i;
                break;
            }
        }

        if (clickedIdx != -1) {
            boolean success = BountyContractManager.claimContract(player, clickedIdx);
            if (success) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 1.2f);
                player.sendMessage(ChatColor.GREEN + "✦ ¡Recompensa de Contrato reclamada con éxito!");
                BountyContractGUI.open(player);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(BountyContractGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
