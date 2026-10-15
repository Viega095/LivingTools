package com.livingtools.ai;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.manager.BloodMoonManager;
import com.livingtools.manager.ServerEventManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * SmartMobAIEngine — Inteligencia Artificial Táctica y Adaptativa de Criaturas.
 *
 * Activa comportamientos dinámicos durante Luna de Sangre, Noche Corrupta y en el Reino:
 *  1. Esqueleto Gladiador: Cambia a espada oscura con robo de vida al acercarse el jugador.
 *  2. Zombie Berserker: Entra en frenesí al bajar del 50% de HP y ejecuta saltos de embestida (Leap).
 *  3. Araña Neurotóxica: Lanza telarañas temporales y veneno ralentizante.
 *  4. Creeper de Pulso Sísmico: Emite pulso sónico que inhabilita escudos antes de detonar.
 */
public class SmartMobAIEngine implements Listener {

    private static final Map<UUID, Long> mobCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> skeletonInMeleeMode = new HashMap<>();
    private static final Map<UUID, Boolean> zombieEnraged = new HashMap<>();
    private static final Map<Location, Material> originalBlockMap = new HashMap<>();
    private static BukkitTask aiLoopTask = null;

    public static void startLoop() {
        if (aiLoopTask != null) aiLoopTask.cancel();

        aiLoopTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    boolean bloodMoon = BloodMoonManager.isBloodMoonActive();
                    boolean corruptedNight = ServerEventManager.getActiveEvent(world) == ServerEventManager.WorldEvent.CORRUPTED_NIGHT;
                    boolean inRealm = world.getName().contains("realm");

                    // Si no hay evento activo en este mundo, solo procesar en cuevas profundas (Y < 0)
                    for (Player player : world.getPlayers()) {
                        Location pLoc = player.getLocation();
                        boolean isDeepCave = pLoc.getY() < 0;

                        if (!bloodMoon && !corruptedNight && !inRealm && !isDeepCave) {
                            continue;
                        }

                        // Monstruos hostiles a menos de 16 bloques del jugador
                        for (Entity entity : world.getNearbyEntities(pLoc, 16, 16, 16)) {
                            if (!(entity instanceof Monster)) continue;
                            Monster mob = (Monster) entity;
                            if (mob.getTarget() == null || !(mob.getTarget() instanceof Player)) continue;

                            Player target = (Player) mob.getTarget();
                            double dist = mob.getLocation().distance(target.getLocation());

                            // 1. Esqueleto Gladiador (Cambio Arco ⇄ Espada)
                            if (mob instanceof Skeleton) {
                                handleSkeletonAI((Skeleton) mob, target, dist);
                            }

                            // 2. Zombie Berserker (Salto / Embestida Leap)
                            else if (mob instanceof Zombie && !(mob instanceof PigZombie)) {
                                handleZombieAI((Zombie) mob, target, dist);
                            }

                            // 3. Araña Neurotóxica (Trampa de Telaraña)
                            else if (mob instanceof Spider) {
                                handleSpiderAI((Spider) mob, target, dist);
                            }

                            // 4. Creeper de Pulso Sísmico (Anti-Escudo)
                            else if (mob instanceof Creeper) {
                                handleCreeperAI((Creeper) mob, target, dist);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 10L); // Cada 10 ticks (0.5s)
    }

    // -------------------------------------------------------------------------
    // Lógica por Criatura
    // -------------------------------------------------------------------------

    private static void handleSkeletonAI(Skeleton skeleton, Player target, double dist) {
        UUID id = skeleton.getUniqueId();
        EntityEquipment eq = skeleton.getEquipment();
        if (eq == null) return;

        boolean inMelee = skeletonInMeleeMode.getOrDefault(id, false);

        // Si el jugador está a 4 bloques o menos -> Desenfunda Espada
        if (dist <= 4.0 && !inMelee) {
            skeletonInMeleeMode.put(id, true);

            ItemStack darkSword = new ItemStack(Material.IRON_SWORD);
            darkSword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
            eq.setItemInMainHand(darkSword);

            Location loc = skeleton.getLocation();
            loc.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1.4f);
            loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc.add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);

            target.sendMessage(ChatColor.RED + "⚠ " + ChatColor.BOLD + "¡El Esqueleto desenfunda una espada oscura con robo de vida!");
        }
        // Si el jugador se aleja a más de 6 bloques -> Vuelve al Arco
        else if (dist > 6.0 && inMelee) {
            skeletonInMeleeMode.put(id, false);

            ItemStack bow = new ItemStack(Material.BOW);
            bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
            eq.setItemInMainHand(bow);

            Location loc = skeleton.getLocation();
            loc.getWorld().playSound(loc, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1f, 1.2f);
        }
    }

    private static void handleZombieAI(Zombie zombie, Player target, double dist) {
        UUID id = zombie.getUniqueId();
        double hp = zombie.getHealth();
        double maxHp = zombie.getMaxHealth();

        // Modo Berserker al bajar del 50% de HP
        if (hp <= maxHp * 0.55 && !zombieEnraged.getOrDefault(id, false)) {
            zombieEnraged.put(id, true);
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1, false, true));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 60, 0, false, true));

            Location loc = zombie.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 0.6f);
            loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc.add(0, 1.5, 0), 8, 0.4, 0.4, 0.4, 0);

            target.sendMessage(ChatColor.DARK_RED + "☠ " + ChatColor.BOLD + "¡El Zombie entra en Furia Berserker!");
        }

        // Salto de Asalto (Leap) cada 8 segundos si el objetivo está a 4-9 bloques
        long now = System.currentTimeMillis();
        long lastLeap = mobCooldowns.getOrDefault(id, 0L);
        if (dist >= 4.0 && dist <= 9.0 && now - lastLeap > 8000L) {
            mobCooldowns.put(id, now);

            Vector dir = target.getLocation().toVector().subtract(zombie.getLocation().toVector()).normalize();
            dir.multiply(1.1).setY(0.45);
            zombie.setVelocity(dir);

            Location loc = zombie.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ATTACK, 1f, 1.3f);
            loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 5, 0.2, 0.2, 0.2, 0.05);
        }
    }

    private static void handleSpiderAI(Spider spider, Player target, double dist) {
        UUID id = spider.getUniqueId();
        long now = System.currentTimeMillis();
        long lastWeb = mobCooldowns.getOrDefault(id, 0L);

        // Dispara telaraña temporal cada 15 segundos si está a 3-8 bloques
        if (dist >= 3.0 && dist <= 8.0 && now - lastWeb > 15000L) {
            mobCooldowns.put(id, now);

            Location targetFeet = target.getLocation().getBlock().getLocation();
            Block block = targetFeet.getBlock();

            if (block.getType() == Material.AIR || block.getType().isAir()) {
                originalBlockMap.put(targetFeet, block.getType());
                block.setType(Material.COBWEB);

                spider.getWorld().playSound(targetFeet, Sound.ENTITY_SPIDER_DEATH, 0.8f, 1.6f);
                spider.getWorld().spawnParticle(Particle.SPELL_MOB, targetFeet.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);

                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 3, 0));
                target.sendMessage(ChatColor.DARK_GREEN + "🕸 " + ChatColor.BOLD + "¡Una Araña te ha atrapado en su telaraña neurotóxica!");

                // Restaurar bloque a aire tras 3 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() == Material.COBWEB) {
                            block.setType(Material.AIR);
                            originalBlockMap.remove(targetFeet);
                            targetFeet.getWorld().spawnParticle(Particle.CLOUD, targetFeet.clone().add(0.5, 0.5, 0.5), 6, 0.2, 0.2, 0.2, 0.05);
                        }
                    }
                }.runTaskLater(LivingToolsPlugin.getInstance(), 60L);
            }
        }
    }

    private static void handleCreeperAI(Creeper creeper, Player target, double dist) {
        if (dist <= 5.0 && target.isBlocking()) {
            UUID id = creeper.getUniqueId();
            long now = System.currentTimeMillis();
            long lastPulse = mobCooldowns.getOrDefault(id, 0L);

            if (now - lastPulse > 12000L) {
                mobCooldowns.put(id, now);

                // Pulso Sísmico que desactiva el escudo
                target.setCooldown(Material.SHIELD, 60); // 3 segundos sin escudo
                Location loc = creeper.getLocation().add(0, 1, 0);

                loc.getWorld().playSound(loc, Sound.ITEM_SHIELD_BREAK, 1.2f, 0.8f);
                loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.8f);
                loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);

                target.sendTitle("", ChatColor.RED + "⚡ ¡ESCUDO DESACTIVADO POR PULSO SÍSMICO! ⚡", 5, 30, 10);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Eventos de Combate (Lifesteal y Limpieza)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Robo de vida de Esqueleto Gladiador
        if (event.getDamager() instanceof Skeleton && event.getEntity() instanceof Player) {
            Skeleton skel = (Skeleton) event.getDamager();
            if (skeletonInMeleeMode.getOrDefault(skel.getUniqueId(), false)) {
                double heal = event.getFinalDamage() * 0.25; // 25% lifesteal
                skel.setHealth(Math.min(skel.getMaxHealth(), skel.getHealth() + heal));

                Location loc = skel.getLocation().add(0, 1, 0);
                loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 4, 0.2, 0.3, 0.2, 0.1);
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_DRINK, 0.8f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        mobCooldowns.remove(id);
        skeletonInMeleeMode.remove(id);
        zombieEnraged.remove(id);
    }
}
