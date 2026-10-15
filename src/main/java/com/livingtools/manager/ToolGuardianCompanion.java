package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * ToolGuardianCompanion — Espíritu Guardián flotante manifestado por el alma de la herramienta.
 *
 * Funcionalidades:
 *  - Ángel de la Guarda: Si el portador recibe daño letal, absorbe el golpe, sana 4 corazones y otorga buffs de supervivencia (cooldown 10m).
 *  - Radar Resonante: Detecta Ancient Debris, Diamantes y Esmeraldas a 12 bloques con partículas guía.
 *  - Vórtice Recolector: Succiona minerales y orbes de experiencia hacia el inventario.
 */
public class ToolGuardianCompanion implements Listener {

    private static final Set<UUID> enabledCompanions = new HashSet<>();
    private static final Map<UUID, Long> lastRadarScan = new HashMap<>();
    private static final Map<UUID, Long> angelCooldowns = new HashMap<>();
    private static final long ANGEL_COOLDOWN_MS = 10 * 60 * 1000L; // 10 minutos
    private static BukkitTask companionTask = null;

    public static void startLoop() {
        if (companionTask != null) companionTask.cancel();

        companionTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!enabledCompanions.contains(player.getUniqueId())) continue;

                    ItemStack held = player.getInventory().getItemInMainHand();
                    if (!LivingTool.isLivingTool(held)) continue;

                    LivingTool tool = new LivingTool(held);
                    ToolData data = tool.getData();

                    // Posición flotante sobre el hombro del jugador
                    Location eye = player.getEyeLocation();
                    Vector right = eye.getDirection().crossProduct(new Vector(0, 1, 0)).normalize();
                    Location companionLoc = eye.clone().add(right.multiply(0.85)).add(0, 0.35, 0);

                    // Renderizar Wisp Espiritual
                    String personality = data.getPersonality() != null ? data.getPersonality() : "WISE";
                    renderWisp(companionLoc, personality);

                    // Magnetismo de XP y Recolección de Drops de Minerales
                    for (org.bukkit.entity.Entity e : player.getWorld().getNearbyEntities(companionLoc, 6, 6, 6)) {
                        if (e instanceof ExperienceOrb) {
                            Vector pull = player.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.35);
                            e.setVelocity(pull);
                        } else if (e instanceof Item) {
                            Item item = (Item) e;
                            Material mat = item.getItemStack().getType();
                            if (mat.name().contains("ORE") || mat.name().contains("RAW") || mat == Material.DIAMOND
                                    || mat == Material.EMERALD || mat == Material.ANCIENT_DEBRIS || mat == Material.NETHERITE_SCRAP) {
                                Vector pull = player.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.25);
                                item.setVelocity(pull);
                            }
                        }
                    }

                    // Radar de Minerales Ocultos (cada 6 segundos)
                    scanNearbyOres(player, tool, companionLoc);
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 20L, 4L);
    }

    private static void renderWisp(Location loc, String personality) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle coreParticle;
        Color color;
        switch (personality.toUpperCase()) {
            case "AGGRESSIVE":
                coreParticle = Particle.FLAME;
                color = Color.fromRGB(255, 60, 0);
                break;
            case "LAZY":
                coreParticle = Particle.SNOWFLAKE;
                color = Color.fromRGB(120, 220, 255);
                break;
            case "CHEERFUL":
                coreParticle = Particle.ELECTRIC_SPARK;
                color = Color.fromRGB(255, 220, 0);
                break;
            default: // WISE, HEROIC
                coreParticle = Particle.END_ROD;
                color = Color.fromRGB(150, 80, 255);
                break;
        }

        world.spawnParticle(Particle.REDSTONE, loc, 2, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(color, 1.2f));
        world.spawnParticle(coreParticle, loc, 1, 0.05, 0.05, 0.05, 0.01);
    }

    private static void scanNearbyOres(Player player, LivingTool tool, Location companionLoc) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - lastRadarScan.getOrDefault(uuid, 0L) < 6000L) return;
        lastRadarScan.put(uuid, now);

        Location pLoc = player.getLocation();
        int radius = 12; // Ampliado a 12 bloques
        Block targetOre = null;

        searchLoop:
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = pLoc.clone().add(x, y, z).getBlock();
                    Material mat = b.getType();
                    if (mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE
                            || mat == Material.ANCIENT_DEBRIS || mat == Material.EMERALD_ORE || mat == Material.DEEPSLATE_EMERALD_ORE) {
                        targetOre = b;
                        break searchLoop;
                    }
                }
            }
        }

        if (targetOre != null) {
            Location oreLoc = targetOre.getLocation().add(0.5, 0.5, 0.5);
            ParticleOptimizer.spawnLine(companionLoc, oreLoc, Particle.SPELL_WITCH, 0.6, null);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.8f);
        }
    }

    // -------------------------------------------------------------------------
    // Habilidad: Ángel de la Guarda (Salvación Letal)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        if (!enabledCompanions.contains(uuid)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) return;

        // Comprobar si el daño sería letal
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            long lastAngel = angelCooldowns.getOrDefault(uuid, 0L);
            if (System.currentTimeMillis() - lastAngel < ANGEL_COOLDOWN_MS) {
                return; // En cooldown
            }

            // Cancelar la muerte
            event.setCancelled(true);
            angelCooldowns.put(uuid, System.currentTimeMillis());

            // Efectos de salvación
            player.setHealth(8.0); // 4 corazones
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 30, 0, false, false, true));

            Location loc = player.getLocation();
            player.playSound(loc, Sound.ITEM_TOTEM_USE, 1f, 1f);
            player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.8f);
            player.spawnParticle(Particle.TOTEM, loc.add(0, 1, 0), 100, 0.5, 0.8, 0.5, 0.4);

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡ÁNGEL DE LA GUARDA ACTIVADO! " + ChatColor.GOLD + "✦");
            player.sendMessage(ChatColor.YELLOW + "Tu Espíritu Guardián ha interceptado un golpe letal y ha salvado tu vida.");
            player.sendMessage(ChatColor.GRAY + "Cooldown: 10 minutos.");
            player.sendMessage("");
        }
    }

    public static boolean toggleCompanion(Player player) {
        UUID uuid = player.getUniqueId();
        if (enabledCompanions.contains(uuid)) {
            enabledCompanions.remove(uuid);
            player.sendMessage(ChatColor.GRAY + "[LivingTools] Espíritu Guardián desactivado.");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.2f);
            return false;
        } else {
            enabledCompanions.add(uuid);
            player.sendMessage(ChatColor.AQUA + "✦ " + ChatColor.BOLD + "¡Espíritu Guardián Invocado! ✦");
            player.sendMessage(ChatColor.GRAY + "Orbitará a tu lado brindando radar de minerales, recolección y Ángel de la Guarda.");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
            return true;
        }
    }

    public static boolean isEnabled(Player player) {
        return enabledCompanions.contains(player.getUniqueId());
    }

    public static void cleanup(UUID uuid) {
        enabledCompanions.remove(uuid);
        lastRadarScan.remove(uuid);
        angelCooldowns.remove(uuid);
    }
}
