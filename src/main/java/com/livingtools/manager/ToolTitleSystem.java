package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * ToolTitleSystem — otorga títulos especiales a la herramienta en base
 * al nivel, prestige, mobs eliminados, y hitos completados.
 *
 * Títulos disponibles (solo uno visible a la vez — el más alto desbloqueado):
 *
 *   Nivel 10    → "Aprendiz"
 *   Nivel 25    → "Forjado"
 *   Nivel 50    → "Veterano"
 *   Nivel 75    → "Guardián"
 *   Nivel 100   → "Maestro"
 *   Nivel 150   → "Gran Maestro"
 *   Nivel 200   → "Ascendido"
 *   Prestige 1+ → "El Inmortal"
 *   Prestige 3+ → "El Eterno"
 *   500+ kills  → "Cazador"
 *   2000+ kills → "Verdugo"
 *   5000+ kills → "La Muerte"
 *   Todos hitos → "Completo"
 *
 * El título aparece en el lore y en el displayName de la herramienta.
 */
public class ToolTitleSystem {

    // PDC key para guardar el título desbloqueado actual
    private static NamespacedKey KEY_EARNED_TITLE;
    private static NamespacedKey keyEarnedTitle() {
        if (KEY_EARNED_TITLE == null)
            KEY_EARNED_TITLE = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "earned_title");
        return KEY_EARNED_TITLE;
    }

    public enum ToolTitle {
        // En orden de prioridad (el más alto al final, ese se muestra)
        APRENDIZ      ("Aprendiz",    ChatColor.GRAY),
        FORJADO       ("Forjado",     ChatColor.WHITE),
        VETERANO      ("Veterano",    ChatColor.GREEN),
        GUARDIAN      ("Guardián",    ChatColor.DARK_GREEN),
        CAZADOR       ("Cazador",     ChatColor.YELLOW),
        MAESTRO       ("Maestro",     ChatColor.GOLD),
        GRAN_MAESTRO  ("Gran Maestro",ChatColor.LIGHT_PURPLE),
        VERDUGO       ("Verdugo",     ChatColor.RED),
        INMORTAL      ("el Inmortal", ChatColor.DARK_RED),
        ASCENDIDO     ("Ascendido",   ChatColor.AQUA),
        LA_MUERTE     ("la Muerte",   ChatColor.DARK_PURPLE),
        ETERNO        ("el Eterno",   ChatColor.DARK_GRAY),
        COMPLETO      ("el Completo", ChatColor.DARK_AQUA);

        final String displayName;
        final ChatColor color;

        ToolTitle(String displayName, ChatColor color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String formatted() {
            return color + "「" + displayName + "」";
        }
    }

    /**
     * Evalúa todos los títulos que debería tener la herramienta y devuelve
     * el más alto. Null si no hay ninguno desbloqueado.
     */
    public static ToolTitle getHighestTitle(LivingTool tool) {
        ToolData data = tool.getData();
        int level = data.getLevel();
        int prestige = data.getPrestige();
        long kills = data.getMobKills();

        // Verificar hitos completos
        boolean allMilestones = MilestoneManager.countCompleted(tool) >= MilestoneManager.Milestone.values().length;

        ToolTitle highest = null;

        if (level >= 10)    highest = ToolTitle.APRENDIZ;
        if (level >= 25)    highest = ToolTitle.FORJADO;
        if (level >= 50)    highest = ToolTitle.VETERANO;
        if (level >= 75)    highest = ToolTitle.GUARDIAN;
        if (kills >= 500)   highest = ToolTitle.CAZADOR;
        if (level >= 100)   highest = ToolTitle.MAESTRO;
        if (level >= 150)   highest = ToolTitle.GRAN_MAESTRO;
        if (kills >= 2000)  highest = ToolTitle.VERDUGO;
        if (prestige >= 1)  highest = ToolTitle.INMORTAL;
        if (level >= 200)   highest = ToolTitle.ASCENDIDO;
        if (kills >= 5000)  highest = ToolTitle.LA_MUERTE;
        if (prestige >= 3)  highest = ToolTitle.ETERNO;
        if (allMilestones)  highest = ToolTitle.COMPLETO;

        return highest;
    }

    /**
     * Guarda el título actual en PDC de la herramienta.
     * Llama esto cuando el nivel/prestige/kills cambie.
     */
    public static boolean updateTitle(Player player, LivingTool tool) {
        ToolTitle newTitle = getHighestTitle(tool);
        if (newTitle == null) return false;

        // Verificar si el título cambió
        ItemStack item = tool.getItem();
        if (!item.hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

        String currentTitleStr = meta.getPersistentDataContainer()
                .getOrDefault(keyEarnedTitle(), PersistentDataType.STRING, "");

        if (newTitle.name().equals(currentTitleStr)) return false; // Sin cambio

        // Guardar nuevo título
        meta.getPersistentDataContainer().set(keyEarnedTitle(), PersistentDataType.STRING, newTitle.name());
        item.setItemMeta(meta);

        // Notificar al jugador
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ¡Nuevo título desbloqueado! "
                + newTitle.formatted());
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);

        return true;
    }

    /**
     * Obtiene el título guardado en el PDC de la herramienta (o null).
     */
    public static ToolTitle getSavedTitle(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return null;
        String saved = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(keyEarnedTitle(), PersistentDataType.STRING);
        if (saved == null || saved.isEmpty()) return null;
        try {
            return ToolTitle.valueOf(saved);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Retorna el texto formateado del título para incluir en el lore.
     */
    public static String getTitleLoreLine(LivingTool tool) {
        ToolTitle title = getSavedTitle(tool);
        if (title == null) return null;
        return ChatColor.DARK_GRAY + "Título: " + title.formatted();
    }
}
