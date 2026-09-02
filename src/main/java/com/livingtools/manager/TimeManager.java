package com.livingtools.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TimeManager {

    private static final Map<UUID, LinkedList<Location>> playerHistory = new HashMap<>();
    private static final int HISTORY_SECONDS = 5;

    public static void init(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                recordLocation(player);
            }
        }, 20L, 20L); // Run every second
    }

    private static void recordLocation(Player player) {
        playerHistory.putIfAbsent(player.getUniqueId(), new LinkedList<>());
        LinkedList<Location> history = playerHistory.get(player.getUniqueId());

        history.add(player.getLocation());
        if (history.size() > HISTORY_SECONDS) {
            history.removeFirst();
        }
    }

    public static Location getPastLocation(Player player) {
        if (!playerHistory.containsKey(player.getUniqueId()))
            return player.getLocation();
        LinkedList<Location> history = playerHistory.get(player.getUniqueId());
        if (history.isEmpty())
            return player.getLocation();
        return history.getFirst(); // Oldest location (5 seconds ago)
    }

    // Chronomancy Methods (Phase 56)
    private static final Set<UUID> frozenEntities = new HashSet<>();

    public static void skipTime(org.bukkit.World world, long ticks) {
        long current = world.getTime();
        world.setTime(current + ticks);
        world.playSound(world.getSpawnLocation(), org.bukkit.Sound.BLOCK_BEACON_POWER_SELECT, 10, 0.5f);
    }

    public static void freezeEntity(org.bukkit.entity.LivingEntity entity, int durationTicks) {
        if (frozenEntities.contains(entity.getUniqueId()))
            return;

        frozenEntities.add(entity.getUniqueId());

        // Visuals
        entity.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, entity.getLocation().add(0, 1, 0), 20, 0.5, 1,
                0.5, 0.01);
        entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 1, 2.0f);

        // Effect (Slowness 255 + Jump Boost 128)
        entity.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW,
                durationTicks, 255, false, false, true));
        entity.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.JUMP,
                durationTicks, 128, false, false, true));

        // Disable AI
        boolean wasAI = entity.hasAI();
        entity.setAI(false);

        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (entity.isValid()) {
                    entity.setAI(wasAI);
                    entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
                }
                frozenEntities.remove(entity.getUniqueId());
            }
        }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), durationTicks);
    }

    public static boolean isFrozen(org.bukkit.entity.Entity entity) {
        return frozenEntities.contains(entity.getUniqueId());
    }
}
