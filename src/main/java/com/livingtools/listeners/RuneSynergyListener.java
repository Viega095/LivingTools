package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.RuneSynergyEngine;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * RuneSynergyListener — Dispara las super sinergias de runas arcanas durante el combate.
 */
public class RuneSynergyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombatSynergy(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(held)) return;
        LivingTool tool = new LivingTool(held);

        RuneSynergyEngine.applySynergy(player, target, tool, event);
    }
}
