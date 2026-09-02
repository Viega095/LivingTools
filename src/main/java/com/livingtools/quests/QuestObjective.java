package com.livingtools.quests;

import org.bukkit.entity.Player;

public interface QuestObjective {
    boolean isCompleted(Player player);

    String getDescription();

    int getProgress(Player player);

    int getRequired();
}
