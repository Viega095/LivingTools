package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CursedForgeVisualizer {

    private static final Map<UUID, Integer> activeTasks = new HashMap<>();

    public static void toggleGuide(Player player) {
        if (activeTasks.containsKey(player.getUniqueId())) {
            int taskId = activeTasks.remove(player.getUniqueId());
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            player.sendMessage(ChatColor.RED + "Guía de Forja Maldita desactivada.");
        } else {
            startGuide(player);
            player.sendMessage(ChatColor.DARK_RED + "Guía de Forja Maldita activada.");
            sendMaterialLegend(player);
        }
    }

    public static void sendMaterialLegend(Player player) {
        StructureGuideHelper.sendHeader(player, "Forja Maldita");
        StructureGuideHelper.sendLine(player, "Centro", ChatColor.YELLOW, "Yunque",
                ChatColor.GRAY, "bloque (cualquier estado)");
        StructureGuideHelper.sendLine(player, "Piso 3×3", ChatColor.YELLOW, "Blackstone Pulida",
                ChatColor.DARK_GRAY, "bloques");
        StructureGuideHelper.sendLine(player, "Esquinas (misma altura)", ChatColor.YELLOW, "Obsidiana Llorosa",
                ChatColor.DARK_PURPLE, "bloques");
        StructureGuideHelper.sendLine(player, "Sobre esquinas", ChatColor.YELLOW, "Velas Rojas",
                ChatColor.RED, "bloque encima de la obsidiana");
        StructureGuideHelper.sendFooter(player,
                "Partículas: gris=piso, morado=obsidiana, llama=velas rojas.");
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

                StructureGuideHelper.spawnMarker(player, center, Particle.DAMAGE_INDICATOR);
                StructureGuideHelper.spawnMarker(player, center, Particle.SMOKE_LARGE);

                Location floorCenter = center.clone().add(0, -1, 0);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location floor = floorCenter.clone().add(x, 0, z);
                        StructureGuideHelper.spawnDust(player, floor, Color.fromRGB(30, 30, 35), 1.0f);
                    }
                }

                int[][] corners = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
                for (int[] corner : corners) {
                    Location obsidian = center.clone().add(corner[0], 0, corner[1]);
                    StructureGuideHelper.spawnDust(player, obsidian, Color.PURPLE, 1.1f);
                    StructureGuideHelper.spawnMarker(player, obsidian, Particle.PORTAL);
                    StructureGuideHelper.spawnCandleMarker(player, obsidian.clone().add(0, 1, 0));
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 20L).getTaskId();

        activeTasks.put(player.getUniqueId(), taskId);
    }
}
