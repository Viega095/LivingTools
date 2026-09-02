package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Eventos de mundo globales que afectan a todos los jugadores.
 *
 * NOCHE CORRUPTA  → Activo de medianoche (tick 18000) a amanecer (tick 24000)
 *   - Mobs hostiles tienen más HP y +20% daño
 *   - +50% XP de combate
 *   - Chance de drop de Shard of Darkness al matar mobs
 *
 * TORMENTA DE RUNAS → Se activa con tormenta eléctrica
 *   - +300% chance de drop de Geoda Rúnica al minar
 *   - Chance de que runas equipadas ganen un nivel de "resonancia" temporal
 */
public class ServerEventManager implements Listener {

    public enum WorldEvent {
        NONE, CORRUPTED_NIGHT, RUNE_STORM
    }

    // Estado por mundo
    private static final Map<UUID, WorldEvent> activeEvents = new HashMap<>();

    // -----------------------------------------------------------------------
    // Tick checker — registrado en onEnable
    // -----------------------------------------------------------------------
    public static void startEventScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL) continue;
                    WorldEvent current = activeEvents.getOrDefault(world.getUID(), WorldEvent.NONE);
                    long time = world.getTime();
                    boolean nightTime = time >= 13800 && time <= 23000;
                    boolean storming = world.isThundering();

                    WorldEvent desired = storming ? WorldEvent.RUNE_STORM
                            : nightTime ? WorldEvent.CORRUPTED_NIGHT
                            : WorldEvent.NONE;

                    if (desired != current) {
                        onEventChange(world, current, desired);
                        activeEvents.put(world.getUID(), desired);
                    }
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 100L); // cada 5s
    }

    private static void onEventChange(World world, WorldEvent old, WorldEvent next) {
        if (next == WorldEvent.NONE) {
            if (old != WorldEvent.NONE) {
                for (Player p : world.getPlayers()) {
                    p.sendMessage(ChatColor.GRAY + "[LivingTools] " + ChatColor.RESET
                            + "El evento de mundo ha terminado.");
                }
            }
            return;
        }
        String[] lines = getEventAnnounce(next);
        for (Player p : world.getPlayers()) {
            for (String l : lines) p.sendMessage(l);
            p.sendTitle(lines[0], lines[1], 15, 60, 20);
            p.playSound(p.getLocation(), next == WorldEvent.CORRUPTED_NIGHT
                    ? Sound.AMBIENT_CAVE : Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 0.8f);
        }
    }

    private static String[] getEventAnnounce(WorldEvent e) {
        switch (e) {
            case CORRUPTED_NIGHT:
                return new String[]{
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "☠ NOCHE CORRUPTA",
                    ChatColor.RED + "Los mobs se vuelven más peligrosos — +50% XP de combate",
                    ChatColor.GRAY + "[LivingTools] " + ChatColor.RED + "☠ Noche Corrupta — "
                            + ChatColor.WHITE + "+50% XP de combate, mobs más fuertes, drops especiales"
                };
            case RUNE_STORM:
                return new String[]{
                    ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "⚡ TORMENTA DE RUNAS",
                    ChatColor.AQUA + "+300% chance de Geoda Rúnica al minar",
                    ChatColor.GRAY + "[LivingTools] " + ChatColor.LIGHT_PURPLE + "⚡ Tormenta de Runas — "
                            + ChatColor.WHITE + "Mayor chance de Geodas y Resonancia de Runas activa"
                };
            default: return new String[]{"", "", ""};
        }
    }

    // -----------------------------------------------------------------------
    // API — consultada por ExperienceListener y RuneManager
    // -----------------------------------------------------------------------

    public static WorldEvent getActiveEvent(World world) {
        return activeEvents.getOrDefault(world.getUID(), WorldEvent.NONE);
    }

    /** Multiplicador de XP de combate para Noche Corrupta */
    public static double getCombatXPMultiplier(World world) {
        return getActiveEvent(world) == WorldEvent.CORRUPTED_NIGHT ? 1.5 : 1.0;
    }

    /** Multiplicador de chance de geoda para Tormenta de Runas */
    public static double getGeodaDropMultiplier(World world) {
        return getActiveEvent(world) == WorldEvent.RUNE_STORM ? 4.0 : 1.0;
    }

    /** Shard of Darkness — drop especial de la Noche Corrupta */
    public static ItemStack createShardOfDarkness() {
        ItemStack shard = new ItemStack(Material.AMETHYST_SHARD);
        org.bukkit.inventory.meta.ItemMeta meta = shard.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Shard of Darkness");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Un fragmento de corrupción pura.",
                ChatColor.GRAY + "Úsalo en la Forja Maldita para",
                ChatColor.GRAY + "desbloquear encantamientos oscuros.",
                "",
                ChatColor.DARK_RED + "⚠ Corrompido"
        ));
        meta.setCustomModelData(9001);
        NamespacedKey key = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "shard_of_darkness");
        meta.getPersistentDataContainer().set(key,
                org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        shard.setItemMeta(meta);
        return shard;
    }

    public static boolean isShardOfDarkness(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "shard_of_darkness");
        return item.getItemMeta().getPersistentDataContainer()
                .has(key, org.bukkit.persistence.PersistentDataType.BYTE);
    }
}
