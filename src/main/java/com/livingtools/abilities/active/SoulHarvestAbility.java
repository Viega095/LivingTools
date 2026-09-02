package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulHarvestAbility extends ActiveAbility {

    private static final Map<UUID, Integer> soulCounts = new HashMap<>();
    private static final int MAX_SOULS = 3;

    public SoulHarvestAbility() {
        super("soulharvest", "Soul Harvest",
                "Click Derecho: Invoca almas que atacan por ti.", 40000); // 40s cooldown
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        int currentSouls = soulCounts.getOrDefault(player.getUniqueId(), 0);

        if (currentSouls >= MAX_SOULS) {
            player.sendMessage(ChatColor.RED + "¡Ya tienes el máximo de almas!");
            return;
        }

        // Summon soul
        summonSoul(player);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.DARK_PURPLE + "☠ Soul Harvest: " +
                        (soulCounts.get(player.getUniqueId())) + "/" + MAX_SOULS + " ☠"));
    }

    private void summonSoul(Player owner) {
        Location spawnLoc = owner.getLocation().add(
                Math.random() * 4 - 2,
                1,
                Math.random() * 4 - 2);

        // Spawn vex as soul entity
        Vex soul = (Vex) owner.getWorld().spawnEntity(spawnLoc, EntityType.VEX);
        soul.setCustomName(ChatColor.DARK_PURPLE + "☠ Alma Cosechada");
        soul.setCustomNameVisible(true);
        soul.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        soul.setHealth(20.0);
        soul.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(2.0);

        // Increment soul count
        int count = soulCounts.getOrDefault(owner.getUniqueId(), 0) + 1;
        soulCounts.put(owner.getUniqueId(), count);

        // Summon effects
        owner.getWorld().spawnParticle(Particle.SOUL, spawnLoc, 50, 0.5, 0.5, 0.5, 0.1);
        owner.getWorld().spawnParticle(Particle.SMOKE_LARGE, spawnLoc, 20, 0.3, 0.3, 0.3, 0.05);
        owner.playSound(spawnLoc, Sound.ENTITY_VEX_AMBIENT, 1, 0.5f);
        owner.playSound(spawnLoc, Sound.PARTICLE_SOUL_ESCAPE, 1, 1);

        owner.sendMessage(ChatColor.DARK_PURPLE + "☠ " + ChatColor.LIGHT_PURPLE +
                "Alma invocada (" + count + "/" + MAX_SOULS + ")");

        // Soul behavior and lifespan
        new BukkitRunnable() {
            int ticks = 0;
            final int maxDuration = 300; // 15 seconds

            @Override
            public void run() {
                if (!soul.isValid() || soul.isDead() || ticks >= maxDuration) {
                    // Soul expired or died
                    if (soul.isValid() && !soul.isDead()) {
                        soul.getWorld().spawnParticle(Particle.SOUL, soul.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
                        soul.remove();
                        owner.sendMessage(ChatColor.GRAY + "Un alma regresó al vacío...");
                    }

                    // Decrement count
                    int current = soulCounts.getOrDefault(owner.getUniqueId(), 0);
                    if (current > 0) {
                        soulCounts.put(owner.getUniqueId(), current - 1);
                    }

                    this.cancel();
                    return;
                }

                // Soul particles
                if (ticks % 10 == 0) {
                    soul.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                            soul.getLocation(), 3, 0.2, 0.2, 0.2, 0);
                }

                // Make soul attack nearby enemies
                if (ticks % 20 == 0) {
                    LivingEntity target = findNearestEnemy(soul.getLocation(), owner, 15);
                    if (target != null && soul instanceof Creature) {
                        ((Creature) soul).setTarget(target);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
    }

    private LivingEntity findNearestEnemy(Location center, Player owner, double radius) {
        LivingEntity nearest = null;
        double nearestDist = radius;

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != owner && !(entity instanceof Vex)) {
                if (entity instanceof Monster || (entity instanceof Player && entity != owner)) {
                    double dist = entity.getLocation().distance(center);
                    if (dist < nearestDist) {
                        nearest = (LivingEntity) entity;
                        nearestDist = dist;
                    }
                }
            }
        }

        return nearest;
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().contains("SKULL") || type.name().endsWith("_HOE") || type.name().endsWith("_SWORD");
    }
}
