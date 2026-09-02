package com.livingtools.abilities.mythic;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

public class MeteorAbility extends Ability {

    public MeteorAbility() {
        super("meteor", "Meteoro", "Invoca un meteoro devastador. (Coste: 200 XP)", 70, AbilityType.ACTIVE);
    }

    @Override
    public void onTrigger(Player player, Event event) {
        if (isOnCooldown(player))
            return;

        Location target = player.getTargetBlock(null, 50).getLocation();
        Location spawn = target.clone().add(0, 20, 0);

        Fireball fireball = player.getWorld().spawn(spawn, Fireball.class);
        fireball.setDirection(new Vector(0, -1, 0));
        fireball.setYield(5.0f); // Explosion power
        fireball.setShooter(player);

        player.getWorld().spawnParticle(Particle.FLAME, spawn, 50, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 0.5f);

        player.sendMessage("§6¡Juicio final!");
        addCooldown(player, 60000); // 60s
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
