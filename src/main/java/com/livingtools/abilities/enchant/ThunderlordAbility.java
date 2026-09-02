package com.livingtools.abilities.enchant;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ThunderlordAbility extends Ability {

    public ThunderlordAbility() {
        super("thunderlord", "Thunderlord", "Probabilidad de invocar un rayo al golpear.", 1, AbilityType.PASSIVE);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity target = (LivingEntity) event.getEntity();
        Player player = (Player) event.getDamager();

        if (Math.random() < 0.10) { // 10% chance
            target.getWorld().strikeLightning(target.getLocation());
            player.sendMessage(org.bukkit.ChatColor.GOLD + "¡El Señor del Trueno golpea!");
        }
    }
}
