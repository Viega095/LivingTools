package com.livingtools.abilities.chronomancy;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.LivingToolsPlugin;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StasisAbility extends Ability {

    public StasisAbility() {
        super("stasis", "Éxtasis", "Congela a un enemigo en el tiempo. (Coste: 50 XP)", 55, AbilityType.ACTIVE);
    }

    @Override
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event,
            com.livingtools.data.LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        if (!(event.getDamager() instanceof Player))
            return;

        Player player = (Player) event.getDamager();
        // Check if ability is active/triggered (usually active abilities need a
        // trigger, but let's make this a chance on hit or sneak+hit)
        if (!player.isSneaking())
            return;

        if (isOnCooldown(player))
            return;

        LivingEntity target = (LivingEntity) event.getEntity();

        // Freeze
        target.setAI(false);
        target.setGravity(false);
        target.setInvulnerable(true); // Stasis usually means no damage too

        player.getWorld().playSound(target.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 2);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    target.setAI(true);
                    target.setGravity(true);
                    target.setInvulnerable(false);
                    player.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
                    this.cancel();
                    return;
                }

                target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0);
                ticks += 5;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0, 5);

        player.sendMessage("§b¡Objetivo congelado en el tiempo!");
        addCooldown(player, 45000); // 45s
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD");
    }
}
