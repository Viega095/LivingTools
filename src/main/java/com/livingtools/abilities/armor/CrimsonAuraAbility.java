package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CrimsonAuraAbility extends Ability {

    public CrimsonAuraAbility() {
        super("crimson_aura", "Aura Carmesí", "Daña a enemigos cercanos constantemente.", 50, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Run every tick (called from AbilityListener onMove/Task)
        // To prevent spamming damage every tick, we should use a cooldown or low
        // probability?
        // Or just rely on the invulnerability ticks of the mob.
        // Mobs have 10 ticks (0.5s) invulnerability.
        // So applying damage every tick is fine, they will just take it every 0.5s.

        // Visuals
        if (Math.random() < 0.1) {
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 5, 1, 0.5, 1,
                    new Particle.DustOptions(org.bukkit.Color.MAROON, 1));
        }

        for (Entity entity : player.getNearbyEntities(3, 2, 3)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(1.0, player); // 0.5 Hearts
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_CHESTPLATE");
    }
}
