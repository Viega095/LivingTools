package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager implements Listener {

    private static final Map<UUID, BossBar> activeBars = new HashMap<>();
    private static final Map<UUID, LivingEntity> activeBosses = new HashMap<>();

    public static void addBoss(LivingEntity boss, String name, BarColor color) {
        BossBar bar = Bukkit.createBossBar(MessageUtils.color(name), color, BarStyle.SOLID);
        activeBars.put(boss.getUniqueId(), bar);
        activeBosses.put(boss.getUniqueId(), boss);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || boss.isDead()) {
                    removeBoss(boss);
                    this.cancel();
                    return;
                }

                // Update Progress
                double progress = boss.getHealth()
                        / boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

                // Update Players
                for (Player player : boss.getWorld().getPlayers()) {
                    if (player.getLocation().distance(boss.getLocation()) < 50) {
                        bar.addPlayer(player);
                    } else {
                        bar.removePlayer(player);
                    }
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 20);
    }

    public static void removeBoss(LivingEntity boss) {
        BossBar bar = activeBars.remove(boss.getUniqueId());
        activeBosses.remove(boss.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (activeBars.containsKey(event.getEntity().getUniqueId())) {
            removeBoss(event.getEntity());
        }
    }
}
