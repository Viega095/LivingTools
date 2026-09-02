package com.livingtools.abilities.utility;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

public class XPMagnetAbility extends Ability {

    public XPMagnetAbility() {
        super("xp_magnet", "Imán de XP", "Atrae orbes de experiencia cercanos.", 15, AbilityType.UTILITY);
    }

    @Override
    public void onHold(Player player) {
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof ExperienceOrb) {
                entity.setVelocity(player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize()
                        .multiply(0.5));
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        // Compatible with all tools
        return true;
    }
}
