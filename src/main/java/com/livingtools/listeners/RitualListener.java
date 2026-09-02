package com.livingtools.listeners;

import com.livingtools.manager.RitualManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class RitualListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block clicked = event.getClickedBlock();
        if (clicked == null)
            return;

        // Altar Interaction (No Shift required)
        if (clicked.getType() == Material.ENCHANTING_TABLE) {
            if (RitualManager.isValidAltar(clicked)) {
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ ¡Altar de Rituales Conectado! ✨");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1, 2);
                com.livingtools.gui.RitualGUI.open(player);
                event.setCancelled(true);
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "El altar está incompleto o es inválido.");
                com.livingtools.visuals.RitualVisualizer.sendMaterialLegend(event.getPlayer());
            }
        }

        // Cursed Forge Interaction (No Shift required)
        else if (com.livingtools.manager.CursedForgeManager.isCursedForge(clicked)) {
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.DARK_RED + "🔥 ¡Has despertado la Forja Maldita! 🔥");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 0.5f);
            com.livingtools.manager.CursedForgeManager.openGUI(player);
            event.setCancelled(true);
        }

        // Rune Forge — mesas de herrería las maneja AssemblyTableManager
    }
}
