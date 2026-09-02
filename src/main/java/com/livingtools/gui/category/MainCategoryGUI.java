package com.livingtools.gui.category;

import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Menú principal unificado de categorías y recetas.
 */
public class MainCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Living Tools - Menú");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        GUIBuilder.setBorder54(gui, Material.LIME_STAINED_GLASS_PANE);

        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.BOOK,
                GUIBuilder.PRIMARY + "✦ Libro de Recetas",
                "",
                GUIBuilder.INFO + "Explora crafteos, forjas y reliquias",
                GUIBuilder.INFO + "de Living Tools"));

        gui.setItem(10, createCategory(Material.DIAMOND_PICKAXE, "⛏ Herramientas",
                "Picos, palas, hachas y azadas"));
        gui.setItem(12, createCategory(Material.DIAMOND_CHESTPLATE, "🛡 Armaduras",
                "Todas las piezas de armadura viviente"));
        gui.setItem(14, createCategory(Material.DIAMOND_SWORD, "⚔ Armas",
                "Espadas y progresión de combate"));
        gui.setItem(16, createCategory(Material.ENCHANTED_BOOK, "✧ Artefactos",
                "Amuletos, gemas, runas y geodas"));

        gui.setItem(28, createCategory(Material.NETHER_STAR, "✦ Forja de Jefes",
                "Recetas legendarias en cruz"));
        gui.setItem(30, createCategory(Material.CRAFTING_TABLE, "⚒ Mesa de Ensamblaje",
                "Estructura 3x3 para crafteos avanzados"));
        gui.setItem(32, createCategory(Material.OAK_SAPLING, "🌿 Reliquias de Jefe",
                "Dryad, Wyrm y Leviatán"));

        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createCategory(Material material, String name, String description) {
        return GUIBuilder.createGlowingItem(
                material,
                GUIBuilder.PRIMARY + name,
                "",
                GUIBuilder.INFO + description,
                "",
                GUIBuilder.SECONDARY + "Click para explorar");
    }
}
