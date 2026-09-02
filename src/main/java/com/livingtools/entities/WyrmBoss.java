package com.livingtools.entities;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WyrmBoss {

    private final Ravager boss;
    private final Location spawnLocation;
    private final List<Silverfish> scorpions = new ArrayList<>();
    private int phase = 1;
    private boolean deathHandled = false;
    private boolean underground = false;

    public WyrmBoss(Location location) {
        this.spawnLocation = location;
        this.boss = (Ravager) location.getWorld().spawnEntity(location, EntityType.RAVAGER);
        setupBoss();
        startBehavior();
    }

    private void setupBoss() {
        boss.setCustomName(ChatColor.GOLD + "🏜 " + ChatColor.BOLD + "WYRM DE LAS ARENAS" + ChatColor.GOLD + " 🏜");
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setCanPickupItems(false);

        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1800.0);
        boss.setHealth(1800.0);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(22.0);
        boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        com.livingtools.manager.BossDropManager.tagWyrm(boss);
    }

    private void startBehavior() {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    onDeath();
                    cancel();
                    return;
                }

                tick++;

                // Cambiar fase
                double healthPercent = boss.getHealth()
                        / boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                if (healthPercent < 0.66 && phase == 1) {
                    phase = 2;
                    announcePhase2();
                } else if (healthPercent < 0.33 && phase == 2) {
                    phase = 3;
                    announcePhase3();
                }

                // Habilidades cada 6 segundos
                if (tick % 120 == 0) {
                    useAbility();
                }

                // Esconderse bajo la arena cada 15 segundos en fase 2+
                if (phase >= 2 && tick % 300 == 0 && !underground) {
                    burrowUnderground();
                }

                // Invocar escorpiones cada 10 segundos en fase 3
                if (phase >= 3 && tick % 200 == 0) {
                    summonScorpions();
                }

                // Tormenta de arena
                if (tick % 20 == 0) {
                    createSandstorm();
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);
    }

    private void useAbility() {
        List<Player> nearbyPlayers = getNearbyPlayers(25);
        if (nearbyPlayers.isEmpty())
            return;

        switch ((int) (Math.random() * 3)) {
            case 0:
                sandBlast();
                break;
            case 1:
                desertHeat();
                break;
            case 2:
                quicksand();
                break;
        }
    }

    private void sandBlast() {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GHAST_SHOOT, 2.0f, 0.5f);

        for (Player player : getNearbyPlayers(15)) {
            Vector direction = player.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
            player.setVelocity(direction.multiply(2.0).setY(0.8));
            player.damage(18.0, boss);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
        }

        // Partículas de arena
        for (int i = 0; i < 80; i++) {
            boss.getWorld().spawnParticle(Particle.BLOCK_CRACK,
                    boss.getLocation().add(Math.random() * 10 - 5, Math.random() * 3, Math.random() * 10 - 5),
                    5, Material.SAND.createBlockData());
        }
    }

    private void desertHeat() {
        for (Player player : getNearbyPlayers(20)) {
            player.setFireTicks(100);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
            player.sendMessage(ChatColor.GOLD + "¡El calor del desierto te debilita!");
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 2.0f, 0.5f);
    }

    private void quicksand() {
        for (Player player : getNearbyPlayers(12)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 140, 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 140, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 140, 128)); // Jump Boost negativo
            player.sendMessage(ChatColor.YELLOW + "¡Estás atrapado en arenas movedizas!");

            // Daño por tick
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 140 || player.isDead()) {
                        cancel();
                        return;
                    }
                    if (ticks % 20 == 0) {
                        player.damage(3.0, boss);
                    }
                }
            }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);
        }
    }

    private void burrowUnderground() {
        underground = true;
        boss.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
        boss.setInvulnerable(true);

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_SAND_BREAK, 3.0f, 0.5f);

        // Emerger en una posición aleatoria después de 5 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = getNearbyPlayers(30);
                if (!players.isEmpty()) {
                    Player target = players.get((int) (Math.random() * players.size()));
                    Location emergeLoc = target.getLocation().add(
                            Math.random() * 10 - 5,
                            0,
                            Math.random() * 10 - 5);

                    boss.teleport(emergeLoc);
                    boss.removePotionEffect(PotionEffectType.INVISIBILITY);
                    boss.setInvulnerable(false);
                    underground = false;

                    // Daño de emergencia
                    for (Player player : getNearbyPlayers(5)) {
                        player.setVelocity(new Vector(0, 1.5, 0));
                        player.damage(25.0, boss);
                    }

                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 3.0f, 0.5f);
                    boss.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, boss.getLocation(), 5);
                }
            }
        }.runTaskLater(LivingToolsPlugin.getInstance(), 100L);
    }

    private void summonScorpions() {
        for (int i = 0; i < 4; i++) {
            Location spawnLoc = boss.getLocation().add(
                    Math.random() * 8 - 4,
                    0,
                    Math.random() * 8 - 4);

            Silverfish scorpion = (Silverfish) boss.getWorld().spawnEntity(spawnLoc, EntityType.SILVERFISH);
            scorpion.setCustomName(ChatColor.YELLOW + "Escorpión Gigante");
            scorpion.setCustomNameVisible(true);
            scorpion.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            scorpion.setHealth(40.0);
            scorpion.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8.0);
            scorpion.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

            scorpions.add(scorpion);
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SILVERFISH_AMBIENT, 2.0f, 0.5f);
    }

    private void createSandstorm() {
        for (Player player : getNearbyPlayers(25)) {
            if (Math.random() < 0.3) {
                player.getWorld().spawnParticle(Particle.BLOCK_DUST,
                        player.getLocation().add(0, 1, 0),
                        10, 1, 1, 1,
                        Material.SAND.createBlockData());
            }
        }
    }

    private void announcePhase2() {
        for (Player player : getNearbyPlayers(50)) {
            player.sendTitle(
                    ChatColor.GOLD + "🏜 FASE 2 🏜",
                    ChatColor.YELLOW + "¡El Wyrm se esconde bajo la arena!",
                    10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        }
    }

    private void announcePhase3() {
        for (Player player : getNearbyPlayers(50)) {
            player.sendTitle(
                    ChatColor.DARK_RED + "🏜 FASE FINAL 🏜",
                    ChatColor.RED + "¡El desierto se levanta!",
                    10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
        }
    }

    private void onDeath() {
        if (deathHandled) {
            return;
        }
        deathHandled = true;

        // Limpiar escorpiones
        for (Silverfish scorpion : scorpions) {
            if (!scorpion.isDead()) {
                scorpion.remove();
            }
        }

        // Limpieza y anuncio (drops vía BossDropManager + loot bag)
        for (Player player : getNearbyPlayers(100)) {
            player.sendTitle(
                    ChatColor.GOLD + "⚔ VICTORIA ⚔",
                    ChatColor.YELLOW + "¡El Wyrm de las Arenas ha sido derrotado!",
                    10, 100, 20);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private List<Player> getNearbyPlayers(double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity entity : boss.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public Ravager getBoss() {
        return boss;
    }
}
