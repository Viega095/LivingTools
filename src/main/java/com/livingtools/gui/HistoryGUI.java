package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.manager.LegacyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GUI de Historial de la Herramienta Viva.
 * Muestra stats completos en un inventario de 54 slots.
 */
public class HistoryGUI {

    public static final String TITLE = ChatColor.DARK_AQUA + "Historial de Herramienta";

    public static void open(Player player, LivingTool tool) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        ToolData data = tool.getData();

        // Fill background with cyan glass
        ItemStack bg = makeItem(Material.CYAN_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, bg);
        }

        // ── Slot 4: Tool Identity
        String toolName = tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName()
                ? tool.getItem().getItemMeta().getDisplayName()
                : ChatColor.GRAY + tool.getItem().getType().name();
        String customName = data.getCustomName();
        String title = data.getTitle();
        List<String> identityLore = new ArrayList<>();
        if (!customName.isEmpty()) identityLore.add(ChatColor.YELLOW + "Nombre: " + ChatColor.WHITE + customName);
        if (!title.isEmpty())      identityLore.add(ChatColor.GOLD   + "Título: " + ChatColor.WHITE + title);
        identityLore.add(ChatColor.GRAY + "Material: " + ChatColor.WHITE + tool.getItem().getType().name());
        identityLore.add(ChatColor.GRAY + "Personalidad: " + ChatColor.WHITE + orUnknown(data.getPersonality()));
        inv.setItem(4, makeLoreItem(Material.BOOK, ChatColor.AQUA + "✎ Identidad", identityLore));

        // ── Slot 19: Progression
        List<String> progressLore = new ArrayList<>();
        progressLore.add(ChatColor.GREEN  + "Nivel: " + ChatColor.WHITE + data.getLevel());
        progressLore.add(ChatColor.GREEN  + "XP Total: " + ChatColor.WHITE + formatNumber(data.getXP()));
        progressLore.add(ChatColor.AQUA   + "Prestigio: " + ChatColor.WHITE + LivingTool.romanNumeral(data.getPrestige()));
        int legacyCount = LegacyManager.getLegacyCountFromItem(tool.getItem());
        if (legacyCount > 0) {
            progressLore.add(ChatColor.GOLD + "Legado: " + ChatColor.WHITE
                    + LivingTool.romanNumeral(legacyCount) + " (+" + (legacyCount * 10) + "% XP)");
        }
        inv.setItem(19, makeLoreItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "⬆ Progresión", progressLore));

        // ── Slot 21: Combat Stats
        List<String> combatLore = new ArrayList<>();
        combatLore.add(ChatColor.RED     + "Mobs eliminados: "     + ChatColor.WHITE + formatNumber(data.getMobKills()));
        combatLore.add(ChatColor.DARK_RED + "Jugadores eliminados: " + ChatColor.WHITE + data.getPlayerKills());
        combatLore.add(ChatColor.YELLOW  + "Acciones pacíficas: "  + ChatColor.WHITE + data.getPeacefulActions());
        inv.setItem(21, makeLoreItem(Material.IRON_SWORD, ChatColor.RED + "⚔ Combate", combatLore));

        // ── Slot 23: Mining Stats
        List<String> miningLore = new ArrayList<>();
        miningLore.add(ChatColor.YELLOW + "Bloques minados: " + ChatColor.WHITE + formatNumber(data.getBlocksMined()));
        inv.setItem(23, makeLoreItem(Material.IRON_PICKAXE, ChatColor.YELLOW + "⛏ Minería", miningLore));

        // ── Slot 25: Origin
        String dateStr = "Desconocida";
        long created = data.getCreationDate();
        if (created > 0) {
            dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(created));
        }
        List<String> originLore = new ArrayList<>();
        originLore.add(ChatColor.AQUA  + "Creador: "   + ChatColor.WHITE + orUnknown(data.getOwnerName()));
        originLore.add(ChatColor.AQUA  + "Nacimiento: " + ChatColor.WHITE + dateStr);
        inv.setItem(25, makeLoreItem(Material.CLOCK, ChatColor.AQUA + "📜 Origen", originLore));

        // ── Slot 37: Abilities
        List<String> abilityLore = new ArrayList<>();
        List<String> abilities = tool.getAbilities();
        if (abilities.isEmpty()) {
            abilityLore.add(ChatColor.GRAY + "Sin habilidades desbloqueadas.");
        } else {
            for (String ab : abilities) {
                abilityLore.add(ChatColor.GREEN + "▸ " + ChatColor.WHITE + ab);
            }
        }
        inv.setItem(37, makeLoreItem(Material.ENCHANTED_BOOK, ChatColor.LIGHT_PURPLE + "✦ Habilidades", abilityLore));

        // ── Slot 39: Runes
        List<String> runeLore = new ArrayList<>();
        List<com.livingtools.runes.RuneType> runes = data.getRunes();
        if (runes.isEmpty()) {
            runeLore.add(ChatColor.GRAY + "Sin runas equipadas.");
        } else {
            for (com.livingtools.runes.RuneType r : runes) {
                runeLore.add(ChatColor.LIGHT_PURPLE + "▸ " + ChatColor.WHITE + r.getName());
            }
        }
        inv.setItem(39, makeLoreItem(Material.AMETHYST_SHARD, ChatColor.DARK_PURPLE + "◈ Runas", runeLore));

        // ── Slot 41: Biome Flags
        List<String> biomeLore = new ArrayList<>();
        int netherXP = data.getBiomeXP(com.livingtools.manager.BiomeManager.BiomeCategory.NETHER);
        int oceanXP  = data.getBiomeXP(com.livingtools.manager.BiomeManager.BiomeCategory.OCEAN);
        int skyXP    = data.getBiomeXP(com.livingtools.manager.BiomeManager.BiomeCategory.SKY);
        if (netherXP > 0) biomeLore.add(ChatColor.RED   + "▸ Nether: " + ChatColor.WHITE + formatNumber(netherXP) + " XP");
        if (oceanXP  > 0) biomeLore.add(ChatColor.AQUA  + "▸ Océano: " + ChatColor.WHITE + formatNumber(oceanXP)  + " XP");
        if (skyXP    > 0) biomeLore.add(ChatColor.WHITE + "▸ Cielo: "  + ChatColor.GRAY  + formatNumber(skyXP)    + " XP");
        if (biomeLore.isEmpty())  biomeLore.add(ChatColor.GRAY + "Sin biomas especiales explorados.");
        inv.setItem(41, makeLoreItem(Material.COMPASS, ChatColor.GREEN + "🌍 Biomas", biomeLore));

        // ── Slot 49: Close button
        inv.setItem(49, makeLoreItem(Material.BARRIER, ChatColor.RED + "✖ Cerrar", null));

        player.openInventory(inv);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static ItemStack makeItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeLoreItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String orUnknown(String s) {
        return (s == null || s.isEmpty()) ? "Desconocido" : s;
    }

    private static String formatNumber(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }
}
