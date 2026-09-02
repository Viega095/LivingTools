package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TrialManager {

    public enum TrialType {
        KILL_WITHER(1),
        MINE_OBSIDIAN(64),
        BLOCK_BREAK(500),
        KILL_ZOMBIES(50),
        TAKE_DAMAGE(100), // For Armor
        NONE(0);

        private final int targetAmount;

        TrialType(int targetAmount) {
            this.targetAmount = targetAmount;
        }

        public int getTargetAmount() {
            return targetAmount;
        }
    }

    public static void checkTrialStart(Player player, LivingTool tool) {
        int level = tool.getData().getLevel();
        if ((level == 30 || level == 50 || level == 75) && getCurrentTrial(tool) == TrialType.NONE) {
            assignTrial(player, tool, level);
        }
    }

    private static void assignTrial(Player player, LivingTool tool, int level) {
        TrialType trial = TrialType.NONE;
        String type = tool.getItem().getType().toString();

        if (level == 30) {
            if (type.contains("_PICKAXE")) {
                trial = TrialType.MINE_OBSIDIAN;
            } else if (type.contains("_SHOVEL") || type.contains("_AXE")) {
                trial = TrialType.BLOCK_BREAK;
            } else if (type.contains("_SWORD")) {
                trial = TrialType.KILL_ZOMBIES;
            } else if (type.contains("_CHESTPLATE")) {
                trial = TrialType.TAKE_DAMAGE;
            }
        } else if (level == 50) {
            // Level 50 Trials
            if (type.contains("_PICKAXE")) {
                trial = TrialType.MINE_OBSIDIAN; // Placeholder for harder mining trial
            } else if (type.contains("_SHOVEL") || type.contains("_AXE")) {
                trial = TrialType.BLOCK_BREAK;
            } else {
                trial = TrialType.KILL_WITHER;
            }
        } else if (level == 75) {
            trial = TrialType.KILL_WITHER;
        }

        if (trial != TrialType.NONE) {
            tool.getData().setTrial(trial.name());
            tool.getData().setTrialProgress(0);
            player.sendMessage(ChatColor.RED + "¡Has alcanzado un límite de poder!");
            player.sendMessage(
                    ChatColor.GOLD + "Para ascender, debes completar una prueba: " + getTrialDescription(trial));
            tool.updateLore();
        }
    }

    public static String getTrialDescription(TrialType trial) {
        switch (trial) {
            case KILL_ZOMBIES:
                return "Mata 50 Zombies con esta herramienta.";
            case MINE_OBSIDIAN:
                return "Mina 64 bloques de Obsidiana.";
            case BLOCK_BREAK:
                return "Rompe 500 bloques.";
            case KILL_WITHER:
                return "Mata al Wither.";
            case TAKE_DAMAGE:
                return "Recibe 100 puntos de daño.";
            default:
                return "";
        }
    }

    public static TrialType getCurrentTrial(LivingTool tool) {
        String trialName = tool.getData().getTrial();
        if (trialName == null || trialName.isEmpty())
            return TrialType.NONE;
        try {
            return TrialType.valueOf(trialName);
        } catch (IllegalArgumentException e) {
            return TrialType.NONE;
        }
    }

    public static void onKill(Player player, LivingTool tool, EntityType type) {
        TrialType current = getCurrentTrial(tool);
        if (current == TrialType.KILL_ZOMBIES) {
            if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED
                    || type == EntityType.ZOMBIE_VILLAGER) {
                incrementProgress(player, tool, current.getTargetAmount());
            }
        } else if (current == TrialType.KILL_WITHER && type == EntityType.WITHER) {
            incrementProgress(player, tool, current.getTargetAmount());
        }
    }

    public static void onMine(Player player, LivingTool tool, Material material) {
        TrialType current = getCurrentTrial(tool);
        if (current == TrialType.MINE_OBSIDIAN && material == Material.OBSIDIAN) {
            incrementProgress(player, tool, current.getTargetAmount());
        } else if (current == TrialType.BLOCK_BREAK) {
            incrementProgress(player, tool, current.getTargetAmount());
        }
    }

    public static void onDamageTaken(Player player, LivingTool tool, double damage) {
        TrialType current = getCurrentTrial(tool);
        if (current == TrialType.TAKE_DAMAGE) {
            incrementProgress(player, tool, current.getTargetAmount(), (int) damage);
        }
    }

    public static void onDamageTaken(Player player, com.livingtools.data.LivingArmor armor, double damage) {
        TrialType current = getCurrentTrial(armor);
        if (current == TrialType.TAKE_DAMAGE) {
            incrementProgress(player, armor, current.getTargetAmount(), (int) damage);
        }
    }

    public static TrialType getCurrentTrial(com.livingtools.data.LivingArmor armor) {
        String trialName = armor.getTrial();
        if (trialName == null || trialName.isEmpty())
            return TrialType.NONE;
        try {
            return TrialType.valueOf(trialName);
        } catch (IllegalArgumentException e) {
            return TrialType.NONE;
        }
    }

    private static void incrementProgress(Player player, com.livingtools.data.LivingArmor armor, int goal,
            int amount) {
        int current = armor.getTrialProgress();
        current += amount;
        armor.setTrialProgress(current);

        if (current >= goal) {
            completeTrial(player, armor);
        } else if (current % 10 == 0 || (goal - current) <= 5) {
            com.livingtools.utils.MessageUtils.sendActionBar(player,
                    ChatColor.YELLOW + "Prueba de Armadura: " + current + "/" + goal);
        }
        armor.updateLore();
    }

    private static void completeTrial(Player player, com.livingtools.data.LivingArmor armor) {
        armor.setTrial(TrialType.NONE.name());
        armor.setTrialProgress(0);

        // Force Level Up
        int currentLevel = armor.getLevel();
        armor.setLevel(currentLevel + 1);

        player.sendMessage(
                ChatColor.GOLD + "¡PRUEBA DE ARMADURA COMPLETADA! Ha ascendido al nivel " + (currentLevel + 1) + "!");
        com.livingtools.utils.MessageUtils.playLevelUpEffects(player);
        armor.updateLore();
    }

    private static void incrementProgress(Player player, LivingTool tool, int goal) {
        incrementProgress(player, tool, goal, 1);
    }

    private static void incrementProgress(Player player, LivingTool tool, int goal, int amount) {
        int current = tool.getData().getTrialProgress();
        current += amount;
        tool.getData().setTrialProgress(current);

        if (current >= goal) {
            completeTrial(player, tool);
        } else if (current % 10 == 0 || (goal - current) <= 5) {
            // Notify every 10 or when close
            com.livingtools.utils.MessageUtils.sendActionBar(player,
                    ChatColor.YELLOW + "Prueba: " + current + "/" + goal);
        }
        tool.updateLore();
    }

    private static void completeTrial(Player player, LivingTool tool) {
        tool.getData().setTrial(TrialType.NONE.name());
        tool.getData().setTrialProgress(0);

        // Force Level Up to break the loop (30 -> 31)
        int currentLevel = tool.getData().getLevel();
        tool.getData().setLevel(currentLevel + 1);

        player.sendMessage(ChatColor.GOLD + "¡PRUEBA COMPLETADA! Tu herramienta ha ascendido al nivel "
                + (currentLevel + 1) + "!");
        com.livingtools.utils.MessageUtils.playLevelUpEffects(player);
        tool.updateLore();
    }
}
