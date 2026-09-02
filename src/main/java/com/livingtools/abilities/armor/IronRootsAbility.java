package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class IronRootsAbility extends Ability {

    public IronRootsAbility() {
        super("iron_roots", "Raíces de Hierro",
                "Inmunidad completa a knockback y empuje",
                30, AbilityType.ARMOR);
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        // Cancelar cualquier knockback
        Vector originalVelocity = player.getVelocity();

        // Programar para el siguiente tick (después de que se aplique el knockback)
        org.bukkit.Bukkit.getScheduler().runTask(
                com.livingtools.LivingToolsPlugin.getInstance(),
                () -> player.setVelocity(new Vector(0, originalVelocity.getY(), 0)));
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("LEGGINGS");
    }
}
