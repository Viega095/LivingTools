package com.livingtools.manager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class RuneForgeManager {

    public static boolean isRuneForge(Block table) {
        if (table.getType() != Material.SMITHING_TABLE) {
            return false;
        }

        // Check Floor (3x3 Amethyst Block under the table)
        // Center
        if (table.getRelative(BlockFace.DOWN).getType() != Material.AMETHYST_BLOCK)
            return false;

        // Surrounding 8 blocks
        Block centerFloor = table.getRelative(BlockFace.DOWN);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;
                if (centerFloor.getRelative(x, 0, z).getType() != Material.AMETHYST_BLOCK)
                    return false;
            }
        }

        // Check Corners (Purple Candle)
        // Corners relative to Table: (+1, 0, +1), (+1, 0, -1), (-1, 0, +1), (-1, 0, -1)
        // Candles should be on top of the amethyst corners, so same Y as table.
        int[][] corners = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int[] corner : corners) {
            Block cornerBlock = table.getRelative(corner[0], 0, corner[1]);
            if (cornerBlock.getType() != Material.PURPLE_CANDLE && cornerBlock.getType() != Material.PURPLE_CANDLE_CAKE)
                return false;
        }

        return true;
    }

    public static void tryOpenForge(Player player, Block clickedBlock) {
        if (isRuneForge(clickedBlock)) {
            openGUI(player);
            player.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "✨ Has accedido a la Forja Rúnica ✨");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
        } else {
            player.sendMessage(org.bukkit.ChatColor.RED + "La Forja Rúnica parece incompleta...");
            com.livingtools.visuals.RuneForgeVisualizer.sendMaterialLegend(player);
        }
    }

    public static void openGUI(Player player) {
        com.livingtools.gui.RuneForgeGUI.open(player);
    }
}
