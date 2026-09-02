package com.livingtools.abilities.active;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class WindSlashAbility extends Ability {

    public WindSlashAbility() {
        super("windslash", "Corte de Viento", "Shift + Click Izquierdo para lanzar una hoja de viento.", 40);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("LEFT_CLICK") && event.getPlayer().isSneaking()) {
            Player player = event.getPlayer();

            if (isOnCooldown(player))
                return;

            int level = getLevel(tool);
            double range = 5.0 + level; // Lvl 1: 6, Lvl 5: 10
            double damage = 4.0 + (level * 1.5); // Lvl 1: 5.5, Lvl 5: 11.5
            boolean pierce = (level >= 5);

            Location origin = player.getEyeLocation();
            Vector direction = origin.getDirection().normalize();

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 2);

            // Raycast simulation
            for (double d = 0; d < range; d += 0.5) {
                Location point = origin.clone().add(direction.clone().multiply(d));
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 1, 0, 0, 0, 0);

                for (Entity e : point.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
                    if (e instanceof LivingEntity && e != player) {
                        ((LivingEntity) e).damage(damage, player);
                        if (!pierce) {
                            addXP(player, tool, 10);
                            addCooldown(player, 3000); // 3s cooldown
                            return; // Stop if not piercing
                        }
                    }
                }
            }

            addXP(player, tool, 5); // XP on cast
            addCooldown(player, 3000); // 3s cooldown
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD");
    }
}
