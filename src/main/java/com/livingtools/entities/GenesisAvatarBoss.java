package com.livingtools.entities;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.BossCinematicManager;
import com.livingtools.visuals.ParticleOptimizer;
import com.livingtools.visuals.SoundHarmonicsEngine;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * GenesisAvatarBoss — Mega-Raid Boss de 3 Fases: "El Avatar del Génesis" (Salud: 3000 HP).
 */
public class GenesisAvatarBoss {

    public static final String BOSS_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ EL AVATAR DEL GÉNESIS ✦";
    private static final double MAX_HEALTH = 3000.0;

    public static void spawn(Location location) {
        if (location.getWorld() == null) return;

        BossCinematicManager.playSpawnCinematic(location, "EL AVATAR DEL GÉNESIS", () -> {
            IronGolem boss = (IronGolem) location.getWorld().spawnEntity(location, EntityType.IRON_GOLEM);
            boss.setCustomName(BOSS_NAME);
            boss.setCustomNameVisible(true);
            boss.setRemoveWhenFarAway(false);

            if (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
            }
            boss.setHealth(MAX_HEALTH);

            if (boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(35.0);
            }
            if (boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
                boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
            }

            BossBar bossBar = Bukkit.createBossBar(BOSS_NAME, BarColor.PURPLE, BarStyle.SEGMENTED_12);
            bossBar.setProgress(1.0);

            List<ArmorStand> crystals = new ArrayList<>();

            new BukkitRunnable() {
                int tick = 0;
                int currentPhase = 1;
                boolean phase2Triggered = false;
                boolean phase3Triggered = false;

                @Override
                public void run() {
                    if (boss.isDead() || !boss.isValid()) {
                        bossBar.removeAll();
                        for (ArmorStand c : crystals) {
                            if (c.isValid()) c.remove();
                        }
                        this.cancel();
                        handleVictory(location);
                        return;
                    }

                    double health = boss.getHealth();
                    double progress = Math.max(0.0, Math.min(1.0, health / MAX_HEALTH));
                    bossBar.setProgress(progress);

                    // Actualizar jugadores en el BossBar
                    for (Player player : location.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(boss.getLocation()) <= 2500) { // 50 bloques
                            bossBar.addPlayer(player);
                        } else {
                            bossBar.removePlayer(player);
                        }
                    }

                    // FASE 1: Furia de Magma (HP > 2000)
                    if (health > 2000.0) {
                        currentPhase = 1;
                        bossBar.setColor(BarColor.RED);
                        bossBar.setTitle(ChatColor.RED + "✦ EL AVATAR DEL GÉNESIS [FASE 1: Furia de Magma] ✦");

                        if (tick % 80 == 0) { // Ataque de lluvia de meteoros cada 4s
                            for (Player p : bossBar.getPlayers()) {
                                Location targetLoc = p.getLocation();
                                BossCinematicManager.playTelegraphedAttack(targetLoc, 3.5, 30, () -> {
                                    if (targetLoc.getWorld() != null) {
                                        targetLoc.getWorld().spawnParticle(Particle.LAVA, targetLoc, 25, 1, 0.5, 1, 0.2);
                                        targetLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, targetLoc, 2, 0.5, 0.5, 0.5, 0.1);
                                        for (Entity nearby : targetLoc.getWorld().getNearbyEntities(targetLoc, 3.5, 2, 3.5)) {
                                            if (nearby instanceof Player) {
                                                ((Player) nearby).damage(16.0, boss);
                                                nearby.setFireTicks(100);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                    // FASE 2: Escarcha Cósmica (1000 < HP <= 2000)
                    else if (health > 1000.0) {
                        currentPhase = 2;
                        bossBar.setColor(BarColor.BLUE);
                        bossBar.setTitle(ChatColor.AQUA + "✦ EL AVATAR DEL GÉNESIS [FASE 2: Escarcha Cósmica] ✦");

                        if (!phase2Triggered) {
                            phase2Triggered = true;
                            // Invocar 3 Cristales Rúnicos de Escarcha
                            crystals.clear();
                            double angleStep = (2 * Math.PI) / 3;
                            for (int i = 0; i < 3; i++) {
                                double a = i * angleStep;
                                Location cLoc = boss.getLocation().clone().add(Math.cos(a) * 8.0, 1.0, Math.sin(a) * 8.0);
                                ArmorStand crystal = (ArmorStand) cLoc.getWorld().spawnEntity(cLoc, EntityType.ARMOR_STAND);
                                crystal.setCustomName(ChatColor.AQUA + "❄ Cristal Rúnico de Hielo ❄");
                                crystal.setCustomNameVisible(true);
                                crystal.setHelmet(new ItemStack(Material.PACKED_ICE));
                                crystal.setGlowing(true);
                                crystal.setGravity(false);
                                crystals.add(crystal);
                            }
                            Bukkit.broadcastMessage(ChatColor.AQUA + "❄ ¡El Avatar se ha vuelto INMUNE! ¡Destruyan los 3 Cristales de Escarcha!");
                        }

                        // Comprobar cristales vivos
                        crystals.removeIf(c -> !c.isValid() || c.isDead());
                        if (!crystals.isEmpty()) {
                            boss.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 4));
                            ParticleOptimizer.spawnCircle(boss.getLocation().add(0, 1.5, 0), 2.5, Particle.SNOWFLAKE, 16, null);
                        }

                        if (tick % 60 == 0) {
                            // Ventisca de ralentización
                            for (Player p : bossBar.getPlayers()) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1));
                                p.spawnParticle(Particle.SNOW_SHOVEL, p.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                            }
                        }
                    }
                    // FASE 3: Colapso del Vacío (HP <= 1000)
                    else {
                        currentPhase = 3;
                        bossBar.setColor(BarColor.PURPLE);
                        bossBar.setTitle(ChatColor.DARK_PURPLE + "✦ EL AVATAR DEL GÉNESIS [FASE 3: Colapso del Vacío] ✦");

                        if (!phase3Triggered) {
                            phase3Triggered = true;
                            Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "🌑 ¡La Singularidad del Vacío se ha desatado! ¡Cuidado con la gravedad!");
                            SoundHarmonicsEngine.playAbyssalDrone(boss.getLocation());
                        }

                        // Pulso de Gravedad Cero y Vórtice
                        if (tick % 100 == 0) {
                            Location bLoc = boss.getLocation();
                            for (Player p : bossBar.getPlayers()) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 0));
                                Vector pull = bLoc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.2);
                                p.setVelocity(pull);
                            }
                            SoundHarmonicsEngine.playDangerWarning(bLoc);
                        }

                        ParticleOptimizer.spawnHelix(boss.getLocation(), 3.0, 5.0, Particle.PORTAL, null);
                    }

                    tick++;
                }
            }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 2L);
        });
    }

    private static void handleVictory(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        // Recompensas Míticas
        ItemStack genesisEssence = GUIBuilder.createGlowingItem(Material.NETHER_STAR,
                ChatColor.GOLD + "" + ChatColor.BOLD + "Esencia de la Creación",
                ChatColor.GRAY + "Poder primigenio destilado del Avatar del Génesis.",
                ChatColor.YELLOW + "Utilizado para la Ascensión Divina de Herramientas Vivientes.");

        ItemStack genesisCrown = GUIBuilder.createGlowingItem(Material.GOLDEN_HELMET,
                ChatColor.GOLD + "" + ChatColor.BOLD + "Corona del Génesis",
                ChatColor.LIGHT_PURPLE + "Trofeo supremo de victoria raid.",
                ChatColor.AQUA + "✦ Protección V, Reparación Celestial");

        ItemStack genesisGem = GUIBuilder.createGlowingItem(Material.EMERALD,
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Gema de Estrella del Génesis",
                ChatColor.GRAY + "Engarce místico celestial.",
                ChatColor.GREEN + "+35% Daño y Resonancia Total.");

        List<ItemStack> drops = Arrays.asList(genesisEssence, genesisCrown, genesisGem);
        BossCinematicManager.playDeathCinematic((LivingEntity) loc.getWorld().spawn(loc, ArmorStand.class, a -> a.setVisible(false)), drops);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "✦✦✦ ¡EL AVATAR DEL GÉNESIS HA SIDO DERROTADO! ✦✦✦");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "¡Los héroes han reclamado la Esencia de la Creación y la Corona del Génesis!");
        Bukkit.broadcastMessage("");

        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= 2500) {
                SoundHarmonicsEngine.playVictoryFanfare(p);
            }
        }
    }
}
