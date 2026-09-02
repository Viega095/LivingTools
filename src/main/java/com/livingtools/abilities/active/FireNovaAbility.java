package com.livingtools.abilities.active;

import com.livingtools.abilities.ElementalAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FireNovaAbility extends ElementalAbility {

    public FireNovaAbility() {
        super("fire_nova", "Nova de Fuego", "Desata una explosión de fuego alrededor del usuario.", 30, 15000);
    }

    @Override
    protected boolean activate(Player player, LivingTool tool) {
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 100, 3, 1, 3, 0.1);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 20, 1, 1, 1, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

        for (Entity entity : player.getNearbyEntities(5, 2, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100); // 5 seconds
                ((LivingEntity) entity).damage(5.0, player);
            }
        }
        return true;
    }

    @Override
    public boolean isCompatible(org.bukkit.Material material) {
        return material.name().endsWith("_SWORD") || material.name().endsWith("_AXE");
    }
}
