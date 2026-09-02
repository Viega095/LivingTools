package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EvolutionManager implements Listener {

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        // This event is for player XP, not tool XP.
        // Tool XP is handled in LivingTool.addXP.
        // However, we can listen for tool level ups if we had a custom event.
        // For now, LivingTool.checkEvolution calls this logic directly or we can hook
        // into it.
    }

    public static void updateAppearance(LivingTool tool) {
        int level = tool.getData().getLevel();
        ItemStack item = tool.getItem();
        ItemMeta meta = item.getItemMeta();

        // CustomModelData logic
        // Base: 0
        // Lvl 50: 1
        // Lvl 100: 2
        // Lvl 200: 3

        int modelData = 0;
        if (level >= 200)
            modelData = 3;
        else if (level >= 100)
            modelData = 2;
        else if (level >= 50)
            modelData = 1;

        if (meta.hasCustomModelData() && meta.getCustomModelData() == modelData) {
            return; // No change needed
        }

        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
    }
}
