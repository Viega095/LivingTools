package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class VoidRiftManager implements Listener {

    public static void spawnRift(Location location) {
        location.getWorld().playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1, 0.5f);
        Bukkit.broadcastMessage(MessageUtils.color("&5&l¡SE HA ABIERTO UNA GRIETA DEL VACÍO!"));
        Bukkit.broadcastMessage(MessageUtils.color(
                "&7Coordenadas: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 600) { // 30 seconds duration
                    this.cancel();
                    location.getWorld().playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 0.5f);
                    return;
                }

                // Particles
                location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 1, 1, 1, 0.1);
                location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 20, 0.5, 0.5, 0.5, 0.05);

                // Spawn Creatures every 5 seconds
                if (ticks % 100 == 0) {
                    spawnShadowCreature(location);
                }

                ticks += 5;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 5);
    }

    private static void spawnShadowCreature(Location location) {
        World world = location.getWorld();
        if (world == null)
            return;

        LivingEntity entity = (LivingEntity) world.spawnEntity(location, EntityType.WITHER_SKELETON);
        entity.setCustomName(MessageUtils.color("&8Criatura Sombría"));
        entity.setCustomNameVisible(true);

        // Buffs
        entity.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 9999, 1));
        entity.addPotionEffect(
                new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 9999, 1));

        // Tag
        entity.getPersistentDataContainer()
                .set(new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                        "is_shadow_creature"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer()
                .has(new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                        "is_shadow_creature"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            event.getDrops().clear();
            event.getDrops().add(AbyssalManager.createVoidEssence());
        }
    }
}
