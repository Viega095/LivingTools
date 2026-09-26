package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * CosmeticTrailManager — Gestión de estelas y auras cosméticas personalizadas.
 */
public class CosmeticTrailManager {

    public enum TrailStyle {
        DEFAULT         ("Por Defecto",         "Efecto visual según personalidad y nivel.", 0, 0),
        SOLAR_FLAME     ("Llama Solar",         "Rastro ardiente de fuego solar.", 50, 0),
        AMETHYST_MIST   ("Bruma de Amatista",   "Destellos etéreos de amatista.", 100, 0),
        VOID_VORTEX     ("Vórtice Abisal",      "Partículas oscuras de portal y magia.", 0, 1),
        LIGHTNING_AURA  ("Tormenta Eléctrica",  "Chispas y energía eléctrica continua.", 150, 0),
        CELESTIAL_DUST  ("Polvo Celestial",     "Llovizna dorada de bendición cósmica.", 200, 0);

        private final String displayName;
        private final String description;
        private final int requiredLevel;
        private final int requiredPrestige;

        TrailStyle(String displayName, String description, int requiredLevel, int requiredPrestige) {
            this.displayName = displayName;
            this.description = description;
            this.requiredLevel = requiredLevel;
            this.requiredPrestige = requiredPrestige;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getRequiredLevel() { return requiredLevel; }
        public int getRequiredPrestige() { return requiredPrestige; }

        public boolean isUnlocked(LivingTool tool) {
            if (tool == null) return false;
            int lvl = tool.getData().getLevel();
            int pres = tool.getData().getPrestige();
            return lvl >= requiredLevel && pres >= requiredPrestige;
        }
    }

    private static NamespacedKey KEY_COSMETIC_TRAIL;
    private static NamespacedKey keyCosmeticTrail() {
        if (KEY_COSMETIC_TRAIL == null)
            KEY_COSMETIC_TRAIL = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "cosmetic_trail");
        return KEY_COSMETIC_TRAIL;
    }

    public static TrailStyle getActiveTrail(LivingTool tool) {
        if (tool == null || !tool.getItem().hasItemMeta()) return TrailStyle.DEFAULT;
        String raw = tool.getItem().getItemMeta().getPersistentDataContainer()
                .getOrDefault(keyCosmeticTrail(), PersistentDataType.STRING, "DEFAULT");
        try {
            return TrailStyle.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return TrailStyle.DEFAULT;
        }
    }

    public static void setActiveTrail(LivingTool tool, TrailStyle trail) {
        if (tool == null || !tool.getItem().hasItemMeta()) return;
        ItemStack item = tool.getItem();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyCosmeticTrail(), PersistentDataType.STRING, trail.name());
        item.setItemMeta(meta);
        tool.updateLore();
    }
}
