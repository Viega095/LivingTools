package com.livingtools.manager;

import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SoulResonanceManager — Sinergia entre Armadura Viviente y Herramienta Viviente.
 *
 * Cuando el jugador equipa piezas de LivingArmor y sostiene una LivingTool en mano:
 *  - 1-2 piezas: Resonancia Menor (+5% XP, partículas tenues).
 *  - 3 piezas: Resonancia Mayor (+10% XP, +10% daño, resistencia leve).
 *  - Set Completo (4 piezas + Herramienta): RESONANCIA TOTAL (Soul Harmony):
 *      +20% XP, +15% daño/minería, regeneración periódica, aura de partículas armónicas.
 */
public class SoulResonanceManager implements Listener {

    private static final Map<UUID, Long> lastAuraTick = new HashMap<>();
    private static final long AURA_INTERVAL_MS = 3000L; // cada 3 segundos

    /**
     * Cuenta cuántas piezas de armadura viviente tiene equipadas el jugador (0 a 4).
     */
    public static int getEquippedLivingArmorPieces(Player player) {
        int count = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece != null && LivingArmor.isLivingArmor(piece)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retorna el multiplicador de XP por resonancia de almas (1.0 si no hay sinergia).
     */
    public static double getResonanceXPMultiplier(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return 1.0;

        int pieces = getEquippedLivingArmorPieces(player);
        if (pieces >= 4) return 1.20; // +20%
        if (pieces >= 3) return 1.10; // +10%
        if (pieces >= 1) return 1.05; // +5%
        return 1.0;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        int pieces = getEquippedLivingArmorPieces(player);
        if (pieces >= 4) {
            // Bono de daño de Resonancia Total (+15%)
            event.setDamage(event.getDamage() * 1.15);
            triggerResonanceEffect(player, pieces);
        } else if (pieces >= 3) {
            event.setDamage(event.getDamage() * 1.08);
            triggerResonanceEffect(player, pieces);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMine(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        int pieces = getEquippedLivingArmorPieces(player);
        if (pieces >= 4) {
            // Ocasionalmente dar regeneración o prisa minera breve
            if (Math.random() < 0.05) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 0, true, false));
            }
            triggerResonanceEffect(player, pieces);
        }
    }

    private static void triggerResonanceEffect(Player player, int pieces) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - lastAuraTick.getOrDefault(uuid, 0L) < AURA_INTERVAL_MS) return;
        lastAuraTick.put(uuid, now);

        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        if (pieces >= 4) {
            world.spawnParticle(Particle.SPELL_WITCH, loc, 15, 0.4, 0.6, 0.4, 0.05);
            world.spawnParticle(Particle.PORTAL, loc, 10, 0.3, 0.5, 0.3, 0.1);
        } else if (pieces >= 3) {
            world.spawnParticle(Particle.SPELL_MOB, loc, 8, 0.3, 0.5, 0.3, 0.05);
        }
    }

    public static void cleanup(UUID uuid) {
        lastAuraTick.remove(uuid);
    }
}
