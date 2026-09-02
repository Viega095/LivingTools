package com.livingtools.abilities.time;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.TimeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class TimeWarpAbility extends Ability {

    public TimeWarpAbility() {
        super("time_warp", "Salto Temporal", "Regresa 5 segundos en el tiempo.", 30, AbilityType.ACTIVE);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("RIGHT")) {
            Player player = event.getPlayer();
            if (!isOnCooldown(player)) {
                Location pastLoc = TimeManager.getPastLocation(player);

                // Visuals at current location
                player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1, 2);

                if (pastLoc != null) {
                    player.teleport(pastLoc);
                    // Visuals at new location
                    player.getWorld().spawnParticle(Particle.SPELL_WITCH, pastLoc, 50, 0.5, 1, 0.5, 0.1);
                    player.playSound(pastLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    player.sendMessage("§d§l[TIEMPO] §7¡Has retrocedido en el tiempo!");
                } else {
                    player.sendMessage("§cNo hay registro temporal disponible.");
                }

                addCooldown(player, 60000); // 60s cooldown
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return true;
    }
}
