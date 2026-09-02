package com.livingtools.api;

import com.livingtools.data.LivingTool;
import com.livingtools.data.LivingArmor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class LivingToolsAPI {

    private static LivingToolsAPI instance;

    private LivingToolsAPI() {
    }

    public static LivingToolsAPI getInstance() {
        if (instance == null) {
            instance = new LivingToolsAPI();
        }
        return instance;
    }

    /**
     * Checks if an item is a Living Tool.
     *
     * @param item The item to check.
     * @return True if it is a Living Tool, false otherwise.
     */
    public boolean isLivingTool(ItemStack item) {
        return LivingTool.isLivingTool(item);
    }

    /**
     * Checks if an item is Living Armor.
     *
     * @param item The item to check.
     * @return True if it is Living Armor, false otherwise.
     */
    public boolean isLivingArmor(ItemStack item) {
        return LivingArmor.isLivingArmor(item);
    }

    /**
     * Gets the level of a Living Tool or Armor.
     *
     * @param item The item to check.
     * @return The level, or 0 if not a living item.
     */
    public int getLevel(ItemStack item) {
        if (isLivingTool(item)) {
            return new LivingTool(item).getData().getLevel();
        } else if (isLivingArmor(item)) {
            return new LivingArmor(item).getLevel();
        }
        return 0;
    }

    /**
     * Adds XP to a Living Tool or Armor.
     *
     * @param player The player holding/wearing the item (for messages/effects).
     * @param item   The item to add XP to.
     * @param amount The amount of XP to add.
     */
    public void addXP(Player player, ItemStack item, long amount) {
        if (isLivingTool(item)) {
            new LivingTool(item).addXP(player, amount);
        } else if (isLivingArmor(item)) {
            new LivingArmor(item).addXP(player, amount);
        }
    }

    /**
     * Gets the list of abilities on a Living Item.
     *
     * @param item The item to check.
     * @return A list of ability IDs, or an empty list.
     */
    public List<String> getAbilities(ItemStack item) {
        if (isLivingTool(item)) {
            return new LivingTool(item).getData().getAbilities();
        } else if (isLivingArmor(item)) {
            return new LivingArmor(item).getAbilities();
        }
        return Collections.emptyList();
    }
}
