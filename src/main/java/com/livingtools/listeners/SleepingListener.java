package com.livingtools.listeners;

import com.livingtools.manager.SleepingManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SleepingListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;

        EntityType type = event.getEntityType();
        double chance = 0.0;
        Material toolType = Material.DIAMOND_PICKAXE; // Default

        if (type == EntityType.WITHER) {
            chance = 0.50; // 50%
            Material[] possibleTypes = {
                    Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE,
                    Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
                    Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
                    Material.NETHERITE_BOOTS
            };
            toolType = possibleTypes[random.nextInt(possibleTypes.length)];
        } else if (type == EntityType.ENDER_DRAGON) {
            chance = 1.00; // 100%
            toolType = Material.NETHERITE_PICKAXE;
        } else if (type == EntityType.WARDEN) {
            chance = 0.75; // 75%
            toolType = Material.NETHERITE_AXE;
        }

        if (random.nextDouble() < chance) {
            // Randomize Tool Type if not boss specific
            if (type != EntityType.WITHER && type != EntityType.ENDER_DRAGON && type != EntityType.WARDEN) {
                Material[] possibleTypes = {
                        Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE,
                        Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
                        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
                        Material.DIAMOND_BOOTS
                };
                toolType = possibleTypes[random.nextInt(possibleTypes.length)];
            }

            ItemStack sleepingTool = SleepingManager.createSleepingTool(toolType);
            event.getDrops().add(sleepingTool);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            ItemStack item = event.getItem();
            if (SleepingManager.isSleepingTool(item)) {
                // Awakening is now only possible via Ritual Table
                // SleepingManager.tryAwaken(event.getPlayer(), item);
                // event.setCancelled(true);

                // Maybe send a message hinting at the ritual?
                event.getPlayer().sendMessage(org.bukkit.ChatColor.GRAY
                        + "Esta herramienta duerme profundamente... Quizás un ritual pueda despertarla.");
            }
        }
    }
}
