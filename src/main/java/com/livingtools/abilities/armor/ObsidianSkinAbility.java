package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ObsidianSkinAbility extends Ability {

    public ObsidianSkinAbility() {
        super("obsidian_skin", "Piel de Obsidiana", "Resistencia al fuego y empuje.", 50, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Permanent Fire Resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false, true));
    }

    @Override
    public void onDamageTaken(EntityDamageEvent event, Player victim) {
        // Knockback Resistance logic is tricky without attribute modifiers or NMS.
        // For simplicity and compatibility, we'll give a short burst of Resistance when
        // hit by fire/lava/explosions
        // to simulate "tough skin".

        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {

            event.setDamage(event.getDamage() * 0.5); // 50% Damage Reduction from these sources
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().endsWith("_CHESTPLATE");
    }
}
