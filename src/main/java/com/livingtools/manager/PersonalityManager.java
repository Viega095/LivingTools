package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.mechanics.Personality;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

public class PersonalityManager {

    private static final Random random = new Random();

    public enum EventType {
        LEVEL_UP,
        MINING,
        KILL_MOB,
        LOW_DURABILITY,
        AWAKENING
    }

    public static void assignRandomPersonality(LivingTool tool) {
        com.livingtools.mechanics.Personality[] personalities = com.livingtools.mechanics.Personality
                .values();
        String newPersonality = personalities[random.nextInt(personalities.length)].name();
        tool.getData().setPersonality(newPersonality);
    }

    public static void sayLine(Player player, LivingTool tool, EventType event) {
        String personalityName = tool.getData().getPersonality();
        if (personalityName == null) {
            return;
        }

        Personality personality;
        try {
            personality = Personality.valueOf(personalityName);
        } catch (IllegalArgumentException e) {
            return;
        }

        String message = getMessage(personality, event);
        if (message == null) {
            return;
        }

        if (isChatterEvent(event) && !DialogueCooldown.tryToolChat(player)) {
            return;
        }

        player.sendMessage(formatMessage(tool, message));
        playSound(player, event);
    }

    private static boolean isChatterEvent(EventType event) {
        return event == EventType.MINING || event == EventType.KILL_MOB || event == EventType.LOW_DURABILITY;
    }

    private static String getMessage(Personality personality, EventType event) {
        switch (event) {
            case MINING:
                if (random.nextInt(100) >= 2) {
                    return null;
                }
                break;
            case KILL_MOB:
                if (random.nextInt(100) >= 5) {
                    return null;
                }
                break;
            case LOW_DURABILITY:
                if (random.nextInt(100) >= 10) {
                    return null;
                }
                break;
            default:
                break;
        }
        return DialogueRepository.getRandomMessage(personality, event);
    }

    private static String formatMessage(LivingTool tool, String message) {
        String name = tool.getData().getCustomName();
        if (name.isEmpty())
            name = "Herramienta";
        return ChatColor.GOLD + "[" + name + "]: " + ChatColor.WHITE + message;
    }

    private static void playSound(Player player, EventType event) {
        switch (event) {
            case LEVEL_UP:
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                break;
            case AWAKENING:
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
                break;
            default:
                // Subtle sound for normal chatter
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 0.5f, 1.5f);
                break;
        }
    }
}
