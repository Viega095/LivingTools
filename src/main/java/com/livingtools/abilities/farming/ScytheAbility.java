package com.livingtools.abilities.farming;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ScytheAbility extends Ability {

    public ScytheAbility() {
        super("scythe", "Guadaña", "Ataque en área (Barrido) al golpear enemigos.", 25);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        Player attacker = (Player) event.getDamager();
        LivingEntity primaryVictim = (LivingEntity) event.getEntity();
        double damage = event.getDamage();

        attacker.getWorld().spawnParticle(Particle.SWEEP_ATTACK, primaryVictim.getLocation().add(0, 1, 0), 1);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);

        // Mastery Scaling
        int level = getLevel(tool);
        double damagePercent = 0.50 + (level * 0.10); // Lvl 1: 60%, Lvl 5: 100%

        int count = 0;
        for (Entity e : primaryVictim.getNearbyEntities(2.5, 1, 2.5)) {
            if (e instanceof LivingEntity && e != attacker && e != primaryVictim) {
                ((LivingEntity) e).damage(damage * damagePercent, attacker);
                count++;
            }
        }

        // Add XP
        if (count > 0) {
            addXP(attacker, tool, count * 5);
        }

        if (count > 0) {
            attacker.sendMessage(ChatColor.GRAY + "Has golpeado a " + count + " enemigos extra.");
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HOE");
    }
}
