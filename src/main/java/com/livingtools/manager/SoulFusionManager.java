package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * SoulFusionManager — Motor de Fusión de Almas y Personalidades Híbridas.
 *
 * Permite fusionar dos herramientas vivientes sacrificando la secundaria
 * para desbloquear una Personalidad Híbrida con sinergias elementales duales,
 * heredar hasta un 50% de la experiencia acumulada y despertar un Rasgo Quimérico.
 */
public class SoulFusionManager {

    private static NamespacedKey KEY_HYBRID_PERSONALITY;
    private static NamespacedKey KEY_CHIMERA_TRAIT;

    public static NamespacedKey getKeyHybridPersonality() {
        if (KEY_HYBRID_PERSONALITY == null) {
            KEY_HYBRID_PERSONALITY = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_hybrid_personality");
        }
        return KEY_HYBRID_PERSONALITY;
    }

    public static NamespacedKey getKeyChimeraTrait() {
        if (KEY_CHIMERA_TRAIT == null) {
            KEY_CHIMERA_TRAIT = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_chimera_trait");
        }
        return KEY_CHIMERA_TRAIT;
    }

    public enum HybridPersonality {
        ANCIENT_FURY("Furia Ancestral", ChatColor.RED + "⚔ Furia Ancestral",
                "Combina la furia ígnea con sabiduría táctica (+25% Daño Crítico, -20% Coste Habilidades)",
                "AGGRESSIVE", "WISE", Particle.FLAME, Particle.TOTEM),
        SERENE_HARMONY("Armonía Serena", ChatColor.AQUA + "❄ Armonía Serena",
                "Paz gélida y alegría radiante (Regeneración de Durabilidad y Aura de Haste)",
                "CHEERFUL", "LAZY", Particle.SNOWFLAKE, Particle.ELECTRIC_SPARK),
        LIGHTNING_STORM("Tormenta Eléctrica", ChatColor.GOLD + "⚡ Tormenta Eléctrica",
                "Ira desatada con relámpagos (Descargas en cadena en cada golpe cargado)",
                "AGGRESSIVE", "CHEERFUL", Particle.FLAME, Particle.ELECTRIC_SPARK),
        VOID_TWILIGHT("Crepúsculo del Vacío", ChatColor.DARK_PURPLE + "🌑 Crepúsculo del Vacío",
                "Sabiduría milenaria del reposo profundo (Evasión del 15% y Drenaje de Maná)",
                "WISE", "LAZY", Particle.PORTAL, Particle.SPELL_WITCH),
        TRANSCENDENCE("Trascendencia Suprema", ChatColor.LIGHT_PURPLE + "✨ Trascendencia Suprema",
                "Fusión de almas afines idénticas (Multiplicador de XP x1.5 y Resistencia)",
                "*", "*", Particle.END_ROD, Particle.TOTEM);

        private final String id;
        private final String displayName;
        private final String description;
        private final String p1;
        private final String p2;
        private final Particle particle1;
        private final Particle particle2;

        HybridPersonality(String id, String displayName, String description, String p1, String p2, Particle particle1, Particle particle2) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.p1 = p1;
            this.p2 = p2;
            this.particle1 = particle1;
            this.particle2 = particle2;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Particle getParticle1() { return particle1; }
        public Particle getParticle2() { return particle2; }
    }

    /**
     * Determina la personalidad híbrida resultante de combinar dos personalidades.
     */
    public static HybridPersonality calculateHybrid(String pers1, String pers2) {
        if (pers1 == null) pers1 = "WISE";
        if (pers2 == null) pers2 = "WISE";

        pers1 = pers1.toUpperCase();
        pers2 = pers2.toUpperCase();

        if (pers1.equals(pers2)) {
            return HybridPersonality.TRANSCENDENCE;
        }

        Set<String> pair = new HashSet<>(Arrays.asList(pers1, pers2));
        if (pair.contains("AGGRESSIVE") && pair.contains("WISE")) return HybridPersonality.ANCIENT_FURY;
        if (pair.contains("CHEERFUL") && pair.contains("LAZY")) return HybridPersonality.SERENE_HARMONY;
        if (pair.contains("AGGRESSIVE") && pair.contains("CHEERFUL")) return HybridPersonality.LIGHTNING_STORM;
        if (pair.contains("WISE") && pair.contains("LAZY")) return HybridPersonality.VOID_TWILIGHT;

        return HybridPersonality.ANCIENT_FURY; // Fallback
    }

    /**
     * Ejecuta la fusión de almas entre dos herramientas.
     */
    public static boolean executeFusion(Player player, LivingTool primary, LivingTool sacrifice) {
        if (primary == null || sacrifice == null) return false;
        if (primary.getItem().equals(sacrifice.getItem())) return false;

        ToolData primaryData = primary.getData();
        ToolData sacrificeData = sacrifice.getData();

        String pers1 = primaryData.getPersonality();
        String pers2 = sacrificeData.getPersonality();
        HybridPersonality hybrid = calculateHybrid(pers1, pers2);

        // 1. Asignar Personalidad Híbrida al PDC
        primary.getItem().getItemMeta();
        org.bukkit.inventory.meta.ItemMeta meta = primary.getItem().getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getKeyHybridPersonality(), PersistentDataType.STRING, hybrid.name());
            meta.getPersistentDataContainer().set(getKeyChimeraTrait(), PersistentDataType.STRING, hybrid.getDescription());
            primary.getItem().setItemMeta(meta);
        }

        // 2. Transferir el 50% de la XP de la herramienta sacrificada
        long transferredXP = (long) (sacrificeData.getXP() * 0.50);
        if (transferredXP > 0) {
            primary.addXP(player, transferredXP);
        }

        // 3. Actualizar la personalidad base de ToolData para compatibilidad
        primaryData.setPersonality(hybrid.name());
        primary.updateLore();

        // 4. Efectos visuales cinematográficos de Fusión de Almas
        Location loc = player.getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (world != null) {
            ParticleOptimizer.spawnHelix(loc, 1.2, 2.5, hybrid.getParticle1(), null);
            ParticleOptimizer.spawnCircle(loc, 2.0, hybrid.getParticle2(), 24, null);
            world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.4f, 1.2f);
            world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.2f, 1.5f);
        }

        // 5. Destruir la herramienta sacrificada del inventario del jugador
        player.getInventory().removeItem(sacrifice.getItem());

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "╔══════════════════════════════════════════════╗");
        player.sendMessage(ChatColor.GOLD + "  ✦ ¡FUSIÓN DE ALMAS COMPLETADA CON ÉXITO! ✦");
        player.sendMessage(ChatColor.YELLOW + "  Nueva Conciencia: " + hybrid.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "  Efecto: " + ChatColor.ITALIC + hybrid.getDescription());
        player.sendMessage(ChatColor.GREEN + "  +XP Transferida: " + ChatColor.WHITE + transferredXP + " XP");
        player.sendMessage(ChatColor.DARK_PURPLE + "╚══════════════════════════════════════════════╝");
        player.sendMessage("");

        return true;
    }

    public static HybridPersonality getHybridPersonality(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return null;
        String raw = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(getKeyHybridPersonality(), PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return HybridPersonality.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
