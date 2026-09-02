package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ElementalStormManager {

    private static final Random random = new Random();
    private static StormType currentStorm = StormType.NONE;

    public enum StormType {
        NONE("Ninguna", ""),
        INFERNO("Tormenta Infernal", "&c¡El calor se intensifica! (Daño de Fuego +50%)"),
        GLACIAL("Ventisca Glacial", "&b¡El frío congela hasta los huesos! (Ralentización +2s)"),
        THUNDER("Tormenta Eléctrica", "&e¡El aire crepita con energía! (Probabilidad de Rayo +10%)");

        private final String name;
        private final String message;

        StormType(String name, String message) {
            this.name = name;
            this.message = message;
        }

        public String getName() {
            return name;
        }

        public String getMessage() {
            return message;
        }
    }

    public static void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 10% chance to change weather every 5 minutes (6000 ticks)
                if (random.nextDouble() < 0.10) {
                    changeWeather();
                } else if (currentStorm != StormType.NONE) {
                    // 20% chance to end storm
                    if (random.nextDouble() < 0.20) {
                        endStorm();
                    }
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 6000, 6000);
    }

    private static void changeWeather() {
        StormType[] storms = StormType.values();
        StormType newStorm = storms[random.nextInt(storms.length)];

        if (newStorm == StormType.NONE || newStorm == currentStorm)
            return;

        currentStorm = newStorm;
        broadcastStorm();
    }

    private static void endStorm() {
        currentStorm = StormType.NONE;
        Bukkit.broadcastMessage(MessageUtils.color("&7La tormenta elemental ha cesado."));
    }

    private static void broadcastStorm() {
        Bukkit.broadcastMessage(MessageUtils.color("&8[&6Evento&8] " + currentStorm.getMessage()));

        // Visual effects could be added here
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), org.bukkit.Sound.AMBIENT_CAVE, 1, 0.5f);
        }
    }

    public static StormType getCurrentStorm() {
        return currentStorm;
    }
}
