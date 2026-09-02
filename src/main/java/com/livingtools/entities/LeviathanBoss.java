package com.livingtools.entities;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class LeviathanBoss {

    private final Giant boss;
    private final Location spawnLocation;
    private final List<Guardian> minions = new ArrayList<>();
    private int phase = 1;
    private boolean deathHandled = false;
    private boolean whirlpoolActive = false;

    public LeviathanBoss(Location location) {
        this.spawnLocation = location;
        this.boss = (Giant) location.getWorld().spawnEntity(location, EntityType.GIANT);
        setupBoss();
        startBehavior();
    }

    private void setupBoss() {
        boss.setCustomName(ChatColor.DARK_AQUA + "⚓ " + ChatColor.BOLD + "LEVIATÁN" + ChatColor.DARK_AQUA + " ⚓");
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setCanPickupItems(false);

        // Stats masivos
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2000.0);
        boss.setHealth(2000.0);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(25.0);
        boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);

        // Efectos permanentes
        boss.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 2, false, false));
        com.livingtools.manager.BossDropManager.tagLeviathan(boss);
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

                // Cambiar fase según vida
                double healthPercent = boss.getHealth()
                        / boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                if (healthPercent < 0.66 && phase == 1) {
                    phase = 2;
                    announcePhase2();
                } else if (healthPercent < 0.33 && phase == 2) {
                    phase = 3;
                    announcePhase3();
                }

                // Habilidades cada 5 segundos
                if (tick % 100 == 0) {
                    useAbility();
                }

                // Remolino cada 10 segundos en fase 2+
                if (phase >= 2 && tick % 200 == 0) {
                    createWhirlpool();
                }

                // Invocar guardianes cada 15 segundos en fase 3
                if (phase >= 3 && tick % 300 == 0) {
                    summonGuardians();
                }

                // Partículas de agua
                if (tick % 10 == 0) {
                    spawnWaterParticles();
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 1L);
    }

    private void useAbility() {
        List<Player> nearbyPlayers = getNearbyPlayers(30);
        if (nearbyPlayers.isEmpty())
            return;

        Player target = nearbyPlayers.get((int) (Math.random() * nearbyPlayers.size()));

        switch ((int) (Math.random() * 3)) {
            case 0:
                tidalWave(target);
                break;
            case 1:
                waterPrison(target);
                break;
            case 2:
                crushingDepths(target);
                break;
        }
    }

    private void tidalWave(Player target) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2.0f, 0.5f);

        // Empujar jugadores cercanos
        for (Player player : getNearbyPlayers(15)) {
            player.setVelocity(player.getLocation().toVector()
                    .subtract(boss.getLocation().toVector())
                    .normalize()
                    .multiply(2.0)
                    .setY(1.0));
            player.damage(15.0, boss);
        }

        // Efectos visuales
        for (int i = 0; i < 50; i++) {
            boss.getWorld().spawnParticle(Particle.WATER_SPLASH,
                    boss.getLocation().add(Math.random() * 10 - 5, Math.random() * 3, Math.random() * 10 - 5),
                    10, 0.5, 0.5, 0.5);
        }
    }

    private void waterPrison(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 4));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 4));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));

        target.sendMessage(ChatColor.DARK_AQUA + "¡Estás atrapado en una prisión de agua!");

        // Partículas de prisión
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ >= 100 || target.isDead()) {
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.DRIP_WATER, target.getLocation().add(0, 1, 0), 20, 1, 1, 1);
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 5L);
    }

    private void crushingDepths(Player target) {
        target.damage(20.0, boss);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_DROWN, 1.0f, 0.5f);
    }

    private void createWhirlpool() {
        whirlpoolActive = true;
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 3.0f, 0.5f);

        new BukkitRunnable() {
            int duration = 0;

            @Override
            public void run() {
                if (duration++ >= 100 || boss.isDead()) {
                    whirlpoolActive = false;
                    cancel();
                    return;
                }

                // Atraer jugadores hacia el jefe
                for (Player player : getNearbyPlayers(20)) {
                    Location playerLoc = player.getLocation();
                    Location bossLoc = boss.getLocation();

                    player.setVelocity(bossLoc.toVector()
                            .subtract(playerLoc.toVector())
                            .normalize()
                            .multiply(0.3));

                    if (duration % 20 == 0) {
                        player.damage(5.0, boss);
                    }
                }

                // Partículas de remolino
                double radius = 10;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle + duration * 0.1) * radius;
                    double z = Math.sin(angle + duration * 0.1) * radius;
                    boss.getWorld().spawnParticle(Particle.WATER_WAKE,
                            boss.getLocation().add(x, 0, z), 1);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 2L);
    }

    private void summonGuardians() {
        for (int i = 0; i < 3; i++) {
            Location spawnLoc = boss.getLocation().add(
                    Math.random() * 10 - 5,
                    0,
                    Math.random() * 10 - 5);

            Guardian guardian = (Guardian) boss.getWorld().spawnEntity(spawnLoc, EntityType.GUARDIAN);
            guardian.setCustomName(ChatColor.AQUA + "Guardián Corrupto");
            guardian.setCustomNameVisible(true);
            guardian.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
            guardian.setHealth(100.0);

            minions.add(guardian);
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2.0f, 1.5f);
    }

    private void spawnWaterParticles() {
        boss.getWorld().spawnParticle(Particle.DRIP_WATER,
                boss.getLocation().add(0, 3, 0),
                5, 1, 1, 1);
    }

    private void announcePhase2() {
        for (Player player : getNearbyPlayers(50)) {
            player.sendTitle(
                    ChatColor.DARK_AQUA + "⚡ FASE 2 ⚡",
                    ChatColor.AQUA + "¡El Leviatán enfurece!",
                    10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        }
    }

    private void announcePhase3() {
        for (Player player : getNearbyPlayers(50)) {
            player.sendTitle(
                    ChatColor.DARK_RED + "⚡ FASE FINAL ⚡",
                    ChatColor.RED + "¡El Leviatán invoca a sus guardianes!",
                    10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        }
    }

    private void onDeath() {
        if (deathHandled) {
            return;
        }
        deathHandled = true;

        // Limpiar minions
        for (Guardian minion : minions) {
            if (!minion.isDead()) {
                minion.remove();
            }
        }

        // Anuncio (recompensas vía BossDropManager + loot bag)
        for (Player player : getNearbyPlayers(100)) {
            player.sendTitle(
                    ChatColor.GOLD + "⚔ VICTORIA ⚔",
                    ChatColor.YELLOW + "¡El Leviatán ha sido derrotado!",
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

    public Giant getBoss() {
        return boss;
    }
}
