package com.livingtools.listeners;

import com.livingtools.manager.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * PlayerCleanupListener — asegura la liberación de memoria cuando los jugadores se desconectan.
 *
 * Limpia mapas estáticos en memoria en todos los managers para evitar
 * fugas de memoria (memory leaks) en servidores con actividad prolongada.
 */
public class PlayerCleanupListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleCleanup(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        handleCleanup(event.getPlayer().getUniqueId());
    }

    private void handleCleanup(UUID uuid) {
        try {
            SleepBonusManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            BiomeAffinityBonus.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            ToolMemoryManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            WeatherBonusManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            MiningEnchantListener.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            NamingCeremonyManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            TrailManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            ToolAwakeningManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            DailyBonusManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            SoulResonanceManager.cleanup(uuid);
        } catch (Throwable ignored) {}

        try {
            PartyXPManager.cleanup(uuid);
        } catch (Throwable ignored) {}
    }
}
