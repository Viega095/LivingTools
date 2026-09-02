package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VoidSetBonusAbility extends Ability {

    public VoidSetBonusAbility() {
        super("void_set_bonus", "Set del Vacío",
                "+20% daño nocturno + invisibilidad en oscuridad + daño del Void",
                1, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        long time = player.getWorld().getTime();
        boolean isNight = time >= 13000 && time <= 23000;

        if (isNight) {
            // Invisibilidad parcial en oscuridad
            int lightLevel = player.getLocation().getBlock().getLightLevel();
            if (lightLevel < 5) {
                if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
                }
            }

            // Partículas del Void
            if (Math.random() < 0.05) {
                player.getWorld().spawnParticle(
                        org.bukkit.Particle.PORTAL,
                        player.getLocation().add(0, 1, 0),
                        5,
                        0.3, 0.5, 0.3);
            }
        }
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player player) {
        // Daño adicional del Void en la noche
        long time = player.getWorld().getTime();
        boolean isNight = time >= 13000 && time <= 23000;

        if (isNight && event.getDamager() instanceof LivingEntity) {
            event.setDamage(event.getDamage() * 1.20); // +20% daño
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
