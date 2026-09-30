package com.livingtools.listeners;

import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import com.livingtools.gui.RhythmicForgeGUI;
import com.livingtools.manager.RhythmicForgeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * RhythmicForgeListener — Gestiona las interacciones con la estructura de la Forja Rítmica,
 * el uso del Núcleo de Forja y los clicks dentro del minijuego.
 */
public class RhythmicForgeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        // 1. Uso del Núcleo de la Forja Rítmica para auto-construir
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && RhythmicForgeManager.isSoulForgeCore(held)) {
            event.setCancelled(true);
            Block clicked = event.getClickedBlock();
            if (clicked != null) {
                Block placeLoc = clicked.getRelative(event.getBlockFace());
                RhythmicForgeManager.buildStructure(placeLoc.getLocation());

                // Consumir 1 núcleo si no está en creativo
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    held.setAmount(held.getAmount() - 1);
                }

                player.sendMessage(ChatColor.AQUA + "✦ ¡La Forja Rítmica de Almas ha sido erigida con éxito!");
                return;
            }
        }

        // 2. Interacción con Yunque / Forja de Almas
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Material mat = block.getType();
            boolean isAnvil = (mat == Material.ANVIL || mat == Material.CHIPPED_ANVIL ||
                    mat == Material.DAMAGED_ANVIL || mat == Material.SMITHING_TABLE || mat == Material.LODESTONE);

            if (isAnvil) {
                // Si el jugador sostiene una Herramienta o Armadura Viviente
                if (LivingTool.isLivingTool(held)) {
                    // Si el jugador está agachado (sneak), dejamos que ToolReforgeListener maneje el reforge
                    if (player.isSneaking()) return;

                    event.setCancelled(true);
                    player.playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
                    player.playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
                    RhythmicForgeGUI.open(player, new LivingTool(held));
                    return;
                } else if (LivingArmor.isLivingArmor(held)) {
                    if (player.isSneaking()) return;

                    event.setCancelled(true);
                    player.playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
                    RhythmicForgeGUI.open(player, new LivingTool(held));
                    return;
                }

                // Si no sostiene herramienta viva pero la estructura es una Forja Rítmica
                if (RhythmicForgeManager.isRhythmicForge(block)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.YELLOW + "✦ Sostén tu " + ChatColor.AQUA + "Herramienta o Armadura Viviente"
                            + ChatColor.YELLOW + " en la mano principal para forjarla, repararla o templarla.");
                    player.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(RhythmicForgeGUI.TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Slot 22: Botón de Martillear
        if (slot == 22) {
            RhythmicForgeManager.handleStrike(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(RhythmicForgeGUI.TITLE)) return;

        Player player = (Player) event.getPlayer();
        RhythmicForgeManager.cleanup(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(RhythmicForgeGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
