package com.livingtools.abilities.elemental;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class GlacialAbility extends Ability {

    public GlacialAbility() {
        super("glacial", "Glacial", "Congela y ralentiza a tus enemigos.", 20, 10000);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().contains("SWORD") || type.name().contains("AXE");
    }

    @Override
    public List<String> getIncompatibleAbilities() {
        return Arrays.asList("inferno", "thunder");
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1)); // 3 seconds, Slowness II
            target.setFreezeTicks(100); // 5 seconds freeze
            target.getWorld().spawnParticle(Particle.SNOWBALL, target.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
        }
    }
}
