package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.LivingToolsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpectralWingsAbility extends Ability {

    public SpectralWingsAbility() {
        super("spectral_wings", "Alas Espectrales",
                "Al caer desde altura, obtiene vuelo temporal como elytra",
                50, AbilityType.ARMOR);
    }

    @Override
    public void onDamageTaken(EntityDamageEvent event, Player player) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            double fallDistance = player.getFallDistance();

            if (fallDistance > 5.0) {
                // Reducir daño de caída
                event.setDamage(event.getDamage() * 0.3);

                // Activar vuelo temporal
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setVelocity(new Vector(0, 0.5, 0));

                player.sendMessage("§d✦ ¡Alas Espectrales activadas!");

                // Desactivar después de 5 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnGround()) {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            player.sendMessage("§7✦ Alas Espectrales desactivadas");
                        }
                    }
                }.runTaskLater(LivingToolsPlugin.getInstance(), 100L);
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("CHESTPLATE");
    }
}
