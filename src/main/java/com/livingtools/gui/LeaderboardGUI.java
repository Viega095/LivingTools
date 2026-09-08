package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.ConfigManager;
import com.livingtools.manager.MilestoneManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * LeaderboardGUI — Top 10 herramientas de jugadores online
 * por nivel, prestige, mobs matados y bloques minados.
 *
 * Se abre con /lt top
 * Actualiza datos en tiempo real de los jugadores online.
 */
public class LeaderboardGUI {

    private static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Top Herramientas ✦";

    public static void open(Player viewer) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        // Recopilar datos de jugadores online
        List<ToolSnapshot> snapshots = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack held = p.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(held)) {
                LivingTool tool = new LivingTool(held);
                snapshots.add(new ToolSnapshot(p, tool));
            }
            // Chequear también offhand
            ItemStack offhand = p.getInventory().getItemInOffHand();
            if (LivingTool.isLivingTool(offhand)) {
                LivingTool tool = new LivingTool(offhand);
                snapshots.add(new ToolSnapshot(p, tool));
            }
        }

        // Cabeceras de categoría
        setupBorder(gui);

        // === Columna 1: Top por Nivel (col 1, slots 10-16) ===
        List<ToolSnapshot> byLevel = new ArrayList<>(snapshots);
        byLevel.sort((a, b) -> {
            if (b.prestige != a.prestige) return Integer.compare(b.prestige, a.prestige);
            return Integer.compare(b.level, a.level);
        });
        addCategoryHeader(gui, 1, Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Top Nivel");
        addEntries(gui, byLevel, 10, (s) -> ChatColor.GREEN + "Lv." + s.level
                + (s.prestige > 0 ? ChatColor.GOLD + " [P" + s.prestige + "]" : ""));

        // === Columna 2: Top por Mobs (col 3, slots 12-18) ===
        List<ToolSnapshot> byMobs = new ArrayList<>(snapshots);
        byMobs.sort((a, b) -> Long.compare(b.mobKills, a.mobKills));
        addCategoryHeader(gui, 3, Material.IRON_SWORD, ChatColor.RED + "Top Mobs");
        addEntries(gui, byMobs, 12, (s) -> ChatColor.RED + "" + formatNum(s.mobKills) + " kills");

        // === Columna 3: Top por Bloques (col 5, slots 14-20) ===
        List<ToolSnapshot> byBlocks = new ArrayList<>(snapshots);
        byBlocks.sort((a, b) -> Long.compare(b.blocksMined, a.blocksMined));
        addCategoryHeader(gui, 5, Material.DIAMOND_PICKAXE, ChatColor.AQUA + "Top Bloques");
        addEntries(gui, byBlocks, 14, (s) -> ChatColor.AQUA + "" + formatNum(s.blocksMined) + " bloques");

        // === Columna 4: Top Hitos (col 7, slots 16-22) ===
        List<ToolSnapshot> byMilestones = new ArrayList<>(snapshots);
        byMilestones.sort((a, b) -> Integer.compare(b.milestonesCompleted, a.milestonesCompleted));
        addCategoryHeader(gui, 7, Material.NETHER_STAR, ChatColor.GOLD + "Top Hitos");
        addEntries(gui, byMilestones, 16, (s) -> ChatColor.GOLD + "" + s.milestonesCompleted + "/"
                + MilestoneManager.Milestone.values().length + " hitos");

        // Info footer
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(ChatColor.GRAY + "Mostrando " + snapshots.size() + " herramienta(s) de "
                + Bukkit.getOnlinePlayers().size() + " jugador(es) online");
        List<String> infoLore = Arrays.asList(
                "",
                ChatColor.GRAY + "Solo se muestran herramientas activas.",
                ChatColor.GRAY + "Actualiza al abrir el menú."
        );
        im.setLore(infoLore);
        info.setItemMeta(im);
        gui.setItem(49, info);

        // Botón cerrar
        ItemStack close = GUIBuilder.createGlowingItem(Material.BARRIER, ChatColor.RED + "✖ Cerrar");
        gui.setItem(53, close);

        viewer.openInventory(gui);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static void addCategoryHeader(Inventory gui, int col, Material mat, String title) {
        ItemStack header = new ItemStack(mat);
        ItemMeta meta = header.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Ranking en vivo"));
        header.setItemMeta(meta);
        gui.setItem(col, header); // fila 0 (top border), columna col
    }

    private static void addEntries(Inventory gui, List<ToolSnapshot> list, int startSlot,
                                    java.util.function.Function<ToolSnapshot, String> statLine) {
        String[] medals = {ChatColor.GOLD + "① ", ChatColor.GRAY + "② ", ChatColor.YELLOW + "③ ",
                ChatColor.WHITE + "④ ", ChatColor.WHITE + "⑤ "};
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            ToolSnapshot snap = list.get(i);
            ItemStack icon = snap.toolItem.clone();
            ItemMeta meta = icon.getItemMeta() != null ? icon.getItemMeta()
                    : new ItemStack(Material.STICK).getItemMeta();
            String medal = i < medals.length ? medals[i] : (i + 1) + ". ";
            meta.setDisplayName(medal + ChatColor.WHITE + snap.playerName);
            List<String> lore = Arrays.asList(
                    "",
                    ChatColor.GRAY + "Herramienta: " + ChatColor.WHITE + snap.toolDisplayName,
                    ChatColor.GRAY + "Personalidad: " + ChatColor.YELLOW + snap.personality,
                    ChatColor.GRAY + "Stat: " + statLine.apply(snap),
                    ""
            );
            meta.setLore(lore);
            icon.setItemMeta(meta);
            gui.setItem(startSlot + (i * 9), icon);
        }
    }

    private static void setupBorder(Inventory gui) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        // Fila superior e inferior
        for (int i = 0; i < 9; i++) gui.setItem(i, border);
        for (int i = 45; i < 54; i++) gui.setItem(i, border);
        // Columnas laterales
        for (int i = 1; i <= 4; i++) {
            gui.setItem(i * 9, border);
            gui.setItem(i * 9 + 8, border);
        }
    }

    private static String formatNum(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }

    // -----------------------------------------------------------------------
    // Snapshot data class
    // -----------------------------------------------------------------------
    private static class ToolSnapshot {
        final String playerName;
        final String toolDisplayName;
        final String personality;
        final int level;
        final int prestige;
        final long mobKills;
        final long blocksMined;
        final int milestonesCompleted;
        final ItemStack toolItem;

        ToolSnapshot(Player player, LivingTool tool) {
            this.playerName = player.getName();
            ToolData data = tool.getData();
            ItemStack item = tool.getItem();
            this.toolDisplayName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                    ? item.getItemMeta().getDisplayName()
                    : item.getType().name().replace("_", " ").toLowerCase();
            this.personality = data.getPersonality() != null ? data.getPersonality() : "???";
            this.level = data.getLevel();
            this.prestige = data.getPrestige();
            this.mobKills = data.getMobKills();
            this.blocksMined = data.getBlocksMined();
            this.milestonesCompleted = MilestoneManager.countCompleted(tool);
            this.toolItem = item.clone();
        }
    }
}
