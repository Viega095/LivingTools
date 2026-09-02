package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SkullCrusherAbility extends Ability {

    private final Random random = new Random();

    public SkullCrusherAbility() {
        super("skullcrusher", "Rompecráneos", "Probabilidad de aturdir enemigos al golpear.", 30);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity victim = (LivingEntity) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Mastery Scaling
        int level = getLevel(tool);
        double chance = 0.10 + (level * 0.03); // Lvl 1: 13%, Lvl 5: 25%
        int duration = 20 + (level * 10); // Lvl 1: 1.5s, Lvl 5: 3.5s

        if (random.nextDouble() < chance) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration + 20, 2)); // Slowness lasts
                                                                                               // slightly longer
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0));

            attacker.sendMessage(ChatColor.GOLD + "¡Golpe aturdidor!");
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 0.5f);
            victim.getWorld().spawnParticle(Particle.CRIT_MAGIC, victim.getEyeLocation(), 10, 0.2, 0.2, 0.2, 0.1);

            // Add XP
            addXP(attacker, tool, 10);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE");
    }
}
