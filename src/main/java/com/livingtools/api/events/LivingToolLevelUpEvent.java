package com.livingtools.api.events;

import com.livingtools.data.LivingTool;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LivingToolLevelUpEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final LivingTool tool;
    private final int newLevel;

    public LivingToolLevelUpEvent(Player player, LivingTool tool, int newLevel) {
        this.player = player;
        this.tool = tool;
        this.newLevel = newLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public LivingTool getTool() {
        return tool;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
