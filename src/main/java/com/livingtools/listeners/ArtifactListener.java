package com.livingtools.listeners;

import com.livingtools.manager.ArtifactManager;
import com.livingtools.manager.ArtifactManager.ArtifactType;
import org.bukkit.ChatColor;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ArtifactListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(ArtifactManager.KEY_ARTIFACT_TYPE, PersistentDataType.STRING))
            return;

        String typeStr = meta.getPersistentDataContainer().get(ArtifactManager.KEY_ARTIFACT_TYPE,
                PersistentDataType.STRING);
        ArtifactType type;
        try {
            type = ArtifactType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (type == ArtifactType.SOUL_GEM) {
            handleSoulGem(player, item, meta);
        } else if (type == ArtifactType.RUNE_POUCH) {
            handleRunePouch(player);
        } else if (type == ArtifactType.LIVING_CHARM) {
            player.sendMessage(ChatColor.GREEN + "Encanto Viviente activo: "
                    + ChatColor.GRAY + "+10% XP de herramientas mientras esté en tu inventario.");
        }
    }

    private void handleSoulGem(Player player, ItemStack gem, ItemMeta meta) {
        // Check stored souls
        NamespacedKey keySouls = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                "soul_gem_souls");
        int souls = meta.getPersistentDataContainer().getOrDefault(keySouls, PersistentDataType.INTEGER, 0);

        if (souls >= 10) {
            // Repair Tool in Offhand or Hotbar
            ItemStack toolItem = player.getInventory().getItemInOffHand();
            if (!com.livingtools.data.LivingTool.isLivingTool(toolItem)) {
                // Try main hand if gem is in offhand? No, usually gem is in hand.
                // Let's check hotbar for damaged living tool.
                toolItem = null;
                for (int i = 0; i < 9; i++) {
                    ItemStack s = player.getInventory().getItem(i);
                    if (com.livingtools.data.LivingTool.isLivingTool(s)) {
                        if (((org.bukkit.inventory.meta.Damageable) s.getItemMeta()).getDamage() > 0) {
                            toolItem = s;
                            break;
                        }
                    }
                }
            }

            if (toolItem != null && com.livingtools.data.LivingTool.isLivingTool(toolItem)) {
                org.bukkit.inventory.meta.Damageable toolMeta = (org.bukkit.inventory.meta.Damageable) toolItem
                        .getItemMeta();
                if (toolMeta.getDamage() > 0) {
                    toolMeta.setDamage(0);
                    toolItem.setItemMeta(toolMeta);

                    // Consume souls
                    souls -= 10;
                    meta.getPersistentDataContainer().set(keySouls, PersistentDataType.INTEGER, souls);
                    updateSoulGemLore(meta, souls);
                    gem.setItemMeta(meta);

                    player.sendMessage(ChatColor.AQUA + "¡La Gema de Almas ha reparado tu herramienta!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1, 2);
                } else {
                    player.sendMessage(ChatColor.RED + "No tienes herramientas dañadas para reparar.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "No se encontró una Herramienta Viviente dañada.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Necesitas 10 almas para reparar. (Actual: " + souls + ")");
        }
    }

    private void handleRunePouch(Player player) {
        com.livingtools.gui.RunePouchGUI.open(player, player.getInventory().getItemInMainHand());
    }

    public static void updateSoulGemLore(ItemMeta meta, int souls) {
        java.util.List<String> lore = meta.getLore();
        if (lore == null)
            lore = new java.util.ArrayList<>();

        // Remove old soul count line if exists
        lore.removeIf(line -> line.contains("Almas:"));

        lore.add(ChatColor.LIGHT_PURPLE + "Almas: " + souls + "/10");
        meta.setLore(lore);
    }
}
