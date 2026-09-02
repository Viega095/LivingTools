package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ConvergenceManager implements Listener {

    public static void startEvent(Location location) {
        Bukkit.broadcastMessage(MessageUtils.color("&4&l¡LA CONVERGENCIA HA COMENZADO!"));
        Bukkit.broadcastMessage(
                MessageUtils.color("&7El Titán descenderá en " + location.getBlockX() + ", " + location.getBlockZ()));

        location.getWorld().playSound(location, Sound.EVENT_RAID_HORN, 5, 1);

        new BukkitRunnable() {
            int seconds = 60;

            @Override
            public void run() {
                if (seconds <= 0) {
                    this.cancel();
                    spawnTitan(location);
                    return;
                }
                if (seconds % 10 == 0) {
                    Bukkit.broadcastMessage(MessageUtils.color("&cEl Titán llega en " + seconds + " segundos..."));
                }
                seconds--;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 20);
    }

    private static void spawnTitan(Location location) {
        World world = location.getWorld();
        if (world == null)
            return;

        LivingEntity entity = (LivingEntity) world.spawnEntity(location, EntityType.GIANT);
        entity.setCustomName(MessageUtils.color("&4&lTITÁN DE LA CONVERGENCIA"));
        entity.setCustomNameVisible(true);

        // Stats
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(2000);
        entity.setHealth(2000);
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(50);

        // Tag
        entity.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "is_titan"),
                org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);

        Bukkit.broadcastMessage(MessageUtils.color("&4&l¡EL TITÁN HA LLEGADO! ¡UNÍOS O PERECED!"));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "is_titan"),
                org.bukkit.persistence.PersistentDataType.BYTE)) {
            event.getDrops().clear();
            event.getDrops().add(createTranscendenceShard());
            Bukkit.broadcastMessage(MessageUtils.color("&6&l¡EL TITÁN HA SIDO DERROTADO!"));
        }
    }

    public static ItemStack createTranscendenceShard() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&d&lFragmento de Trascendencia"));
        List<String> lore = new ArrayList<>();
        lore.add(org.bukkit.ChatColor.GRAY + "Poder puro más allá del bien y el mal.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
