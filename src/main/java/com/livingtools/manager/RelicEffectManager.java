package com.livingtools.manager;

import org.bukkit.block.Biome;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestiona los efectos pasivos de las reliquias de jefes (Dryad, Wyrm, Leviathan).
 * Se activa cuando el jugador tiene la reliquia en inventario.
 */
public class RelicEffectManager implements Listener {

    // Cooldown para el mensaje de activación de reliquia (2 min)
    private static final Map<UUID, Long> messageCD = new HashMap<>();
    private static final long MSG_COOLDOWN_MS = 120_000L;

    // Relic identifier stored in PDC
    private static final String RELIC_KEY_FOREST = "dryad_relic";
    private static final String RELIC_KEY_DESERT  = "wyrm_relic";
    private static final String RELIC_KEY_OCEAN   = "leviathan_relic";
    private static final org.bukkit.NamespacedKey PDC_RELIC_KEY =
            new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "relic_type");

    /** Marca un item como una reliquia del tipo indicado. */
    public static void markRelic(ItemStack item, String relicType) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(PDC_RELIC_KEY, PersistentDataType.STRING, relicType);
        item.setItemMeta(meta);
    }

    /** Devuelve el tipo de reliquia o null si el item no es una reliquia. */
    public static String getRelicType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(PDC_RELIC_KEY, PersistentDataType.STRING);
    }

    // -----------------------------------------------------------------------
    // Tick periódico — aplica efectos pasivos a jugadores con reliquia
    // -----------------------------------------------------------------------

    public static void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    applyRelicEffects(player);
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 60L); // every 3 sec
    }

    private static void applyRelicEffects(Player player) {
        boolean hasDryad     = hasRelicInInventory(player, RELIC_KEY_FOREST);
        boolean hasWyrm      = hasRelicInInventory(player, RELIC_KEY_DESERT);
        boolean hasLeviathan = hasRelicInInventory(player, RELIC_KEY_OCEAN);

        if (!hasDryad && !hasWyrm && !hasLeviathan) return;

        // DRYAD RELIC — Regen + Fuerza en biomas de bosque
        if (hasDryad) {
            boolean inForest = isForestBiome(player);
            if (inForest) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false));
                notifyRelicActive(player, "&2✦ Reliquia del Bosque activa: Regeneración y Fuerza");
            } else {
                // Fuera del bosque: solo regen leve
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false));
            }
        }

        // WYRM RELIC — Resistencia al fuego + Velocidad en biomas desérticos
        if (hasWyrm) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false));
            boolean inDesert = isDesertBiome(player);
            if (inDesert) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false));
                notifyRelicActive(player, "&6✦ Reliquia del Desierto activa: Resistencia al Fuego y Velocidad");
            }
        }

        // LEVIATHAN RELIC — Respiración acuática + Velocidad submarina
        if (hasLeviathan) {
            if (player.isInWater()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 80, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, false, false));
                notifyRelicActive(player, "&3✦ Reliquia Abisal activa: Visión nocturna y Gracia del Delfín");
            } else {
                // Fuera del agua: respiración acuática siempre activa (útil al sumergirse)
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, false, false));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Reducción de daño especial — reliquias reducen daño de su jefe original
    // -----------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Dryad relic: reduce daño de entidades tipo Witch (Dríade)
        if (event.getDamager().getType() == org.bukkit.entity.EntityType.WITCH
                && hasRelicInInventory(player, RELIC_KEY_FOREST)) {
            event.setDamage(event.getDamage() * 0.65); // -35% damage
        }

        // Wyrm relic: reduce daño de Phantoms (mapeados al Wyrm)
        if (event.getDamager().getType() == org.bukkit.entity.EntityType.ELDER_GUARDIAN
                && hasRelicInInventory(player, RELIC_KEY_DESERT)) {
            event.setDamage(event.getDamage() * 0.65);
        }

        // Leviathan relic: reduce daño de Guardian/Elder Guardian
        if ((event.getDamager().getType() == org.bukkit.entity.EntityType.GUARDIAN
                || event.getDamager().getType() == org.bukkit.entity.EntityType.ELDER_GUARDIAN)
                && hasRelicInInventory(player, RELIC_KEY_OCEAN)) {
            event.setDamage(event.getDamage() * 0.50); // -50%
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();

        // Wyrm relic gives 75% fire damage reduction even outside biome
        if (hasRelicInInventory(player, RELIC_KEY_DESERT)
                && (cause == EntityDamageEvent.DamageCause.FIRE
                    || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                    || cause == EntityDamageEvent.DamageCause.LAVA)) {
            event.setDamage(event.getDamage() * 0.25);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static boolean hasRelicInInventory(Player player, String relicType) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (relicType.equals(getRelicType(item))) return true;
        }
        return false;
    }

    private static boolean isForestBiome(Player player) {
        Biome b = player.getLocation().getBlock().getBiome();
        String name = b.name();
        return name.contains("FOREST") || name.contains("JUNGLE") || name.contains("SWAMP")
                || name.contains("TAIGA") || name.contains("BIRCH");
    }

    private static boolean isDesertBiome(Player player) {
        Biome b = player.getLocation().getBlock().getBiome();
        String name = b.name();
        return name.contains("DESERT") || name.contains("BADLANDS") || name.contains("SAVANNA")
                || name.contains("DRY");
    }

    private static void notifyRelicActive(Player player, String msg) {
        long now = System.currentTimeMillis();
        Long last = messageCD.get(player.getUniqueId());
        if (last != null && now - last < MSG_COOLDOWN_MS) return;
        messageCD.put(player.getUniqueId(), now);
        com.livingtools.utils.MessageUtils.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', msg));
    }
}
