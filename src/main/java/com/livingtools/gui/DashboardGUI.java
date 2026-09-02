package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.ConfigManager;
import com.livingtools.manager.MilestoneManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DashboardGUI {

    public static void open(Player player, LivingTool tool) {
        boolean isArmor = com.livingtools.data.LivingArmor.isLivingArmor(tool.getItem());
        String title = isArmor ? "Living Armor Dashboard"
                : ConfigManager.getRawMessage("dashboard_title");

        Inventory gui = Bukkit.createInventory(null, 54, title);

        // ── Background
        ItemStack bg   = GUIBuilder.createGlassPane(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack dark = GUIBuilder.createGlassPane(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack acct = GUIBuilder.createGlassPane(Material.PURPLE_STAINED_GLASS_PANE);

        for (int i = 0; i < 54; i++) gui.setItem(i, bg);
        // Top and bottom rows — dark
        for (int i = 0; i < 9;  i++) gui.setItem(i, dark);
        for (int i = 45; i < 54; i++) gui.setItem(i, dark);
        // Corners accent
        for (int s : new int[]{0, 8, 45, 53}) gui.setItem(s, acct);

        ToolData data = tool.getData();

        // ── Slot 4: Tool showcase
        gui.setItem(4, tool.getItem());

        // ── Slot 13: Level / XP progress
        long currentXP = data.getXP();
        int level = data.getLevel();
        long nextLevelXP = (long)(100 * Math.pow(1.15, level)); // same formula as LivingTool
        long xpForThisLevel = (long)(100 * Math.pow(1.15, level - 1));
        long xpProgress = currentXP - xpForThisLevel;
        long xpNeeded   = nextLevelXP - xpForThisLevel;
        double pct = xpNeeded > 0 ? Math.min(1.0, (double) xpProgress / xpNeeded) : 1.0;

        List<String> progressLore = new ArrayList<>();
        progressLore.add("");
        progressLore.add(GUIBuilder.INFO + "Nivel: " + GUIBuilder.PRIMARY + level
                + (data.getPrestige() > 0 ? ChatColor.GOLD + " [✦ Prestige " + data.getPrestige() + "]" : ""));
        progressLore.add(GUIBuilder.INFO + "XP Total: " + GUIBuilder.SECONDARY + formatNum(currentXP));
        progressLore.add(GUIBuilder.INFO + "Para nivel " + (level + 1) + ": " + GUIBuilder.SECONDARY
                + formatNum(xpNeeded > 0 ? xpNeeded - xpProgress : 0) + " XP");
        progressLore.add("");
        progressLore.add(buildProgressBar(pct));
        progressLore.add(ChatColor.GRAY + String.format("  %.1f%%", pct * 100));
        progressLore.add("");
        progressLore.add(GUIBuilder.INFO + "Hitos: " + ChatColor.GOLD + MilestoneManager.countCompleted(tool)
                + ChatColor.GRAY + "/" + com.livingtools.manager.MilestoneManager.Milestone.values().length);

        ItemStack levelItem = GUIBuilder.createGlowingItem(Material.EXPERIENCE_BOTTLE,
                GUIBuilder.ACCENT + "⚡ Nivel y Progreso", progressLore.toArray(new String[0]));
        gui.setItem(13, levelItem);

        // ── Slot 22: Personalidad + Humor
        String personality = data.getPersonality() != null ? data.getPersonality() : "Desconocida";
        int mood = data.getMood();
        String moodStr = mood >= 8 ? ChatColor.GREEN + "😄 Feliz" :
                         mood >= 5 ? ChatColor.YELLOW + "😐 Normal" :
                         mood >= 2 ? ChatColor.GOLD + "😟 Triste" :
                                     ChatColor.RED + "😡 Enojado";
        int fullness = data.getFullness();
        String hungerStr = fullness >= 8 ? ChatColor.GREEN + "Saciado (" + fullness + "/10)" :
                           fullness >= 4 ? ChatColor.YELLOW + "Con hambre (" + fullness + "/10)" :
                                           ChatColor.RED + "¡Muerto de hambre! (" + fullness + "/10)";

        ItemStack soulItem = GUIBuilder.createGlowingItem(Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "✨ Alma de la Herramienta",
                "",
                GUIBuilder.INFO + "Personalidad: " + ChatColor.WHITE + personality,
                GUIBuilder.INFO + "Humor: " + moodStr,
                GUIBuilder.INFO + "Hambre: " + hungerStr,
                GUIBuilder.INFO + "Título: " + ChatColor.GOLD + (data.getTitle() != null && !data.getTitle().isEmpty() ? data.getTitle() : "Sin título"),
                "",
                GUIBuilder.INFO + "Corrupcíon: " + ChatColor.DARK_RED + data.getCorruption() + "%");
        gui.setItem(22, soulItem);

        // ── Slot 31: Estadísticas de combate
        ItemStack combatItem = GUIBuilder.createGlowingItem(Material.IRON_SWORD,
                ChatColor.RED + "⚔ Estadísticas de Combate",
                "",
                GUIBuilder.INFO + "Mobs eliminados: "  + ChatColor.WHITE + formatNum(data.getMobKills()),
                GUIBuilder.INFO + "Jugadores: "        + ChatColor.WHITE + formatNum(data.getPlayerKills()),
                GUIBuilder.INFO + "Bloques minados: "  + ChatColor.WHITE + formatNum(data.getBlocksMined()),
                GUIBuilder.INFO + "Acciones pacíficas: " + ChatColor.WHITE + data.getPeacefulActions());
        gui.setItem(31, combatItem);

        // ── Botones de navegación (fila inferior)

        // Slot 46: Árbol de habilidades
        ItemStack skillsBtn = GUIBuilder.createGlowingItem(Material.ENCHANTED_BOOK,
                ConfigManager.getMessage("stats_title"),
                "",
                ChatColor.GRAY + "Ver y desbloquear habilidades.",
                ChatColor.YELLOW + "► Click para abrir");
        gui.setItem(46, skillsBtn);

        // Slot 47: Historial
        ItemStack historyBtn = GUIBuilder.createGlowingItem(Material.BOOK,
                ConfigManager.getMessage("history_title"),
                "",
                ChatColor.GRAY + "Historial completo de la herramienta.",
                ChatColor.YELLOW + "► Click para abrir");
        gui.setItem(47, historyBtn);

        // Slot 48: Armadura
        ItemStack armorBtn = GUIBuilder.createGlowingItem(Material.DIAMOND_CHESTPLATE,
                ConfigManager.getMessage("armor_title"),
                "",
                ChatColor.GRAY + "Ver piezas de armadura viviente.",
                ChatColor.YELLOW + "► Click para abrir");
        int armorCount = 0;
        for (ItemStack it : player.getInventory().getArmorContents()) {
            if (com.livingtools.data.LivingArmor.isLivingArmor(it)) armorCount++;
        }
        if (armorCount > 0) {
            ItemMeta am = armorBtn.getItemMeta();
            List<String> al = am.getLore();
            al.add(ChatColor.GREEN + "Piezas activas: " + armorCount);
            am.setLore(al);
            armorBtn.setItemMeta(am);
        }
        gui.setItem(48, armorBtn);

        // Slot 50: Hitos (ruta de progresión)
        int milestonesCompleted = MilestoneManager.countCompleted(tool);
        int milestonesTotal = MilestoneManager.Milestone.values().length;
        ItemStack milestonesBtn = GUIBuilder.createGlowingItem(Material.NETHER_STAR,
                ChatColor.GOLD + "✦ Hitos de Progresión",
                "",
                ChatColor.YELLOW + "" + milestonesCompleted + "/" + milestonesTotal + " hitos completados",
                buildProgressBar((double) milestonesCompleted / milestonesTotal),
                "",
                ChatColor.GRAY + "Completa hitos para ganar XP bonus.",
                ChatColor.YELLOW + "► Click para ver hitos");
        gui.setItem(50, milestonesBtn);

        // Slot 51: Ayuda
        ItemStack helpBtn = GUIBuilder.createGlowingItem(Material.WRITABLE_BOOK,
                ConfigManager.getMessage("help_title"),
                "",
                ChatColor.GRAY + "Guía completa de Living Tools.",
                ChatColor.YELLOW + "► Click para abrir");
        gui.setItem(51, helpBtn);

        // Slot 53: Cerrar
        ItemStack close = GUIBuilder.createGlowingItem(Material.BARRIER,
                ChatColor.RED + "✖ Cerrar");
        gui.setItem(53, close);

        player.openInventory(gui);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String buildProgressBar(double pct) {
        int filled = (int) Math.round(pct * 20);
        StringBuilder sb = new StringBuilder(ChatColor.GREEN + "  [");
        for (int i = 0; i < 20; i++) {
            sb.append(i < filled ? ChatColor.GREEN + "█" : ChatColor.DARK_GRAY + "░");
        }
        sb.append(ChatColor.GREEN + "]");
        return sb.toString();
    }

    private static String formatNum(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }
}
