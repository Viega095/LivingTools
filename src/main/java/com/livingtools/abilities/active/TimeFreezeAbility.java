package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TimeFreezeAbility extends ActiveAbility {

    public TimeFreezeAbility() {
        super("timefreeze", "Time Freeze",
                "Click Derecho: Congela el tiempo para todos los enemigos cercanos.", 60000); // 60s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        double radius = 10.0;
        int duration = 60; // 3 seconds

        // Find all entities to freeze
        List<LivingEntity> frozenEntities = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                frozenEntities.add((LivingEntity) entity);
            }
        }

        if (frozenEntities.isEmpty()) {
            player.sendMessage(ChatColor.RED + "¡No hay enemigos cerca para congelar!");
            return;
        }

        // Activation effects
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1, 2.0f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.AQUA + "⧖ TIEMPO CONGELADO ⧖"));

        // Freeze effect
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    // Unfreeze
                    for (LivingEntity entity : frozenEntities) {
                        if (entity.isValid()) {
                            entity.setAI(true);
                            entity.setInvulnerable(false);
                            entity.setGlowing(false);
                        }
                    }

                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 2.0f);
                    this.cancel();
                    return;
                }

                // Apply freeze each tick
                for (LivingEntity entity : frozenEntities) {
                    if (!entity.isValid())
                        continue;

                    // Freeze in place
                    entity.setAI(false);
                    entity.setVelocity(new Vector(0, 0, 0));
                    entity.setGlowing(true);
                    entity.setInvulnerable(true); // Can't be damaged while frozen

                    // Frozen particles
                    if (ticks % 5 == 0) {
                        entity.getWorld().spawnParticle(Particle.SNOWFLAKE,
                                entity.getLocation().add(0, entity.getHeight() / 2, 0),
                                10, 0.3, 0.5, 0.3, 0);
                        entity.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,
                                entity.getLocation().add(0, entity.getHeight() / 2, 0),
                                3, 0.2, 0.3, 0.2, 0);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);

        // Success message
        player.sendMessage(ChatColor.AQUA + "⧖ " + ChatColor.YELLOW +
                "¡" + frozenEntities.size() + " enemigos congelados en el tiempo!");
    }

    @Override
    public boolean isCompatible(Material type) {
        return true; // Universal time magic
    }
}
