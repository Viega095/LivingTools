package com.livingtools.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

/**
 * WorldEventScheduler — Sistema de Eventos Cíclicos Automáticos del Mundo.
 *
 * Inicia eventos mundiales automáticos cada 2 horas (duración: 15 minutos).
 */
public class WorldEventScheduler {

    public enum AutomatedWorldEvent {
        METEOR_SHOWER   ("🌠 Lluvia de Meteoros Rúnicos",
                ChatColor.AQUA + "¡Fragmentos estelares caen del cielo! +30% geodas/runas y +25% XP de minería.",
                1.25, 1.0),
        BLOOD_MOON      ("🩸 Noche de Luna Sangrienta",
                ChatColor.RED + "¡La oscuridad se intensifica! Mobs otorgan ×2.0 XP de combate.",
                1.0, 2.0),
        SOLAR_RESONANCE ("☀️ Resonancia Solar",
                ChatColor.GOLD + "¡El sol bendice la forja! Prisa Minera gratuita y herramientas indestructibles.",
                1.30, 1.30);

        private final String displayName;
        private final String description;
        private final double miningXPMult;
        private final double combatXPMult;

        AutomatedWorldEvent(String displayName, String description, double miningXPMult, double combatXPMult) {
            this.displayName = displayName;
            this.description = description;
            this.miningXPMult = miningXPMult;
            this.combatXPMult = combatXPMult;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public double getMiningXPMult() { return miningXPMult; }
        public double getCombatXPMult() { return combatXPMult; }
    }

    private static AutomatedWorldEvent activeEvent = null;
    private static long eventEndTimeMs = 0L;
    private static final Random random = new Random();
    private static BukkitTask loopTask = null;

    private static final long EVENT_DURATION_MS = 15 * 60 * 1000L; // 15 minutos
    private static final long INTERVAL_TICKS = 144_000L; // 2 horas en ticks (2 * 72000)

    public static void startScheduler() {
        if (loopTask != null) loopTask.cancel();

        loopTask = new BukkitRunnable() {
            @Override
            public void run() {
                AutomatedWorldEvent[] events = AutomatedWorldEvent.values();
                AutomatedWorldEvent picked = events[random.nextInt(events.length)];
                startEvent(picked);
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 72_000L, INTERVAL_TICKS); // Primer evento en 1 hora, luego cada 2 horas
    }

    public static void startEvent(AutomatedWorldEvent event) {
        activeEvent = event;
        eventEndTimeMs = System.currentTimeMillis() + EVENT_DURATION_MS;

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "═════════════════════════════════════════");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "✦ ¡EVENTO MUNDIAL ACTIVADO! ✦");
        Bukkit.broadcastMessage(ChatColor.WHITE + "  " + event.getDisplayName());
        Bukkit.broadcastMessage(ChatColor.GRAY + "  " + event.getDescription());
        Bukkit.broadcastMessage(ChatColor.AQUA + "  Duración: 15 minutos.");
        Bukkit.broadcastMessage(ChatColor.GOLD + "═════════════════════════════════════════");
        Bukkit.broadcastMessage("");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
        }

        // Programar finalización en 15 min
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeEvent == event) {
                    stopEvent();
                }
            }
        }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 20 * 60 * 15L);
    }

    public static void stopEvent() {
        if (activeEvent != null) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "[LivingTools] El evento "
                    + activeEvent.getDisplayName() + ChatColor.GRAY + " ha concluido.");
            activeEvent = null;
            eventEndTimeMs = 0L;
        }
    }

    public static AutomatedWorldEvent getActiveEvent() {
        if (activeEvent != null && System.currentTimeMillis() > eventEndTimeMs) {
            activeEvent = null;
        }
        return activeEvent;
    }

    public static double getMiningMultiplier() {
        AutomatedWorldEvent e = getActiveEvent();
        return e != null ? e.getMiningXPMult() : 1.0;
    }

    public static double getCombatMultiplier() {
        AutomatedWorldEvent e = getActiveEvent();
        return e != null ? e.getCombatXPMult() : 1.0;
    }

    public static long getRemainingMinutes() {
        if (activeEvent == null) return 0;
        long rem = eventEndTimeMs - System.currentTimeMillis();
        return Math.max(0, rem / 60_000L);
    }
}
