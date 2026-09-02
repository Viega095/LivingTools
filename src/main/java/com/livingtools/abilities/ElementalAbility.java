package com.livingtools.abilities;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ElementalAbility extends Ability {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;

    public ElementalAbility(String id, String name, String description, int requiredLevel, long cooldownMillis) {
        super(id, name, description, requiredLevel);
        this.cooldownMillis = cooldownMillis;
    }

    public void tryActivate(Player player, LivingTool tool) {
        if (isOnCooldown(player)) {
            long remaining = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.RED + "Habilidad en enfriamiento: " + remaining + "s");
            return;
        }

        if (activate(player, tool)) {
            long expiry = System.currentTimeMillis() + cooldownMillis;
            cooldowns.put(player.getUniqueId(), expiry);
            startCooldownTask(player, expiry);
        }
    }

    protected abstract boolean activate(Player player, LivingTool tool);

    protected boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player.getUniqueId())
                && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    private void startCooldownTask(Player player, long expiryTime) {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || System.currentTimeMillis() >= expiryTime) {
                    this.cancel();
                    if (player.isOnline()) {
                        com.livingtools.utils.MessageUtils.sendActionBar(player,
                                ChatColor.GREEN + getName() + " ¡Lista!");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                    }
                    return;
                }

                long remainingMillis = expiryTime - System.currentTimeMillis();
                double progress = (double) remainingMillis / cooldownMillis;
                int totalBars = 20;
                int filledBars = (int) (progress * totalBars);
                int emptyBars = totalBars - filledBars;

                StringBuilder bar = new StringBuilder();
                bar.append(ChatColor.RED);
                for (int i = 0; i < filledBars; i++)
                    bar.append("|");
                bar.append(ChatColor.GRAY);
                for (int i = 0; i < emptyBars; i++)
                    bar.append("|");

                String timeString = String.format("%.1fs", remainingMillis / 1000.0);
                com.livingtools.utils.MessageUtils.sendActionBar(player,
                        ChatColor.YELLOW + getName() + " " + bar.toString() + " " + ChatColor.WHITE + timeString);
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 2L); // Update every 0.1s
    }
}
