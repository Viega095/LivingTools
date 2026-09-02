package com.livingtools.abilities.enchant;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class SoulReaperAbility extends Ability {

    public SoulReaperAbility() {
        super("soul_reaper", "Soul Reaper", "Probabilidad de curarte al matar enemigos.", 1, AbilityType.PASSIVE);
    }

    @Override
    public void onKill(EntityDeathEvent event, LivingTool tool) {
        Player player = event.getEntity().getKiller();
        if (player == null)
            return;

        if (Math.random() < 0.15) { // 15% chance
            double heal = 2.0; // 1 Heart
            double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(player.getHealth() + heal, maxHealth));

            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1.5f); // Burp sound for "eating" soul?
                                                                                       // Or maybe something more
                                                                                       // mystical.
            // Let's use generic drink sound
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.5f, 1.5f);
        }
    }
}
