package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * KillStreakManager — sistema de racha de kills consecutivos.
 *
 * Matar mobs en menos de 5 segundos entre kills construye una racha.
 * La racha da un multiplicador de XP creciente y mensajes en action bar.
 *
 * Streak 3:  ×1.2 XP  — "¡Triple Kill!"
 * Streak 5:  ×1.5 XP  — "¡Pentakill!"
 * Streak 8:  ×1.8 XP  — "¡Imparable!"
 * Streak 10: ×2.0 XP  — "¡Legendario!"  + partículas
 * Streak 15: ×2.5 XP  — "¡DIOS DE LA GUERRA!"  + partículas + sound
 *
 * La racha se reinicia si pasan más de 5 segundos sin matar nada.
 */
public class KillStreakManager implements Listener {

    private static final long STREAK_WINDOW_MS = 5000L; // 5 segundos

    // UUID → [lastKillTime, currentStreak]
    private static final Map<UUID, Long>    lastKillTime = new HashMap<>();
    private static final Map<UUID, Integer> currentStreak = new HashMap<>();

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        if (event.getEntity() instanceof Player) return; // No racha PvP

        org.bukkit.inventory.ItemStack item = killer.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;

        UUID uuid = killer.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastKillTime.getOrDefault(uuid, 0L);
        int streak = currentStreak.getOrDefault(uuid, 0);

        if (now - last <= STREAK_WINDOW_MS) {
            streak++;
        } else {
            streak = 1; // reinicia
        }

        lastKillTime.put(uuid, now);
        currentStreak.put(uuid, streak);

        // Notificaciones por umbrales
        announceStreak(killer, streak, item);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastKillTime.remove(uuid);
        currentStreak.remove(uuid);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Multiplicador de XP según la racha actual (1.0 si no hay racha) */
    public static double getStreakMultiplier(Player player) {
        // Verificar si la racha expiró
        UUID uuid = player.getUniqueId();
        long last = lastKillTime.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - last > STREAK_WINDOW_MS * 2) {
            // Racha fría — no aplica multiplicador pero sí conserva el streak histórico
            return 1.0;
        }
        int streak = currentStreak.getOrDefault(uuid, 0);
        if (streak >= 15) return 2.5;
        if (streak >= 10) return 2.0;
        if (streak >= 8)  return 1.8;
        if (streak >= 5)  return 1.5;
        if (streak >= 3)  return 1.2;
        return 1.0;
    }

    /** Racha actual */
    public static int getStreak(Player player) {
        return currentStreak.getOrDefault(player.getUniqueId(), 0);
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static void announceStreak(Player player, int streak, org.bukkit.inventory.ItemStack item) {
        String barMsg = null;
        boolean doParticles = false;
        boolean doSound = false;

        switch (streak) {
            case 3:
                barMsg = ChatColor.YELLOW + "⚡ ¡Triple Kill!  ×1.2 XP";
                break;
            case 5:
                barMsg = ChatColor.GOLD + "⚔ ¡Pentakill!  ×1.5 XP";
                doParticles = true;
                break;
            case 8:
                barMsg = ChatColor.RED + "🔥 ¡Imparable!  ×1.8 XP";
                doParticles = true;
                doSound = true;
                break;
            case 10:
                barMsg = ChatColor.DARK_RED + "" + ChatColor.BOLD + "💀 ¡LEGENDARIO!  ×2.0 XP";
                doParticles = true;
                doSound = true;
                break;
            case 15:
                barMsg = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "☠ ¡DIOS DE LA GUERRA!  ×2.5 XP";
                doParticles = true;
                doSound = true;
                // Broadcast al servidor
                String toolName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                        ? item.getItemMeta().getDisplayName() : ChatColor.GOLD + "Su herramienta";
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD
                        + "[!] " + ChatColor.WHITE + player.getName()
                        + ChatColor.GRAY + " alcanzó racha ×15 con " + toolName
                        + ChatColor.GRAY + "!");
                break;
            default:
                // Sin mensaje para rachas bajas
                if (streak > 3) barMsg = ChatColor.GRAY + "Racha: " + streak + "  ×" + streakMult(streak) + " XP";
                break;
        }

        if (barMsg != null) MessageUtils.sendActionBar(player, barMsg);

        if (doParticles) {
            player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.3);
        }
        if (doSound) {
            Sound s = streak >= 15 ? Sound.ENTITY_WITHER_SPAWN : Sound.ENTITY_PLAYER_LEVELUP;
            player.playSound(player.getLocation(), s, 1f, streak >= 15 ? 0.5f : 1.2f);
        }
    }

    private static String streakMult(int streak) {
        if (streak >= 15) return "2.5";
        if (streak >= 10) return "2.0";
        if (streak >= 8)  return "1.8";
        if (streak >= 5)  return "1.5";
        return "1.2";
    }
}
