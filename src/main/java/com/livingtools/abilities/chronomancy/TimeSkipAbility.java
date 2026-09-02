package com.livingtools.abilities.chronomancy;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.manager.TimeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class TimeSkipAbility extends Ability {

    public TimeSkipAbility() {
        super("time_skip", "Salto Temporal", "Adelanta el tiempo 12 horas. (Cooldown: 30m)", 1800000,
                AbilityType.ACTIVE); // 30 min cooldown
    }

    @Override
    public void onTrigger(Player player, Event event) {
        TimeManager.skipTime(player.getWorld(), 12000);
        player.sendMessage(ChatColor.AQUA + "Has manipulado el flujo del tiempo...");
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_PICKAXE") || material.name().endsWith("_SWORD");
    }
}
