package com.livingtools.quests.rewards;

import com.livingtools.quests.QuestReward;
import org.bukkit.entity.Player;

public class XPReward implements QuestReward {

    private final int amount;

    public XPReward(int amount) {
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        player.giveExp(amount);
        player.sendMessage("§a+ " + amount + " XP");
    }

    @Override
    public String getDescription() {
        return amount + " XP";
    }
}
