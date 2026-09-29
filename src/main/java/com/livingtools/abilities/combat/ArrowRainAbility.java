package com.livingtools.abilities.combat;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ArrowRainAbility — Desata una lluvia celestial de flechas en el área objetivo al presionar Shift + Click Derecho.
 */
public class ArrowRainAbility extends Ability {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 25000L; // 25s

    public ArrowRainAbility() {
        super("arrow_rain", "Lluvia de Flechas", "Invoca una andanada de flechas celestiales en el área apuntada.", 50);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(uuid, 0L);
        if (now - last < COOLDOWN_MS) {
            long remaining = (COOLDOWN_MS - (now - last)) / 1000;
            player.sendMessage(ChatColor.RED + "⏳ Lluvia de Flechas en enfriamiento: " + remaining + "s");
            return;
        }

        cooldowns.put(uuid, now);

        Location targetLoc = player.getTargetBlockExact(30) != null
                ? player.getTargetBlockExact(30).getLocation().add(0.5, 1, 0.5)
                : player.getLocation().add(player.getLocation().getDirection().multiply(15));

        player.sendMessage(ChatColor.GOLD + "🏹 ¡Lluvia de Flechas desatada sobre el objetivo!");
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.2f, 0.5f);

        // Indicador de área
        ParticleOptimizer.spawnCircle(targetLoc, 4.0, Particle.ENCHANTMENT_TABLE, 24, null);

        new BukkitRunnable() {
            int wave = 0;

            @Override
            public void run() {
                if (wave >= 4) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 4; i++) {
                    double ox = (Math.random() - 0.5) * 6.0;
                    double oz = (Math.random() - 0.5) * 6.0;
                    Location spawnLoc = targetLoc.clone().add(ox, 12 + Math.random() * 3, oz);

                    Arrow arrow = targetLoc.getWorld().spawnArrow(spawnLoc, new Vector(0, -1.8, 0), 2.0f, 6.0f);
                    arrow.setShooter(player);
                    arrow.setDamage(8.0);
                    arrow.setCritical(true);
                }

                targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ARROW_SHOOT, 1f, 1.5f);
                wave++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 5L, 6L);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }
}
