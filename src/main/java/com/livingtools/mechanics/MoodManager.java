package com.livingtools.mechanics;

import com.livingtools.data.LivingTool;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MoodManager {

    public static void applyMoodEffects(Player player, LivingTool tool) {
        LivingTool.Mood mood = tool.getMood();

        switch (mood) {
            case HAPPY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 0, false, false));
                break;
            case SAD:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, false));
                break;
            case FURIOUS:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false));
                break;
            case NEUTRAL:
            default:
                break;
        }
    }

    public static double getDamageModifier(LivingTool tool) {
        if (tool.getMood() == LivingTool.Mood.FURIOUS) {
            return 1.10; // +10% Damage
        }
        return 1.0;
    }

    public static int getDurabilityCostModifier(LivingTool tool) {
        if (tool.getMood() == LivingTool.Mood.FURIOUS) {
            return 2; // Double durability cost
        }
        return 1;
    }
}
