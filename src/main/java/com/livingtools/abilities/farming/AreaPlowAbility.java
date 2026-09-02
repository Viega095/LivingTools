package com.livingtools.abilities.farming;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerInteractEvent;

public class AreaPlowAbility extends Ability {

    public AreaPlowAbility() {
        super("areaplow", "Arado de Área", "Ara un área de 3x3 al hacer clic derecho.", 10);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HOE");
    }

    @Override
    public void onInteract(PlayerInteractEvent event, com.livingtools.data.LivingTool tool) {
        if (!event.getAction().toString().contains("RIGHT_CLICK_BLOCK"))
            return;
        Block center = event.getClickedBlock();
        if (center == null)
            return;

        Material type = center.getType();
        // Support both DIRT_PATH (1.17+) and GRASS_PATH (1.9-1.16) if possible, but
        // since GRASS_PATH failed, we assume DIRT_PATH or use string check
        boolean isPath = type.name().equals("DIRT_PATH") || type.name().equals("GRASS_PATH");

        if (type != Material.DIRT && type != Material.GRASS_BLOCK && !isPath && type != Material.COARSE_DIRT
                && type != Material.ROOTED_DIRT) {
            return;
        }

        // Plow 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block target = center.getRelative(x, 0, z);
                Block above = target.getRelative(BlockFace.UP);

                boolean targetIsPath = target.getType().name().equals("DIRT_PATH")
                        || target.getType().name().equals("GRASS_PATH");

                if ((target.getType() == Material.DIRT || target.getType() == Material.GRASS_BLOCK || targetIsPath)
                        && above.getType() == Material.AIR) {
                    target.setType(Material.FARMLAND);
                    // Play sound/particle?
                }
            }
        }
    }
}
