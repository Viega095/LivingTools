package com.livingtools.manager;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InfusionManager {

    public static boolean applyInfusion(Player player, LivingTool tool, String infusionId) {
        Ability infusion = AbilityRegistry.getAbility(infusionId);
        if (infusion == null || infusion.getType() != AbilityType.INFUSION) {
            return false;
        }

        // Remove existing infusions
        List<String> toRemove = new ArrayList<>();
        for (String abilityId : tool.getAbilities()) {
            Ability ability = AbilityRegistry.getAbility(abilityId);
            if (ability != null && ability.getType() == AbilityType.INFUSION) {
                toRemove.add(abilityId);
            }
        }

        for (String id : toRemove) {
            tool.removeAbility(id);
        }

        // Add new infusion
        tool.addAbility(infusionId);
        player.sendMessage(ChatColor.GREEN + "¡Tu herramienta ha sido imbuida con " + infusion.getName() + "!");
        return true;
    }

    public static boolean hasInfusion(LivingTool tool) {
        for (String abilityId : tool.getAbilities()) {
            Ability ability = AbilityRegistry.getAbility(abilityId);
            if (ability != null && ability.getType() == AbilityType.INFUSION) {
                return true;
            }
        }
        return false;
    }
}
