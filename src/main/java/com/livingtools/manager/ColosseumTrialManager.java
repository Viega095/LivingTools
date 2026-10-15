package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * ColosseumTrialManager — Coliseo de Pruebas por Oleadas.
 * Desafío de combate táctico de 5 oleadas escalonadas que recompensa a los cazadores con XP y botín mítico.
 */
public class ColosseumTrialManager implements Listener {

    private static class ActiveTrial {
        private final UUID playerUUID;
        private final Location arenaCenter;
        private int currentWave = 0;
        private final Set<UUID> livingMobs = new HashSet<>();
        private boolean finished = false;

        public ActiveTrial(UUID playerUUID, Location arenaCenter) {
            this.playerUUID = playerUUID;
            this.arenaCenter = arenaCenter;
        }
    }

    private static final Map<UUID, ActiveTrial> activeTrials = new HashMap<>();

    public static boolean startTrial(Player player) {
        if (activeTrials.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ya tienes una prueba del Coliseo activa.");
            return false;
        }

        ActiveTrial trial = new ActiveTrial(player.getUniqueId(), player.getLocation());
        activeTrials.put(player.getUniqueId(), trial);

        player.sendMessage(ChatColor.GOLD + "⚔ " + ChatColor.BOLD + "¡HAS ENTRADO AL COLISEO DE PRUEBAS! " + ChatColor.GOLD + "⚔");
        player.sendMessage(ChatColor.YELLOW + "Sobrevive a 5 oleadas de criaturas inteligentes para reclamar el Botín Primordial.");
        player.playSound(player.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeTrials.containsKey(player.getUniqueId())) {
                    spawnNextWave(player, trial);
                }
            }
        }.runTaskLater(LivingToolsPlugin.getInstance(), 60L); // 3 segundos de preparación

        return true;
    }

    private static void spawnNextWave(Player player, ActiveTrial trial) {
        if (!player.isOnline()) {
            endTrial(player.getUniqueId());
            return;
        }

        trial.currentWave++;
        trial.livingMobs.clear();

        player.sendTitle(
                ChatColor.RED + "Oleada " + trial.currentWave + "/5",
                ChatColor.GRAY + "¡Prepárate para el combate!",
                10, 40, 10
        );
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 1.5f);

        Location loc = trial.arenaCenter;
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 4, 1, 4, 0.05);

        switch (trial.currentWave) {
            case 1:
                // Oleada 1: 4 Zombies y 2 Esqueletos
                for (int i = 0; i < 4; i++) {
                    Zombie z = (Zombie) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.ZOMBIE);
                    z.setTarget(player);
                    trial.livingMobs.add(z.getUniqueId());
                }
                for (int i = 0; i < 2; i++) {
                    Skeleton s = (Skeleton) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.SKELETON);
                    s.setTarget(player);
                    trial.livingMobs.add(s.getUniqueId());
                }
                break;

            case 2:
                // Oleada 2: 3 Arañas Neurotóxicas y 2 Zombies Berserker
                for (int i = 0; i < 3; i++) {
                    Spider sp = (Spider) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.SPIDER);
                    sp.setCustomName(ChatColor.GREEN + "Araña Neurotóxica");
                    sp.setCustomNameVisible(true);
                    sp.setTarget(player);
                    trial.livingMobs.add(sp.getUniqueId());
                }
                for (int i = 0; i < 2; i++) {
                    Zombie zb = (Zombie) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.ZOMBIE);
                    zb.setCustomName(ChatColor.RED + "Zombie Berserker");
                    zb.setCustomNameVisible(true);
                    zb.setTarget(player);
                    trial.livingMobs.add(zb.getUniqueId());
                }
                break;

            case 3:
                // Oleada 3: 4 Esqueletos Gladiadores y 2 Creepers Sísmicos
                for (int i = 0; i < 4; i++) {
                    Skeleton sk = (Skeleton) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.SKELETON);
                    sk.setCustomName(ChatColor.RED + "Esqueleto Gladiador");
                    sk.setCustomNameVisible(true);
                    sk.setTarget(player);
                    trial.livingMobs.add(sk.getUniqueId());
                }
                for (int i = 0; i < 2; i++) {
                    Creeper cr = (Creeper) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.CREEPER);
                    cr.setCustomName(ChatColor.DARK_RED + "Creeper Sísmico");
                    cr.setCustomNameVisible(true);
                    cr.setTarget(player);
                    trial.livingMobs.add(cr.getUniqueId());
                }
                break;

            case 4:
                // Oleada 4: 3 Espectros del Vacío (Endermites/Phantoms) y 3 Minions de Lava
                for (int i = 0; i < 3; i++) {
                    MagmaCube mc = (MagmaCube) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.MAGMA_CUBE);
                    mc.setSize(2);
                    mc.setCustomName(ChatColor.GOLD + "Esbirro de Magma");
                    mc.setCustomNameVisible(true);
                    mc.setTarget(player);
                    trial.livingMobs.add(mc.getUniqueId());
                }
                for (int i = 0; i < 2; i++) {
                    Enderman em = (Enderman) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.ENDERMAN);
                    em.setCustomName(ChatColor.DARK_PURPLE + "Espectro de Sombra");
                    em.setCustomNameVisible(true);
                    em.setTarget(player);
                    trial.livingMobs.add(em.getUniqueId());
                }
                break;

            case 5:
                // Oleada 5: Campeón del Coliseo (Iron Golem Corrupto)
                IronGolem boss = (IronGolem) loc.getWorld().spawnEntity(getOffsetLocation(loc), EntityType.IRON_GOLEM);
                boss.setCustomName(ChatColor.GOLD + "👑 Campeón del Coliseo");
                boss.setCustomNameVisible(true);
                boss.setTarget(player);
                trial.livingMobs.add(boss.getUniqueId());
                break;
        }
    }

    private static Location getOffsetLocation(Location center) {
        Random rnd = new Random();
        double offsetX = (rnd.nextDouble() * 12.0) - 6.0;
        double offsetZ = (rnd.nextDouble() * 12.0) - 6.0;
        return center.clone().add(offsetX, 0, offsetZ);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UUID mobId = event.getEntity().getUniqueId();

        for (Map.Entry<UUID, ActiveTrial> entry : activeTrials.entrySet()) {
            ActiveTrial trial = entry.getValue();
            if (trial.livingMobs.remove(mobId)) {
                Player player = Bukkit.getPlayer(trial.playerUUID);
                if (player != null && trial.livingMobs.isEmpty()) {
                    // Oleada superada
                    if (trial.currentWave >= 5) {
                        // Victoria total
                        completeTrial(player, trial);
                    } else {
                        player.sendMessage(ChatColor.GREEN + "✔ ¡Oleada " + trial.currentWave + " superada! Siguiente oleada en 4 segundos...");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);

                        // XP intermedia para el arma
                        ItemStack held = player.getInventory().getItemInMainHand();
                        if (LivingTool.isLivingTool(held)) {
                            LivingTool tool = new LivingTool(held);
                            tool.addXP(player, 150L);
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (activeTrials.containsKey(player.getUniqueId())) {
                                    spawnNextWave(player, trial);
                                }
                            }
                        }.runTaskLater(LivingToolsPlugin.getInstance(), 80L);
                    }
                }
                break;
            }
        }
    }

    private static void completeTrial(Player player, ActiveTrial trial) {
        trial.finished = true;
        activeTrials.remove(trial.playerUUID);

        player.sendTitle(ChatColor.GOLD + "✦ ¡VICTORIA EN EL COLISEO! ✦", ChatColor.YELLOW + "Has derrotado al Campeón.", 10, 60, 20);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        // Recompensas épicas
        ItemStack held = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(held)) {
            LivingTool tool = new LivingTool(held);
            tool.addXP(player, 1500L);
            player.sendMessage(ChatColor.GREEN + "✦ Tu arma viviente ha absorbido +1,500 XP de almas gladiadoras.");
        }

        player.getInventory().addItem(new ItemStack(Material.NETHERITE_INGOT, 1));
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
        player.getInventory().addItem(new ItemStack(Material.AMETHYST_SHARD, 8));
        player.sendMessage(ChatColor.GOLD + "✦ Has recibido: Lingote de Netherite, 5 Diamantes y 8 Fragmentos de Alma.");
    }

    public static void endTrial(UUID uuid) {
        activeTrials.remove(uuid);
    }
}
