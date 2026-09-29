package com.livingtools.gui;

import com.livingtools.manager.SoulGuildManager;
import com.livingtools.manager.SoulGuildManager.SoulGuild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SoulGuildVaultGUI — Interfaz de Bóveda Compartida de la Hermandad de Almas con sincronización segura.
 */
public class SoulGuildVaultGUI {

    public static final String VAULT_TITLE_PREFIX = ChatColor.DARK_AQUA + "✦ Bóveda: ";
    private static final Map<UUID, SoulGuild> openVaults = new ConcurrentHashMap<>();

    public static void open(Player player) {
        SoulGuild guild = SoulGuildManager.getGuild(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "No perteneces a ninguna Hermandad de Almas.");
            return;
        }

        if (guild.getLevel() < 4) {
            player.sendMessage(ChatColor.RED + "La Bóveda de Hermandad requiere Nivel 4 de Hermandad (Nivel actual: " + guild.getLevel() + ").");
            return;
        }

        int size = (guild.getLevel() >= 8) ? 54 : 27;
        String title = VAULT_TITLE_PREFIX + guild.getName() + " ✦";
        Inventory inv = Bukkit.createInventory(player, size, title);

        ItemStack[] saved = guild.getVaultContents();
        if (saved != null) {
            for (int i = 0; i < Math.min(size, saved.length); i++) {
                if (saved[i] != null) {
                    inv.setItem(i, saved[i].clone());
                }
            }
        }

        openVaults.put(player.getUniqueId(), guild);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 0.8f);
    }

    public static SoulGuild getOpenVaultGuild(UUID playerUuid) {
        return openVaults.get(playerUuid);
    }

    public static void removeOpenVault(UUID playerUuid) {
        openVaults.remove(playerUuid);
    }
}
