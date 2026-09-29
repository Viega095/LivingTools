package com.livingtools.listeners;

import com.livingtools.manager.SoulCompassManager;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

/**
 * SoulCompassListener — Maneja la sintonización y rastreo con la Brújula de Almas.
 */
public class SoulCompassListener implements Listener {

    @EventHandler
    public void onCompassInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || !SoulCompassManager.isSoulCompass(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        Location meteorLoc = SoulCompassManager.getCurrentMeteorLocation();

        if (meteorLoc == null || meteorLoc.getWorld() == null || !meteorLoc.getWorld().equals(player.getWorld())) {
            player.sendMessage(ChatColor.GRAY + "🧭 La Brújula de Almas gira suavemente: No hay perturbaciones cósmicas activas en este mundo.");
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
            return;
        }

        // Sintonizar brújula
        if (item.getItemMeta() instanceof CompassMeta) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            meta.setLodestoneTracked(false);
            meta.setLodestone(meteorLoc);
            item.setItemMeta(meta);
        }

        int distance = (int) player.getLocation().distance(meteorLoc);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.6f);
        MessageUtils.sendActionBar(player, ChatColor.AQUA + "🧭 Meteorito Celestial detectado a " + ChatColor.GOLD + distance + "m" + ChatColor.AQUA + " de distancia!");
        player.sendMessage(ChatColor.AQUA + "🧭 Brújula sintonizada con el impacto del meteorito (" + ChatColor.YELLOW + distance + "m" + ChatColor.AQUA + ").");
    }
}
