package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * AdminFreezeManager — permite a los moderadores congelar temporalmente
 * una herramienta viviente para prevenir abusos, ganancia de XP y habilidades.
 */
public class AdminFreezeManager {

    private static NamespacedKey KEY_FROZEN;
    private static NamespacedKey keyFrozen() {
        if (KEY_FROZEN == null)
            KEY_FROZEN = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "tool_frozen");
        return KEY_FROZEN;
    }

    public static boolean isFrozen(LivingTool tool) {
        if (tool == null || !tool.getItem().hasItemMeta()) return false;
        return tool.getItem().getItemMeta().getPersistentDataContainer()
                .getOrDefault(keyFrozen(), PersistentDataType.BYTE, (byte) 0) == (byte) 1;
    }

    public static void setFrozen(LivingTool tool, boolean frozen) {
        if (tool == null || !tool.getItem().hasItemMeta()) return;
        ItemStack item = tool.getItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyFrozen(), PersistentDataType.BYTE, (byte) (frozen ? 1 : 0));
        item.setItemMeta(meta);
        tool.updateLore();
    }
}
