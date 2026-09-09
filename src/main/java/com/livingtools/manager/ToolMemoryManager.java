package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * ToolMemoryManager — la herramienta recuerda su historia.
 *
 * Datos almacenados en PDC:
 *   - Bioma favorito (el más visitado)
 *   - Tipo de mob favorito (el más matado)
 *   - Jugador con quien más juega (UUID + kills compartidas)
 *   - Primera vez que se usó (timestamp en ms)
 *
 * Se actualiza con:
 *   - onBlockMined(player, biome)   → registra el bioma actual
 *   - onKill(player, entityType)    → registra el mob matado
 *
 * Para mostrar en /lt inspect o HistoryGUI.
 */
public class ToolMemoryManager {

    // PDC keys — lazy
    private static NamespacedKey KEY_BIOME_COUNTS;
    private static NamespacedKey KEY_MOB_COUNTS;
    private static NamespacedKey KEY_FIRST_USE;
    private static NamespacedKey KEY_TOTAL_SESSIONS;

    private static NamespacedKey keyBiomeCounts() {
        if (KEY_BIOME_COUNTS == null)
            KEY_BIOME_COUNTS = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "mem_biome");
        return KEY_BIOME_COUNTS;
    }
    private static NamespacedKey keyMobCounts() {
        if (KEY_MOB_COUNTS == null)
            KEY_MOB_COUNTS = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "mem_mob");
        return KEY_MOB_COUNTS;
    }
    private static NamespacedKey keyFirstUse() {
        if (KEY_FIRST_USE == null)
            KEY_FIRST_USE = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "mem_first_use");
        return KEY_FIRST_USE;
    }
    private static NamespacedKey keyTotalSessions() {
        if (KEY_TOTAL_SESSIONS == null)
            KEY_TOTAL_SESSIONS = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "mem_sessions");
        return KEY_TOTAL_SESSIONS;
    }

    // Throttle en memoria para no escribir PDC en cada bloque
    private static final Map<UUID, Long> lastBiomeWrite = new HashMap<>();
    private static final long BIOME_WRITE_INTERVAL_MS = 10_000L; // cada 10s

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Registra el bioma actual del jugador (throttled) */
    public static void onBlockMined(Player player, LivingTool tool, Biome biome) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - lastBiomeWrite.getOrDefault(uuid, 0L) < BIOME_WRITE_INTERVAL_MS) return;
        lastBiomeWrite.put(uuid, now);

        if (!tool.getItem().hasItemMeta()) return;
        incrementCount(tool, keyBiomeCounts(), biome.name());
        ensureFirstUse(tool);
    }

    /** Registra un mob matado */
    public static void onKill(Player player, LivingTool tool, EntityType entityType) {
        if (!tool.getItem().hasItemMeta()) return;
        incrementCount(tool, keyMobCounts(), entityType.name());
        ensureFirstUse(tool);
    }

    /** Bioma favorito (el más registrado). Null si no hay datos. */
    public static String getFavoriteBiome(LivingTool tool) {
        return getTopKey(tool, keyBiomeCounts());
    }

    /** Mob favorito. Null si no hay datos. */
    public static String getFavoriteMob(LivingTool tool) {
        String raw = getTopKey(tool, keyMobCounts());
        if (raw == null) return null;
        // Humanizar
        return raw.replace("_", " ").toLowerCase();
    }

    /** Fecha de primer uso en ms (0 si desconocida) */
    public static long getFirstUseDateMs(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return 0L;
        return tool.getItem().getItemMeta().getPersistentDataContainer()
                .getOrDefault(keyFirstUse(), PersistentDataType.LONG, 0L);
    }

    /** Edad de la herramienta en días (0 si desconocida) */
    public static int getAgeInDays(LivingTool tool) {
        long firstUse = getFirstUseDateMs(tool);
        if (firstUse == 0) return 0;
        return (int)((System.currentTimeMillis() - firstUse) / 86_400_000L);
    }

    /** Todos los biomas visitados con sus conteos (sorted DESC por count) */
    public static List<Map.Entry<String, Integer>> getBiomeHistory(LivingTool tool) {
        return getAllCounts(tool, keyBiomeCounts());
    }

    /** Todos los mobs con sus conteos (sorted DESC) */
    public static List<Map.Entry<String, Integer>> getMobHistory(LivingTool tool) {
        return getAllCounts(tool, keyMobCounts());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static void ensureFirstUse(LivingTool tool) {
        ItemStack item = tool.getItem();
        if (!item.hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(keyFirstUse(), PersistentDataType.LONG)) {
            meta.getPersistentDataContainer().set(keyFirstUse(), PersistentDataType.LONG,
                    System.currentTimeMillis());
            item.setItemMeta(meta);
        }
    }

    /**
     * Incrementa el conteo de una clave en un mapa serializado como CSV "KEY=VAL;KEY=VAL".
     */
    private static void incrementCount(LivingTool tool, NamespacedKey nk, String key) {
        ItemStack item = tool.getItem();
        if (!item.hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        Map<String, Integer> map = parseMap(meta.getPersistentDataContainer()
                .getOrDefault(nk, PersistentDataType.STRING, ""));
        map.merge(key, 1, Integer::sum);
        // Limitar a top-20 entradas para no inflar PDC infinitamente
        if (map.size() > 20) {
            String minKey = map.entrySet().stream()
                    .min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
            if (minKey != null) map.remove(minKey);
        }
        meta.getPersistentDataContainer().set(nk, PersistentDataType.STRING, serializeMap(map));
        item.setItemMeta(meta);
    }

    private static String getTopKey(LivingTool tool, NamespacedKey nk) {
        if (!tool.getItem().hasItemMeta()) return null;
        Map<String, Integer> map = parseMap(tool.getItem().getItemMeta()
                .getPersistentDataContainer().getOrDefault(nk, PersistentDataType.STRING, ""));
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static List<Map.Entry<String, Integer>> getAllCounts(LivingTool tool, NamespacedKey nk) {
        if (!tool.getItem().hasItemMeta()) return Collections.emptyList();
        Map<String, Integer> map = parseMap(tool.getItem().getItemMeta()
                .getPersistentDataContainer().getOrDefault(nk, PersistentDataType.STRING, ""));
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return list;
    }

    private static Map<String, Integer> parseMap(String raw) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split(";")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try { map.put(kv[0], Integer.parseInt(kv[1])); }
                catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private static String serializeMap(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }
}
