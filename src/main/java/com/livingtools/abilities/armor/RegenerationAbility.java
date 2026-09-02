package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RegenerationAbility extends Ability {

    public RegenerationAbility() {
        super("regeneration", "Regeneración", "Regenera vida fuera de combate.", 20);
    }

    private int tickCounter = 0;

    @Override
    public void onHold(Player player) {
        // Heal 1 HP (0.5 hearts) every 100 ticks (5 seconds)
        if (player.getHealth() < player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
            tickCounter++;
            if (tickCounter >= 100) {
                double newHealth = Math.min(player.getHealth() + 1.0,
                        player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setHealth(newHealth);
                tickCounter = 0;
            }
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_HELMET") || material.name().endsWith("_CHESTPLATE")
                || material.name().endsWith("_LEGGINGS") || material.name().endsWith("_BOOTS");
    }
}
