package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordManager {

    public static void sendWebhook(String content) {
        String webhookUrl = ConfigManager.getString("discord.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(LivingToolsPlugin.getInstance(), () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Simple JSON construction to avoid dependencies
                String json = "{\"content\": \"" + escapeJson(content) + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode(); // Trigger request
                connection.disconnect();
            } catch (Exception e) {
                LivingToolsPlugin.getInstance().getLogger()
                        .warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    public static void sendBossKill(String player, String bossName) {
        if (ConfigManager.getBoolean("discord.events.boss-kill")) {
            sendWebhook("**" + player + "** has defeated the legendary **" + ChatColor.stripColor(bossName) + "**!");
        }
    }

    public static void sendLevelUp(String player, String toolName, int level) {
        if (ConfigManager.getBoolean("discord.events.level-up")) {
            sendWebhook(
                    "**" + player + "**'s " + ChatColor.stripColor(toolName) + " has reached **Level " + level + "**!");
        }
    }

    public static void sendEventStart(String eventName) {
        if (ConfigManager.getBoolean("discord.events.server-event")) {
            sendWebhook("**:warning: EVENT STARTED:** " + ChatColor.stripColor(eventName) + " has begun!");
        }
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
