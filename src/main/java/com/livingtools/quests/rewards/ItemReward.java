package com.livingtools.quests.rewards;

import com.livingtools.quests.QuestReward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemReward implements QuestReward {

    private final ItemStack item;

    public ItemReward(ItemStack item) {
        this.item = item.clone();
    }

    @Override
    public void give(Player player) {
        player.getInventory().addItem(item);
        player.sendMessage("§a+ " + item.getAmount() + "x " + item.getType().name());
    }

    @Override
    public String getDescription() {
        return item.getAmount() + "x " + item.getType().name();
    }
}
