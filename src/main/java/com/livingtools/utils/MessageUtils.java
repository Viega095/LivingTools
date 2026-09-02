package com.livingtools.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static void send(CommandSender sender, String message) {
        // Legacy support or raw messages
        String prefix = com.livingtools.manager.ConfigManager.getRawMessage("prefix");
        sender.sendMessage(color(prefix + message));
    }

    public static void sendKey(CommandSender sender, String key) {
        sender.sendMessage(com.livingtools.manager.ConfigManager.getMessage(key));
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(color(message)));
    }

    public static void playLevelUpEffects(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        player.spawnParticle(org.bukkit.Particle.TOTEM, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
    }

    public static void playXPGainSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }

    public static String gradient(String text, String color1, String color2) {
        return color(color1 + text);
    }
}
