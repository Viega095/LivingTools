package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class DoubleJumpAbility extends Ability {

    public DoubleJumpAbility() {
        super("double_jump", "Doble Salto", "Salta en el aire.", 20, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
            }
        }
    }

    // We need a listener for the flight toggle.
    // Since Ability class doesn't have a generic event hook for this, we might need
    // to register a listener
    // or handle it in the main AbilityListener if we want to keep it clean.
    // However, for now, let's assume we can add a hook or just handle it here if we
    // register this class as a listener?
    // No, Ability instances are not Listeners.
    // We need to add a hook in AbilityListener to delegate PlayerToggleFlightEvent.

    // For now, I will implement a public method that AbilityListener can call.
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;

        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        // Jump
        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(1));

        // Effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0, 0.5, 0.1);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_BOOTS");
    }
}
