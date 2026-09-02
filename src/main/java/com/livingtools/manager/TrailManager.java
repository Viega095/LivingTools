package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Trails de partículas para herramientas de alto nivel.
 * Nivel 50+  → trail sutil según personalidad
 * Nivel 100+ → trail más intenso con color de personalidad
 * Nivel 200+ → aura completa pre-prestige
 * Prestige   → efecto dorado especial
 */
public class TrailManager implements Listener {

    // Tick cooldown por jugador para no spamear
    private static final Map<UUID, Long> lastTrailTick = new HashMap<>();
    private static final long TRAIL_INTERVAL_MS = 250; // cada 250ms

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Solo si se movió de bloque real (optimización)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()) return;

        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastTrailTick.get(player.getUniqueId());
        if (last != null && now - last < TRAIL_INTERVAL_MS) return;
        lastTrailTick.put(player.getUniqueId(), now);

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        LivingTool tool = new LivingTool(held);
        int level = tool.getData().getLevel();
        int prestige = tool.getData().getPrestige();
        String personality = tool.getData().getPersonality();
        if (personality == null) personality = "WISE";

        // Escala de trail
        if (prestige >= 1) {
            spawnPrestigeAura(player, prestige);
        } else if (level >= 200) {
            spawnPrePrestigeAura(player, personality);
        } else if (level >= 100) {
            spawnLevelTrail(player, personality, true);
        } else if (level >= 50) {
            spawnLevelTrail(player, personality, false);
        }
    }

    // -----------------------------------------------------------------------

    private void spawnPrestigeAura(Player player, int prestige) {
        int count = Math.min(prestige * 4, 12);
        // Dorado con spirals
        player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 0.1, 0),
                count, 0.4, 0.1, 0.4, 0,
                new Particle.DustOptions(Color.fromRGB(255, 200, 0), 1.2f));
        if (prestige >= 2) {
            player.spawnParticle(Particle.END_ROD,
                    player.getLocation().add(0, 0.5, 0), 2, 0.3, 0.3, 0.3, 0.01);
        }
        if (prestige >= 3) {
            player.spawnParticle(Particle.TOTEM,
                    player.getLocation().add(0, 1.0, 0), 3, 0.5, 0.5, 0.5, 0.02);
        }
    }

    private void spawnPrePrestigeAura(Player player, String personality) {
        Color c = getPersonalityColor(personality);
        player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 0.1, 0),
                6, 0.3, 0.1, 0.3, 0, new Particle.DustOptions(c, 1.0f));
        player.spawnParticle(Particle.SPELL_WITCH,
                player.getLocation().add(0, 1.0, 0), 2, 0.2, 0.3, 0.2, 0.01);
    }

    private void spawnLevelTrail(Player player, String personality, boolean intense) {
        Color c = getPersonalityColor(personality);
        int count = intense ? 4 : 2;
        float size  = intense ? 0.9f : 0.6f;
        double spread = intense ? 0.25 : 0.15;
        player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 0.05, 0),
                count, spread, 0.05, spread, 0,
                new Particle.DustOptions(c, size));
    }

    private Color getPersonalityColor(String personality) {
        switch (personality.toUpperCase()) {
            case "AGGRESSIVE": return Color.fromRGB(220, 30, 30);   // Rojo
            case "LAZY":       return Color.fromRGB(80, 80, 200);   // Azul
            case "CHEERFUL":   return Color.fromRGB(255, 180, 0);   // Amarillo
            case "WISE":       return Color.fromRGB(100, 200, 100); // Verde
            default:           return Color.fromRGB(180, 100, 255); // Morado
        }
    }

    // Aura de combate al atacar — llamar desde AbilityListener
    public static void spawnAttackBurst(Player player, LivingTool tool) {
        int level = tool.getData().getLevel();
        if (level < 50) return;
        String personality = tool.getData().getPersonality();
        if (personality == null) return;

        Color c;
        switch (personality.toUpperCase()) {
            case "AGGRESSIVE": c = Color.fromRGB(255, 50, 0); break;
            case "WISE":       c = Color.fromRGB(0, 200, 100); break;
            case "CHEERFUL":   c = Color.fromRGB(255, 220, 0); break;
            default:           c = Color.fromRGB(150, 50, 255); break;
        }
        int count = Math.min(level / 25, 8);
        player.getWorld().spawnParticle(Particle.REDSTONE,
                player.getLocation().add(0, 1, 0),
                count, 0.5, 0.5, 0.5, 0,
                new Particle.DustOptions(c, 1.5f));
    }
}
