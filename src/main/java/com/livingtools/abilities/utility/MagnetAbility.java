package com.livingtools.abilities.utility;

import com.livingtools.abilities.Ability;
import com.livingtools.manager.ConfigManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MagnetAbility extends Ability {

    public MagnetAbility() {
        super("magnet", "Imán", "Atrae los ítems cercanos hacia ti.", 15);
    }

    @Override
    public void onHold(Player player) {
        double radius = ConfigManager.getMagnetRadius();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                if (item.getPickupDelay() == 0) {
                    Vector direction = player.getLocation().toVector().subtract(item.getLocation().toVector())
                            .normalize();
                    item.setVelocity(direction.multiply(0.5));
                }
            }
        }
    }

    @Override
    public boolean isCompatible(org.bukkit.Material type) {
        String name = type.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")
                || name.endsWith("_SWORD");
    }
}
