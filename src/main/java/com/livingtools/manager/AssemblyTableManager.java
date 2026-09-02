package com.livingtools.manager;

import com.livingtools.gui.AssemblyGUI;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Enruta clics en Mesa de Herrería a la estructura correcta (prioridad única).
 */
public class AssemblyTableManager implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onSmithingTableInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.SMITHING_TABLE) {
            return;
        }

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (BossForgeManager.checkStructure(clicked.getLocation())) {
            BossForgeManager.openForge(player, clicked.getLocation());
            return;
        }

        if (RuneForgeManager.isRuneForge(clicked)) {
            RuneForgeManager.openGUI(player);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ Has accedido a la Forja Rúnica ✨");
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
            return;
        }

        if (clicked.getRelative(0, -1, 0).getType() == Material.IRON_BLOCK) {
            AssemblyGUI.open(player);
            player.sendMessage(ChatColor.GREEN + "Abriendo Mesa de Ensamblaje...");
            return;
        }

        player.sendMessage(ChatColor.RED + "Ninguna estructura válida detectada en esta mesa de herrería.");
        sendStructureHint(player, clicked);
    }

    private static void sendStructureHint(Player player, Block table) {
        Material below = table.getRelative(0, -1, 0).getType();
        if (below == Material.AMETHYST_BLOCK) {
            player.sendMessage(MessageUtils.color("&7Parece una &dForja Rúnica &7incompleta:"));
            com.livingtools.visuals.RuneForgeVisualizer.sendMaterialLegend(player);
            return;
        }
        if (below == Material.POLISHED_BLACKSTONE_BRICKS || below == Material.MAGMA_BLOCK
                || below == Material.POLISHED_BLACKSTONE) {
            player.sendMessage(MessageUtils.color("&7Parece una &4Forja de Jefes &7incompleta:"));
            com.livingtools.visuals.BossForgeVisualizer.sendMaterialLegend(player);
            return;
        }
        player.sendMessage(MessageUtils.color("&7Para &2Mesa de Ensamblaje&7:"));
        com.livingtools.visuals.AssemblyVisualizer.sendMaterialLegend(player);
    }
}
