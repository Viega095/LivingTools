package com.livingtools.quests.objectives;

import com.livingtools.quests.QuestObjective;
import com.livingtools.quests.QuestManager;
import org.bukkit.entity.Player;

public class KillMobsObjective implements QuestObjective {

    private final String mobType;
    private final int required;
    private final String questId;

    public KillMobsObjective(String questId, String mobType, int required) {
        this.questId = questId;
        this.mobType = mobType;
        this.required = required;
    }

    @Override
    public boolean isCompleted(Player player) {
        return getProgress(player) >= required;
    }

    @Override
    public String getDescription() {
        return "Mata " + required + " " + mobType;
    }

    @Override
    public int getProgress(Player player) {
        return QuestManager.getProgress(player, questId + "_" + mobType);
    }

    @Override
    public int getRequired() {
        return required;
    }

    public String getMobType() {
        return mobType;
    }

    public String getQuestId() {
        return questId;
    }
}
