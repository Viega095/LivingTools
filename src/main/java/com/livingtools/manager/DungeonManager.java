package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonManager implements Listener {

    private static final Map<UUID, String> activeDungeons = new HashMap<>();
    private static final Map<String, Location> dungeonSpawns = new HashMap<>();

    public static void init() {
        // Register default dungeon locations (Placeholders for now)
        // In a real scenario, these would be loaded from config or generated
        if (Bukkit.getWorld("world") != null) {
            dungeonSpawns.put("forge", new Location(Bukkit.getWorld("world"), 0, 100, 0)); // Placeholder
        }

        Bukkit.getPluginManager().registerEvents(new DungeonManager(), LivingToolsPlugin.getInstance());
    }

    public static void enterDungeon(Player player, String dungeonId) {
        if (!dungeonSpawns.containsKey(dungeonId)) {
            MessageUtils.send(player, "&c¡Esa mazmorra no existe!");
            return;
        }

        if (activeDungeons.containsKey(player.getUniqueId())) {
            MessageUtils.send(player, "&c¡Ya estás en una mazmorra!");
            return;
        }

        activeDungeons.put(player.getUniqueId(), dungeonId);
        player.teleport(dungeonSpawns.get(dungeonId));
        MessageUtils.send(player, "&aHas entrado a: &e" + dungeonId.toUpperCase());
        MessageUtils.playLevelUpEffects(player); // Reusing effect for entry
    }

    public static void leaveDungeon(Player player) {
        if (!activeDungeons.containsKey(player.getUniqueId()))
            return;

        activeDungeons.remove(player.getUniqueId());
        player.teleport(player.getWorld().getSpawnLocation()); // Return to spawn
        MessageUtils.send(player, "&eHas abandonado la mazmorra.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        leaveDungeon(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (activeDungeons.containsKey(event.getEntity().getUniqueId())) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            MessageUtils.send(event.getEntity(), "&cHas caído en la mazmorra... pero tu equipo persiste.");
            leaveDungeon(event.getEntity());
        }
    }
}
