package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class IcePathAbility extends Ability {

    public IcePathAbility() {
        super("ice_path", "Camino Helado", "Camina sobre el agua (Frost Walker).", 30, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Frost Walker logic is hard to replicate perfectly without NMS or complex
        // block handling.
        // But we can simulate it by turning water under feet to FROSTED_ICE if we want,
        // OR just giving the enchantment effect if possible?
        // Actually, we can just check if the boots have Frost Walker. If not, add it?
        // No, let's do manual block replacement for "Water Walking".

        Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (block.getType() == Material.WATER && player.getLocation().getBlock().getType() == Material.AIR) {
            block.setType(Material.FROSTED_ICE);
            // Note: Frosted Ice naturally melts, so we don't need to manage reversion!
        }

        // Also check surrounding blocks for smoother walking
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block rel = block.getRelative(x, 0, z);
                if (rel.getType() == Material.WATER && rel.getRelative(BlockFace.UP).getType() == Material.AIR) {
                    rel.setType(Material.FROSTED_ICE);
                }
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_BOOTS");
    }
}
