package com.livingtools.entities;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DryadBoss {

    private final Witch boss;
    private final Location spawnLocation;
    private final List<Zombie> treants = new ArrayList<>();
    private int phase = 1;
    private boolean deathHandled = false;

    public DryadBoss(Location location) {
        this.spawnLocation = location;
        this.boss = (Witch) location.getWorld().spawnEntity(location, EntityType.WITCH);
        setupBoss();
        startBehavior();
    }

    private void setupBoss() {
        boss.setCustomName(
                ChatColor.DARK_GREEN + "🌳 " + ChatColor.BOLD + "DRÍADE CORRUPTA" + ChatColor.DARK_GREEN + " 🌳");
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setCanPickupItems(false);

        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1500.0);
        boss.setHealth(1500.0);
        boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8);

        // Efectos permanentes
        boss.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        com.livingtools.manager.BossDropManager.tagDryad(boss);
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

                // Habilidades cada 7 segundos
                if (tick % 140 == 0) {
                    useAbility();
                }

                // Invocar treants cada 12 segundos en fase 2+
                if (phase >= 2 && tick % 240 == 0) {
                    summonTreants();
                }

                // Regeneración en bosque
                if (tick % 40 == 0 && isInForest()) {
                    boss.setHealth(Math.min(boss.getHealth() + 20,
                            boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
                }

                // Partículas de naturaleza
                if (tick % 10 == 0) {
                    spawnNatureParticles();
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
                entanglingRoots();
                break;
            case 1:
                poisonousSpores();
                break;
            case 2:
                thornBarrier();
                break;
        }
    }

    private void entanglingRoots() {
        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_GRASS_BREAK, 2.0f, 0.5f);

        for (Player player : getNearbyPlayers(15)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.sendMessage(ChatColor.DARK_GREEN + "¡Raíces te atrapan!");

            // Crear bloques de enredaderas temporales
            Location playerLoc = player.getLocation();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = playerLoc.clone().add(x, 0, z).getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.VINE);

                        // Remover después de 5 segundos
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (block.getType() == Material.VINE) {
                                    block.setType(Material.AIR);
                                }
                            }
                        }.runTaskLater(LivingToolsPlugin.getInstance(), 100L);
                    }
                }
            }
        }
    }

    private void poisonousSpores() {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITCH_THROW, 2.0f, 0.8f);

        for (Player player : getNearbyPlayers(20)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
            player.damage(10.0, boss);
        }

        // Partículas de esporas
        for (int i = 0; i < 100; i++) {
            boss.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                    boss.getLocation().add(Math.random() * 15 - 7.5, Math.random() * 3, Math.random() * 15 - 7.5),
                    1);
        }
    }

    private void thornBarrier() {
        // Crear barrera de espinas alrededor del jefe
        double radius = 5;
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = boss.getLocation().add(x, 0, z);

            if (loc.getBlock().getType() == Material.AIR) {
                loc.getBlock().setType(Material.SWEET_BERRY_BUSH);

                // Remover después de 10 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (loc.getBlock().getType() == Material.SWEET_BERRY_BUSH) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }
                }.runTaskLater(LivingToolsPlugin.getInstance(), 200L);
            }
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_SWEET_BERRY_BUSH_PLACE, 2.0f, 0.5f);
    }

    private void summonTreants() {
        for (int i = 0; i < 2; i++) {
            Location spawnLoc = boss.getLocation().add(
                    Math.random() * 8 - 4,
                    0,
                    Math.random() * 8 - 4);

            Zombie treant = (Zombie) boss.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            treant.setCustomName(ChatColor.GREEN + "Treant");
            treant.setCustomNameVisible(true);
            treant.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
            treant.setHealth(80.0);
            treant.getEquipment().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
            treant.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));

            treants.add(treant);
        }

        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_WOOD_BREAK, 2.0f, 0.5f);
    }

    private void spawnNatureParticles() {
        boss.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                boss.getLocation().add(0, 2, 0),
                3, 0.5, 0.5, 0.5);
    }

    private boolean isInForest() {
        int treeCount = 0;
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                Block block = boss.getLocation().add(x, 0, z).getBlock();
                if (block.getType().name().contains("LOG") || block.getType().name().contains("LEAVES")) {
                    treeCount++;
                }
            }
        }
        return treeCount > 20;
    }

    private void announcePhase2() {
        for (Player player : getNearbyPlayers(50)) {
            player.sendTitle(
                    ChatColor.DARK_GREEN + "🌿 FASE 2 🌿",
                    ChatColor.GREEN + "¡La Dríade invoca a la naturaleza!",
                    10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
        }
    }

    private void announcePhase3() {
        for (Player player : getNearbyPlayers(50)) {
            player.sendTitle(
                    ChatColor.DARK_RED + "🌿 FASE FINAL 🌿",
                    ChatColor.RED + "¡El bosque se rebela!",
                    10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
        }
    }

    private void onDeath() {
        if (deathHandled) {
            return;
        }
        deathHandled = true;

        // Limpiar treants
        for (Zombie treant : treants) {
            if (!treant.isDead()) {
                treant.remove();
            }
        }

        // Limpieza y anuncio (drops vía BossDropManager + loot bag)
        for (Player player : getNearbyPlayers(100)) {
            player.sendTitle(
                    ChatColor.GOLD + "⚔ VICTORIA ⚔",
                    ChatColor.YELLOW + "¡La Dríade Corrupta ha sido purificada!",
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

    public Witch getBoss() {
        return boss;
    }
}
