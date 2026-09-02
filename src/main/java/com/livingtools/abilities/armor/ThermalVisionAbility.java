package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ThermalVisionAbility extends Ability {

    public ThermalVisionAbility() {
        super("thermal_vision", "Visión Térmica",
                "Permite ver entidades a través de paredes con efecto de brillo",
                30, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Aplicar efecto de brillo a entidades cercanas
        player.getNearbyEntities(20, 20, 20).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !(entity instanceof Player) || !((Player) entity).hasPermission("livingtools.admin"))
                .forEach(entity -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (!livingEntity.hasPotionEffect(PotionEffectType.GLOWING)) {
                        livingEntity.addPotionEffect(
                                new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));
                    }
                });
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("HELMET");
    }
}
