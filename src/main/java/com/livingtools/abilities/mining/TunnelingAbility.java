package com.livingtools.abilities.mining;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;

public class TunnelingAbility extends Ability {

    public TunnelingAbility() {
        super("tunneling", "Tuneladora", "Rompe un área de 3x3 al agacharse.", 30);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, com.livingtools.data.LivingTool tool) {
        if (!event.getPlayer().isSneaking())
            return;

        Block center = event.getBlock();
        BlockFace face = getTargetBlockFace(event.getPlayer());

        if (face == null)
            return;

        // Determine the offsets based on level
        int level = getLevel(tool);
        int[][] offsets = getOffsets(face, level);

        for (int[] offset : offsets) {
            Block target = center.getRelative(offset[0], offset[1], offset[2]);
            if (target.getType() != Material.BEDROCK && target.getType() != Material.AIR) {
                target.breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                // Add XP (1 per block)
                addXP(event.getPlayer(), tool, 1);
            }
        }
    }

    private BlockFace getTargetBlockFace(org.bukkit.entity.Player player) {
        // Raytrace to find the face hit.
        org.bukkit.util.RayTraceResult result = player.rayTraceBlocks(5.0);
        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlockFace();
        }
        return BlockFace.NORTH; // Fallback
    }

    private int[][] getOffsets(BlockFace face, int level) {
        // Level 1: 1x2 (Vertical Strip - Center + 1 Up)
        if (level == 1) {
            if (face == BlockFace.UP || face == BlockFace.DOWN)
                return new int[][] { { 1, 0, 0 } }; // 1x2 on floor? Let's just do center + 1 X
            return new int[][] { { 0, 1, 0 } }; // Center + 1 Up
        }

        // Level 2: 1x3 (Vertical Strip - Center + 1 Up + 1 Down)
        if (level == 2) {
            if (face == BlockFace.UP || face == BlockFace.DOWN)
                return new int[][] { { 1, 0, 0 }, { -1, 0, 0 } };
            return new int[][] { { 0, 1, 0 }, { 0, -1, 0 } };
        }

        // Level 3: Cross (3x3 without corners)
        if (level == 3) {
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                return new int[][] { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };
            }
            // Side view: Up, Down, Left, Right
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                return new int[][] { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
            }
            return new int[][] { { 0, 0, 1 }, { 0, 0, -1 }, { 0, 1, 0 }, { 0, -1, 0 } };
        }

        // Level 4: 3x3 (Standard)
        if (level == 4) {
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                return new int[][] {
                        { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 },
                        { 1, 0, 1 }, { 1, 0, -1 }, { -1, 0, 1 }, { -1, 0, -1 }
                };
            }
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                return new int[][] {
                        { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 },
                        { 1, 1, 0 }, { 1, -1, 0 }, { -1, 1, 0 }, { -1, -1, 0 }
                };
            }
            return new int[][] {
                    { 0, 0, 1 }, { 0, 0, -1 }, { 0, 1, 0 }, { 0, -1, 0 },
                    { 0, 1, 1 }, { 0, 1, -1 }, { 0, -1, 1 }, { 0, -1, -1 }
            };
        }

        // Level 5: 5x5 (Massive)
        if (level >= 5) {
            java.util.List<int[]> offsets = new java.util.ArrayList<>();
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    if (x == 0 && y == 0)
                        continue; // Skip center

                    if (face == BlockFace.UP || face == BlockFace.DOWN) {
                        offsets.add(new int[] { x, 0, y });
                    } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                        offsets.add(new int[] { x, y, 0 });
                    } else {
                        offsets.add(new int[] { 0, y, x });
                    }
                }
            }
            return offsets.toArray(new int[0][]);
        }

        return new int[][] {}; // Fallback
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL");
    }
}
