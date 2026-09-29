package com.livingtools.visuals;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * VisualOverhaulManager — Motor de efectos visuales cinematográficos para LivingTools:
 * Doble Hélice de Level Up, Ondas de Choque en Minería y Forja Rítmica.
 */
public class VisualOverhaulManager {

    /**
     * Doble Hélice de partículas ascendente con acordes armónicos al subir de nivel.
     */
    public static void playLevelUpHelix(Player player, String personality) {
        Location base = player.getLocation();
        World world = player.getWorld();

        Particle p1 = Particle.TOTEM;
        Particle p2 = Particle.ENCHANTMENT_TABLE;

        if (personality != null) {
            switch (personality.toUpperCase()) {
                case "AGGRESSIVE": p1 = Particle.FLAME; p2 = Particle.LAVA; break;
                case "LAZY":       p1 = Particle.SNOWFLAKE; p2 = Particle.VILLAGER_HAPPY; break;
                case "CHEERFUL":   p1 = Particle.ELECTRIC_SPARK; p2 = Particle.TOTEM; break;
                default:           p1 = Particle.END_ROD; p2 = Particle.SPELL_WITCH; break;
            }
        }

        final Particle f1 = p1;
        final Particle f2 = p2;

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step >= 20 || !player.isOnline()) {
                    cancel();
                    return;
                }

                double height = step * 0.15;
                double angle1 = step * 0.4;
                double angle2 = angle1 + Math.PI;

                double x1 = base.getX() + Math.cos(angle1) * 1.0;
                double z1 = base.getZ() + Math.sin(angle1) * 1.0;
                double x2 = base.getX() + Math.cos(angle2) * 1.0;
                double z2 = base.getZ() + Math.sin(angle2) * 1.0;

                world.spawnParticle(f1, new Location(world, x1, base.getY() + height, z1), 1, 0, 0, 0, 0);
                world.spawnParticle(f2, new Location(world, x2, base.getY() + height, z2), 1, 0, 0, 0, 0);

                if (step == 0) world.playSound(base, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.0f);
                if (step == 7) world.playSound(base, Sound.BLOCK_BELL_USE, 0.8f, 1.5f);
                if (step == 14) world.playSound(base, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);

                step++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 1L);
    }

    /**
     * Onda de choque expansiva en el túnel tras romper vetas completas.
     */
    public static void playMiningShockwave(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        ParticleOptimizer.spawnCircle(loc.add(0.5, 0.5, 0.5), 2.5, Particle.CRIT, 20, null);
        ParticleOptimizer.spawnCircle(loc, 1.5, Particle.EXPLOSION_NORMAL, 12, null);
        world.playSound(loc, Sound.ENTITY_IRON_GOLEM_DAMAGE, 0.8f, 1.6f);
    }

    /**
     * Secuencia rítmica de 3 martillazos sobre el yunque con chispas incandescentes.
     */
    public static void playReforgeSequence(Player player, Location anvilLoc) {
        World world = anvilLoc.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int strikes = 0;

            @Override
            public void run() {
                if (strikes >= 3 || !player.isOnline()) {
                    cancel();
                    world.playSound(anvilLoc, Sound.BLOCK_ANVIL_USE, 1.2f, 1.5f);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, anvilLoc.add(0.5, 1.2, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                    return;
                }

                float pitch = 0.9f + (strikes * 0.2f);
                world.playSound(anvilLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, pitch);
                world.spawnParticle(Particle.LAVA, anvilLoc.add(0.5, 1.0, 0.5), 10, 0.2, 0.2, 0.2, 0.1);
                world.spawnParticle(Particle.CRIT, anvilLoc, 15, 0.3, 0.3, 0.3, 0.2);

                strikes++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 5L);
    }
}
