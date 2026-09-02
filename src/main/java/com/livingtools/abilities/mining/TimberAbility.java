package com.livingtools.abilities.mining;

import com.livingtools.abilities.Ability;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class TimberAbility extends Ability {

    public TimberAbility() {
        super("timber", "Leñador", "Corta todo el árbol al agacharse.", 25);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, com.livingtools.data.LivingTool tool) {
        if (!event.getPlayer().isSneaking())
            return;

        Block startBlock = event.getBlock();
        if (!isLog(startBlock.getType()))
            return;

        // Mastery Scaling
        int level = getLevel(tool);
        int maxBlocks = 50 + (level * 50); // Lvl 1: 100, Lvl 5: 300

        breakRecursively(startBlock, event.getPlayer().getInventory().getItemInMainHand(), new HashSet<>(),
                maxBlocks, event.getPlayer(), tool);
    }

    private void breakRecursively(Block block, ItemStack toolItem, Set<Block> visited, int maxBlocks,
            org.bukkit.entity.Player player, com.livingtools.data.LivingTool tool) {
        if (visited.size() >= maxBlocks) // Max logs based on level
            return;
        if (visited.contains(block))
            return;
        if (!isLog(block.getType()))
            return;

        visited.add(block);
        block.breakNaturally(toolItem);

        // Add XP (1 per log)
        addXP(player, tool, 1);

        // Check neighbors (Upwards biased for trees)
        int[][] directions = { { 0, 1, 0 }, { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 }, { 1, 1, 0 },
                { -1, 1, 0 }, { 0, 1, 1 }, { 0, 1, -1 } };
        for (int[] dir : directions) {
            breakRecursively(block.getRelative(dir[0], dir[1], dir[2]), toolItem, visited, maxBlocks, player,
                    tool);
        }
    }

    private boolean isLog(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_STEM"); // For nether trees
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE");
    }
}
