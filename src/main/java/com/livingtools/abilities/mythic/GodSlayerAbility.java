package com.livingtools.abilities.mythic;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GodSlayerAbility extends Ability {

    public GodSlayerAbility() {
        super("godslayer", "Matadioses", "Inflige daño extra a jefes.", 75);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity target = (LivingEntity) event.getEntity();

        switch (target.getType()) {
            case ENDER_DRAGON:
            case WITHER:
            case ELDER_GUARDIAN:
            case WARDEN:
                double maxHealth = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                double bonusDamage = maxHealth * 0.05; // 5% of max health
                event.setDamage(event.getDamage() + bonusDamage);

                if (event.getDamager() instanceof Player) {
                    ((Player) event.getDamager()).sendMessage(
                            org.bukkit.ChatColor.GOLD + "¡Golpe Matadioses! +" + (int) bonusDamage + " daño.");
                }
                break;
            default:
                break;
        }
    }
}
