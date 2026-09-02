package com.livingtools.abilities;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ActiveAbility extends Ability {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;

    public ActiveAbility(String id, String name, String description, long cooldownMillis) {
        super(id, name, description, 1); // Default required level 1 for active abilities
        this.cooldownMillis = cooldownMillis;
    }

    public abstract void onRightClick(Player player, com.livingtools.data.LivingTool tool);

    protected boolean checkCooldown(Player player) {
        long duration = cooldownMillis;
        // Check config override
        int configSeconds = com.livingtools.manager.ConfigManager
                .getInt("abilities." + getId() + ".cooldown");
        if (configSeconds > 0) {
            duration = configSeconds * 1000L;
        }

        if (cooldowns.containsKey(player.getUniqueId())) {
            long lastUse = cooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() - lastUse < duration) {
                long remaining = (duration - (System.currentTimeMillis() - lastUse)) / 1000;
                player.sendMessage(org.bukkit.ChatColor.RED + "Habilidad en enfriamiento: " + remaining + "s");
                return false;
            }
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }
}
