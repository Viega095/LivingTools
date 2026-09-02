package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RitualVisualizer {

    private static final Map<UUID, Integer> activeTasks = new HashMap<>();

    public static void toggleGuide(Player player) {
        if (activeTasks.containsKey(player.getUniqueId())) {
            int taskId = activeTasks.remove(player.getUniqueId());
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            player.sendMessage(com.livingtools.manager.ConfigManager.getMessage("ritual_guide_disabled"));
        } else {
            startGuide(player);
            player.sendMessage(ChatColor.GREEN + "Guía de Altar de Rituales activada.");
            sendMaterialLegend(player);
        }
    }

    public static void sendMaterialLegend(Player player) {
        StructureGuideHelper.sendHeader(player, "Altar de Rituales");
        StructureGuideHelper.sendLine(player, "Centro", ChatColor.YELLOW, "Mesa de Encantamientos",
                ChatColor.LIGHT_PURPLE, "bloque");
        StructureGuideHelper.sendLine(player, "Cruz (N/S/E/O)", ChatColor.YELLOW, "Polvo de Redstone",
                ChatColor.RED, "gota en el suelo");
        StructureGuideHelper.sendLine(player, "Esquinas (diagonal)", ChatColor.YELLOW, "Velas",
                ChatColor.WHITE, "bloque, cualquier color");
        StructureGuideHelper.sendLine(player, "Infusión (opcional)", ChatColor.YELLOW,
                "Magma / Hielo Azul / Pararrayos / Faro", ChatColor.AQUA,
                "bloque bajo cada vela (las 4 iguales)");
        StructureGuideHelper.sendFooter(player,
                "Partículas: morado=mesa, rojo=redstone, llama=velas.");
    }

    private static void startGuide(Player player) {
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    activeTasks.remove(player.getUniqueId());
                    return;
                }

                Location center = player.getLocation().getBlock().getLocation();

                StructureGuideHelper.spawnMarker(player, center, Particle.VILLAGER_HAPPY);
                StructureGuideHelper.spawnMarker(player, center, Particle.ENCHANTMENT_TABLE);

                StructureGuideHelper.spawnWireMarker(player, center.clone().add(1, 0, 0));
                StructureGuideHelper.spawnWireMarker(player, center.clone().add(-1, 0, 0));
                StructureGuideHelper.spawnWireMarker(player, center.clone().add(0, 0, 1));
                StructureGuideHelper.spawnWireMarker(player, center.clone().add(0, 0, -1));

                StructureGuideHelper.spawnCandleMarker(player, center.clone().add(1, 0, 1));
                StructureGuideHelper.spawnCandleMarker(player, center.clone().add(1, 0, -1));
                StructureGuideHelper.spawnCandleMarker(player, center.clone().add(-1, 0, 1));
                StructureGuideHelper.spawnCandleMarker(player, center.clone().add(-1, 0, -1));
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 20L).getTaskId();

        activeTasks.put(player.getUniqueId(), taskId);
    }
}
