package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class SoulReaperAbility extends Ability {

    public SoulReaperAbility() {
        super("soulreaper", "Segador de Almas", "Recuperas vida al matar enemigos.", 35);
    }

    @Override
    public void onKill(EntityDeathEvent event, LivingTool tool) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null)
            return;

        int level = getLevel(tool);
        double healAmount = 1.0 + (level * 0.5); // Lvl 1: 1.5, Lvl 5: 3.5

        double maxHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = killer.getHealth();

        if (currentHealth < maxHealth) {
            killer.setHealth(Math.min(maxHealth, currentHealth + healAmount));
            killer.playSound(killer.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.1f, 2.0f);
            killer.getWorld().spawnParticle(Particle.SOUL, killer.getLocation().add(0, 1, 0), 5, 0.2, 0.5, 0.2, 0.05);

            // Lvl 5 Bonus: Absorption Shield
            if (level >= 5) {
                double absorption = killer.getAbsorptionAmount();
                if (absorption < 4.0) { // Max 2 hearts absorption
                    killer.setAbsorptionAmount(Math.min(4.0, absorption + 1.0));
                }
            }

            addXP(killer, tool, 15);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
    }
}
