package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LightSetBonusAbility extends Ability {

    public LightSetBonusAbility() {
        super("light_set_bonus", "Set de Luz",
                "+20% daño diurno + brillo permanente + regeneración bajo el sol",
                1, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        long time = player.getWorld().getTime();
        boolean isDay = time >= 0 && time < 13000;

        // Brillo permanente
        if (!player.hasPotionEffect(PotionEffectType.GLOWING)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 220, 0, false, false));
        }

        if (isDay) {
            // Regeneración bajo el sol
            int lightLevel = player.getLocation().getBlock().getLightFromSky();
            if (lightLevel > 12) {
                if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, false, false));
                }

                // Partículas de luz
                if (Math.random() < 0.05) {
                    player.getWorld().spawnParticle(
                            org.bukkit.Particle.END_ROD,
                            player.getLocation().add(0, 1, 0),
                            3,
                            0.3, 0.5, 0.3,
                            0.01);
                }
            }
        }
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        // Daño adicional durante el día
        long time = player.getWorld().getTime();
        boolean isDay = time >= 0 && time < 13000;

        if (isDay) {
            event.setDamage(event.getDamage() * 1.20); // +20% daño
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
