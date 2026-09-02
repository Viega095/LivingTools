package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class SwapAbility extends ActiveAbility {

    public SwapAbility() {
        super("swap", "Swap",
                "Click Derecho mirando a un objetivo: Intercambia posiciones.", 25000); // 25s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        double maxRange = ConfigManager.getDouble("abilities.swap.max-range");

        // Raycast to find target
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                maxRange,
                entity -> entity instanceof LivingEntity && entity != player);

        if (result == null || result.getHitEntity() == null) {
            player.sendMessage(ChatColor.RED + "¡No hay objetivo válido!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        Entity target = result.getHitEntity();

        // Check permissions
        if (target instanceof Player) {
            if (!ConfigManager.getBoolean("abilities.swap.works-on-players")) {
                player.sendMessage(ChatColor.RED + "¡No puedes intercambiar con jugadores!");
                return;
            }
        } else if (!ConfigManager.getBoolean("abilities.swap.works-on-mobs")) {
            player.sendMessage(ChatColor.RED + "¡No puedes intercambiar con criaturas!");
            return;
        }

        Location playerLoc = player.getLocation().clone();
        Location targetLoc = target.getLocation().clone();

        // Spatial particles at both locations
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, playerLoc, 40, 0.5, 1, 0.5, 0.15);
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 40, 0.5, 1, 0.5, 0.15);
        player.getWorld().spawnParticle(Particle.FLASH, playerLoc, 1);
        player.getWorld().spawnParticle(Particle.FLASH, targetLoc, 1);

        // Sound effects
        player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.8f);
        player.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.2f);
        player.playSound(playerLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2.0f);

        // Perform swap
        player.teleport(targetLoc.setDirection(player.getLocation().getDirection()));
        target.teleport(playerLoc.setDirection(target.getLocation().getDirection()));

        // Notification
        String targetName = target instanceof Player ? ((Player) target).getName() : target.getType().name();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.AQUA + "↔ Intercambiado con " + targetName + " ↔"));

        if (target instanceof Player) {
            ((Player) target).spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.YELLOW + "↔ " + player.getName() + " usó Swap contigo ↔"));
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        // Works with swords and bows for tactical gameplay
        return type.name().endsWith("_SWORD") || type.name().endsWith("_BOW");
    }
}
