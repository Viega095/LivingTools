package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhaseShiftAbility extends ActiveAbility {

    private static final Map<UUID, Integer> phaseTasks = new HashMap<>();

    public PhaseShiftAbility() {
        super("phaseshift", "Phase Shift",
                "Click Derecho: Vuélvete incorpóreo y atraviesa paredes.", 45000); // 45s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        if (phaseTasks.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "¡Ya estás en Phase Shift!");
            return;
        }

        int duration = 60; // 3 seconds

        // Visual activation
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);

        // Apply effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 4, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 1, false, false, false));

        // Glowing effect
        player.setGlowing(true);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.LIGHT_PURPLE + "◈ Phase Shift - Incorpóreo ◈"));

        // Phase task
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= duration) {
                    // End phase
                    player.setGlowing(false);
                    player.getWorld().spawnParticle(Particle.SOUL, player.getLocation(), 30, 0.5, 1, 0.5, 0.1);
                    player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 0.5f);

                    phaseTasks.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                // Spectral particles
                player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0),
                        5, 0.3, 0.5, 0.3, 0.01);

                // Allow walking through walls (noclip simulation)
                // This is client-side only in vanilla, so we use teleportation tricks
                if (player.getLocation().getBlock().getType().isSolid()) {
                    // Move player forward slightly if stuck in wall
                    player.setVelocity(player.getLocation().getDirection().multiply(0.2));
                }

                ticks++;
            }
        };

        int taskId = task.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1).getTaskId();
        phaseTasks.put(player.getUniqueId(), taskId);
    }

    /**
     * Cancel phase shift early (if player attacks or takes damage)
     */
    public static void cancelPhaseShift(Player player) {
        Integer taskId = phaseTasks.remove(player.getUniqueId());
        if (taskId != null) {
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            player.setGlowing(false);
            player.sendMessage(ChatColor.YELLOW + "Phase Shift cancelado.");
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        // Works with all tools - dimensional magic is universal
        return true;
    }
}
