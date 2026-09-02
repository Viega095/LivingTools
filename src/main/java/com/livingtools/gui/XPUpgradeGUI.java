package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI mejorada para mejorar herramientas con XP
 */
public class XPUpgradeGUI {

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, GUIBuilder.createTitle("Mejorar con XP"));

        // Borde decorativo
        GUIBuilder.setBorder27(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Item central - la herramienta
        gui.setItem(13, tool.getItem());

        // Información de XP
        int playerXP = getTotalExperience(player);
        int currentLevel = tool.getData().getLevel();
        int xpCost = calculateXPCost(currentLevel);

        ItemStack xpInfoItem = GUIBuilder.createItem(
                Material.EXPERIENCE_BOTTLE,
                GUIBuilder.ACCENT + "Información de XP",
                "",
                GUIBuilder.INFO + "Tu XP: " + GUIBuilder.SECONDARY + GUIBuilder.formatNumber(playerXP),
                GUIBuilder.INFO + "Costo: " + GUIBuilder.PRIMARY + GUIBuilder.formatNumber(xpCost) + " XP",
                "",
                playerXP >= xpCost ? GUIBuilder.SUCCESS + "✔ Tienes suficiente XP"
                        : GUIBuilder.ERROR + "✖ XP insuficiente");
        gui.setItem(10, xpInfoItem);

        // Botón de mejora
        if (playerXP >= xpCost) {
            ItemStack upgradeButton = GUIBuilder.createGlowingItem(
                    Material.ANVIL,
                    GUIBuilder.SUCCESS + "⚒ Mejorar Nivel",
                    "Costo: " + xpCost + " XP",
                    "",
                    "Nivel actual: " + currentLevel,
                    "Nivel siguiente: " + (currentLevel + 1),
                    "",
                    GUIBuilder.SUCCESS + "Click para mejorar");
            gui.setItem(16, upgradeButton);
        } else {
            ItemStack lockedButton = GUIBuilder.createItem(
                    Material.BARRIER,
                    GUIBuilder.ERROR + "⚒ Mejora Bloqueada",
                    "Necesitas " + xpCost + " XP");
            gui.setItem(16, lockedButton);
        }

        // Información de nivel
        ItemStack progressItem = GUIBuilder.createItem(
                Material.NETHER_STAR,
                GUIBuilder.ACCENT + "Progreso de Nivel",
                "",
                GUIBuilder.INFO + "Nivel: " + GUIBuilder.PRIMARY + currentLevel,
                GUIBuilder.INFO + "XP acumulado: " + GUIBuilder.SECONDARY + tool.getData().getXP());
        gui.setItem(4, progressItem);

        // Botón de cerrar
        gui.setItem(22, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static int calculateXPCost(int currentLevel) {
        return (int) (100 * Math.pow(currentLevel, 1.5));
    }

    private static int getTotalExperience(Player player) {
        int exp = Math.round(player.getExp() * player.getExpToLevel());
        int level = player.getLevel();

        if (level >= 32) {
            exp += (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 17) {
            exp += (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            exp += level * level + 6 * level;
        }

        return exp;
    }

    public static void removeXP(Player player, int amount) {
        int currentXP = getTotalExperience(player);
        int newXP = Math.max(0, currentXP - amount);

        player.setLevel(0);
        player.setExp(0);
        player.giveExp(newXP);
    }

    public static boolean processUpgrade(Player player, LivingTool tool) {
        int xpCost = calculateXPCost(tool.getData().getLevel());
        int playerXP = getTotalExperience(player);

        if (playerXP < xpCost) {
            player.sendMessage(GUIBuilder.ERROR + "No tienes suficiente XP!");
            return false;
        }

        removeXP(player, xpCost);
        tool.getData().setLevel(tool.getData().getLevel() + 1);
        tool.updateLore();

        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        player.sendMessage(GUIBuilder.SUCCESS + "✔ ¡Herramienta mejorada al nivel " + tool.getData().getLevel() + "!");

        return true;
    }
}
