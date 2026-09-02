package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.GuideManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class GuideListener implements Listener {

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        checkSleepingTool(player, item);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        com.livingtools.manager.GuideBookManager.giveBook(player);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                checkSleepingTool(player, item);
            }
        }
    }

    private void checkSleepingTool(Player player, ItemStack item) {
        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            // If it's a "Sleeping" tool, it might have a specific lore or NBT.
            // Assuming "Sleeping" tools are just LivingTools that haven't been fully
            // awakened or just found.
            // But the user said "no importa dormida", implying any tool that needs
            // awakening.
            // Usually sleeping tools have level 0 or specific lore.
            // For now, we trigger if it's a LivingTool. The GuideManager handles "already
            // done" checks.

            // We need to find a tool that IS awake to speak to the player about the
            // sleeping one?
            // Or does the sleeping one speak? The user said "le avisa que puede
            // despertarla".
            // If the player has NO active tool, who speaks? The system (Whisper)?
            // GuideManager.triggerStep uses "tool" to get personality.
            // If the picked up tool is sleeping (no personality?), we might need a
            // fallback.

            // Let's iterate player's inventory to find an ACTIVE tool to speak.
            LivingTool activeTool = findActiveTool(player);

            if (activeTool != null) {
                // An active tool speaks about the new item
                GuideManager.triggerStep(player, activeTool, GuideManager.TutorialStep.SLEEPING_FOUND);
            } else {
                // No active tool, maybe the sleeping tool itself whispers (if it has some
                // consciousness)
                // or we just use the sleeping tool with NORMAL personality default.
                GuideManager.triggerStep(player, tool, GuideManager.TutorialStep.SLEEPING_FOUND);
            }
        }
    }

    private LivingTool findActiveTool(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                // Check if it's "Awake" (e.g. Level > 0 or has owner)
                if (tool.getData().getLevel() > 0) {
                    return tool;
                }
            }
        }
        return null;
    }
}
