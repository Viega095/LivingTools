package com.livingtools.abilities.mining;

import com.livingtools.abilities.Ability;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class VeinMinerAbility extends Ability {

    public VeinMinerAbility() {
        super("veinminer", "Vena Minera", "Rompe toda la veta de minerales al picar uno.", 20);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, com.livingtools.data.LivingTool tool) {
        Block startBlock = event.getBlock();
        Material type = startBlock.getType();

        if (!isOre(type))
            return;

        // Mastery Scaling
        int level = getLevel(tool);
        int maxBlocks = 5 + (level * 5); // Lvl 1: 10, Lvl 5: 30

        breakRecursively(startBlock, type, event.getPlayer().getInventory().getItemInMainHand(), new HashSet<>(),
                maxBlocks, event.getPlayer(), tool);
    }

    private void breakRecursively(Block block, Material type, ItemStack toolItem, Set<Block> visited,
            int maxBlocks, org.bukkit.entity.Player player, com.livingtools.data.LivingTool tool) {
        if (visited.size() >= maxBlocks)
            return;
        if (visited.contains(block))
            return;
        if (block.getType() != type)
            return;

        visited.add(block);
        block.breakNaturally(toolItem);

        // Add XP (1 per block)
        addXP(player, tool, 1);

        // Check neighbors
        int[][] directions = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };
        for (int[] dir : directions) {
            breakRecursively(block.getRelative(dir[0], dir[1], dir[2]), type, toolItem, visited, maxBlocks,
                    player, tool);
        }
    }

    private boolean isOre(Material mat) {
        String name = mat.name();
        return name.endsWith("_ORE") || name.equals("ANCIENT_DEBRIS");
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL");
    }
}
