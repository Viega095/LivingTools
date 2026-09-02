package com.livingtools.abilities.mythic;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

public class BlackHoleAbility extends Ability {

    public BlackHoleAbility() {
        super("black_hole", "Agujero Negro", "Atrae a los enemigos hacia un punto. (Coste: 100 XP)", 60,
                AbilityType.ACTIVE);
    }

    @Override
    public void onTrigger(Player player, Event event) {
        if (isOnCooldown(player))
            return;

        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(5));

        // Visuals
        player.getWorld().spawnParticle(Particle.SQUID_INK, center, 100, 1, 1, 1, 0.1);
        player.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.1f);

        List<Entity> nearby = player.getWorld().getEntities();
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && entity != player && entity.getLocation().distance(center) < 10) {
                Vector direction = center.toVector().subtract(entity.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(1.5));
                ((LivingEntity) entity).damage(5.0, player);
            }
        }

        player.sendMessage("§5¡El vacío consume!");
        addCooldown(player, 30000); // 30s
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
