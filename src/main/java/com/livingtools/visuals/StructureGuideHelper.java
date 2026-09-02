package com.livingtools.visuals;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Leyenda de materiales y partículas compartidas para guías de estructura.
 */
public final class StructureGuideHelper {

    private StructureGuideHelper() {
    }

    public static void sendHeader(Player player, String structureName) {
        player.sendMessage(ChatColor.GOLD + "=== Materiales: " + structureName + " ===");
    }

    public static void sendLine(Player player, String role, ChatColor roleColor, String material, ChatColor materialColor,
            String blockType) {
        player.sendMessage(roleColor + role + ChatColor.GRAY + ": "
                + materialColor + material + ChatColor.DARK_GRAY + " (" + blockType + ")");
    }

    public static void sendFooter(Player player, String hint) {
        player.sendMessage(ChatColor.GRAY + hint);
    }

    public static void spawnDust(Player player, Location loc, Color color, float size) {
        Location at = loc.clone().add(0.5, 0.35, 0.5);
        player.spawnParticle(Particle.REDSTONE, at, 8, 0.18, 0.06, 0.18, 0,
                new Particle.DustOptions(color, size));
    }

    public static void spawnMarker(Player player, Location loc, Particle particle) {
        Location at = loc.clone().add(0.5, 0.55, 0.5);
        player.spawnParticle(particle, at, 6, 0.12, 0.12, 0.12, 0);
    }

    public static void spawnWireMarker(Player player, Location loc) {
        spawnDust(player, loc, Color.RED, 1.0f);
        spawnMarker(player, loc, Particle.FIREWORKS_SPARK);
    }

    public static void spawnCandleMarker(Player player, Location loc) {
        spawnMarker(player, loc, Particle.FLAME);
        spawnMarker(player, loc.clone().add(0, 0.25, 0), Particle.SMOKE_NORMAL);
    }

    public static void drawBlockOutline(Player player, Location blockLoc, Color dustColor, Particle accent) {
        double x = blockLoc.getBlockX();
        double y = blockLoc.getBlockY();
        double z = blockLoc.getBlockZ();

        for (double i = 0; i <= 1; i += 1) {
            for (double j = 0; j <= 1; j += 0.25) {
                player.spawnParticle(Particle.REDSTONE, x + i, y + j, z, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x + i, y + j, z + 1, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x, y + j, z + i, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x + 1, y + j, z + i, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x + j, y + i, z, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x + j, y + i, z + 1, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x, y + i, z + j, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
                player.spawnParticle(Particle.REDSTONE, x + 1, y + i, z + j, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(dustColor, 1.0f));
            }
        }
        if (accent != null) {
            player.spawnParticle(accent, x + 0.5, y + 0.8, z + 0.5, 3, 0.15, 0.15, 0.15, 0);
        }
    }
}
