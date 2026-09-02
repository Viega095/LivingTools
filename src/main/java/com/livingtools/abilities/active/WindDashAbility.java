package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class WindDashAbility extends ActiveAbility {

    public WindDashAbility() {
        super("wind_dash", "Impulso de Viento", "Click Derecho: Impulso rápido hacia adelante (5s CD).", 5000);
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        player.setVelocity(player.getLocation().getDirection().multiply(2)); // Dash

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 2);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD");
    }
}
