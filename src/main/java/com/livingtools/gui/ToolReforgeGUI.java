package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * ToolReforgeGUI — Estación de Reforja y Mantenimiento Ancestral de Living Tools.
 */
public class ToolReforgeGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "✦ Forja Ancestral: Reforja ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // Borde decorativo
        ItemStack border = GUIBuilder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // Slot 4: Info de la herramienta actual
        ItemStack toolDisplay = tool.getItem().clone();
        gui.setItem(4, toolDisplay);

        // Slot 11: Reparar Durabilidad
        int currentDamage = 0;
        int maxDur = tool.getItem().getType().getMaxDurability();
        if (tool.getItem().getItemMeta() instanceof Damageable) {
            currentDamage = ((Damageable) tool.getItem().getItemMeta()).getDamage();
        }
        int remainingDur = maxDur - currentDamage;

        ItemStack repairBtn = GUIBuilder.createGlowingItem(Material.ANVIL,
                ChatColor.GREEN + "" + ChatColor.BOLD + "🔨 Reparar Durabilidad",
                ChatColor.GRAY + "Restaura la durabilidad de tu herramienta",
                ChatColor.GRAY + "sin alterar sus estadísticas vivientes.",
                "",
                ChatColor.YELLOW + "Durabilidad actual: " + ChatColor.WHITE + remainingDur + "/" + maxDur,
                ChatColor.AQUA + "Costo: " + ChatColor.WHITE + "300 XP de herramienta",
                "",
                (currentDamage > 0 ? ChatColor.GREEN + "► Click para reparar" : ChatColor.GRAY + "Tu herramienta ya está al 100%"));
        gui.setItem(11, repairBtn);

        // Slot 13: Pulido de Almas
        ItemStack polishBtn = GUIBuilder.createGlowingItem(Material.AMETHYST_SHARD,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✧ Pulido de Almas",
                ChatColor.GRAY + "Pule la superficie de la herramienta con",
                ChatColor.GRAY + "energía espiritual purificada.",
                "",
                ChatColor.AQUA + "Costo: " + ChatColor.WHITE + "500 XP de herramienta",
                ChatColor.YELLOW + "Beneficio: " + ChatColor.GREEN + "Aura brillante + Felicidad (+10 Mood)",
                "",
                ChatColor.YELLOW + "► Click para pulir");
        gui.setItem(13, polishBtn);

        // Slot 15: Re-alinear Personalidad
        String currentPers = tool.getData().getPersonality();
        ItemStack rerollBtn = GUIBuilder.createGlowingItem(Material.NETHERITE_SCRAP,
                ChatColor.GOLD + "" + ChatColor.BOLD + "🔮 Re-Alineación de Conciencia",
                ChatColor.GRAY + "Permite despertar una nueva personalidad",
                ChatColor.GRAY + "en tu herramienta viviente.",
                "",
                ChatColor.YELLOW + "Personalidad actual: " + ChatColor.WHITE + (currentPers != null ? currentPers : "WISE"),
                ChatColor.AQUA + "Costo: " + ChatColor.WHITE + "1 Netherite Scrap en inventario",
                "",
                ChatColor.YELLOW + "► Click para cambiar personalidad");
        gui.setItem(15, rerollBtn);

        // Slot 22: Cerrar
        ItemStack closeBtn = GUIBuilder.createItem(Material.BARRIER, ChatColor.RED + "Cerrar");
        gui.setItem(22, closeBtn);

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
    }
}
