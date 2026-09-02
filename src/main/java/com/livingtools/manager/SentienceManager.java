package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Random;

public class SentienceManager {

    private static final Random random = new Random();

    public static boolean shouldRefuseWork(Player player, LivingTool tool) {
        if ("LAZY".equals(tool.getData().getPersonality()) && random.nextDouble() < 0.05) {
            if (DialogueCooldown.tryToolChat(player)) {
                player.sendMessage(ChatColor.GOLD + "[" + tool.getData().getCustomName() + ChatColor.GOLD + "] "
                        + ChatColor.WHITE + "No quiero hacer eso ahora.");
            }
            return true;
        }
        return false;
    }

    public static void checkEffects(Player player, LivingTool tool) {
        // Placeholder for mood effects
    }

    public static void onKill(Player player, LivingTool tool) {
        // Diálogo unificado en PersonalityManager (evita doble mensaje por kill).
    }

    public static void onPlayerDeath(Player player, LivingTool tool) {
        if (!"SARCASTIC".equals(tool.getData().getPersonality()) || !DialogueCooldown.tryToolChat(player)) {
            return;
        }
        player.sendMessage(ChatColor.GOLD + "[" + tool.getData().getCustomName() + ChatColor.GOLD + "] "
                + ChatColor.WHITE + "Eso dolió, ¿verdad?");
    }
}
