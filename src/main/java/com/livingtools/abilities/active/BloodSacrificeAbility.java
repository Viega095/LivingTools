package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodSacrificeAbility extends ActiveAbility {

    private static final Map<UUID, Long> activeBuffs = new HashMap<>();

    public BloodSacrificeAbility() {
        super("bloodsacrifice", "Blood Sacrifice",
                "Click Derecho: Sacrifica salud por poder devastador.", 30000); // 30s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

        // Check minimum health requirement
        if (currentHealth < maxHealth * 0.4) {
            player.sendMessage(ChatColor.RED + "¡Salud insuficiente! (mínimo 40%)");
            return;
        }

        // Sacrifice 50% current health
        double sacrifice = currentHealth * 0.5;
        player.setHealth(Math.max(1.0, currentHealth - sacrifice));

        // Grant massive buffs
        int duration = 200; // 10 seconds

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 1, false, true, true)); // Strength
                                                                                                                    // II
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, true, true)); // Speed II
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration, 1, false, true, true)); // Jump Boost
                                                                                                         // II
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 0, false, true, true)); // Resistance
                                                                                                                      // I

        // Track active buff for damage modifier
        activeBuffs.put(player.getUniqueId(), System.currentTimeMillis() + (duration * 50));

        // Visual effects
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0),
                100, 0.5, 1, 0.5, 1,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f)); // Dark red
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.05);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 2.0f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.DARK_RED + "⚔ SANGRE POR PODER ⚔"));

        player.sendMessage(ChatColor.DARK_RED + "⚔ " + ChatColor.RED +
                "¡Tu sangre alimenta tu furia! (+100% daño por 10s)");

        // Blood trail effect
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    player.sendMessage(ChatColor.GRAY + "El poder de la sangre se desvanece...");
                    this.cancel();
                    return;
                }

                // Blood trail
                player.getWorld().spawnParticle(Particle.REDSTONE,
                        player.getLocation(),
                        3, 0.2, 0.1, 0.2, 0,
                        new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.0f));

                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
    }

    /**
     * Check if player has blood sacrifice buff active
     */
    public static boolean hasBloodSacrificeBuff(Player player) {
        Long expiry = activeBuffs.get(player.getUniqueId());
        if (expiry != null && System.currentTimeMillis() < expiry) {
            return true;
        }
        activeBuffs.remove(player.getUniqueId());
        return false;
    }

    /**
     * Get damage multiplier (called from damage listener)
     */
    public static double getDamageMultiplier(Player player) {
        return hasBloodSacrificeBuff(player) ? 2.0 : 1.0; // +100% damage (2x total)
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
