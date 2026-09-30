package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.RhythmicForgeManager;
import com.livingtools.manager.SoulElementalSealManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * RhythmicForgeGUI — Interfaz interactiva de la Gran Forja Rítmica de Almas.
 */
public class RhythmicForgeGUI {

    public static final String TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "✦ Forja Rítmica de Almas ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // Bordes decorativos con cristal azul de alma
        ItemStack border = GUIBuilder.createItem(Material.CYAN_STAINED_GLASS_PANE, " ");
        ItemStack darkBorder = GUIBuilder.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i % 2 == 0) ? border : darkBorder);
        }

        // Slot 0: Manual / Guía de la Forja
        ItemStack manual = GUIBuilder.createGlowingItem(
                Material.BOOK,
                ChatColor.AQUA + "" + ChatColor.BOLD + "📜 Manual del Forjador de Almas",
                "",
                ChatColor.GRAY + "La Forja Rítmica canaliza el fuego de almas",
                ChatColor.GRAY + "para reparar y templar herramientas vivientes.",
                "",
                ChatColor.YELLOW + "✦ Cómo Jugar:",
                ChatColor.WHITE + "  1. El martillo se moverá en la pista central.",
                ChatColor.WHITE + "  2. Haz click en el botón inferior cuando",
                ChatColor.WHITE + "     pase por la " + ChatColor.GREEN + "zona verde central" + ChatColor.WHITE + ".",
                "",
                ChatColor.GOLD + "✦ Recompensas:",
                ChatColor.GREEN + "  • Zona Verde: " + ChatColor.AQUA + "100% Reparación " + ChatColor.GOLD + "+ Obra Maestra (+15%)",
                ChatColor.YELLOW + "  • Zona Dorada: " + ChatColor.GREEN + "50% Reparación + 150 XP",
                ChatColor.WHITE + "  • Zona Cobre: " + ChatColor.YELLOW + "25% Reparación + 75 XP"
        );
        gui.setItem(0, manual);

        // Slot 4: Herramienta en Forja con Estado Detallado
        ItemStack toolDisplay = tool.getItem().clone();
        ItemMeta meta = toolDisplay.getItemMeta();
        if (meta != null) {
            List<String> lore = (meta.getLore() != null) ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GOLD + "═══ ESTADO EN LA FORJA ═══");

            // Durabilidad
            if (meta instanceof Damageable) {
                Damageable dam = (Damageable) meta;
                int max = tool.getItem().getType().getMaxDurability();
                if (max > 0) {
                    int current = max - dam.getDamage();
                    int percent = (int) Math.round(((double) current / max) * 100);
                    String bar = getProgressBar(percent);
                    lore.add(ChatColor.GRAY + "Durabilidad: " + bar + " " + ChatColor.YELLOW + percent + "%");
                }
            }

            if (tool.isBroken()) {
                lore.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚠ ¡ESTADO: ROTA / INSERVIBLE!");
            } else {
                lore.add(ChatColor.GREEN + "✔ Estado Operativo");
            }

            if (RhythmicForgeManager.isMasterwork(tool)) {
                lore.add(ChatColor.GOLD + "🌟 Obra Maestra Activa (+15% Stats)");
            }

            SoulElementalSealManager.ElementalSeal seal = SoulElementalSealManager.getSeal(tool.getItem());
            if (seal != null) {
                lore.add(ChatColor.AQUA + "✦ Sello: " + seal.getFormattedName());
            }

            meta.setLore(lore);
            toolDisplay.setItemMeta(meta);
        }
        gui.setItem(4, toolDisplay);

        // Slot 8: Catalizador / Fogata de Alma
        ItemStack catalyst = GUIBuilder.createGlowingItem(
                Material.SOUL_CAMPFIRE,
                ChatColor.AQUA + "" + ChatColor.BOLD + "🔥 Fuego de Almas Activo",
                "",
                ChatColor.GRAY + "La estructura está en resonancia mágica.",
                ChatColor.GRAY + "La reparación es " + ChatColor.GREEN + "GRATUITA " + ChatColor.GRAY + "por maestría rítmica.",
                "",
                ChatColor.LIGHT_PURPLE + "✦ ¡Demuestra tu sincronización!"
        );
        gui.setItem(8, catalyst);

        // Slot 22: Botón de Martillear
        ItemStack strikeBtn = GUIBuilder.createGlowingItem(
                Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                ChatColor.RED + "" + ChatColor.BOLD + "🔨 ¡MARTILLEAR AHORA! 🔨",
                "",
                ChatColor.GRAY + "Haz click cuando el martillo pase por la",
                ChatColor.GREEN + "zona verde central " + ChatColor.GRAY + "para reparar al " + ChatColor.AQUA + "100%",
                ChatColor.GRAY + "y obtener calidad " + ChatColor.GOLD + "Obra Maestra (+15% Stats).",
                "",
                ChatColor.YELLOW + "  ► ¡CLICK AQUÍ PARA GOLPEAR! ◄"
        );
        gui.setItem(22, strikeBtn);

        player.openInventory(gui);
        RhythmicForgeManager.startGame(player, tool, gui);
    }

    private static String getProgressBar(int percent) {
        int filled = Math.max(0, Math.min(10, percent / 10));
        StringBuilder sb = new StringBuilder(ChatColor.GREEN + "[");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                sb.append("█");
            } else {
                sb.append(ChatColor.DARK_GRAY).append("░");
            }
        }
        sb.append(ChatColor.GREEN).append("]");
        return sb.toString();
    }
}
