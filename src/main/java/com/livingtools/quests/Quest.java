package com.livingtools.quests;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Quest {

    private final String id;
    private final String name;
    private final String description;
    private final QuestType type;
    private final List<QuestObjective> objectives;
    private final List<QuestReward> rewards;
    private final int requiredLevel;

    public Quest(String id, String name, String description, QuestType type, int requiredLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.objectives = new ArrayList<>();
        this.rewards = new ArrayList<>();
    }

    public void addObjective(QuestObjective objective) {
        objectives.add(objective);
    }

    public void addReward(QuestReward reward) {
        rewards.add(reward);
    }

    public boolean isCompleted(Player player) {
        for (QuestObjective objective : objectives) {
            if (!objective.isCompleted(player)) {
                return false;
            }
        }
        return true;
    }

    public void giveRewards(Player player) {
        for (QuestReward reward : rewards) {
            reward.give(player);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public QuestType getType() {
        return type;
    }

    public List<QuestObjective> getObjectives() {
        return objectives;
    }

    public List<QuestReward> getRewards() {
        return rewards;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
