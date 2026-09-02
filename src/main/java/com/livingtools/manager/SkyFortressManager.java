package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SkyFortressManager implements Listener {

    public static void spawnFortress(Player player) {
        spawnFortress(player, true);
    }

    public static void spawnFortress(Player player, boolean spawnBoss) {
        Location center = player.getLocation().add(0, 50, 0);
        java.util.List<Location> blocks = new java.util.ArrayList<>();

        player.sendMessage(MessageUtils.color("&e&l¡Una Fortaleza Celestial ha aparecido sobre ti!"));

        // Procedural Generation
        new BukkitRunnable() {
            int layer = 0;

            @Override
            public void run() {
                if (layer > 10) {
                    this.cancel();
                    if (spawnBoss) {
                        org.bukkit.entity.LivingEntity boss = spawnSeraphim(center.clone().add(0, 12, 0));
                        if (boss != null) {
                            BossAbilityManager.registerArena(boss.getUniqueId(), blocks);
                        }
                    }
                    return;
                }

                generateLayer(center, layer, blocks);
                layer++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 5);
    }

    private static void generateLayer(Location center, int layer, java.util.List<Location> blocks) {
        int radius = 10 - layer;
        if (radius < 3)
            radius = 3;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location loc = center.clone().add(x, layer, z);
                    if (layer == 0) {
                        loc.getBlock().setType(Material.QUARTZ_BLOCK); // Base
                        blocks.add(loc);
                    } else if (layer == 10) {
                        loc.getBlock().setType(Material.GOLD_BLOCK); // Top platform
                        blocks.add(loc);
                    } else if (x == radius || x == -radius || z == radius || z == -radius) {
                        if (layer % 3 == 0) {
                            loc.getBlock().setType(Material.QUARTZ_PILLAR); // Pillars
                            blocks.add(loc);
                        }
                    }
                }
            }
        }
    }

    public static LivingEntity spawnSeraphim(Location location) {
        World world = location.getWorld();
        if (world == null)
            return null;

        LivingEntity entity = (LivingEntity) world.spawnEntity(location, EntityType.BLAZE); // Placeholder model
        entity.setCustomName(MessageUtils.color("&e&lSerafín Guardián"));
        entity.setCustomNameVisible(true);

        // Stats
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(500);
        entity.setHealth(500);

        // Tag
        entity.getPersistentDataContainer()
                .set(new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                        "is_seraphim"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);

        Bukkit.broadcastMessage(MessageUtils.color("&e&l¡El Serafín ha descendido para proteger la fortaleza!"));
        BossBarManager.addBoss(entity, "&e&lSerafín Guardián", org.bukkit.boss.BarColor.YELLOW);
        return entity;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer()
                .has(new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                        "is_seraphim"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            event.getDrops().clear();
            event.getDrops().add(AetherialManager.createStarlightEssence());
            Bukkit.broadcastMessage(
                    MessageUtils.color("&e&l¡El Serafín ha caído! La Esencia Estelar ha sido liberada."));
        }
    }
}
