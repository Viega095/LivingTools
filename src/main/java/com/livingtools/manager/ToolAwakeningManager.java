package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * ToolAwakeningManager — Efectos temáticos del Despertar / Overdrive según la personalidad.
 * Activado a través de SoulEnergyManager (Shift + F o botón en el menú de la herramienta).
 */
public class ToolAwakeningManager implements Listener {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 90_000L; // 90 segundos
    private static final int DURATION_TICKS = 20 * 15; // 15 segundos

    public static void activateAwakening(Player player, LivingTool tool) {
        ToolData data = tool.getData();
        String personality = data.getPersonality() != null ? data.getPersonality().toUpperCase() : "WISE";
        Location loc = player.getLocation();
        World world = player.getWorld();

        String toolName = (tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName())
                ? tool.getItem().getItemMeta().getDisplayName() : "Tu herramienta";

        switch (personality) {
            case "AGGRESSIVE":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, DURATION_TICKS, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION_TICKS, 1));
                world.spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 60, 0.8, 0.8, 0.8, 0.15);
                world.spawnParticle(Particle.LAVA, loc, 20, 0.5, 0.5, 0.5, 0.05);
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.2f);
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "⚡ " + ChatColor.BOLD + "¡DESPERTAR: FURIA DESATADA!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.RED + "¡DESTRUYE A TODOS!" + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;

            case "WISE":
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, DURATION_TICKS, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, DURATION_TICKS, 0));
                world.spawnParticle(Particle.ENCHANTMENT_TABLE, loc.clone().add(0, 1, 0), 80, 1, 1, 1, 0.5);
                world.spawnParticle(Particle.END_ROD, loc, 30, 0.6, 0.6, 0.6, 0.05);
                world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
                player.sendMessage("");
                player.sendMessage(ChatColor.AQUA + "✧ " + ChatColor.BOLD + "¡DESPERTAR: CLARIVIDENCIA ANCESTRAL!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.AQUA + "La sabiduría antigua guía nuestras manos." + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;

            case "LAZY":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, DURATION_TICKS, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 5, 0));
                // Reparar durabilidad
                if (tool.getItem().getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                    org.bukkit.inventory.meta.Damageable dam = (org.bukkit.inventory.meta.Damageable) tool.getItem().getItemMeta();
                    dam.setDamage(Math.max(0, dam.getDamage() - 250));
                    tool.getItem().setItemMeta((org.bukkit.inventory.meta.ItemMeta) dam);
                }
                world.spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 1, 0), 40, 0.8, 0.8, 0.8, 0.1);
                world.playSound(loc, Sound.BLOCK_ANVIL_USE, 0.6f, 1.4f);
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "💤 " + ChatColor.BOLD + "¡DESPERTAR: EFICIENCIA ABSOLUTA!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.GREEN + "Listo, arreglado. Ahora a no esforzarse." + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;

            case "HEROIC":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, DURATION_TICKS, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, DURATION_TICKS, 2));
                world.spawnParticle(Particle.FLASH, loc.clone().add(0, 1, 0), 2);
                world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "⚔ " + ChatColor.BOLD + "¡DESPERTAR: CORAJE HEROICO!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.GOLD + "¡Nadie caerá bajo mi guardia!" + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;

            default: // CHEERFUL, GLUTTONOUS, ETC.
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, DURATION_TICKS, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1));
                world.spawnParticle(Particle.TOTEM, loc.clone().add(0, 1.5, 0), 50, 0.8, 1.0, 0.8, 0.2);
                world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡DESPERTAR: CELEBRACIÓN RADIANTE!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.YELLOW + "¡SÍ! ¡Somos invencibles juntos!" + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static long getCooldownRemainingSeconds(Player player) {
        long elapsed = System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L);
        return Math.max(0, (COOLDOWN_MS - elapsed) / 1000L);
    }

    public static void cleanup(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
