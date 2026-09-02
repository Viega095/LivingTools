package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class BossForgeManager {

    // Structure: 5x3 Platform
    // Center: Smithing Table
    // Platform: Polished Blackstone Bricks
    // Corners: Magma Blocks

    public static boolean checkStructure(Location center) {
        Block table = center.getBlock();
        if (table.getType() != Material.SMITHING_TABLE)
            return false;

        // Check Platform (y-1)
        // 5 wide (x-axis relative to facing? Let's assume axis aligned or just check
        // area)
        // Let's check a 5x3 area centered on the block below.
        // Since orientation matters for 5x3, we should check both orientations (NS and
        // EW).

        Block centerBase = table.getRelative(BlockFace.DOWN);

        // Check NS orientation (5 along X, 3 along Z)
        if (checkPlatform(centerBase, 2, 1))
            return true;

        // Check EW orientation (3 along X, 5 along Z)
        if (checkPlatform(centerBase, 1, 2))
            return true;

        return false;
    }

    private static boolean checkPlatform(Block center, int xRadius, int zRadius) {
        for (int x = -xRadius; x <= xRadius; x++) {
            for (int z = -zRadius; z <= zRadius; z++) {
                Block b = center.getRelative(x, 0, z);

                // Corners: Magma Blocks
                boolean isCorner = (Math.abs(x) == xRadius && Math.abs(z) == zRadius);

                if (isCorner) {
                    if (b.getType() != Material.MAGMA_BLOCK)
                        return false;
                } else {
                    if (b.getType() != Material.POLISHED_BLACKSTONE_BRICKS)
                        return false;
                }
            }
        }
        return true;
    }

    public static void handleInteract(Player player, Location location) {
        if (checkStructure(location)) {
            openForge(player, location);
        } else {
            player.sendMessage(MessageUtils.color("&cLa estructura de la Forja de Jefes está incompleta."));
            com.livingtools.visuals.BossForgeVisualizer.sendMaterialLegend(player);
        }
    }

    public static void openForge(Player player, Location location) {
        player.playSound(location, Sound.BLOCK_ANVIL_USE, 1, 0.5f);
        player.spawnParticle(Particle.FLAME, location.clone().add(0.5, 1, 0.5), 20, 0.2, 0.2, 0.2, 0.05);
        com.livingtools.gui.BossForgeGUI.open(player);
    }
}
