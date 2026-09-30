package com.livingtools.listeners;

import com.livingtools.manager.StructureCoreManager;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * StructureCoreListener — Detecta cuando un jugador utiliza un Núcleo de Estructura
 * en el suelo y despliega la estructura automáticamente.
 */
public class StructureCoreListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        StructureCoreManager.StructureType type = StructureCoreManager.getStructureType(held);

        if (type == null) return;

        event.setCancelled(true);
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Block placeLoc = clicked.getRelative(event.getBlockFace());

        // Consumir 1 núcleo si no está en modo creativo
        if (player.getGameMode() != GameMode.CREATIVE) {
            held.setAmount(held.getAmount() - 1);
        }

        // Desplegar estructura con animación y sonido
        StructureCoreManager.deployStructure(player, placeLoc.getLocation(), type);
    }
}
