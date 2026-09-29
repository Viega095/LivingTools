package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.visuals.ParticleOptimizer;
import com.livingtools.visuals.SoundHarmonicsEngine;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SoulTarotManager — Sistema de Cartas del Destino y Tarot Arcano de Almas.
 */
public class SoulTarotManager {

    public static final long CARD_DURATION_MS = 30 * 60 * 1000L; // 30 min
    public static final long DAILY_COOLDOWN_MS = 24 * 60 * 60 * 1000L; // 24 horas

    public enum TarotCard {
        THE_SUN("☀️ El Sol", "Inmunidad al fuego, curación pasiva a la luz del día y +50% daño ígneo.",
                Material.SUNFLOWER, Color.fromRGB(255, 200, 0), Particle.FLAME),

        THE_MOON("🌙 La Luna", "+100% XP durante la noche, visión nocturna constante y +30% evasión de daño.",
                Material.ENDER_EYE, Color.fromRGB(120, 180, 255), Particle.SPELL_MOB),

        THE_TOWER("🏰 La Torre", "+60% daño de ataque devastador, pero recibes +20% daño adicional.",
                Material.OBSIDIAN, Color.fromRGB(150, 20, 20), Particle.SMOKE_LARGE),

        THE_STAR("🔮 La Estrella", "+50% velocidad, saltos ágiles y +40% regeneración de durabilidad.",
                Material.NETHER_STAR, Color.fromRGB(180, 100, 255), Particle.END_ROD),

        THE_JUSTICE("⚖️ La Justicia", "El 25% del daño recibido se refleja como daño de retribución a los agresores.",
                Material.GOLDEN_SWORD, Color.fromRGB(255, 230, 80), Particle.CRIT_MAGIC),

        WHEEL_OF_FORTUNE("🎡 La Rueda de la Fortuna", "+200% probabilidad de encontrar gemas, reliquias y tesoros raros.",
                Material.EMERALD, Color.fromRGB(50, 255, 100), Particle.VILLAGER_HAPPY);

        private final String displayName;
        private final String description;
        private final Material icon;
        private final Color auraColor;
        private final Particle particle;

        TarotCard(String displayName, String description, Material icon, Color auraColor, Particle particle) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.auraColor = auraColor;
            this.particle = particle;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public Color getAuraColor() { return auraColor; }
        public Particle getParticle() { return particle; }
    }

    private static final Map<UUID, TarotCard> activeCards = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cardExpirations = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastDailyDraw = new ConcurrentHashMap<>();

    public static TarotCard getActiveCard(UUID playerUuid) {
        Long exp = cardExpirations.get(playerUuid);
        if (exp == null) return null;
        if (System.currentTimeMillis() >= exp) {
            activeCards.remove(playerUuid);
            cardExpirations.remove(playerUuid);
            return null;
        }
        return activeCards.get(playerUuid);
    }

    public static long getRemainingSeconds(UUID playerUuid) {
        Long exp = cardExpirations.get(playerUuid);
        if (exp == null) return 0;
        long rem = exp - System.currentTimeMillis();
        return rem > 0 ? rem / 1000 : 0;
    }

    public static boolean canDrawDaily(UUID playerUuid) {
        Long last = lastDailyDraw.get(playerUuid);
        if (last == null) return true;
        return (System.currentTimeMillis() - last >= DAILY_COOLDOWN_MS);
    }

    public static TarotCard drawRandomCard() {
        TarotCard[] cards = TarotCard.values();
        return cards[new Random().nextInt(cards.length)];
    }

    public static boolean applyCard(Player player, TarotCard card) {
        if (player == null || card == null) return false;

        UUID uuid = player.getUniqueId();
        activeCards.put(uuid, card);
        cardExpirations.put(uuid, System.currentTimeMillis() + CARD_DURATION_MS);
        lastDailyDraw.put(uuid, System.currentTimeMillis());

        Location loc = player.getLocation();
        ParticleOptimizer.spawnCircle(loc.add(0, 0.5, 0), 2.5, card.getParticle(), 20, null);
        SoundHarmonicsEngine.playDivineChime(loc);
        SoundHarmonicsEngine.playVictoryFanfare(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦✦✦ ¡HAS ROBADO LA CARTA DEL DESTINO: " + card.getDisplayName() + ChatColor.GOLD + "! ✦✦✦");
        player.sendMessage(ChatColor.YELLOW + "  \"" + card.getDescription() + "\"");
        player.sendMessage(ChatColor.GRAY + "  (Duración: 30 minutos)");
        player.sendMessage("");
        return true;
    }

    public static boolean drawWithToolXP(Player player, LivingTool tool, int xpCost) {
        if (tool.getData().getXP() < xpCost) {
            player.sendMessage(ChatColor.RED + "Tu herramienta necesita al menos " + xpCost + " XP para consultar el Tarot Arcano.");
            return false;
        }

        tool.getData().setXP(tool.getData().getXP() - xpCost);
        tool.updateLore();

        TarotCard card = drawRandomCard();
        return applyCard(player, card);
    }

    public static void cleanup(UUID uuid) {
        activeCards.remove(uuid);
        cardExpirations.remove(uuid);
    }
}
