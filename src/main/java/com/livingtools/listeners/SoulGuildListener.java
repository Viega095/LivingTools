package com.livingtools.listeners;

import com.livingtools.gui.SoulGuildVaultGUI;
import com.livingtools.manager.SoulGuildManager;
import com.livingtools.manager.SoulGuildManager.SoulGuild;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * SoulGuildListener — Maneja la sincronización atómica de la Bóveda de Hermandad y eventos de clan.
 */
public class SoulGuildListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        SoulGuild guild = SoulGuildVaultGUI.getOpenVaultGuild(player.getUniqueId());
        if (guild != null) {
            guild.setVaultContents(event.getInventory().getContents());
            SoulGuildManager.save();
            SoulGuildVaultGUI.removeOpenVault(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 0.8f);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SoulGuildVaultGUI.removeOpenVault(player.getUniqueId());
        SoulGuildManager.cleanup(player.getUniqueId());
    }
}
