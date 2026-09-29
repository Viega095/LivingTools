package com.livingtools.listeners;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import com.livingtools.visuals.DamageIndicatorManager;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * LivingBowListener — Sistema de combate, trayectoria de proyectiles y experiencia para Arcos y Ballestas Vivientes.
 */
public class LivingBowListener implements Listener {

    private static final String META_LIVING_BOW = "living_bow_shooter";
    private static final String META_SHOOT_LOC = "living_bow_shoot_loc";

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (bow == null || !LivingTool.isLivingTool(bow)) return;

        LivingTool tool = new LivingTool(bow);
        Entity projectile = event.getProjectile();
        if (projectile == null) return;

        // Marcar el proyectil
        projectile.setMetadata(META_LIVING_BOW, new FixedMetadataValue(LivingToolsPlugin.getInstance(), player.getUniqueId().toString()));
        projectile.setMetadata(META_SHOOT_LOC, new FixedMetadataValue(LivingToolsPlugin.getInstance(), player.getLocation()));

        // Partículas en vuelo temáticas según personalidad
        ToolData data = tool.getData();
        String pers = data.getPersonality() != null ? data.getPersonality() : "WISE";

        Particle trailParticle = Particle.CRIT_MAGIC;
        if (pers.contains("AGGRESSIVE") || pers.contains("FURY")) trailParticle = Particle.FLAME;
        else if (pers.contains("LAZY") || pers.contains("HARMONY")) trailParticle = Particle.SNOWFLAKE;
        else if (pers.contains("CHEERFUL") || pers.contains("STORM")) trailParticle = Particle.ELECTRIC_SPARK;
        else if (pers.contains("VOID")) trailParticle = Particle.PORTAL;

        final Particle finalParticle = trailParticle;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (projectile.isDead() || !projectile.isValid() || ticks > 100) {
                    cancel();
                    return;
                }

                Location loc = projectile.getLocation();
                loc.getWorld().spawnParticle(finalParticle, loc, 2, 0.05, 0.05, 0.05, 0.01);
                ticks++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 1L, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) return;
        Projectile projectile = (Projectile) event.getDamager();

        if (!projectile.hasMetadata(META_LIVING_BOW)) return;

        String shooterUuidStr = projectile.getMetadata(META_LIVING_BOW).get(0).asString();
        Player shooter = Bukkit.getPlayer(UUID.fromString(shooterUuidStr));
        if (shooter == null || !shooter.isOnline()) return;

        ItemStack held = shooter.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            held = shooter.getInventory().getItemInOffHand();
        }
        if (!LivingTool.isLivingTool(held)) return;

        LivingTool tool = new LivingTool(held);
        Location hitLoc = event.getEntity().getLocation();
        Location shootLoc = (Location) projectile.getMetadata(META_SHOOT_LOC).get(0).value();

        double distance = shootLoc != null ? shootLoc.distance(hitLoc) : 5.0;

        // Cálculo de XP por impacto a distancia
        long baseXP = 8L;
        if (distance >= 30.0) {
            baseXP = (long) (baseXP * 2.5); // Sniper bonus
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2f, 1.8f);
            MessageUtils.sendActionBar(shooter, ChatColor.GOLD + "🏹 ¡TIRO A LARGA DISTANCIA! (" + (int) distance + "m) +" + baseXP + " XP");
            DamageIndicatorManager.spawnIndicator(hitLoc.add(0, 1, 0), ChatColor.GOLD + "🎯 " + (int) distance + "m!");
        } else {
            baseXP += (long) (distance * 0.3);
            MessageUtils.sendActionBar(shooter, ChatColor.AQUA + "🏹 Impacto certero (+" + baseXP + " XP)");
        }

        tool.addXP(shooter, baseXP);
        tool.getData().setMobKills(tool.getData().getMobKills() + 1);
    }
}
