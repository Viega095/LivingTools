package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShadowStepAbility extends ActiveAbility {

    public ShadowStepAbility() {
        super("shadowstep", "Shadow Step",
                "Click Derecho: Teletransporte con invisibilidad temporal.", 20000); // 20s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();
        double maxRange = ConfigManager.getDouble("abilities.shadowstep.max-range");

        // Find valid destination
        Location target = findValidDestination(start, direction, maxRange);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "¡Destino no válido!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
            return;
        }

        // Create shadow particles at origin
        int particleDensity = ConfigManager.getInt("abilities.shadowstep.particle-density");
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, start, particleDensity, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.SQUID_INK, start, particleDensity / 2, 0.5, 1, 0.5, 0.05);

        // Teleport
        player.teleport(target.setDirection(player.getLocation().getDirection()));

        // Create shadow particles at destination
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, target, particleDensity, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.SQUID_INK, target, particleDensity / 2, 0.5, 1, 0.5, 0.05);

        // Apply invisibility
        int invisDuration = ConfigManager.getInt("abilities.shadowstep.invisibility-duration");
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, invisDuration, 0, false, false, true));

        // Sound effects
        player.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 0.5f);
        player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 0.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 0.5f, 1.5f);

        // Action bar notification
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Modo Sigilo Activado"
                        + ChatColor.DARK_GRAY + " ✦"));
    }

    private Location findValidDestination(Location start, Vector direction, double maxRange) {
        Location target = start.clone();
        Location lastValid = start.clone();

        for (double i = 0; i < maxRange; i += 0.5) {
            Location next = target.clone().add(direction.clone().multiply(0.5));

            if (next.getBlock().getType().isSolid()) {
                break;
            }

            if (isValidDestination(next)) {
                lastValid = next.clone();
                target = next;
            } else {
                break;
            }
        }

        if (isValidDestination(lastValid) && lastValid.distance(start) > 1.0) {
            return lastValid;
        }

        return null;
    }

    private boolean isValidDestination(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();

        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }

        // Check ground
        boolean hasGround = false;
        for (int i = 0; i < 4; i++) {
            Block below = loc.clone().subtract(0, i, 0).getBlock();
            if (below.getType().isSolid() && below.getType() != Material.LAVA) {
                hasGround = true;
                break;
            }
        }

        if (!hasGround) {
            return false;
        }

        // Check for dangerous blocks
        Material feetType = feet.getType();
        if (feetType == Material.LAVA || feetType == Material.FIRE ||
                feetType == Material.SOUL_FIRE || feetType == Material.CACTUS) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isCompatible(Material type) {
        // Compatible with swords and daggers (if you add dagger items)
        return type.name().endsWith("_SWORD");
    }
}
