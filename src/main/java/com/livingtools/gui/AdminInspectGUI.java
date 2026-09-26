package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.AdminFreezeManager;
import com.livingtools.manager.LevelUpRewardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * AdminInspectGUI — Menú de Moderación e Inspección en vivo para Administradores.
 */
public class AdminInspectGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_RED + "Moderación: ";

    public static void open(Player admin, Player target) {
        ItemStack held = target.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            admin.sendMessage(ChatColor.RED + target.getName() + " no tiene una herramienta viviente en su mano principal.");
            return;
        }

        LivingTool tool = new LivingTool(held);
        ToolData data = tool.getData();

        Inventory gui = Bukkit.createInventory(null, 54, TITLE_PREFIX + target.getName());

        // Borde
        ItemStack border = GUIBuilder.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, border);
        }

        // Slot 4: Herramienta del jugador
        gui.setItem(4, held.clone());

        // Slot 19: Congelar / Descongelar
        boolean isFrozen = AdminFreezeManager.isFrozen(tool);
        ItemStack freezeBtn = GUIBuilder.createGlowingItem(isFrozen ? Material.PACKED_ICE : Material.ICE,
                (isFrozen ? ChatColor.RED + "❄ Herramienta CONGELADA" : ChatColor.AQUA + "❄ Herramienta ACTIVA"),
                ChatColor.GRAY + "Estado: " + (isFrozen ? ChatColor.RED + "Bloqueada" : ChatColor.GREEN + "Normal"),
                "",
                ChatColor.YELLOW + "► Click para " + (isFrozen ? "DESCONGELAR" : "CONGELAR"));
        gui.setItem(19, freezeBtn);

        // Slot 21: Purificar Corrupción
        ItemStack purifyBtn = GUIBuilder.createGlowingItem(Material.SUNFLOWER,
                ChatColor.GOLD + "🧹 Purificar Corrupción",
                ChatColor.GRAY + "Corrupción actual: " + ChatColor.DARK_PURPLE + data.getCorruption() + "%",
                "",
                ChatColor.YELLOW + "► Click para resetear corrupción a 0%");
        gui.setItem(21, purifyBtn);

        // Slot 23: Re-Vincular / Liberar Alma
        ItemStack rebindBtn = GUIBuilder.createGlowingItem(Material.NAME_TAG,
                ChatColor.LIGHT_PURPLE + "🔄 Re-Vincular Dueño",
                ChatColor.GRAY + "Dueño actual: " + ChatColor.WHITE + target.getName(),
                "",
                ChatColor.YELLOW + "► Click para transferir vínculo a ti mismo");
        gui.setItem(23, rebindBtn);

        // Slot 25: Strip Living Data (Vanilla)
        ItemStack stripBtn = GUIBuilder.createItem(Material.LAVA_BUCKET,
                ChatColor.DARK_RED + "" + ChatColor.BOLD + "🗑 Convertir a Ítem Vanilla",
                ChatColor.RED + "ADVERTENCIA: Elimina todas las estadísticas vivientes,",
                ChatColor.RED + "devolviendo un ítem estándar de Minecraft.",
                "",
                ChatColor.RED + "► Click para transformar a Vanilla");
        gui.setItem(25, stripBtn);

        // Botones de ajuste de Nivel
        gui.setItem(29, GUIBuilder.createItem(Material.LIME_DYE, ChatColor.GREEN + "+1 Nivel"));
        gui.setItem(30, GUIBuilder.createItem(Material.LIME_DYE, ChatColor.GREEN + "+5 Niveles"));
        gui.setItem(31, GUIBuilder.createItem(Material.LIME_DYE, ChatColor.GREEN + "+25 Niveles"));

        gui.setItem(38, GUIBuilder.createItem(Material.RED_DYE, ChatColor.RED + "-1 Nivel"));
        gui.setItem(39, GUIBuilder.createItem(Material.RED_DYE, ChatColor.RED + "-5 Niveles"));
        gui.setItem(40, GUIBuilder.createItem(Material.RED_DYE, ChatColor.RED + "-25 Niveles"));

        // Slot 33: Dar Puntos de Habilidad
        int points = LevelUpRewardManager.getSkillPoints(tool);
        ItemStack pointsBtn = GUIBuilder.createGlowingItem(Material.EXPERIENCE_BOTTLE,
                ChatColor.YELLOW + "🎁 Puntos de Habilidad",
                ChatColor.GRAY + "Puntos disponibles: " + ChatColor.GREEN + points + "/20",
                "",
                ChatColor.YELLOW + "► Click para otorgar +5 Puntos de Talento");
        gui.setItem(33, pointsBtn);

        // Slot 49: Cerrar
        gui.setItem(49, GUIBuilder.createItem(Material.BARRIER, ChatColor.RED + "Cerrar"));

        admin.openInventory(gui);
        admin.playSound(admin.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.2f);
    }
}
