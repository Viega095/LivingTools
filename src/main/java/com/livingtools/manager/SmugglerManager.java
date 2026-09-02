package com.livingtools.manager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SmugglerManager implements Listener {

    private static final String SMUGGLER_NAME = ChatColor.DARK_RED + "Contrabandista";

    public static void spawnSmuggler(Location loc) {
        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        villager.setCustomName(SMUGGLER_NAME);
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.NITWIT);
        villager.setVillagerType(Villager.Type.SWAMP);
        villager.setAI(false); // No movement
        villager.setInvulnerable(true);

        // Optional: Add some particles or sound
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_VILLAGER_AMBIENT, 1, 0.5f);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!(event.getRightClicked() instanceof Villager))
            return;

        Villager villager = (Villager) event.getRightClicked();
        if (villager.getCustomName() == null || !villager.getCustomName().equals(SMUGGLER_NAME))
            return;

        event.setCancelled(true); // Prevent trading UI
        openSmugglerGUI(event.getPlayer());
    }

    private void openSmugglerGUI(Player player) {

        player.sendMessage(ChatColor.GRAY + "El contrabandista te mira con sospecha...");
        new com.livingtools.gui.SmugglerGUI(player).open();
    }
}
