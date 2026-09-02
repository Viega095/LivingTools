package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ForgeManager {

    // Structure: 3x3 Obsidian Base, Center Lava, Corners Netherite Blocks
    // N O N
    // O L O
    // N O N
    // Where N=Netherite Block, O=Obsidian, L=Lava.

    public static boolean isForge(Location center) {
        if (center.getBlock().getType() != Material.LAVA)
            return false;

        Block b = center.getBlock();

        // Relative coordinates
        int[][] corners = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        int[][] sides = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };

        for (int[] cord : corners) {
            if (b.getRelative(cord[0], 0, cord[1]).getType() != Material.NETHERITE_BLOCK)
                return false;
        }

        for (int[] cord : sides) {
            if (b.getRelative(cord[0], 0, cord[1]).getType() != Material.OBSIDIAN)
                return false;
        }

        return true;
    }

    public static void attemptFusion(Item item1, Item item2) {
        ItemStack stack1 = item1.getItemStack();
        ItemStack stack2 = item2.getItemStack();

        if (!LivingTool.isLivingTool(stack1) || !LivingTool.isLivingTool(stack2))
            return;

        LivingTool tool1 = new LivingTool(stack1);
        LivingTool tool2 = new LivingTool(stack2);

        // Fusion Logic
        // Keep highest level
        int newLevel = Math.max(tool1.getData().getLevel(), tool2.getData().getLevel());
        long newXP = Math.max(tool1.getData().getXP(), tool2.getData().getXP());

        // Merge Abilities
        List<String> abilities = new ArrayList<>(tool1.getAbilities());
        for (String ab : tool2.getAbilities()) {
            if (!abilities.contains(ab)) {
                abilities.add(ab);
            }
        }

        // Create Result
        // Use type of tool1 (primary)
        LivingTool result = tool1;
        result.getData().setLevel(newLevel);
        result.getData().setXP(newXP);

        // Clear and re-add abilities
        for (String ab : abilities) {
            if (!result.hasAbility(ab)) {
                result.addAbility(ab);
            }
        }

        // Visuals
        Location loc = item1.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
        loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_USE, 1, 1);

        // Update Item
        item1.setItemStack(result.getItem());
        item2.remove(); // Remove second item
    }
}
