package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LavaWalkerAbility extends Ability {

    public LavaWalkerAbility() {
        super("lava_walker", "Caminante de Lava",
                "Convierte la lava en magma temporal al caminar sobre ella",
                40, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();

        if (blockBelow.getType() == Material.LAVA) {
            // Convertir lava en magma temporalmente
            blockBelow.setType(Material.MAGMA_BLOCK);

            // Volver a lava después de 3 segundos
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    com.livingtools.LivingToolsPlugin.getInstance(),
                    () -> {
                        if (blockBelow.getType() == Material.MAGMA_BLOCK) {
                            blockBelow.setType(Material.LAVA);
                        }
                    },
                    60L);
        }

        // Inmunidad al fuego
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("BOOTS");
    }
}
