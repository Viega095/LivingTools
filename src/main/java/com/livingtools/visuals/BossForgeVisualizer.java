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

public class BossForgeVisualizer {

    private static final Map<UUID, BukkitRunnable> activeGuides = new HashMap<>();

    public static void toggleGuide(Player player) {
        if (activeGuides.containsKey(player.getUniqueId())) {
            activeGuides.get(player.getUniqueId()).cancel();
            activeGuides.remove(player.getUniqueId());
            player.sendMessage(MessageUtils.color("&cGuía de Forja de Jefes desactivada."));
        } else {
            startGuide(player);
            player.sendMessage(MessageUtils.color("&aGuía de Forja de Jefes activada."));
            sendMaterialLegend(player);
        }
    }

    public static void sendMaterialLegend(Player player) {
        StructureGuideHelper.sendHeader(player, "Forja de Jefes");
        StructureGuideHelper.sendLine(player, "Centro (arriba)", ChatColor.YELLOW, "Mesa de Herrería",
                ChatColor.GOLD, "bloque");
        StructureGuideHelper.sendLine(player, "Plataforma 5×3", ChatColor.YELLOW,
                "Ladrillos de Blackstone Pulida", ChatColor.DARK_GRAY, "bloques");
        StructureGuideHelper.sendLine(player, "Esquinas de plataforma", ChatColor.YELLOW,
                "Bloques de Magma", ChatColor.RED, "bloques");
        StructureGuideHelper.sendLine(player, "Crafteo GUI", ChatColor.YELLOW,
                "Ingredientes en cruz", ChatColor.AQUA, "drops nombrados de jefe");
        StructureGuideHelper.sendFooter(player,
                "Partículas: gris=plataforma, naranja=magma, dorado=mesa. Orientación 5×3 N-S o E-O.");
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

                StructureGuideHelper.spawnMarker(player, center, Particle.VILLAGER_HAPPY);

                for (int x = -2; x <= 2; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location blockLoc = base.clone()
                                .add(right.clone().multiply(x))
                                .add(forward.clone().multiply(z));
                        boolean isCorner = Math.abs(x) == 2 && Math.abs(z) == 1;
                        if (isCorner) {
                            StructureGuideHelper.drawBlockOutline(player, blockLoc, Color.ORANGE, Particle.FLAME);
                        } else {
                            StructureGuideHelper.drawBlockOutline(player, blockLoc, Color.fromRGB(40, 40, 45),
                                    Particle.SMOKE_NORMAL);
                        }
                    }
                }

                // Cruz de crafteo (referencia GUI)
                Location crossY = center.clone().add(0, 1, 0);
                StructureGuideHelper.spawnDust(player, crossY.clone().add(0, 0, 0), Color.AQUA, 0.9f);
                StructureGuideHelper.spawnDust(player, crossY.clone().add(right.clone().multiply(0.8)), Color.YELLOW, 0.8f);
                StructureGuideHelper.spawnDust(player, crossY.clone().add(right.clone().multiply(-0.8)), Color.YELLOW, 0.8f);
                StructureGuideHelper.spawnDust(player, crossY.clone().add(forward.clone().multiply(0.8)), Color.YELLOW, 0.8f);
                StructureGuideHelper.spawnDust(player, crossY.clone().add(forward.clone().multiply(-0.8)), Color.YELLOW, 0.8f);
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

    private static Vector getRightVector(Location loc) {
        float yaw = loc.getYaw();
        return new Vector(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw)));
    }

    private static Vector getForwardVector(Location loc) {
        float yaw = loc.getYaw();
        return new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
    }
}
