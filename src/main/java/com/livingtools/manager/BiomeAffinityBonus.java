package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * BiomeAffinityBonus — la herramienta gana +10% XP extra cuando se usa
 * en su bioma favorito (el más minado según ToolMemoryManager).
 *
 * También muestra un mensaje de reconocimiento la primera vez por sesión.
 */
public class BiomeAffinityBonus {

    private static final double AFFINITY_BONUS = 0.10; // 10% extra

    /**
     * Retorna el multiplicador de XP por afinidad de bioma.
     * 1.10 si está en su bioma favorito, 1.0 en caso contrario.
     */
    public static double getAffinityMultiplier(Player player, LivingTool tool, Biome currentBiome) {
        String favBiome = ToolMemoryManager.getFavoriteBiome(tool);
        if (favBiome == null) return 1.0;

        if (!currentBiome.name().equalsIgnoreCase(favBiome)) return 1.0;

        // Mostrar mensaje de bienvenida al bioma (1 vez por sesión de minería)
        notifyAffinityActive(player, tool, favBiome);
        return 1.0 + AFFINITY_BONUS;
    }

    // --- Session-level notification throttle (in-memory only) ---
    private static final java.util.Map<java.util.UUID, String> lastNotifiedBiome = new java.util.HashMap<>();

    private static void notifyAffinityActive(Player player, LivingTool tool, String biome) {
        String lastBiome = lastNotifiedBiome.get(player.getUniqueId());
        if (biome.equals(lastBiome)) return; // Ya notificado
        lastNotifiedBiome.put(player.getUniqueId(), biome);

        String toolName = tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName()
                ? tool.getItem().getItemMeta().getDisplayName() : ChatColor.GOLD + "Tu herramienta";
        String biomeName = biome.replace("_", " ").toLowerCase();

        MessageUtils.sendActionBar(player,
                ChatColor.LIGHT_PURPLE + "🌍 Afinidad con " + ChatColor.WHITE + biomeName
                + ChatColor.LIGHT_PURPLE + " — +" + (int)(AFFINITY_BONUS * 100) + "% XP");
        player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.ITALIC
                + getAffinityLine(tool.getData()) + ChatColor.GRAY + "\"");
    }

    private static String getAffinityLine(ToolData data) {
        String p = data.getPersonality();
        if (p == null) p = "WISE";
        switch (p) {
            case "AGGRESSIVE": return "¡Este lugar me recuerda nuestras mejores batallas! ¡Vamos!";
            case "WISE": return "Regresamos al lugar donde más aprendimos. El círculo se completa.";
            case "LAZY": return "Ah, este sitio... aquí puedo trabajar sin esforzarme tanto.";
            case "CHEERFUL": return "¡Sí, sí, SÍ! ¡Mi bioma favorito! ¡Hoy va a ser un gran día!";
            default: return "Este lugar nos resulta familiar. La tierra nos recuerda.";
        }
    }
}
