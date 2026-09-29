package com.livingtools.visuals;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ParticleOptimizer — Motor de renderizado vectorial de partículas con optimización
 * de rendimiento (caching trigonométrico y limitación de frecuencia).
 */
public class ParticleOptimizer {

    // Cache trigonométrico para 360 grados (resolución de 5 grados = 72 puntos)
    private static final int RESOLUTION = 72;
    private static final double[] SIN_CACHE = new double[RESOLUTION];
    private static final double[] COS_CACHE = new double[RESOLUTION];

    static {
        for (int i = 0; i < RESOLUTION; i++) {
            double angle = (2 * Math.PI * i) / RESOLUTION;
            SIN_CACHE[i] = Math.sin(angle);
            COS_CACHE[i] = Math.cos(angle);
        }
    }

    // Throttle por jugador (evita enviar más de 60 paquetes de partículas cosméticas/segundo)
    private static final Map<UUID, Long> lastCosmeticPacket = new HashMap<>();
    private static final long THROTTLE_INTERVAL_MS = 16L; // ~60 FPS cap

    public static boolean canSpawnCosmetic(Player player) {
        if (player == null) return true;
        long now = System.currentTimeMillis();
        Long last = lastCosmeticPacket.get(player.getUniqueId());
        if (last != null && now - last < THROTTLE_INTERVAL_MS) {
            return false;
        }
        lastCosmeticPacket.put(player.getUniqueId(), now);
        return true;
    }

    /**
     * Dibuja un anillo horizontal continuo en el suelo.
     */
    public static void spawnCircle(Location center, double radius, Particle particle, int count, Object extraData) {
        World world = center.getWorld();
        if (world == null) return;

        int step = Math.max(1, RESOLUTION / Math.max(8, count));
        for (int i = 0; i < RESOLUTION; i += step) {
            double x = center.getX() + radius * COS_CACHE[i];
            double z = center.getZ() + radius * SIN_CACHE[i];
            Location pLoc = new Location(world, x, center.getY(), z);

            if (extraData != null) {
                world.spawnParticle(particle, pLoc, 1, 0, 0, 0, 0, extraData);
            } else {
                world.spawnParticle(particle, pLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Dibuja una espiral helicoidal ascendente.
     */
    public static void spawnHelix(Location base, double radius, double height, Particle particle, Object extraData) {
        World world = base.getWorld();
        if (world == null) return;

        double yStep = height / RESOLUTION;
        for (int i = 0; i < RESOLUTION; i += 2) {
            double x = base.getX() + radius * COS_CACHE[i];
            double y = base.getY() + (i * yStep);
            double z = base.getZ() + radius * SIN_CACHE[i];
            Location pLoc = new Location(world, x, y, z);

            if (extraData != null) {
                world.spawnParticle(particle, pLoc, 1, 0, 0, 0, 0, extraData);
            } else {
                world.spawnParticle(particle, pLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Dibuja una línea de partículas entre dos puntos.
     */
    public static void spawnLine(Location from, Location to, Particle particle, double spacing, Object extraData) {
        World world = from.getWorld();
        if (world == null || !from.getWorld().equals(to.getWorld())) return;

        double distance = from.distance(to);
        if (distance <= 0.01) return;

        int points = (int) (distance / spacing);
        double dx = (to.getX() - from.getX()) / points;
        double dy = (to.getY() - from.getY()) / points;
        double dz = (to.getZ() - from.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location pLoc = from.clone().add(dx * i, dy * i, dz * i);
            if (extraData != null) {
                world.spawnParticle(particle, pLoc, 1, 0, 0, 0, 0, extraData);
            } else {
                world.spawnParticle(particle, pLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void cleanup(UUID uuid) {
        lastCosmeticPacket.remove(uuid);
    }
}
