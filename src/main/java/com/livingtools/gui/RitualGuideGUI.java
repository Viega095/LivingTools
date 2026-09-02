package com.livingtools.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RitualGuideGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "Guía de Rituales";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Fill background
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Ritual 1: Restoration & Prestige
        ItemStack ritual1 = new ItemStack(Material.BEACON);
        ItemMeta r1Meta = ritual1.getItemMeta();
        r1Meta.setDisplayName(ChatColor.GOLD + "Ritual de Ascensión");
        List<String> r1Lore = new ArrayList<>();
        r1Lore.add(ChatColor.GRAY + "Restaura la durabilidad y");
        r1Lore.add(ChatColor.GRAY + "otorga gran experiencia.");
        r1Lore.add("");
        r1Lore.add(ChatColor.YELLOW + "Ingredientes:");
        r1Lore.add(ChatColor.WHITE + "- Herramienta Viviente (Centro)");
        r1Lore.add(ChatColor.WHITE + "- Altar Completo");
        r1Lore.add("");
        r1Lore.add(ChatColor.LIGHT_PURPLE + "Efecto:");
        r1Lore.add(ChatColor.GRAY + "Reparación Total + 5000 XP");
        r1Meta.setLore(r1Lore);
        ritual1.setItemMeta(r1Meta);
        inv.setItem(11, ritual1);

        // Ritual 2: Awakening (Info only)
        ItemStack ritual2 = new ItemStack(Material.NETHER_STAR);
        ItemMeta r2Meta = ritual2.getItemMeta();
        r2Meta.setDisplayName(ChatColor.AQUA + "El Despertar");
        List<String> r2Lore = new ArrayList<>();
        r2Lore.add(ChatColor.GRAY + "Da vida a una herramienta inerte.");
        r2Lore.add("");
        r2Lore.add(ChatColor.YELLOW + "Requisito:");
        r2Lore.add(ChatColor.WHITE + "- Nivel 30 de XP");
        r2Lore.add(ChatColor.WHITE + "- Herramienta Durmiente");
        r2Lore.add("");
        r2Lore.add(ChatColor.LIGHT_PURPLE + "Acción:");
        r2Lore.add(ChatColor.GRAY + "Clic Derecho con la herramienta");
        r2Meta.setLore(r2Lore);
        ritual2.setItemMeta(r2Meta);
        inv.setItem(13, ritual2);

        // Ritual 3: Reincarnation
        ItemStack ritual3 = new ItemStack(Material.NETHER_STAR);
        ItemMeta r3Meta = ritual3.getItemMeta();
        r3Meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Reencarnación");
        List<String> r3Lore = new ArrayList<>();
        r3Lore.add(ChatColor.GRAY + "Cambia la personalidad de la herramienta.");
        r3Lore.add("");
        r3Lore.add(ChatColor.YELLOW + "Catalizador:");
        r3Lore.add(ChatColor.WHITE + "- Estrella del Nether");
        r3Meta.setLore(r3Lore);
        ritual3.setItemMeta(r3Meta);
        inv.setItem(15, ritual3);

        // Ritual 4: Unbinding
        ItemStack ritual4 = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta r4Meta = ritual4.getItemMeta();
        r4Meta.setDisplayName(ChatColor.AQUA + "Desvinculación");
        List<String> r4Lore = new ArrayList<>();
        r4Lore.add(ChatColor.GRAY + "Elimina al dueño de la herramienta.");
        r4Lore.add("");
        r4Lore.add(ChatColor.YELLOW + "Catalizador:");
        r4Lore.add(ChatColor.WHITE + "- Tótem de la Inmortalidad");
        r4Meta.setLore(r4Lore);
        ritual4.setItemMeta(r4Meta);
        inv.setItem(12, ritual4); // Row 2

        // Ritual 5: Purification
        ItemStack ritual5 = new ItemStack(Material.GHAST_TEAR);
        ItemMeta r5Meta = ritual5.getItemMeta();
        r5Meta.setDisplayName(ChatColor.WHITE + "Purificación");
        List<String> r5Lore = new ArrayList<>();
        r5Lore.add(ChatColor.GRAY + "Reduce la corrupción (-10).");
        r5Lore.add("");
        r5Lore.add(ChatColor.YELLOW + "Catalizador:");
        r5Lore.add(ChatColor.WHITE + "- Lágrima de Ghast");
        r5Meta.setLore(r5Lore);
        ritual5.setItemMeta(r5Meta);
        inv.setItem(14, ritual5); // Row 2

        // Ritual 6: Enchantment
        ItemStack ritual6 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta r6Meta = ritual6.getItemMeta();
        r6Meta.setDisplayName(ChatColor.BLUE + "Ritual de Encantamiento");
        List<String> r6Lore = new ArrayList<>();
        r6Lore.add(ChatColor.GRAY + "Transfiere encantamientos a la herramienta.");
        r6Lore.add("");
        r6Lore.add(ChatColor.YELLOW + "Sacrificio:");
        r6Lore.add(ChatColor.WHITE + "- Libro Encantado");
        r6Meta.setLore(r6Lore);
        ritual6.setItemMeta(r6Meta);
        inv.setItem(16, ritual6);

        // Ritual 7: Soul Reaper
        ItemStack ritual7 = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta r7Meta = ritual7.getItemMeta();
        r7Meta.setDisplayName(ChatColor.DARK_GRAY + "Cosechador de Almas");
        List<String> r7Lore = new ArrayList<>();
        r7Lore.add(ChatColor.GRAY + "Otorga la habilidad Soul Reaper.");
        r7Lore.add("");
        r7Lore.add(ChatColor.YELLOW + "Sacrificio:");
        r7Lore.add(ChatColor.WHITE + "- Calavera de Wither");
        r7Meta.setLore(r7Lore);
        ritual7.setItemMeta(r7Meta);
        inv.setItem(10, ritual7);

        // Ritual 8: Thunderlord
        ItemStack ritual8 = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta r8Meta = ritual8.getItemMeta();
        r8Meta.setDisplayName(ChatColor.YELLOW + "Señor del Trueno");
        List<String> r8Lore = new ArrayList<>();
        r8Lore.add(ChatColor.GRAY + "Otorga la habilidad Thunderlord.");
        r8Lore.add("");
        r8Lore.add(ChatColor.YELLOW + "Sacrificio:");
        r8Lore.add(ChatColor.WHITE + "- Pararrayos");
        r8Meta.setLore(r8Lore);
        ritual8.setItemMeta(r8Meta);
        inv.setItem(20, ritual8);

        // Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Volver al Altar");
        back.setItemMeta(backMeta);
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    public static void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (clicked.getType() == Material.ARROW) {
            RitualGUI.open(player);
        }
    }
}
