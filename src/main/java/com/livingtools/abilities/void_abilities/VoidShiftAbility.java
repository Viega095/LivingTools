package com.livingtools.abilities.void_abilities;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.LivingToolsPlugin;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VoidShiftAbility extends Ability {

    public VoidShiftAbility() {
        super("void_shift", "Cambio de Fase", "Entra en el vacío (Invulnerable/Invisible). (Coste: 100 XP)", 65,
                AbilityType.ACTIVE);
    }

    @Override
    public void onTrigger(Player player, Event event) {
        if (isOnCooldown(player))
            return;

        // Effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
        player.setInvulnerable(true);

        player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_NETHER_WASTES_MOOD, 1, 0.5f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);

        player.sendMessage("§5Has entrado en el vacío...");

        // End task
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
                player.sendMessage("§5Has regresado al plano material.");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            }
        }.runTaskLater(LivingToolsPlugin.getInstance(), 100); // 5 seconds

        addCooldown(player, 60000); // 60s
    }

    @Override
    public boolean isCompatible(Material type) {
        return true; // All tools
    }
}
