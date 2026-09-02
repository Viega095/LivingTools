package com.livingtools.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreManager {

    public enum TomeType {
        ORIGINS("Orígenes del Vacío", "Historia de cómo las herramientas cobraron vida...", "origins"),
        THE_FIRST_SMITH("El Primer Herrero", "Leyenda del creador de la Forja Maldita.", "first_smith"),
        VOID_WHISPERS("Susurros del Vacío", "Relatos de aquellos que miraron al abismo.", "void_whispers"),
        CHRONOMANCY_BASICS("Fundamentos del Tiempo", "Teoría sobre la manipulación temporal.", "chronomancy_basics"),
        SOUL_BINDING("Vínculo de Almas", "El arte de unir un alma a un objeto.", "soul_binding");

        private final String title;
        private final String content;
        private final String id;

        TomeType(String title, String content, String id) {
            this.title = title;
            this.content = content;
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getId() {
            return id;
        }

        public static TomeType fromId(String id) {
            for (TomeType type : values()) {
                if (type.getId().equals(id))
                    return type;
            }
            return null;
        }
    }

    public static ItemStack createTome(TomeType type) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(ChatColor.GOLD + type.getTitle());
            meta.setAuthor("Viega095");
            meta.addPage(type.getContent());
            meta.setLore(List.of(ChatColor.GRAY + "Un tomo perdido de conocimiento antiguo.",
                    ChatColor.DARK_GRAY + "ID: " + type.getId()));
            book.setItemMeta(meta);
        }
        return book;
    }

    public static void unlockTome(org.bukkit.entity.Player player, com.livingtools.data.LivingTool tool,
            TomeType type) {
        if (hasTome(tool, type)) {
            player.sendMessage(ChatColor.RED + "Tu herramienta ya conoce esta historia.");
            return;
        }

        tool.getData().addCollectedTome(type.getId());

        // Reward: 500 XP
        tool.addXP(player, 500);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1.5f);
        player.sendMessage(ChatColor.GOLD + "¡Conocimiento Antiguo desbloqueado! (" + type.getTitle() + ")");
        player.sendMessage(ChatColor.GREEN + "+500 XP para tu herramienta.");
    }

    public static boolean hasTome(com.livingtools.data.LivingTool tool, TomeType type) {
        return tool.getData().getCollectedTomes().contains(type.getId());
    }

    public static List<TomeType> getUnlockedTomes(com.livingtools.data.LivingTool tool) {
        List<String> ids = tool.getData().getCollectedTomes();
        List<TomeType> tomes = new ArrayList<>();
        for (String id : ids) {
            TomeType type = TomeType.fromId(id);
            if (type != null)
                tomes.add(type);
        }
        return tomes;
    }

    public static void tryDropTome(org.bukkit.Location loc) {
        if (Math.random() < 0.05) { // 5% chance
            TomeType[] values = TomeType.values();
            TomeType type = values[(int) (Math.random() * values.length)];
            loc.getWorld().dropItemNaturally(loc, createTome(type));
        }
    }
}
