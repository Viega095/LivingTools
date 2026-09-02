package com.livingtools.abilities.mythic;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class WorldBreakerAbility extends Ability {

    public WorldBreakerAbility() {
        super("worldbreaker", "Rompe Mundos", "Rompe un área de 3x3x3.", 75);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, LivingTool tool) {
        Player player = event.getPlayer();
        Block center = event.getBlock();

        // Prevent recursion loop if needed, but since we break manually, it shouldn't
        // trigger new events unless we call them
        // For simplicity, we just break the blocks naturally

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue; // Skip center (already broken)

                    Block target = center.getRelative(x, y, z);
                    if (target.getType() != Material.BEDROCK && target.getType() != Material.AIR) {
                        target.breakNaturally(player.getInventory().getItemInMainHand());
                    }
                }
            }
        }
    }
}
