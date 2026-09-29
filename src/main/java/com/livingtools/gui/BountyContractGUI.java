package com.livingtools.gui;

import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.BountyContractManager;
import com.livingtools.manager.BountyContractManager.PlayerContract;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * BountyContractGUI — Tablón interactivo de contratos de cacería y recompensas.
 */
public class BountyContractGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Tablón de Contratos de Caza ✦";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.BROWN_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        List<PlayerContract> contracts = BountyContractManager.getContracts(player);
        int[] slots = {11, 13, 15};

        for (int i = 0; i < Math.min(contracts.size(), slots.length); i++) {
            PlayerContract c = contracts.get(i);
            int slot = slots[i];

            String status = c.isClaimed() ? ChatColor.GRAY + "✔ RECOMPENSA RECLAMADA"
                    : c.isCompleted() ? ChatColor.GREEN + "★ ¡COMPLETADO! Click para reclamar"
                    : ChatColor.YELLOW + "Progreso: " + c.getCurrentProgress() + "/" + c.getType().getTargetCount();

            ItemStack item = GUIBuilder.createItem(c.getType().getIcon(),
                    ChatColor.GOLD + "" + ChatColor.BOLD + c.getType().getTitle(),
                    ChatColor.GRAY + c.getType().getDescription(),
                    "",
                    ChatColor.AQUA + "Recompensa: " + ChatColor.WHITE + "+" + c.getType().getRewardXP() + " XP de herramienta + Diamantes",
                    "",
                    status);

            gui.setItem(slot, item);
        }

        ItemStack info = GUIBuilder.createItem(Material.WRITABLE_BOOK,
                ChatColor.YELLOW + "📜 Hermandad de Cazadores",
                ChatColor.GRAY + "Los contratos se renuevan diariamente.",
                ChatColor.GRAY + "Completa objetivos con tu herramienta viviente.");
        gui.setItem(22, info);

        player.openInventory(gui);
    }
}
