package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecallAbility extends ActiveAbility {

    // Stored positions and health per player
    private static final Map<UUID, SavedState> savedStates = new HashMap<>();

    private static class SavedState {
        Location location;
        double health;
        int foodLevel;
        long timestamp;

        SavedState(Location loc, double hp, int food) {
            this.location = loc.clone();
            this.health = hp;
            this.foodLevel = food;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public RecallAbility() {
        super("recall", "Recall",
                "Click Derecho: Regresa a tu posición guardada.", 60000); // 60s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        SavedState state = savedStates.get(player.getUniqueId());

        if (state == null) {
            player.sendMessage(ChatColor.RED + "¡No hay posición guardada! Espera unos segundos.");
            return;
        }

        // Time travel particles at origin
        Location origin = player.getLocation();
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, origin, 50, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.END_ROD, origin, 30, 0.3, 0.5, 0.3, 0.15);
        player.playSound(origin, Sound.BLOCK_PORTAL_TRIGGER, 1, 1.5f);

        // Restore position
        player.teleport(state.location);

        // Restore health if enabled
        if (ConfigManager.getBoolean("abilities.recall.restore-health")) {
            double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(state.health, maxHealth));
        }

        // Restore food if enabled
        if (ConfigManager.getBoolean("abilities.recall.restore-food")) {
            player.setFoodLevel(state.foodLevel);
        }

        // Time travel particles at destination
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, state.location, 50, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.END_ROD, state.location, 30, 0.3, 0.5, 0.3, 0.15);
        player.playSound(state.location, Sound.BLOCK_PORTAL_TRIGGER, 1, 1.2f);
        player.playSound(state.location, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 2.0f);

        // Notification
        long secondsAgo = (System.currentTimeMillis() - state.timestamp) / 1000;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.LIGHT_PURPLE + "⧗ Regresaste " + secondsAgo + "s atrás ⧗"));
    }

    // Static method to start auto-save task for a player
    public static int startAutoSave(Player player) {
        int saveInterval = ConfigManager.getInt("abilities.recall.save-interval");

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                // Save current state
                SavedState state = new SavedState(
                        player.getLocation(),
                        player.getHealth(),
                        player.getFoodLevel());
                savedStates.put(player.getUniqueId(), state);

                // Subtle visual feedback
                player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE,
                        player.getLocation().add(0, 2, 0), 3, 0.1, 0.1, 0.1, 0.01);
            }
        };

        return task.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(),
                saveInterval, saveInterval).getTaskId();
    }

    @Override
    public boolean isCompatible(Material type) {
        // Works with any tool type - time manipulation is universal
        return true;
    }
}
