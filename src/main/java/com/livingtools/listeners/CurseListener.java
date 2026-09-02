package com.livingtools.listeners;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CurseListener implements Listener {

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (!LivingTool.isLivingTool(item))
            return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        NamespacedKey keyFragility = new NamespacedKey(LivingToolsPlugin.getInstance(), "curse_fragility");
        if (meta.getPersistentDataContainer().has(keyFragility, PersistentDataType.BYTE)) {
            // Fragility Curse: Double durability loss
            // We increase the damage by adding the original damage amount again
            // Note: This might be affected by Unbreaking, but it effectively doubles the
            // wear
            event.setDamage(event.getDamage() * 2);
        }
    }
}
