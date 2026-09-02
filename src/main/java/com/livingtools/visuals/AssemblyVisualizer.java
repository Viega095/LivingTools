package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AssemblyVisualizer {

    private static final Map<UUID, BukkitRunnable> activeGuides = new HashMap<>();

    public static void toggleGuide(Player player) {
        if (activeGuides.containsKey(player.getUniqueId())) {
            cancelGuide(player);
            player.sendMessage(MessageUtils.color("&cGuía de Mesa de Ensamblaje desactivada."));
        } else {
            startGuide(player);
            player.sendMessage(MessageUtils.color("&aGuía de Mesa de Ensamblaje activada."));
            sendMaterialLegend(player);
        }
    }

    public static void sendMaterialLegend(Player player) {
        StructureGuideHelper.sendHeader(player, "Mesa de Ensamblaje");
        StructureGuideHelper.sendLine(player, "Centro (arriba)", ChatColor.YELLOW, "Mesa de Herrería",
                ChatColor.GOLD, "bloque");
        StructureGuideHelper.sendLine(player, "Soporte (debajo)", ChatColor.YELLOW, "Bloque de Hierro",
                ChatColor.WHITE, "bloque sólido");
        StructureGuideHelper.sendFooter(player,
                "Partículas: dorado=mesa, gris claro=bloque de hierro. Clic derecho en la mesa para abrir.");
    }

    private static void startGuide(Player player) {
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelGuide(player);
                    return;
                }

                ticks++;
                if (ticks > 600) {
                    cancelGuide(player);
                    player.sendMessage(MessageUtils.color("&eGuía desactivada por tiempo."));
                    return;
                }

                Location center = previewCenter(player);
                Location ironSupport = center.clone().add(0, -1, 0);

                StructureGuideHelper.spawnDust(player, ironSupport, Color.SILVER, 1.2f);
                StructureGuideHelper.drawBlockOutline(player, ironSupport, Color.fromRGB(180, 180, 180), null);

                StructureGuideHelper.spawnMarker(player, center, Particle.VILLAGER_HAPPY);
                StructureGuideHelper.spawnMarker(player, center, Particle.ENCHANTMENT_TABLE);
            }
        };

        task.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 10L);
        activeGuides.put(player.getUniqueId(), task);
    }

    private static Location previewCenter(Player player) {
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        center.setY(player.getLocation().getY());
        center.setPitch(0);
        center.setYaw(Math.round(player.getLocation().getYaw() / 90f) * 90f);
        return center.getBlock().getLocation();
    }

    private static void cancelGuide(Player player) {
        BukkitRunnable task = activeGuides.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
