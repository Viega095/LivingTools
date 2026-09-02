package com.livingtools.abilities.time;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StasisTrapAbility extends Ability {

    public StasisTrapAbility() {
        super("stasis_trap", "Trampa de Estasis", "Congela a un enemigo en el tiempo.", 40, AbilityType.PASSIVE);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            LivingEntity target = (LivingEntity) event.getEntity();

            if (player.isSneaking() && !isOnCooldown(player)) {
                // Freeze logic
                target.setAI(false);
                target.setGravity(false);
                target.setInvulnerable(true); // Cannot be hurt while frozen
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));

                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.5f);

                // Particles
                Bukkit.getScheduler().runTaskTimer(LivingToolsPlugin.getInstance(), (task) -> {
                    if (!target.isValid() || target.hasAI()) {
                        task.cancel();
                        return;
                    }
                    target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 10, 0.5, 1,
                            0.5, 0);
                }, 0L, 5L);

                player.sendMessage("§b§l[ESTASIS] §7Objetivo congelado.");

                // Unfreeze after 3 seconds
                Bukkit.getScheduler().runTaskLater(LivingToolsPlugin.getInstance(), () -> {
                    if (target.isValid()) {
                        target.setAI(true);
                        target.setGravity(true);
                        target.setInvulnerable(false);
                        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 2);
                    }
                }, 60L); // 3 seconds

                addCooldown(player, 45000); // 45s cooldown
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().contains("SWORD") || type.name().contains("AXE");
    }
}
