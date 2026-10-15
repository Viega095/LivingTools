package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * ServerHuntGoalManager — Meta Comunitaria Global de Caza del Servidor.
 * Registra bajas de monstruos hostiles de élite y jefes por parte de todos los jugadores.
 * Al alcanzar la meta (100 bajas), activa un evento de Bendición Global de Almas (+100% XP y bonus drops).
 */
public class ServerHuntGoalManager implements Listener {

    private static final int GOAL_TARGET = 100;
    private static int currentProgress = 0;
    private static long blessingEndTime = 0L;

    public static int getCurrentProgress() {
        return currentProgress;
    }

    public static int getGoalTarget() {
        return GOAL_TARGET;
    }

    public static boolean isBlessingActive() {
        return System.currentTimeMillis() < blessingEndTime;
    }

    public static long getRemainingBlessingMinutes() {
        if (!isBlessingActive()) return 0;
        return (blessingEndTime - System.currentTimeMillis()) / (60 * 1000L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // Bonificación si la bendición está activa
        if (isBlessingActive()) {
            ItemStack held = killer.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(held)) {
                LivingTool tool = new LivingTool(held);
                tool.addXP(killer, 25L);
            }
        }

        currentProgress++;

        // Avisos comunitarios en hitos
        if (currentProgress == 25 || currentProgress == 50 || currentProgress == 75) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "🏹 " + ChatColor.BOLD + "META DE CAZA GLOBAL: " +
                    ChatColor.YELLOW + currentProgress + "/" + GOAL_TARGET + " criaturas abatidas.");
        } else if (currentProgress >= GOAL_TARGET) {
            currentProgress = 0;
            blessingEndTime = System.currentTimeMillis() + (30 * 60 * 1000L); // 30 minutos

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "✦✦✦ " + ChatColor.BOLD + "¡META COMUNITARIA ALCANZADA! " + ChatColor.GOLD + "✦✦✦");
                p.sendMessage(ChatColor.YELLOW + "¡El servidor ha completado 100 bajas de caza cooperativa!");
                p.sendMessage(ChatColor.AQUA + "⚡ Efecto Activado: " + ChatColor.GREEN + "Bendición de Almas Primordiales (30m)");
                p.sendMessage(ChatColor.GRAY + "Todas las armas vivientes obtienen el doble de experiencia y fragmentos.");
                p.sendMessage("");
            }
        }
    }
}
