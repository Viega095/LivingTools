package com.livingtools.listeners;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

    private final LivingToolsPlugin plugin;

    public UpdateListener(LivingToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp())
            return;
        if (!ConfigManager.getBoolean("update-checker.enabled"))
            return;

        String latestVersion = plugin.getLatestVersion();
        if (latestVersion == null)
            return;

        String currentVersion = plugin.getDescription().getVersion();
        if (!currentVersion.equalsIgnoreCase(latestVersion)) {
            event.getPlayer()
                    .sendMessage(ChatColor.GOLD + "[LivingTools] " + ChatColor.YELLOW + "¡Nueva versión disponible!");
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Actual: " + ChatColor.RED + currentVersion
                    + ChatColor.YELLOW + " -> Nueva: " + ChatColor.GREEN + latestVersion);
            event.getPlayer().sendMessage(ChatColor.AQUA + "Descárgala en: https://github.com/"
                    + ConfigManager.getString("update-checker.repository") + "/releases");
        }
    }
}
