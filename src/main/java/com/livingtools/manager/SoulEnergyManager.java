package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * SoulEnergyManager — Sistema dinámico de Energía de Alma y Modo Despertar (Overdrive).
 * Permite cargar el alma de la herramienta viva de 0 a 100% minando y combatiendo,
 * visualizando la energía en el ActionBar y activando el Overdrive apoteósico con Shift + F.
 */
public class SoulEnergyManager implements Listener {

    private static final Map<UUID, Double> energyMap = new HashMap<>();
    private static final Map<UUID, Long> overdriveEndTime = new HashMap<>();
    private static BukkitTask loopTask = null;

    public static void startLoop() {
        if (loopTask != null) loopTask.cancel();

        loopTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (!LivingTool.isLivingTool(hand)) continue;

                    UUID uuid = player.getUniqueId();
                    boolean inOverdrive = isOverdriveActive(player);

                    if (inOverdrive) {
                        long remMs = overdriveEndTime.getOrDefault(uuid, 0L) - System.currentTimeMillis();
                        int remSec = Math.max(0, (int) Math.ceil(remMs / 1000.0));

                        if (remSec <= 0) {
                            overdriveEndTime.remove(uuid);
                            player.sendMessage(ChatColor.GRAY + "[LivingTools] El Modo Despertar ha finalizado.");
                            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                        } else {
                            // Renderizar ActionBar de Overdrive
                            String bar = ChatColor.GOLD + "" + ChatColor.BOLD + "⚡ ¡MODO DESPERTAR ACTIVO (" + remSec + "s)! ⚡";
                            sendActionBar(player, bar);

                            // Partículas de Alas y Halo
                            renderOverdriveWings(player);
                        }
                    } else {
                        double current = getEnergy(player);
                        if (current >= 100.0) {
                            String bar = ChatColor.AQUA + "" + ChatColor.BOLD + "✦ ENERGÍA AL MÁXIMO [100%] ✦ "
                                    + ChatColor.YELLOW + "[Shift + F para Despertar]";
                            sendActionBar(player, bar);
                        } else {
                            String bar = renderProgressBar(current);
                            sendActionBar(player, bar);
                        }
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 20L); // Cada segundo
    }

    private static String renderProgressBar(double current) {
        int totalBars = 10;
        int filled = (int) Math.round((current / 100.0) * totalBars);
        if (filled > totalBars) filled = totalBars;

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.DARK_PURPLE).append("✦ ENERGÍA DE ALMA: [");
        sb.append(ChatColor.LIGHT_PURPLE);
        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        sb.append(ChatColor.DARK_GRAY);
        for (int i = filled; i < totalBars; i++) {
            sb.append("░");
        }
        sb.append(ChatColor.DARK_PURPLE).append("] ");
        sb.append(ChatColor.AQUA).append(String.format("%.0f%%", current)).append(" ✦");
        return sb.toString();
    }

    private static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static double getEnergy(Player player) {
        return energyMap.getOrDefault(player.getUniqueId(), 0.0);
    }

    public static void setEnergy(Player player, double amount) {
        energyMap.put(player.getUniqueId(), Math.min(100.0, Math.max(0.0, amount)));
    }

    public static void addEnergy(Player player, double amount) {
        if (isOverdriveActive(player)) return; // No acumula mientras esté en Overdrive
        double current = getEnergy(player);
        double next = Math.min(100.0, current + amount);
        energyMap.put(player.getUniqueId(), next);

        if (current < 100.0 && next >= 100.0) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.8f);
            player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡ALMA REBOSANTE! "
                    + ChatColor.YELLOW + "Presiona " + ChatColor.AQUA + "Shift + F" + ChatColor.YELLOW + " para liberar el Modo Despertar.");
        }
    }

    public static boolean isOverdriveActive(Player player) {
        Long end = overdriveEndTime.get(player.getUniqueId());
        return end != null && System.currentTimeMillis() < end;
    }

    public static boolean activateOverdrive(Player player) {
        double current = getEnergy(player);
        if (current < 100.0 && !player.isOp()) {
            return false;
        }

        setEnergy(player, 0.0);
        overdriveEndTime.put(player.getUniqueId(), System.currentTimeMillis() + 15_000L); // 15 segundos

        // Sonidos y Efectos apoteósicos
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ITEM_TOTEM_USE, 1f, 1.2f);
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.6f);
        player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);

        player.spawnParticle(Particle.EXPLOSION_HUGE, loc.clone().add(0, 1, 0), 1);
        player.spawnParticle(Particle.TOTEM, loc.clone().add(0, 1, 0), 100, 0.6, 1.0, 0.6, 0.4);

        player.sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "✦ MODO DESPERTAR ✦",
                ChatColor.YELLOW + "¡El poder de tu alma ha sido desatado!", 10, 40, 15);

        // Buffs de velocidad y prisa minera
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 15, 2, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 15, 1, false, false, true));

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(hand)) {
            ToolAwakeningManager.activateAwakening(player, new LivingTool(hand));
        }

        return true;
    }

    private static void renderOverdriveWings(Player player) {
        Location loc = player.getLocation().add(0, 1.2, 0);
        Vector dir = loc.getDirection().normalize();
        Vector right = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

        // Pequeñas alas de partículas radiantes en la espalda
        for (double r = 0.4; r <= 1.2; r += 0.4) {
            Location leftWing = loc.clone().subtract(dir.clone().multiply(0.3)).add(right.clone().multiply(-r)).add(0, r * 0.4, 0);
            Location rightWing = loc.clone().subtract(dir.clone().multiply(0.3)).add(right.clone().multiply(r)).add(0, r * 0.4, 0);

            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, leftWing, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, rightWing, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.END_ROD, leftWing, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.END_ROD, rightWing, 1, 0, 0, 0, 0);
        }
    }

    // -------------------------------------------------------------------------
    // Eventos de Activación y Acumulación
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(mainHand)) return;

        // Cancelar intercambio de mano para usarlo como activación
        event.setCancelled(true);

        if (isOverdriveActive(player)) {
            player.sendMessage(ChatColor.GOLD + "[LivingTools] El Modo Despertar ya se encuentra activo.");
            return;
        }

        if (getEnergy(player) >= 100.0) {
            activateOverdrive(player);
        } else {
            player.sendMessage(ChatColor.YELLOW + "[LivingTools] Energía de Alma insuficiente: "
                    + ChatColor.AQUA + String.format("%.0f%%", getEnergy(player))
                    + ChatColor.YELLOW + " / 100%. ¡Mina o combate para cargarla!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) return;

        Material mat = event.getBlock().getType();
        double gain = 0.5;

        if (mat == Material.COAL_ORE || mat == Material.DEEPSLATE_COAL_ORE || mat == Material.COPPER_ORE) {
            gain = 1.5;
        } else if (mat == Material.IRON_ORE || mat == Material.DEEPSLATE_IRON_ORE) {
            gain = 2.5;
        } else if (mat == Material.GOLD_ORE || mat == Material.DEEPSLATE_GOLD_ORE || mat == Material.REDSTONE_ORE || mat == Material.LAPIS_ORE) {
            gain = 4.0;
        } else if (mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE || mat == Material.EMERALD_ORE || mat == Material.DEEPSLATE_EMERALD_ORE) {
            gain = 8.0;
        } else if (mat == Material.ANCIENT_DEBRIS) {
            gain = 16.0;
        }

        addEnergy(player, gain);

        // Bonificación durante Overdrive: duplicación de drop de minerales
        if (isOverdriveActive(player)) {
            if (mat.name().contains("ORE") || mat == Material.ANCIENT_DEBRIS) {
                for (ItemStack drop : event.getBlock().getDrops(hand, player)) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop.clone());
                }
                player.spawnParticle(Particle.CRIT_MAGIC, event.getBlock().getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKill(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) return;

        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        // Si el mob muere por este golpe
        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            double gain = 3.0;
            if (victim instanceof Monster) gain = 5.0;
            if (victim.getType() == org.bukkit.entity.EntityType.WITHER || victim.getType() == org.bukkit.entity.EntityType.ENDER_DRAGON || victim.getType() == org.bukkit.entity.EntityType.WARDEN) {
                gain = 50.0;
            }
            addEnergy(player, gain);
        }

        // Bonificación de combate durante Overdrive
        if (isOverdriveActive(player)) {
            event.setDamage(event.getDamage() * 1.4); // +40% de daño
            victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation().add(0, 1, 0), 2);
            victim.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, victim.getLocation().add(0, 1, 0), 12, 0.3, 0.5, 0.3, 0.1);

            // Robo de vida espiritual (15% del daño)
            double heal = event.getFinalDamage() * 0.15;
            double nextHealth = Math.min(player.getMaxHealth(), player.getHealth() + heal);
            player.setHealth(nextHealth);
        }
    }
}
