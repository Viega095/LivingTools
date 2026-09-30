package com.livingtools.listeners;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.manager.AutoUpdateManager;
import com.livingtools.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        if (!player.isOp() && !player.hasPermission("livingtools.admin"))
            return;
        if (!ConfigManager.getBoolean("update-checker.enabled"))
            return;

        AutoUpdateManager updateManager = AutoUpdateManager.getInstance();
        if (updateManager == null)
            return;

        if (updateManager.isUpdatePendingReload()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
            player.sendMessage(ChatColor.YELLOW + "  ✨ " + ChatColor.BOLD + "LIVING TOOLS: ACTUALIZACIÓN DESCARGADA");
            player.sendMessage(ChatColor.WHITE + "  Una nueva versión está lista para aplicarse en vivo.");
            player.sendMessage(ChatColor.AQUA + "  ► Ejecuta " + ChatColor.YELLOW + "/livingtool reload" + ChatColor.AQUA + " para activarla de inmediato.");
            player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
            return;
        }

        if (updateManager.isUpdateAvailable()) {
            String currentVersion = updateManager.getCurrentVersion();
            String latestVersion = updateManager.getLatestVersion();

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
            player.sendMessage(ChatColor.YELLOW + "  🔔 " + ChatColor.BOLD + "LIVING TOOLS: NUEVA VERSIÓN DISPONIBLE");
            player.sendMessage(ChatColor.WHITE + "  Versión actual: " + ChatColor.RED + "v" + currentVersion
                    + ChatColor.WHITE + " ➔ Nueva: " + ChatColor.GREEN + "v" + latestVersion);
            player.sendMessage(ChatColor.AQUA + "  ► Usa " + ChatColor.YELLOW + "/livingtool update install"
                    + ChatColor.AQUA + " para auto-actualizar en vivo.");
            player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.2f);
        }
    }
}
