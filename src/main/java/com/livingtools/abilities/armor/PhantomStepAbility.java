package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.LivingToolsPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class PhantomStepAbility extends Ability {

    private final Set<Player> activePlayers = new HashSet<>();

    public PhantomStepAbility() {
        super("phantom_step", "Paso Fantasma",
                "Permite atravesar bloques sólidos brevemente al agacharse",
                45, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        if (player.isSneaking() && !activePlayers.contains(player)) {
            Location loc = player.getLocation();
            Block block = loc.getBlock();

            // Verificar si está dentro de un bloque sólido
            if (block.getType().isSolid() && block.getType() != Material.BEDROCK) {
                activePlayers.add(player);
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("§5👻 Paso Fantasma activado");

                // Volver a modo normal después de 2 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.setGameMode(GameMode.SURVIVAL);
                            activePlayers.remove(player);
                            player.sendMessage("§7👻 Paso Fantasma desactivado");
                        }
                    }
                }.runTaskLater(LivingToolsPlugin.getInstance(), 40L);
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("LEGGINGS");
    }
}
