package com.livingtools.gui;

import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.SoulTarotManager;
import com.livingtools.manager.SoulTarotManager.TarotCard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * SoulTarotGUI — Interfaz visual del Tarot Arcano de Almas y Mazo del Destino.
 */
public class SoulTarotGUI {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ Tarot Arcano del Destino ✦";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);
        UUID uuid = player.getUniqueId();

        ItemStack border = GUIBuilder.createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        TarotCard active = SoulTarotManager.getActiveCard(uuid);
        long remSec = SoulTarotManager.getRemainingSeconds(uuid);

        // Slot 4: Carta Activa
        if (active != null) {
            ItemStack activeItem = GUIBuilder.createGlowingItem(active.getIcon(),
                    ChatColor.GOLD + "✦ Carta Activa: " + active.getDisplayName(),
                    ChatColor.YELLOW + active.getDescription(),
                    "",
                    ChatColor.AQUA + "Tiempo restante: " + (remSec / 60) + "m " + (remSec % 60) + "s");
            gui.setItem(4, activeItem);
        } else {
            ItemStack noActive = GUIBuilder.createItem(Material.BARRIER,
                    ChatColor.RED + "Sin Carta Activa",
                    ChatColor.GRAY + "Roba una carta del mazo para recibir una bendición arcana de 30 min.");
            gui.setItem(4, noActive);
        }

        // Slot 13: Mazo Central
        boolean daily = SoulTarotManager.canDrawDaily(uuid);
        ItemStack deck = GUIBuilder.createGlowingItem(Material.ENCHANTED_BOOK,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Mazo de Almas del Destino",
                ChatColor.GRAY + "Roba una carta para cambiar tu destino durante 30 minutos.",
                "",
                daily ? ChatColor.GREEN + "► ¡Tirada Diaria Disponible! (Click para robar)"
                        : ChatColor.YELLOW + "► Robar carta arcana (Costo: 500 XP de Herramienta)");
        gui.setItem(13, deck);

        // Preview de las 6 cartas
        TarotCard[] cards = TarotCard.values();
        int[] previewSlots = {10, 11, 12, 14, 15, 16};
        for (int i = 0; i < Math.min(cards.length, previewSlots.length); i++) {
            TarotCard c = cards[i];
            ItemStack cardItem = GUIBuilder.createItem(c.getIcon(),
                    c.getDisplayName(),
                    ChatColor.GRAY + c.getDescription());
            gui.setItem(previewSlots[i], cardItem);
        }

        player.openInventory(gui);
    }
}
