package com.livingtools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @deprecated La interacción con mesas de herrería vive en {@link com.livingtools.manager.AssemblyTableManager}.
 */
@Deprecated
public class BossForgeListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Consolidado en AssemblyTableManager
    }
}
