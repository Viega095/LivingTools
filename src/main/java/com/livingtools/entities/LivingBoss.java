package com.livingtools.entities;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LivingBoss {

    public static void spawnTheSmith(Location location) {
        IronGolem boss = (IronGolem) location.getWorld().spawnEntity(location, EntityType.IRON_GOLEM);

        // Stats
        boss.setCustomName("§c§lTHE SMITH");
        boss.setCustomNameVisible(true);
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500);
        boss.setHealth(500);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(20);

        // Boss Logic
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead()) {
                    this.cancel();
                    return;
                }

                // Special Attack: Anvil Drop
                if (Math.random() < 0.1 && boss.getTarget() instanceof Player) {
                    Player target = (Player) boss.getTarget();
                    Location targetLoc = target.getLocation();
                    targetLoc.getWorld().spawnFallingBlock(targetLoc.add(0, 5, 0), Material.ANVIL.createBlockData());
                    MessageUtils.send(target, "&c¡Cuidado con el yunque!");
                }

                // Particles
                boss.getWorld().spawnParticle(Particle.FLAME, boss.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.1);
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 20, 20);
    }
}
