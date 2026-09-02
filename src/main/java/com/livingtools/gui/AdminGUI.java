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

public class AdminGUI {

        public static final String TITLE = ChatColor.DARK_RED + "Panel de Administración";

        public static void open(Player player) {
                Inventory inv = Bukkit.createInventory(null, 27, TITLE);

                // 1. Give Tools
                ItemStack tools = createItem(Material.DIAMOND_PICKAXE, "&bDar Herramientas",
                                "&7Haz clic para obtener", "&7herramientas de prueba.");
                inv.setItem(10, tools);

                // 2. Spawn Bosses
                ItemStack bosses = createItem(Material.NETHER_STAR, "&cInvocar Jefes",
                                "&7Haz clic para abrir el", "&7menú de invocación.");
                inv.setItem(12, bosses);

                // 3. Reload Config
                ItemStack reload = createItem(Material.REDSTONE_TORCH, "&eRecargar Configuración",
                                "&7Aplica cambios de", "&7config.yml y messages.yml");
                inv.setItem(14, reload);

                // 4. Give Artifacts
                ItemStack artifacts = createItem(Material.TOTEM_OF_UNDYING, "&6Dar Artefactos",
                                "&7Haz clic para obtener", "&7artefactos legendarios.");
                inv.setItem(16, artifacts);

                // 5. Give Runes
                ItemStack runes = createItem(Material.LIME_DYE, "&aDar Runas",
                                "&7Haz clic para obtener", "&7runas de poder.");
                inv.setItem(19, runes);

                // 6. Give Soul Gems
                ItemStack souls = createItem(Material.SOUL_SAND, "&bDar Gemas de Alma",
                                "&7Haz clic para obtener", "&7almas capturadas.");
                inv.setItem(21, souls);

                // 7. Manage Players (Placeholder)
                ItemStack players = createItem(Material.PLAYER_HEAD, "&dGestionar Jugadores",
                                "&7Ver estadísticas y", "&7modificar datos.");
                inv.setItem(23, players);

                // 8. Global Settings (Placeholder)
                ItemStack settings = createItem(Material.COMPARATOR, "&eAjustes Globales",
                                "&7Próximamente...");
                inv.setItem(25, settings);

                // Fillers
                ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
                for (int i = 0; i < inv.getSize(); i++) {
                        if (inv.getItem(i) == null) {
                                inv.setItem(i, filler);
                        }
                }

                player.openInventory(inv);
        }

        private static ItemStack createItem(Material material, String name, String... lore) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                List<String> loreList = new ArrayList<>();
                for (String s : lore) {
                        loreList.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                meta.setLore(loreList);
                item.setItemMeta(meta);
                return item;
        }
}
