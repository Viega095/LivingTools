package com.livingtools.quests;

import org.bukkit.entity.Player;

public interface QuestReward {
    void give(Player player);

    String getDescription();
}
