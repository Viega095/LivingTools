package com.livingtools.abilities.combat;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * SoulHomingArrowAbility — Flechas que ajustan suavemente su trayectoria hacia el enemigo más cercano.
 */
public class SoulHomingArrowAbility extends Ability {

    public SoulHomingArrowAbility() {
        super("homing_arrow", "Flecha Teledirigida", "Las flechas corrigen su trayectoria hacia el enemigo más cercano.", 30);
    }

    public void onShoot(EntityShootBowEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Entity projEntity = event.getProjectile();
        if (!(projEntity instanceof Projectile)) return;
        Projectile projectile = (Projectile) projEntity;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (projectile.isDead() || !projectile.isValid() || ticks > 60) {
                    cancel();
                    return;
                }

                // Buscar enemigo más cercano en 8 bloques
                LivingEntity nearest = null;
                double nearestDist = 64.0; // 8^2

                for (Entity e : projectile.getNearbyEntities(8.0, 8.0, 8.0)) {
                    if (e instanceof LivingEntity && !e.equals(player) && !(e instanceof org.bukkit.entity.ArmorStand)) {
                        double d = e.getLocation().distanceSquared(projectile.getLocation());
                        if (d < nearestDist) {
                            nearestDist = d;
                            nearest = (LivingEntity) e;
                        }
                    }
                }

                if (nearest != null) {
                    Vector currentVel = projectile.getVelocity();
                    Vector targetDir = nearest.getEyeLocation().toVector().subtract(projectile.getLocation().toVector()).normalize();
                    // Ajuste suave
                    Vector newVel = currentVel.multiply(0.85).add(targetDir.multiply(0.35));
                    projectile.setVelocity(newVel);
                    projectile.getWorld().spawnParticle(Particle.SPELL_WITCH, projectile.getLocation(), 1, 0, 0, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 3L, 2L);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }
}
