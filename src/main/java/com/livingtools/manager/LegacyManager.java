package com.livingtools.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema de Legado — cuando una herramienta hace Prestige, queda registrado
 * su material. Si el jugador craftea un nuevo LivingTool del mismo material,
 * hereda un bono de XP permanente (acumulable hasta 3 veces, +10% c/u).
 *
 * Almacenamiento: Map en memoria (por sesión). Para persistencia entre reinicios
 * se integra con el PDC del item al craftear.
 */
public class LegacyManager {

    // UUID -> Map<MaterialName, cantidad de legados (max 3)>
    private static final Map<UUID, Map<String, Integer>> legacyMap = new HashMap<>();

    /**
     * Llama esto después de que una herramienta completa un Prestige.
     */
    public static void registerLegacy(Player player, Material material) {
        Map<String, Integer> playerLegacies = legacyMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        String matKey = getMaterialCategory(material);
        int current = playerLegacies.getOrDefault(matKey, 0);
        if (current >= 3) {
            player.sendMessage(ChatColor.GOLD + "✦ Tu legado en este tipo de herramienta ya está al máximo (III).");
            return;
        }
        playerLegacies.put(matKey, current + 1);
        player.sendMessage(ChatColor.GOLD + "✦ ¡Legado registrado! Tu próxima herramienta de este tipo heredará poder ancestral.");
        player.sendMessage(ChatColor.YELLOW + "  Legados acumulados: " + com.livingtools.data.LivingTool.romanNumeral(current + 1) + "/III");
    }

    /**
     * Retorna el multiplicador de XP heredado para este jugador y tipo de herramienta.
     * +10% por cada legado registrado.
     */
    public static double getLegacyMultiplier(Player player, Material material) {
        Map<String, Integer> playerLegacies = legacyMap.get(player.getUniqueId());
        if (playerLegacies == null) return 1.0;
        String matKey = getMaterialCategory(material);
        int legacies = playerLegacies.getOrDefault(matKey, 0);
        return 1.0 + (legacies * 0.10);
    }

    /**
     * Marca en el PDC del item cuántos legados tiene (para mostrar en lore).
     */
    public static void stampLegacyOnItem(Player player, org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        Map<String, Integer> playerLegacies = legacyMap.get(player.getUniqueId());
        if (playerLegacies == null) return;
        String matKey = getMaterialCategory(item.getType());
        int legacies = playerLegacies.getOrDefault(matKey, 0);
        if (legacies == 0) return;

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "legacy_count");
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, legacies);

        // Add lore line
        java.util.List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();
        // Remove old legacy line if present
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Legado:"));
        lore.add(0, ChatColor.GOLD + "Legado: " + com.livingtools.data.LivingTool.romanNumeral(legacies)
                + ChatColor.GRAY + " (XP +" + (legacies * 10) + "%)");
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Lee cuántos legados tiene este item (desde PDC).
     */
    public static int getLegacyCountFromItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "legacy_count");
        Integer count = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return count != null ? count : 0;
    }

    /**
     * Categoriza el material para agrupar herramientas del mismo "tipo".
     * Por ej. IRON_PICKAXE, DIAMOND_PICKAXE y NETHERITE_PICKAXE comparten categoría "PICKAXE".
     */
    public static String getMaterialCategory(Material material) {
        String name = material.name();
        if (name.endsWith("_PICKAXE")) return "PICKAXE";
        if (name.endsWith("_SWORD"))   return "SWORD";
        if (name.endsWith("_AXE"))     return "AXE";
        if (name.endsWith("_SHOVEL"))  return "SHOVEL";
        if (name.endsWith("_HOE"))     return "HOE";
        if (name.endsWith("_HELMET"))  return "HELMET";
        if (name.endsWith("_CHESTPLATE")) return "CHESTPLATE";
        if (name.endsWith("_LEGGINGS")) return "LEGGINGS";
        if (name.endsWith("_BOOTS"))   return "BOOTS";
        return name; // fallback: exact match
    }
}
