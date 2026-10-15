package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.SleepingManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SleepingListener implements Listener {

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        EntityType type = event.getEntityType();

        if (type == EntityType.WITHER) {
            // Drop de Núcleo Titánico garantizado
            event.getDrops().add(SleepingManager.createTitanSoulCore());

            // 50% de probabilidad de dropear la Guadaña Marchita despierta
            if (random.nextDouble() < 0.50) {
                event.getDrops().add(SleepingManager.createWitherScythe());
            }
        } else if (type == EntityType.ENDER_DRAGON) {
            // Drop de Núcleo Titánico garantizado
            event.getDrops().add(SleepingManager.createTitanSoulCore());

            // 100% de probabilidad de dropear la Cuchilla del Vacío despierta
            event.getDrops().add(SleepingManager.createDragonVoidBlade());
        } else if (type == EntityType.WARDEN) {
            // Drop de Núcleo Titánico garantizado
            event.getDrops().add(SleepingManager.createTitanSoulCore());

            // 75% de probabilidad de dropear el Martillo Sísmico despierto
            if (random.nextDouble() < 0.75) {
                event.getDrops().add(SleepingManager.createWardenHammer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Si es una Herramienta Dormida (antigua)
        if (SleepingManager.isSleepingTool(item)) {
            event.setCancelled(true);
            SleepingManager.tryAwaken(player, item);
            return;
        }

        // Si es un Núcleo Titánico
        if (SleepingManager.isTitanSoulCore(item)) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (LivingTool.isLivingTool(offhand)) {
                event.setCancelled(true);
                if (SleepingManager.applyTitanSoulCore(player, offhand)) {
                    item.setAmount(item.getAmount() - 1);
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[LivingTools] " + ChatColor.YELLOW
                        + "Para usar el Núcleo Titánico, colócalo sobre una Herramienta Viviente en tu inventario o sostenla en tu mano secundaria (offhand).");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && SleepingManager.isTitanSoulCore(cursor)) {
            if (current != null && LivingTool.isLivingTool(current)) {
                event.setCancelled(true);
                if (SleepingManager.applyTitanSoulCore(player, current)) {
                    cursor.setAmount(cursor.getAmount() - 1);
                }
            }
        }
    }
}
