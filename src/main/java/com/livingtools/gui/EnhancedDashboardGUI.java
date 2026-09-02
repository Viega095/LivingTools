package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Dashboard mejorado con mejor apariencia visual
 */
public class EnhancedDashboardGUI {

    public static void open(Player player, LivingTool tool) {
        boolean isArmor = com.livingtools.data.LivingArmor.isLivingArmor(tool.getItem());
        String title = GUIBuilder.createTitle(isArmor ? "Armadura Viviente" : "Herramienta Viviente");

        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Borde decorativo con gradiente
        setBorderWithGradient(gui);

        // Item central
        gui.setItem(22, tool.getItem());

        // Información del nivel
        int currentLevel = tool.getData().getLevel();
        long currentXP = tool.getData().getXP();

        ItemStack levelItem = GUIBuilder.createGlowingItem(
                Material.EXPERIENCE_BOTTLE,
                GUIBuilder.ACCENT + "⭐ Nivel y Progreso",
                "",
                GUIBuilder.INFO + "Nivel: " + GUIBuilder.PRIMARY + currentLevel,
                GUIBuilder.INFO + "XP: " + GUIBuilder.SECONDARY + currentXP,
                "",
                GUIBuilder.SECONDARY + "Usa el botón de mejora con XP");
        gui.setItem(4, levelItem);

        // Estadísticas
        ItemStack stats = GUIBuilder.createGlowingItem(
                Material.ENCHANTED_BOOK,
                GUIBuilder.PRIMARY + "📊 Estadísticas",
                "",
                GUIBuilder.INFO + "Ver estadísticas detalladas",
                "",
                GUIBuilder.SECONDARY + "Click para ver detalles");
        gui.setItem(20, stats);

        // Habilidades
        ItemStack abilities = GUIBuilder.createGlowingItem(
                Material.NETHER_STAR,
                GUIBuilder.PRIMARY + "⚡ Habilidades",
                "",
                GUIBuilder.SUCCESS + "✔ " + tool.getAbilities().size() + " activas",
                "",
                GUIBuilder.SECONDARY + "Click para gestionar");
        gui.setItem(24, abilities);

        // Mejora con XP
        ItemStack xpUpgrade = GUIBuilder.createGlowingItem(
                Material.ANVIL,
                GUIBuilder.ACCENT + "⚒ Mejorar con XP",
                "",
                GUIBuilder.INFO + "Usa tu XP para subir de nivel",
                "",
                GUIBuilder.SECONDARY + "Click para abrir");
        gui.setItem(38, xpUpgrade);

        // Botón de cerrar
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static void setBorderWithGradient(Inventory inv) {
        ItemStack corner = GUIBuilder.createGlassPane(Material.PURPLE_STAINED_GLASS_PANE);
        inv.setItem(0, corner);
        inv.setItem(8, corner);
        inv.setItem(45, corner);
        inv.setItem(53, corner);

        ItemStack topBottom = GUIBuilder.createGlassPane(Material.MAGENTA_STAINED_GLASS_PANE);
        for (int i = 1; i < 8; i++) {
            inv.setItem(i, topBottom);
            inv.setItem(45 + i, topBottom);
        }

        ItemStack sides = GUIBuilder.createGlassPane(Material.BLUE_STAINED_GLASS_PANE);
        for (int i = 1; i < 5; i++) {
            inv.setItem(i * 9, sides);
            inv.setItem(i * 9 + 8, sides);
        }
    }
}
