package com.livingtools.manager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class CursedForgeManager {

    public static boolean isCursedForge(Block anvil) {
        if (anvil.getType() != Material.ANVIL && anvil.getType() != Material.CHIPPED_ANVIL
                && anvil.getType() != Material.DAMAGED_ANVIL) {
            return false;
        }

        // Check Floor (3x3 Polished Blackstone under the anvil)
        // Center
        if (anvil.getRelative(BlockFace.DOWN).getType() != Material.POLISHED_BLACKSTONE)
            return false;

        // Surrounding 8 blocks
        Block centerFloor = anvil.getRelative(BlockFace.DOWN);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;
                if (centerFloor.getRelative(x, 0, z).getType() != Material.POLISHED_BLACKSTONE)
                    return false;
            }
        }

        // Check Corners (Crying Obsidian + Red Candle)
        // Corners relative to Anvil: (+1, 0, +1), (+1, 0, -1), (-1, 0, +1), (-1, 0, -1)
        // Wait, the floor is 3x3, so corners are at the edge of the floor.
        // Let's put the pillars on the corners of the 3x3 area, at the same Y as the
        // anvil.

        int[][] corners = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int[] corner : corners) {
            Block cornerBlock = anvil.getRelative(corner[0], 0, corner[1]);
            if (cornerBlock.getType() != Material.CRYING_OBSIDIAN)
                return false;

            Block candle = cornerBlock.getRelative(BlockFace.UP);
            if (candle.getType() != Material.RED_CANDLE && candle.getType() != Material.RED_CANDLE_CAKE)
                return false;
        }

        return true;
    }

    public static void openGUI(Player player) {
        com.livingtools.gui.CursedForgeGUI.open(player);
    }
}
