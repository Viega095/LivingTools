package com.livingtools.mechanics;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CorruptionSystem {

    public static final NamespacedKey KEY_CORRUPTION = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "player_corruption");
    private static final Random random = new Random();

    public static void increaseCorruption(Player player, int amount) {
        int current = getCorruption(player);
        setCorruption(player, Math.min(100, current + amount));
        player.sendMessage(MessageUtils.color("&5La corrupción crece en ti... (" + getCorruption(player) + "%)"));
    }

    public static void decreaseCorruption(Player player, int amount) {
        int current = getCorruption(player);
        setCorruption(player, Math.max(0, current - amount));
    }

    public static int getCorruption(Player player) {
        return player.getPersistentDataContainer().getOrDefault(KEY_CORRUPTION, PersistentDataType.INTEGER, 0);
    }

    public static void setCorruption(Player player, int amount) {
        player.getPersistentDataContainer().set(KEY_CORRUPTION, PersistentDataType.INTEGER, amount);
    }

    public static void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int corruption = getCorruption(player);
                    if (corruption > 0) {
                        applyEffects(player, corruption);
                    }
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 200, 200); // Every 10 seconds
    }

    private static void applyEffects(Player player, int corruption) {
        // Positive Effects (Power at a cost)
        if (corruption >= 50) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0, true, false));
        }
        if (corruption >= 80) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1, true, false));
        }

        // Negative Effects (The cost)
        if (corruption >= 20) {
            // 5% chance for hallucination sound
            if (random.nextDouble() < 0.05) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_STARE, 1, 0.5f);
            }
        }
        if (corruption >= 60) {
            // 5% chance for blindness
            if (random.nextDouble() < 0.05) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true, false));
                player.sendMessage(MessageUtils.color("&8La oscuridad te consume..."));
            }
        }
        if (corruption >= 90) {
            // 2% chance for damage
            if (random.nextDouble() < 0.02) {
                player.damage(2.0);
                player.sendMessage(MessageUtils.color("&5¡El Vacío reclama tu carne!"));
            }
        }
    }
}
