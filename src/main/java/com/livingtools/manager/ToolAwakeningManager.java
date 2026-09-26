package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * ToolAwakeningManager — Habilidad Suprema (Despertar) de herramientas vivientes.
 *
 * Requiere: Nivel ≥ 100 o Prestigio ≥ 1.
 * Activación: Shift + Click Derecho con la herramienta en mano.
 * Cooldown: 90 segundos.
 *
 * Efectos según la personalidad de la herramienta:
 *  - AGGRESSIVE ("Furia Desatada"):
 *      Fuerza I (12s), Velocidad II (12s), Partículas de llama/explosión, sonido de rugido.
 *  - WISE ("Clarividencia Ancestral"):
 *      Prisa Minera II (15s), Visión Nocturna (15s), +50% XP durante el efecto, partículas mágicas.
 *  - LAZY ("Eficiencia Absoluta"):
 *      Resistencia II (12s), Saturación I, reparación instantánea parcial, partículas verdes.
 *  - CHEERFUL ("Celebración Radiante"):
 *      Regeneración II (12s), Absorción II (12s), velocidad a aliados cercanos, fuegos artificiales.
 */
public class ToolAwakeningManager implements Listener {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 90_000L; // 90 segundos

    // Duración de los efectos en ticks (20 ticks = 1 segundo)
    private static final int DURATION_TICKS = 20 * 12; // 12 segundos

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;

        LivingTool tool = new LivingTool(item);
        ToolData data = tool.getData();

        // Requisito: Nivel 100+ o Prestigio 1+
        if (data.getLevel() < 100 && data.getPrestige() < 1) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(uuid, 0L);
        long elapsed = now - lastUse;

        long cooldownDuration = COOLDOWN_MS;
        int quickLvl = TalentTreeManager.getTalentLevel(tool, TalentTreeManager.Talent.QUICK_AWAKENING);
        if (quickLvl == 1) cooldownDuration = 75_000L;
        else if (quickLvl >= 2) cooldownDuration = 60_000L;

        if (elapsed < cooldownDuration) {
            long remainingSec = (cooldownDuration - elapsed) / 1000L;
            MessageUtils.sendActionBar(player,
                    ChatColor.RED + "⏳ Despertar en cooldown: " + ChatColor.YELLOW + remainingSec + "s");
            return;
        }

        // Activar despertar
        cooldowns.put(uuid, now);
        activateAwakening(player, tool);
        event.setCancelled(true);
    }

    private static void activateAwakening(Player player, LivingTool tool) {
        ToolData data = tool.getData();
        String personality = data.getPersonality() != null ? data.getPersonality() : "WISE";
        Location loc = player.getLocation();
        World world = player.getWorld();

        String toolName = (tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName())
                ? tool.getItem().getItemMeta().getDisplayName() : "Tu herramienta";

        switch (personality) {
            case "AGGRESSIVE":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, DURATION_TICKS, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION_TICKS, 1));
                world.spawnParticle(Particle.FLAME, loc.add(0, 1, 0), 60, 0.8, 0.8, 0.8, 0.15);
                world.spawnParticle(Particle.LAVA, loc, 20, 0.5, 0.5, 0.5, 0.05);
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.2f);
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "⚡ " + ChatColor.BOLD + "¡DESPERTAR: FURIA DESATADA!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.RED + "¡DESTRUYE A TODOS!" + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;

            case "WISE":
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 15, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 15, 0));
                world.spawnParticle(Particle.ENCHANTMENT_TABLE, loc.add(0, 1, 0), 80, 1, 1, 1, 0.5);
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
                    dam.setDamage(Math.max(0, dam.getDamage() - 200));
                    tool.getItem().setItemMeta((org.bukkit.inventory.meta.ItemMeta) dam);
                }
                world.spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 1, 0), 40, 0.8, 0.8, 0.8, 0.1);
                world.playSound(loc, Sound.BLOCK_ANVIL_USE, 0.6f, 1.4f);
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "💤 " + ChatColor.BOLD + "¡DESPERTAR: EFICIENCIA ABSOLUTA!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.GREEN + "Listo, arreglado. Ahora a no esforzarse." + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;

            default: // CHEERFUL
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, DURATION_TICKS, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1));
                world.spawnParticle(Particle.TOTEM, loc.add(0, 1.5, 0), 50, 0.8, 1.0, 0.8, 0.2);
                world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡DESPERTAR: CELEBRACIÓN RADIANTE!");
                player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.YELLOW + "¡SÍ! ¡Somos invencibles juntos!" + ChatColor.GRAY + "\"");
                player.sendMessage("");
                break;
        }

        MessageUtils.sendActionBar(player, ChatColor.GOLD + "✦ ¡DESPERTAR ACTIVADO! ✦");
    }

    public static long getCooldownRemainingSeconds(Player player) {
        long elapsed = System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L);
        return Math.max(0, (COOLDOWN_MS - elapsed) / 1000L);
    }

    public static void cleanup(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
