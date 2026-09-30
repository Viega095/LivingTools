package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RhythmicForgeVisualizer — Proyecta en tiempo real la guía de partículas holográfica
 * para la construcción de la Forja Rítmica de Almas.
 */
public class RhythmicForgeVisualizer {

    private static final Map<UUID, BukkitRunnable> activeGuides = new HashMap<>();

    public static void toggleGuide(Player player) {
        if (activeGuides.containsKey(player.getUniqueId())) {
            activeGuides.get(player.getUniqueId()).cancel();
            activeGuides.remove(player.getUniqueId());
            player.sendMessage(MessageUtils.color("&cGuía de Forja Rítmica desactivada."));
        } else {
            startGuide(player);
            player.sendMessage(MessageUtils.color("&a✦ Guía de Forja Rítmica de Almas activada."));
            sendMaterialLegend(player);
        }
    }

    public static void sendMaterialLegend(Player player) {
        StructureGuideHelper.sendHeader(player, "Forja Rítmica de Almas");
        StructureGuideHelper.sendLine(player, "Centro (arriba)", ChatColor.AQUA, "Yunque (Cualquier tipo)",
                ChatColor.YELLOW, "bloque");
        StructureGuideHelper.sendLine(player, "Bajo el Yunque", ChatColor.DARK_AQUA, "Fogata de Almas o Magma",
                ChatColor.AQUA, "bloque");
        StructureGuideHelper.sendLine(player, "Plataforma 3×3", ChatColor.DARK_GRAY,
                "Ladrillos de Blackstone Pulida o Deepslate", ChatColor.GRAY, "bloques");
        StructureGuideHelper.sendLine(player, "4 Esquinas (nivel 0)", ChatColor.LIGHT_PURPLE,
                "Obsidiana Llorosa", ChatColor.DARK_PURPLE, "bloques");
        StructureGuideHelper.sendLine(player, "4 Esquinas (nivel +1)", ChatColor.AQUA,
                "Linternas de Almas", ChatColor.BLUE, "antorchas / linternas");
        StructureGuideHelper.sendFooter(player,
                "Usa /lt forge build para construirla automáticamente o sostén tu herramienta y dale clic derecho.");
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
                Location base = center.clone().add(0, -1, 0);
                Vector right = getRightVector(center).normalize();
                Vector forward = getForwardVector(center).normalize();

                // Marcador del centro (Yunque)
                StructureGuideHelper.spawnMarker(player, center, Particle.SOUL_FIRE_FLAME);

                // Base 3x3
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location blockLoc = base.clone()
                                .add(right.clone().multiply(x))
                                .add(forward.clone().multiply(z));
                        boolean isCenter = (x == 0 && z == 0);
                        boolean isCorner = Math.abs(x) == 1 && Math.abs(z) == 1;

                        if (isCenter) {
                            StructureGuideHelper.drawBlockOutline(player, blockLoc, Color.fromRGB(0, 200, 255), Particle.SOUL_FIRE_FLAME);
                        } else if (isCorner) {
                            StructureGuideHelper.drawBlockOutline(player, blockLoc, Color.fromRGB(180, 50, 220), Particle.PORTAL);
                        } else {
                            StructureGuideHelper.drawBlockOutline(player, blockLoc, Color.fromRGB(50, 50, 60), Particle.SMOKE_NORMAL);
                        }
                    }
                }

                // 4 Linternas de Almas en las esquinas a nivel del yunque
                for (int x = -1; x <= 1; x += 2) {
                    for (int z = -1; z <= 1; z += 2) {
                        Location lanternLoc = center.clone()
                                .add(right.clone().multiply(x))
                                .add(forward.clone().multiply(z));
                        StructureGuideHelper.spawnMarker(player, lanternLoc, Particle.SOUL_FIRE_FLAME);
                        StructureGuideHelper.spawnDust(player, lanternLoc, Color.fromRGB(0, 255, 255), 1.0f);
                    }
                }
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

    public static void cancelGuide(Player player) {
        BukkitRunnable task = activeGuides.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private static Vector getRightVector(Location loc) {
        float yaw = loc.getYaw();
        return new Vector(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw)));
    }

    private static Vector getForwardVector(Location loc) {
        float yaw = loc.getYaw();
        return new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
    }
}
