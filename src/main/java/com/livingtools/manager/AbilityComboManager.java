package com.livingtools.manager;

import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

/**
 * Manages ability combo detection and effects.
 * Tracks recent ability usage and triggers combo bonuses.
 */
public class AbilityComboManager {

    // Track last 3 abilities used per player with timestamps
    private static final Map<UUID, LinkedList<AbilityUse>> abilityHistory = new HashMap<>();

    // Combo definitions
    private static class ComboDefinition {
        String[] sequence;
        long timeWindow; // milliseconds
        ComboEffect effect;

        ComboDefinition(String[] seq, long window, ComboEffect eff) {
            this.sequence = seq;
            this.timeWindow = window;
            this.effect = eff;
        }
    }

    private static class AbilityUse {
        String abilityId;
        long timestamp;

        AbilityUse(String id) {
            this.abilityId = id;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public enum ComboEffect {
        LIGHTNING_STRIKE, // Dash → Blink → Attack (+50% damage)
        CHAIN_BLINK, // Blink → BlinkStrike (50% cooldown reduction)
        ASSASSINATE, // ShadowStep → Backstab (guaranteed crit)
        ELEMENTAL_FURY, // Fire → Ice → Lightning (massive AoE)
        VOID_WALKER // Recall → Swap → Blink (untargetable 2s)
    }

    // Combo registry
    private static final List<ComboDefinition> COMBOS = Arrays.asList(
            new ComboDefinition(new String[] { "dash", "blink", "attack" }, 3000, ComboEffect.LIGHTNING_STRIKE),
            new ComboDefinition(new String[] { "blink", "blinkstrike" }, 2000, ComboEffect.CHAIN_BLINK),
            new ComboDefinition(new String[] { "shadowstep", "backstab" }, 3000, ComboEffect.ASSASSINATE),
            new ComboDefinition(new String[] { "fire_nova", "frost_nova", "lightning" }, 5000,
                    ComboEffect.ELEMENTAL_FURY),
            new ComboDefinition(new String[] { "recall", "swap", "blink" }, 4000, ComboEffect.VOID_WALKER));

    // Active combo effects per player
    private static final Map<UUID, ComboEffect> activeEffects = new HashMap<>();
    private static final Map<UUID, Long> effectExpiry = new HashMap<>();

    /**
     * Record ability usage
     */
    public static void recordAbilityUse(Player player, String abilityId) {
        UUID uuid = player.getUniqueId();

        // Get or create history
        LinkedList<AbilityUse> history = abilityHistory.computeIfAbsent(uuid, k -> new LinkedList<>());

        // Add new use
        history.addFirst(new AbilityUse(abilityId));

        // Keep only last 5
        while (history.size() > 5) {
            history.removeLast();
        }

        // Check for combos
        checkCombos(player, history);
    }

    /**
     * Check if recent abilities form a combo
     */
    private static void checkCombos(Player player, LinkedList<AbilityUse> history) {
        if (history.size() < 2)
            return;

        long now = System.currentTimeMillis();

        for (ComboDefinition combo : COMBOS) {
            if (matchesCombo(history, combo, now)) {
                triggerCombo(player, combo);
                return; // Only one combo at a time
            }
        }
    }

    /**
     * Check if history matches combo sequence
     */
    private static boolean matchesCombo(LinkedList<AbilityUse> history, ComboDefinition combo, long now) {
        if (history.size() < combo.sequence.length)
            return false;

        // Check if sequence matches (most recent first)
        for (int i = 0; i < combo.sequence.length; i++) {
            AbilityUse use = history.get(i);

            // Check if within time window
            if (now - use.timestamp > combo.timeWindow)
                return false;

            // Check if ability matches
            if (!use.abilityId.equals(combo.sequence[combo.sequence.length - 1 - i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Trigger combo effect
     */
    private static void triggerCombo(Player player, ComboDefinition combo) {
        UUID uuid = player.getUniqueId();

        // Set active effect
        activeEffects.put(uuid, combo.effect);
        effectExpiry.put(uuid, System.currentTimeMillis() + 5000); // 5 second window

        // Visual and audio feedback
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);

        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.15);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                player.getLocation().add(0, 1, 0), 30, 0.3, 0.3, 0.3, 0.05);

        // Notification
        String comboName = getComboName(combo.effect);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.GOLD + "⚡ COMBO: " + ChatColor.YELLOW + comboName + ChatColor.GOLD + " ⚡"));

        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + "¡Combo activado: " + comboName + "!");

        // Add mastery XP for combo
        for (String abilityId : combo.sequence) {
            AbilityMasteryManager.addXP(player, abilityId, 5); // Bonus XP for combos
        }
    }

    /**
     * Get human-readable combo name
     */
    private static String getComboName(ComboEffect effect) {
        switch (effect) {
            case LIGHTNING_STRIKE:
                return "Lightning Strike";
            case CHAIN_BLINK:
                return "Chain Blink";
            case ASSASSINATE:
                return "Assassinate";
            case ELEMENTAL_FURY:
                return "Elemental Fury";
            case VOID_WALKER:
                return "Void Walker";
            default:
                return "Unknown Combo";
        }
    }

    /**
     * Check if player has active combo effect
     */
    public static boolean hasComboEffect(Player player, ComboEffect effect) {
        UUID uuid = player.getUniqueId();

        // Check expiry
        Long expiry = effectExpiry.get(uuid);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            activeEffects.remove(uuid);
            effectExpiry.remove(uuid);
            return false;
        }

        return activeEffects.get(uuid) == effect;
    }

    /**
     * Consume combo effect (one-time use)
     */
    public static void consumeEffect(Player player) {
        UUID uuid = player.getUniqueId();
        activeEffects.remove(uuid);
        effectExpiry.remove(uuid);
    }

    /**
     * Get active combo effect
     */
    public static ComboEffect getActiveEffect(Player player) {
        UUID uuid = player.getUniqueId();

        // Check expiry
        Long expiry = effectExpiry.get(uuid);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            activeEffects.remove(uuid);
            effectExpiry.remove(uuid);
            return null;
        }

        return activeEffects.get(uuid);
    }

    /**
     * Clear history for player (on disconnect/death)
     */
    public static void clearHistory(Player player) {
        abilityHistory.remove(player.getUniqueId());
        activeEffects.remove(player.getUniqueId());
        effectExpiry.remove(player.getUniqueId());
    }
}
