package com.livingtools.hooks;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LivingToolsExpansion extends PlaceholderExpansion {

    private final LivingToolsPlugin plugin;

    public LivingToolsExpansion(LivingToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "livingtools";
    }

    @Override
    public String getAuthor() {
        return "Viega";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null)
            return "";

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return "";

        LivingTool tool = new LivingTool(item);

        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(tool.getData().getLevel());
        }
        if (params.equalsIgnoreCase("xp")) {
            return String.valueOf(tool.getData().getXP());
        }
        if (params.equalsIgnoreCase("prestige")) {
            return String.valueOf(tool.getData().getPrestige());
        }

        return null;
    }
}
