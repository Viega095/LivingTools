package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GodModeManager {

    public static void terraform(Player player, LivingTool tool) {
        if (tool.getData().getLevel() < 200) {
            player.sendMessage(ChatColor.RED + "Necesitas Nivel 200 para usar esto.");
            return;
        }

        Location loc = player.getLocation();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y <= 1; y++) {
                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, block.getLocation(), 1);
                    }
                }
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        player.sendMessage(ChatColor.GOLD + "¡El terreno se doblega ante tu poder!");
    }
}
