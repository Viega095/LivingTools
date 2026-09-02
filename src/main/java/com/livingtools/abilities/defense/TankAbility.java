package com.livingtools.abilities.defense;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TankAbility extends Ability {

    public TankAbility() {
        super("tank", "Tanque", "Reduce el daño recibido un 10%.", 20);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HELMET") || type.name().endsWith("_CHESTPLATE")
                || type.name().endsWith("_LEGGINGS") || type.name().endsWith("_BOOTS");
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, com.livingtools.data.LivingTool tool) {
        if (event.getEntity() instanceof Player) {
            double damage = event.getDamage();
            event.setDamage(damage * 0.90); // Reduce by 10%
        }
    }
}
