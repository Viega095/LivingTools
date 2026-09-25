package com.livingtools.hooks;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.*;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
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

        // Global player placeholders (don't strictly require holding tool)
        if (params.equalsIgnoreCase("killstreak") || params.equalsIgnoreCase("streak")) {
            return String.valueOf(KillStreakManager.getStreak(player));
        }
        if (params.equalsIgnoreCase("resonance_pieces")) {
            return String.valueOf(SoulResonanceManager.getEquippedLivingArmorPieces(player));
        }
        if (params.equalsIgnoreCase("awakening_cooldown")) {
            long cd = ToolAwakeningManager.getCooldownRemainingSeconds(player);
            return cd > 0 ? String.valueOf(cd) : "0";
        }

        // Held tool placeholders
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return "";

        LivingTool tool = new LivingTool(item);
        String p = params.toLowerCase();

        switch (p) {
            case "level":
                return String.valueOf(tool.getData().getLevel());
            case "xp":
                return String.valueOf(tool.getData().getXP());
            case "required_xp":
            case "next_xp":
                return String.valueOf(LivingTool.getRequiredXP(tool.getData().getLevel()));
            case "prestige":
                return String.valueOf(tool.getData().getPrestige());
            case "personality":
                return tool.getData().getPersonality() != null ? tool.getData().getPersonality() : "WISE";
            case "name":
            case "custom_name":
                return tool.getData().getCustomName().isEmpty() ? "Herramienta Viviente" : tool.getData().getCustomName();
            case "title": {
                ToolTitleSystem.ToolTitle t = ToolTitleSystem.getHighestTitle(tool);
                return t != null ? t.name() : "Ninguno";
            }
            case "title_formatted": {
                ToolTitleSystem.ToolTitle t = ToolTitleSystem.getHighestTitle(tool);
                return t != null ? t.formatted() : "";
            }
            case "blocks_mined":
                return String.valueOf(tool.getData().getBlocksMined());
            case "mob_kills":
            case "mobs_killed":
                return String.valueOf(tool.getData().getMobKills());
            case "player_kills":
            case "players_killed":
                return String.valueOf(tool.getData().getPlayerKills());
            case "favorite_biome": {
                String fav = ToolMemoryManager.getFavoriteBiome(tool);
                return fav != null ? fav.replace("_", " ").toLowerCase() : "Desconocido";
            }
            case "favorite_mob": {
                String mob = ToolMemoryManager.getFavoriteMob(tool);
                return mob != null ? mob.replace("_", " ").toLowerCase() : "Desconocido";
            }
            case "age_days":
                return String.valueOf(ToolMemoryManager.getAgeInDays(tool));
            case "relic_time":
            case "relic_remaining_min":
                return String.valueOf(RelicFragmentSystem.getRelicRemainingMinutes(tool));
            case "achievements_count":
                return String.valueOf(SecretAchievementManager.countAchievements(tool));
            case "milestones_count":
                return String.valueOf(MilestoneManager.countCompleted(tool));
            case "mood":
                return String.valueOf(tool.getData().getMood());
            default:
                return null;
        }
    }
}
