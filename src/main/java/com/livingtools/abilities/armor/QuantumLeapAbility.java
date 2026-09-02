package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuantumLeapAbility extends Ability {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 5000; // 5 segundos

    public QuantumLeapAbility() {
        super("quantum_leap", "Salto Cuántico",
                "Teletransporte corto al agacharse + saltar (5s cooldown)",
                35, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        if (player.isSneaking() && player.getVelocity().getY() > 0.3) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();

            if (cooldowns.containsKey(uuid)) {
                long lastUse = cooldowns.get(uuid);
                if (now - lastUse < COOLDOWN_MS) {
                    return; // En cooldown
                }
            }

            // Calcular destino (10 bloques adelante)
            Vector direction = player.getLocation().getDirection().normalize();
            Location destination = player.getLocation().add(direction.multiply(10));

            // Ajustar Y para estar en el suelo
            while (destination.getBlock().getType() == Material.AIR && destination.getY() > 0) {
                destination.subtract(0, 1, 0);
            }
            destination.add(0, 1, 0);

            // Efectos visuales
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

            // Teletransportar
            player.teleport(destination);

            // Efectos en destino
            player.getWorld().spawnParticle(Particle.PORTAL, destination, 50, 0.5, 1, 0.5);
            player.getWorld().playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

            player.sendMessage("§b⚡ Salto Cuántico!");
            cooldowns.put(uuid, now);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("LEGGINGS");
    }
}
