package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.CosmeticTrailManager;
import com.livingtools.manager.CosmeticTrailManager.TrailStyle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * CosmeticTrailGUI — Selector interactivo de Estelas y Auras Cosméticas.
 */
public class CosmeticTrailGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "✦ Estelas & Auras Cosméticas ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        TrailStyle active = CosmeticTrailManager.getActiveTrail(tool);

        // Slot 10: DEFAULT
        gui.setItem(10, createTrailItem(tool, TrailStyle.DEFAULT, Material.COMPASS, active));

        // Slot 11: SOLAR_FLAME
        gui.setItem(11, createTrailItem(tool, TrailStyle.SOLAR_FLAME, Material.BLAZE_POWDER, active));

        // Slot 12: AMETHYST_MIST
        gui.setItem(12, createTrailItem(tool, TrailStyle.AMETHYST_MIST, Material.AMETHYST_SHARD, active));

        // Slot 14: VOID_VORTEX
        gui.setItem(14, createTrailItem(tool, TrailStyle.VOID_VORTEX, Material.ENDER_PEARL, active));

        // Slot 15: LIGHTNING_AURA
        gui.setItem(15, createTrailItem(tool, TrailStyle.LIGHTNING_AURA, Material.LIGHTNING_ROD, active));

        // Slot 16: CELESTIAL_DUST
        gui.setItem(16, createTrailItem(tool, TrailStyle.CELESTIAL_DUST, Material.NETHER_STAR, active));

        // Slot 22: Cerrar
        gui.setItem(22, GUIBuilder.createItem(Material.BARRIER, ChatColor.RED + "Cerrar"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
    }

    private static ItemStack createTrailItem(LivingTool tool, TrailStyle style, Material mat, TrailStyle active) {
        boolean isUnlocked = style.isUnlocked(tool);
        boolean isEquipped = (style == active);

        String titleColor = isEquipped ? ChatColor.GREEN + "" + ChatColor.BOLD : (isUnlocked ? ChatColor.GOLD + "" : ChatColor.RED + "");
        String status = isEquipped ? ChatColor.GREEN + " [EQUIPADA]" : (isUnlocked ? ChatColor.YELLOW + " [DESBLOQUEADA]" : ChatColor.RED + " [BLOQUEADA]");

        String reqStr = "";
        if (style.getRequiredLevel() > 0) reqStr += "Nivel " + style.getRequiredLevel() + " ";
        if (style.getRequiredPrestige() > 0) reqStr += "Prestigio " + style.getRequiredPrestige() + " ";
        if (reqStr.isEmpty()) reqStr = "Gratuito";

        String clickHint = isEquipped ? ChatColor.GREEN + "Ya tienes esta estela equipada"
                : (isUnlocked ? ChatColor.YELLOW + "► Click para equipar" : ChatColor.RED + "Requisito: " + reqStr);

        return GUIBuilder.createGlowingItem(mat,
                titleColor + style.getDisplayName() + status,
                ChatColor.GRAY + style.getDescription(),
                "",
                ChatColor.AQUA + "Requisito: " + ChatColor.WHITE + reqStr,
                "",
                clickHint);
    }
}
