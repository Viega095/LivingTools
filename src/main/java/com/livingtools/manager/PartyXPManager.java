package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PartyXPManager — Bonificación de XP por Proximidad / Vínculo de Cuadrilla.
 *
 * Cuando 2 a 4 jugadores con herramientas vivientes están a menos de 15 bloques:
 *  - +8% de XP por cada compañero cercano con herramienta viviente (máx +24%).
 *  - Partículas armónicas de vínculo entre compañeros.
 */
public class PartyXPManager {

    private static final double BONUS_PER_ALLY = 0.08; // 8% por compañero
    private static final double MAX_PARTY_BONUS = 0.24; // Máximo 24%
    private static final double RADIUS_SQUARED = 225.0; // 15 bloques al cuadrado

    private static final Map<UUID, Long> lastPartyNotification = new HashMap<>();
    private static final long NOTIFICATION_COOLDOWN_MS = 10_000L; // cada 10s

    public static double getPartyMultiplier(Player player) {
        int alliesCount = 0;
        Location loc = player.getLocation();

        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            if (other.getLocation().distanceSquared(loc) <= RADIUS_SQUARED) {
                ItemStack otherHeld = other.getInventory().getItemInMainHand();
                if (LivingTool.isLivingTool(otherHeld)) {
                    alliesCount++;
                    // Spawn tether particle occasionally
                    if (Math.random() < 0.15) {
                        Location mid = loc.clone().add(other.getLocation()).multiply(0.5).add(0, 1, 0);
                        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, mid, 3, 0.2, 0.2, 0.2, 0.02);
                    }
                }
            }
        }

        if (alliesCount == 0) return 1.0;

        double bonus = Math.min(MAX_PARTY_BONUS, alliesCount * BONUS_PER_ALLY);

        // Notificar en ActionBar ocasionalmente
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - lastPartyNotification.getOrDefault(uuid, 0L) > NOTIFICATION_COOLDOWN_MS) {
            lastPartyNotification.put(uuid, now);
            MessageUtils.sendActionBar(player,
                    ChatColor.AQUA + "👥 Vínculo de Cuadrilla: " + ChatColor.GREEN + "+" + (int)(bonus * 100) + "% XP "
                    + ChatColor.GRAY + "(" + alliesCount + " compañeros)");
        }

        return 1.0 + bonus;
    }

    public static void cleanup(UUID uuid) {
        lastPartyNotification.remove(uuid);
    }
}
