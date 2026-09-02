package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceAspectAbility extends Ability {

    public IceAspectAbility() {
        super("iceaspect", "Aspecto Helado", "Ralentiza a los enemigos al golpear.", 25);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity victim = (LivingEntity) event.getEntity();
        Player attacker = (Player) event.getDamager();

        int level = getLevel(tool);
        int duration = 20 + (level * 10); // Lvl 1: 1.5s, Lvl 5: 3.5s
        int amplifier = (level >= 3) ? 1 : 0; // Slowness II at Lvl 3+

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier));

        victim.getWorld().spawnParticle(Particle.SNOWBALL, victim.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.1);
        attacker.playSound(attacker.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);

        // Add XP
        addXP(attacker, tool, 5);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
