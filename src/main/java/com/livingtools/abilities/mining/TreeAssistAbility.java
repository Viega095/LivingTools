package com.livingtools.abilities.mining;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class TreeAssistAbility extends Ability {

    public TreeAssistAbility() {
        super("treeassist", "Tala Maestra", "Tala árboles enteros rompiendo el bloque inferior.", 15);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, com.livingtools.data.LivingTool tool) {
        Block block = event.getBlock();
        if (!isLog(block.getType()))
            return;

        // Check if it's the bottom of a tree (block below is dirt/grass)
        Material below = block.getRelative(BlockFace.DOWN).getType();
        if (below != Material.DIRT && below != Material.GRASS_BLOCK && below != Material.PODZOL
                && below != Material.COARSE_DIRT && below != Material.ROOTED_DIRT) {
            return;
        }

        breakRecursively(block, event.getPlayer().getInventory().getItemInMainHand(), new HashSet<>());
    }

    private void breakRecursively(Block block, ItemStack tool, Set<Block> visited) {
        if (visited.size() > 200)
            return; // Safety limit
        if (visited.contains(block))
            return;
        if (!isLog(block.getType()) && !isLeaves(block.getType()))
            return;

        visited.add(block);
        block.breakNaturally(tool);

        // Check neighbors (up and around)
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) { // Prioritize going up
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    breakRecursively(block.getRelative(x, y, z), tool, visited);
                }
            }
        }
    }

    private boolean isLog(Material mat) {
        return mat.name().endsWith("_LOG") || mat.name().endsWith("_WOOD") || mat.name().endsWith("_STEM")
                || mat.name().endsWith("_HYPHAE");
    }

    private boolean isLeaves(Material mat) {
        return mat.name().endsWith("_LEAVES") || mat.name().endsWith("_WART_BLOCK")
                || mat.name().endsWith("_SHROOMLIGHT");
    }
}
