package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * GemSocketManager — Sistema de Engarce de Joyas y Gemas Celestiales.
 */
public class GemSocketManager {

    private static NamespacedKey KEY_SOCKETS;

    public static NamespacedKey getKeySockets() {
        if (KEY_SOCKETS == null) {
            KEY_SOCKETS = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_gem_sockets");
        }
        return KEY_SOCKETS;
    }

    public enum GemType {
        PHOENIX_GEM("Gema del Fénix", ChatColor.GOLD + "🔥 Gema del Fénix",
                "Evita la muerte fatal una vez cada 10 min y desata una explosión ígnea.",
                Material.FIRE_CHARGE, 100),
        CHRONO_GEM("Gema del Tiempo", ChatColor.AQUA + "⏳ Gema del Tiempo",
                "Reduce los tiempos de recarga de todas las habilidades en un 25%.",
                Material.CLOCK, 50),
        COSMIC_FORTUNE_GEM("Gema de Fortuna Cósmica", ChatColor.GREEN + "🍀 Gema de Fortuna Cósmica",
                "Triplica la probabilidad de drops raros al minar y combatir.",
                Material.EMERALD, 50),
        BLOOD_JEWEL("Joya de Sangre Eterna", ChatColor.RED + "🩸 Joya de Sangre Eterna",
                "Acumula el 15% del daño infligido para desatarlo en golpes críticos.",
                Material.REDSTONE, 20),
        VOID_STAR_GEM("Gema de Estrella del Vacío", ChatColor.DARK_PURPLE + "✨ Estrella del Vacío",
                "Otorga inmunidad permanente a la lentitud, ceguera y veneno.",
                Material.NETHER_STAR, 80);

        private final String name;
        private final String displayName;
        private final String description;
        private final Material icon;
        private final int requiredLevel;

        GemType(String name, String displayName, String description, Material icon, int requiredLevel) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.requiredLevel = requiredLevel;
        }

        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public int getRequiredLevel() { return requiredLevel; }
    }

    /**
     * Retorna el número máximo de ranuras desbloqueadas según el nivel de la herramienta.
     */
    public static int getMaxUnlockedSockets(LivingTool tool) {
        int lvl = tool.getData().getLevel();
        int prestige = tool.getData().getPrestige();
        if (prestige > 0 || lvl >= 100) return 4;
        if (lvl >= 80) return 3;
        if (lvl >= 50) return 2;
        if (lvl >= 20) return 1;
        return 0;
    }

    /**
     * Retorna la lista de gemas actualmente engarzadas en la herramienta.
     */
    public static List<GemType> getSocketedGems(LivingTool tool) {
        List<GemType> gems = new ArrayList<>();
        if (!tool.getItem().hasItemMeta()) return gems;

        String raw = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(getKeySockets(), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return gems;

        for (String id : raw.split(",")) {
            try {
                gems.add(GemType.valueOf(id.trim()));
            } catch (IllegalArgumentException ignored) {}
        }
        return gems;
    }

    /**
     * Engarza una gema en la primera ranura libre.
     */
    public static boolean socketGem(LivingTool tool, GemType gem) {
        int max = getMaxUnlockedSockets(tool);
        List<GemType> current = getSocketedGems(tool);
        if (current.size() >= max) return false;

        current.add(gem);
        saveSockets(tool, current);
        return true;
    }

    /**
     * Remueve la gema en el índice dado.
     */
    public static GemType unsocketGem(LivingTool tool, int index) {
        List<GemType> current = getSocketedGems(tool);
        if (index < 0 || index >= current.size()) return null;

        GemType removed = current.remove(index);
        saveSockets(tool, current);
        return removed;
    }

    private static void saveSockets(LivingTool tool, List<GemType> gems) {
        ItemStack item = tool.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (gems.isEmpty()) {
            meta.getPersistentDataContainer().remove(getKeySockets());
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < gems.size(); i++) {
                sb.append(gems.get(i).name());
                if (i < gems.size() - 1) sb.append(",");
            }
            meta.getPersistentDataContainer().set(getKeySockets(), PersistentDataType.STRING, sb.toString());
        }
        item.setItemMeta(meta);
        tool.updateLore();
    }

    public static boolean hasGem(LivingTool tool, GemType gem) {
        return getSocketedGems(tool).contains(gem);
    }

    /**
     * Crea un ítem físico de gema para el inventario.
     */
    public static ItemStack createGemItem(GemType gem) {
        ItemStack item = new ItemStack(gem.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(gem.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + gem.getDescription());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Nivel Mínimo Requerido: " + ChatColor.WHITE + gem.getRequiredLevel());
            lore.add(ChatColor.DARK_PURPLE + "✦ Haz click en /lt sockets para engarzar");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "gem_item_type"),
                    PersistentDataType.STRING, gem.name());
            item.setItemMeta(meta);
        }
        return item;
    }
}
