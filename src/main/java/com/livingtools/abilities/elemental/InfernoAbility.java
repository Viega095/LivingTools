package com.livingtools.abilities.elemental;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Arrays;
import java.util.List;

public class InfernoAbility extends Ability {

    public InfernoAbility() {
        super("inferno", "Inferno", "Quema a tus enemigos.", 20, 10000);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().contains("SWORD") || type.name().contains("AXE");
    }

    @Override
    public List<String> getIncompatibleAbilities() {
        return Arrays.asList("glacial", "thunder");
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            target.setFireTicks(100); // 5 seconds
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
        }
    }
}
