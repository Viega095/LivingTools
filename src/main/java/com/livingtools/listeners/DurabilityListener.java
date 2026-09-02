package com.livingtools.listeners;

import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class DurabilityListener implements Listener {

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (LivingTool.isLivingTool(item)) {
            handleToolDamage(event, player, new LivingTool(item));
        } else if (LivingArmor.isLivingArmor(item)) {
            handleArmorDamage(event, player, new LivingArmor(item));
        }
    }

    private void handleToolDamage(PlayerItemDamageEvent event, Player player, LivingTool tool) {
        if (tool.isBroken()) {
            event.setCancelled(true);
            return;
        }

        ItemMeta meta = event.getItem().getItemMeta();
        if (!(meta instanceof Damageable))
            return;

        Damageable damageable = (Damageable) meta;
        int maxDurability = event.getItem().getType().getMaxDurability();
        int currentDamage = damageable.getDamage();
        int newDamage = currentDamage + event.getDamage();

        // Warning at 10% durability left
        int warningThreshold = (int) (maxDurability * 0.9);
        if (currentDamage < warningThreshold && newDamage >= warningThreshold) {
            player.sendTitle(ChatColor.RED + "¡CUIDADO!", ChatColor.YELLOW + "Tu herramienta está por romperse", 10, 40,
                    10);
            player.sendMessage(ChatColor.RED + "¡Tu herramienta tiene hambre! Usa " + ChatColor.YELLOW
                    + "/livingtool feed" + ChatColor.RED + " para repararla.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
        }

        // Broken State
        if (newDamage >= maxDurability) {
            event.setCancelled(true);
            damageable.setDamage(maxDurability - 1); // Keep at 1 durability
            event.getItem().setItemMeta((ItemMeta) damageable);

            tool.setBroken(true);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            player.sendTitle(ChatColor.DARK_RED + "¡HERRAMIENTA ROTA!",
                    ChatColor.GRAY + "Repárala para volver a usarla", 10, 60, 20);
        }
    }

    private void handleArmorDamage(PlayerItemDamageEvent event, Player player, LivingArmor armor) {
        if (armor.isBroken()) {
            event.setCancelled(true);
            return;
        }

        ItemMeta meta = event.getItem().getItemMeta();
        if (!(meta instanceof Damageable))
            return;

        Damageable damageable = (Damageable) meta;
        int maxDurability = event.getItem().getType().getMaxDurability();
        int newDamage = damageable.getDamage() + event.getDamage();

        // Warning
        int warningThreshold = (int) (maxDurability * 0.9);
        if (damageable.getDamage() < warningThreshold && newDamage >= warningThreshold) {
            player.sendMessage(ChatColor.RED + "¡Tu armadura está a punto de romperse!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
        }

        // Broken State
        if (newDamage >= maxDurability) {
            event.setCancelled(true);
            damageable.setDamage(maxDurability - 1);
            event.getItem().setItemMeta((ItemMeta) damageable);

            armor.setBroken(true);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            player.sendMessage(ChatColor.DARK_RED + "¡Tu armadura se ha roto y ya no te protege!");
        }
    }

    // Prevent Usage if Broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            if (tool.isBroken()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Esta herramienta está rota. Repárala primero.");
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                if (tool.isBroken()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Esta arma está rota. Repárala primero.");
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && LivingTool.isLivingTool(event.getItem())) {
            LivingTool tool = new LivingTool(event.getItem());
            if (tool.isBroken()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Esta herramienta está rota. Repárala primero.");
            }
        }
    }

    // Feeding Mechanic (Inventory Repair)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (cursor == null || clicked == null)
            return;

        if (LivingTool.isLivingTool(clicked)) {
            handleRepair(event, (Player) event.getWhoClicked(), cursor, clicked, new LivingTool(clicked));
        } else if (LivingArmor.isLivingArmor(clicked)) {
            handleArmorRepair(event, (Player) event.getWhoClicked(), cursor, clicked, new LivingArmor(clicked));
        }
    }

    private void handleRepair(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack toolItem,
            LivingTool tool) {
        Material repairMaterial = getRepairMaterial(toolItem.getType());
        if (cursor.getType() == repairMaterial) {
            Damageable damageable = (Damageable) toolItem.getItemMeta();
            if (damageable.getDamage() > 0) {
                event.setCancelled(true); // Prevent swap

                int repairAmount = toolItem.getType().getMaxDurability() / 4; // 25% per item
                int newDamage = Math.max(0, damageable.getDamage() - repairAmount);

                damageable.setDamage(newDamage);
                toolItem.setItemMeta((ItemMeta) damageable);

                // Consume 1 item
                cursor.setAmount(cursor.getAmount() - 1);
                event.getWhoClicked().setItemOnCursor(cursor); // Update cursor

                // Fix broken state
                if (tool.isBroken()) {
                    tool.setBroken(false);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                player.sendMessage(ChatColor.GREEN + "Has reparado tu herramienta.");
            }
        }
    }

    private void handleArmorRepair(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack armorItem,
            LivingArmor armor) {
        Material repairMaterial = getRepairMaterial(armorItem.getType());
        if (cursor.getType() == repairMaterial) {
            Damageable damageable = (Damageable) armorItem.getItemMeta();
            if (damageable.getDamage() > 0) {
                event.setCancelled(true);

                int repairAmount = armorItem.getType().getMaxDurability() / 4;
                int newDamage = Math.max(0, damageable.getDamage() - repairAmount);

                damageable.setDamage(newDamage);
                armorItem.setItemMeta((ItemMeta) damageable);

                cursor.setAmount(cursor.getAmount() - 1);
                event.getWhoClicked().setItemOnCursor(cursor);

                if (armor.isBroken()) {
                    armor.setBroken(false);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                player.sendMessage(ChatColor.GREEN + "Has reparado tu armadura.");
            }
        }
    }

    private Material getRepairMaterial(Material toolType) {
        String name = toolType.name();
        if (name.contains("WOODEN"))
            return Material.OAK_PLANKS; // Generic wood
        if (name.contains("STONE"))
            return Material.COBBLESTONE;
        if (name.contains("IRON"))
            return Material.IRON_INGOT;
        if (name.contains("GOLDEN"))
            return Material.GOLD_INGOT;
        if (name.contains("DIAMOND"))
            return Material.DIAMOND;
        if (name.contains("NETHERITE"))
            return Material.NETHERITE_INGOT;
        return Material.AIR;
    }
}
