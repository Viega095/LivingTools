package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ModelManager {

    private static final int BASE_ID = 1000;

    public static void updateModel(LivingTool tool) {
        ItemStack item = tool.getItem();
        if (item == null)
            return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        // Calculate Model ID based on Level and Infusions
        // This ID is applied to ALL tool types (Iron, Diamond, Netherite, etc.)
        // The resource pack handles mapping this ID to the correct texture/model for
        // each item type.
        int modelId = calculateModelId(tool);
        meta.setCustomModelData(modelId);
        item.setItemMeta(meta);
    }

    private static int calculateModelId(LivingTool tool) {
        int level = tool.getData().getLevel();
        int corruption = tool.getData().getCorruption();

        // Corruption Max (Demonic Form) -> 1300
        if (corruption >= 100) {
            return 1300;
        }

        // Level 100+ (Max/Prestige) -> 1200
        if (level >= 100) {
            return 1200;
        }
        // Level 75+ (Infernal) -> 1100
        else if (level >= 75) {
            return 1100;
        }
        // Level 50+ (Netherite/Evolved) -> 1005
        else if (level >= 50) {
            return 1005;
        }
        // Levels 1-49 -> 1000 + (Level / 10)
        else {
            int tier = level / 10; // 0-4
            return BASE_ID + tier;
        }
    }
}
