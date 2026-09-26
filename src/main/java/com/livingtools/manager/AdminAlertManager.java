package com.livingtools.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * AdminAlertManager — envía notificaciones de moderación al staff online.
 */
public class AdminAlertManager {

    public static final String PERMISSION = "livingtools.admin.alerts";

    public static void sendStaffAlert(String message) {
        String formatted = ChatColor.DARK_RED + "[LT Staff] " + ChatColor.RED + message;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(PERMISSION) || p.isOp()) {
                p.sendMessage(formatted);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2.0f);
            }
        }
    }
}
