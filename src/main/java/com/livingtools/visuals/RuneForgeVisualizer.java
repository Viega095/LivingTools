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

public class RuneForgeVisualizer {

    private static final Map<UUID, Integer> activeTasks = new HashMap<>();

    public static void toggleGuide(Player player) {
        if (activeTasks.containsKey(player.getUniqueId())) {
            int taskId = activeTasks.remove(player.getUniqueId());
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            player.sendMessage(ChatColor.RED + "Guía de Forja Rúnica desactivada.");
        } else {
            startGuide(player);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Guía de Forja Rúnica activada.");
            sendMaterialLegend(player);
        }
    }

    public static void sendMaterialLegend(Player player) {
        StructureGuideHelper.sendHeader(player, "Forja Rúnica");
        StructureGuideHelper.sendLine(player, "Centro (arriba)", ChatColor.YELLOW, "Mesa de Herrería",
                ChatColor.GOLD, "bloque");
        StructureGuideHelper.sendLine(player, "Piso 3×3", ChatColor.YELLOW, "Bloques de Amatista",
                ChatColor.LIGHT_PURPLE, "bloques sólidos");
        StructureGuideHelper.sendLine(player, "Esquinas (misma altura)", ChatColor.YELLOW, "Velas Moradas",
                ChatColor.DARK_PURPLE, "bloque sobre las esquinas del piso");
        StructureGuideHelper.sendFooter(player,
                "Partículas: fucsia=amatista, llama=velas moradas.");
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

                StructureGuideHelper.spawnMarker(player, center, Particle.CRIT_MAGIC);
                StructureGuideHelper.spawnMarker(player, center, Particle.ENCHANTMENT_TABLE);

                Location floorCenter = center.clone().add(0, -1, 0);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location floor = floorCenter.clone().add(x, 0, z);
                        StructureGuideHelper.spawnDust(player, floor, Color.FUCHSIA, 1.0f);
                    }
                }

                int[][] corners = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
                for (int[] corner : corners) {
                    Location candle = center.clone().add(corner[0], 0, corner[1]);
                    StructureGuideHelper.spawnDust(player, candle, Color.PURPLE, 0.9f);
                    StructureGuideHelper.spawnCandleMarker(player, candle);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 20L).getTaskId();

        activeTasks.put(player.getUniqueId(), taskId);
    }
}
