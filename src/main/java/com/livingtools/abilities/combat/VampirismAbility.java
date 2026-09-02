package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class VampirismAbility extends Ability {

    public VampirismAbility() {
        super("vampirism", "Vampirismo", "Probabilidad de robar vida al atacar.", 30, AbilityType.COMBAT);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            // 15% Chance
            if (Math.random() < 0.15) {
                double healAmount = 2.0; // 1 Heart
                double newHealth = Math.min(player.getHealth() + healAmount,
                        player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());

                if (newHealth > player.getHealth()) {
                    player.setHealth(newHealth);

                    // Visuals
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 3, 0.3, 0.3,
                            0.3);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.5f, 1.2f);
                }
            }
        }
    }
}
