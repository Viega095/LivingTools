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
 * Eventos de mundo globales con moderación de frecuencia.
 *
 * NOCHE CORRUPTA  → Ocurre con baja probabilidad (15%) al caer la noche, con un cooldown de 90 minutos.
 * TORMENTA DE RUNAS → Se activa de forma controlada durante tormentas eléctricas.
 */
public class ServerEventManager implements Listener {

    public enum WorldEvent {
        NONE, CORRUPTED_NIGHT, RUNE_STORM
    }

    // Estado por mundo
    private static final Map<UUID, WorldEvent> activeEvents = new HashMap<>();
    private static final Map<UUID, Long> lastCorruptedNightTime = new HashMap<>();
    private static final long CORRUPTED_NIGHT_COOLDOWN_MS = 90 * 60 * 1000L; // 90 minutos
    private static final Random random = new Random();

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

                    WorldEvent desired = WorldEvent.NONE;

                    if (storming) {
                        desired = WorldEvent.RUNE_STORM;
                    } else if (nightTime) {
                        // Si ya está activa en esta noche, la mantiene
                        if (current == WorldEvent.CORRUPTED_NIGHT) {
                            desired = WorldEvent.CORRUPTED_NIGHT;
                        } else if (time >= 13800 && time <= 14200) {
                            // Al inicio de la noche, verificar cooldown y probabilidad del 15%
                            long lastTime = lastCorruptedNightTime.getOrDefault(world.getUID(), 0L);
                            if (System.currentTimeMillis() - lastTime > CORRUPTED_NIGHT_COOLDOWN_MS) {
                                if (random.nextDouble() < 0.15) { // 15% de probabilidad
                                    desired = WorldEvent.CORRUPTED_NIGHT;
                                    lastCorruptedNightTime.put(world.getUID(), System.currentTimeMillis());
                                }
                            }
                        }
                    }

                    if (desired != current) {
                        onEventChange(world, current, desired);
                        activeEvents.put(world.getUID(), desired);
                    }
                }
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 200L, 200L); // cada 10s
    }

    private static void onEventChange(World world, WorldEvent old, WorldEvent next) {
        if (next == WorldEvent.NONE) {
            if (old != WorldEvent.NONE) {
                for (Player p : world.getPlayers()) {
                    p.sendMessage(ChatColor.GRAY + "[LivingTools] El evento " + (old == WorldEvent.CORRUPTED_NIGHT ? "Noche Corrupta" : "Tormenta de Runas") + " ha finalizado.");
                }
            }
            return;
        }

        String[] lines = getEventAnnounce(next);
        for (Player p : world.getPlayers()) {
            p.sendMessage("");
            for (String l : lines) p.sendMessage(l);
            p.sendMessage("");
            p.playSound(p.getLocation(), next == WorldEvent.CORRUPTED_NIGHT
                    ? Sound.AMBIENT_CAVE : Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 0.8f);
        }
    }

    private static String[] getEventAnnounce(WorldEvent e) {
        switch (e) {
            case CORRUPTED_NIGHT:
                return new String[]{
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "☠ ¡UNA NOCHE CORRUPTA HA DESCENDIDO! ☠",
                    ChatColor.RED + "Los monstruos han absorbido energía sombría (+50% XP de combate, mobs hostiles potenciados).",
                    ChatColor.GRAY + "Aprovecha la penumbra para recolectar Esencias de Oscuridad."
                };
            case RUNE_STORM:
                return new String[]{
                    ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "⚡ ¡TORMENTA DE RUNAS CELESTIAL! ⚡",
                    ChatColor.AQUA + "La energía de los rayos potencia la forja (+300% probabilidad de Geodas Rúnicas al minar).",
                    ChatColor.GRAY + "Tus runas resuenan con el poder de la tormenta."
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
        if (meta != null) {
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
        }
        return shard;
    }

    public static boolean isShardOfDarkness(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "shard_of_darkness");
        return item.getItemMeta().getPersistentDataContainer()
                .has(key, org.bukkit.persistence.PersistentDataType.BYTE);
    }
}
