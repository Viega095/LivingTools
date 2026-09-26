package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * TalentTreeManager — Gestión del Árbol de Talentos de LivingTools.
 */
public class TalentTreeManager {

    public enum Talent {
        // Rama Fortuna & Minería
        MINING_INSTINCT ("Instinto Minero",     "Probabilidad de duplicar minerales minados (+5%/+10%/+15%).", 3),
        DEEP_ECHO       ("Eco Profundo",        "Aumenta el radio de escaneo de ORE_ECHO en +3/+6 bloques.", 2),
        TREASURE_MASTER ("Maestro de Tesoros",  "Aumenta la calidad y probabilidad de tesoros (+25%/+50%/+100%).", 3),

        // Rama Poder Marcial
        SPIRIT_STRIKE   ("Golpe Espiritual",    "Probabilidad de golpe crítico mágico de +50% daño (+5%/+10%/+15%).", 3),
        QUICK_AWAKENING ("Despertar Rápido",    "Reduce el cooldown del Despertar en -15s / -30s.", 2),
        IGNITE_BLADE    ("Ira Ígnea",           "Los ataques prenden fuego espiritual al objetivo.", 1),

        // Rama Simbiosis & Alma
        ANCESTRAL_FLOW  ("Flujo Ancestral",     "Aumento permanente de XP para la herramienta (+5%/+10%/+15%).", 3),
        CALM_MIND       ("Mente Serena",        "Reduce en un 50% la pérdida de estado de ánimo y cansancio.", 2),
        IMMORTAL_BOND   ("Lazo Inmortal",       "Salva al jugador de un golpe letal consumiendo durabilidad (CD: 10m).", 1);

        private final String name;
        private final String description;
        private final int maxLevel;

        Talent(String name, String description, int maxLevel) {
            this.name = name;
            this.description = description;
            this.maxLevel = maxLevel;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getMaxLevel() { return maxLevel; }
    }

    private static final Map<Talent, NamespacedKey> TALENT_KEYS = new HashMap<>();

    private static NamespacedKey getKey(Talent talent) {
        return TALENT_KEYS.computeIfAbsent(talent, t ->
                new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "talent_" + t.name().toLowerCase()));
    }

    public static int getTalentLevel(LivingTool tool, Talent talent) {
        if (tool == null || !tool.getItem().hasItemMeta()) return 0;
        return tool.getItem().getItemMeta().getPersistentDataContainer()
                .getOrDefault(getKey(talent), PersistentDataType.INTEGER, 0);
    }

    public static boolean upgradeTalent(LivingTool tool, Talent talent) {
        if (tool == null || !tool.getItem().hasItemMeta()) return false;
        int current = getTalentLevel(tool, talent);
        if (current >= talent.getMaxLevel()) return false;

        int availablePoints = LevelUpRewardManager.getSkillPoints(tool);
        if (availablePoints < 1) return false;

        ItemStack item = tool.getItem();
        ItemMeta meta = item.getItemMeta();

        // Descontar punto de habilidad
        NamespacedKey ptsKey = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "skill_points");
        meta.getPersistentDataContainer().set(ptsKey, PersistentDataType.INTEGER, availablePoints - 1);

        // Subir nivel del talento
        meta.getPersistentDataContainer().set(getKey(talent), PersistentDataType.INTEGER, current + 1);
        item.setItemMeta(meta);
        tool.updateLore();
        return true;
    }

    public static int countTotalSpentPoints(LivingTool tool) {
        int total = 0;
        for (Talent t : Talent.values()) {
            total += getTalentLevel(tool, t);
        }
        return total;
    }

    public static void resetTalents(LivingTool tool) {
        if (tool == null || !tool.getItem().hasItemMeta()) return;
        int spent = countTotalSpentPoints(tool);
        int current = LevelUpRewardManager.getSkillPoints(tool);

        ItemStack item = tool.getItem();
        ItemMeta meta = item.getItemMeta();

        // Resetear todos los talentos a 0
        for (Talent t : Talent.values()) {
            meta.getPersistentDataContainer().remove(getKey(t));
        }

        // Devolver puntos
        NamespacedKey ptsKey = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "skill_points");
        meta.getPersistentDataContainer().set(ptsKey, PersistentDataType.INTEGER, Math.min(20, current + spent));
        item.setItemMeta(meta);
        tool.updateLore();
    }
}
