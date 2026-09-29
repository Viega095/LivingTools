package com.livingtools.gui.category;

import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * MainCategoryGUI — Menú interactivo principal unificado de recetas, bestiario y progresión.
 */
public class MainCategoryGUI {

    public static final String TITLE = GUIBuilder.createTitle("Living Tools - Menú Principal");

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Header
        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.BOOK,
                ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Enciclopedia de Living Tools ✦",
                "",
                ChatColor.AQUA + "Guía completa de crafteos, bestiario de jefes,",
                ChatColor.AQUA + "sendas de ascensión divina y forjas.",
                "",
                ChatColor.YELLOW + "Selecciona una categoría para comenzar:"));

        // Fila 2: Categorías de Equipo (Slots 10, 12, 14, 16)
        gui.setItem(10, createCategory(Material.DIAMOND_PICKAXE, "⛏ Herramientas Vivientes",
                "Picos, palas, hachas y azadas con evolución"));
        gui.setItem(12, createCategory(Material.DIAMOND_CHESTPLATE, "🛡 Armaduras Vivientes",
                "Conjuntos de armadura, afinidades y set bonuses"));
        gui.setItem(14, createCategory(Material.DIAMOND_SWORD, "⚔ Armas & Combate",
                "Espadas, arcos vivientes y habilidades"));
        gui.setItem(16, createCategory(Material.ENCHANTED_BOOK, "✧ Artefactos & Runas",
                "Amuletos, gemas de alma, runas y geodas"));

        // Fila 3: Guía Rápida para Principiantes (Slot 22)
        gui.setItem(22, GUIBuilder.createGlowingItem(
                Material.KNOWLEDGE_BOOK,
                ChatColor.GREEN + "" + ChatColor.BOLD + "📖 ¿Cómo Iniciar? (Guía Rápida)",
                "",
                ChatColor.GRAY + "1. Craftea una herramienta con semillas o carbón.",
                ChatColor.GRAY + "2. Usa " + ChatColor.YELLOW + "/lt bind" + ChatColor.GRAY + " para vincularla a tu alma.",
                ChatColor.GRAY + "3. Mina o lucha para ganar XP y subir de nivel.",
                ChatColor.GRAY + "4. Aliméntala con " + ChatColor.YELLOW + "/lt feed" + ChatColor.GRAY + " cuando tenga hambre.",
                ChatColor.GRAY + "5. Construye estructuras para forjar reliquias.",
                "",
                ChatColor.YELLOW + "► Click para recibir el libro guía detallado"));

        // Fila 4: Estructuras y Bestiario (Slots 28, 30, 32, 34)
        gui.setItem(28, createCategory(Material.NETHER_STAR, "✦ Forja de Jefes (Cruz)",
                "Recetas legendarias con materiales de jefes"));
        gui.setItem(30, createCategory(Material.CRAFTING_TABLE, "⚒ Mesa de Ensamblaje (3x3)",
                "Estructura en el mundo para crafteos avanzados"));
        gui.setItem(32, createCategory(Material.OAK_SAPLING, "🌿 Reliquias & Armas de Jefes",
                "Dryad, Wyrm y Leviatán y armas míticas"));
        gui.setItem(34, GUIBuilder.createGlowingItem(
                Material.ZOMBIE_HEAD,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "🐉 Bestiario de Jefes & Esbirros",
                "",
                ChatColor.GRAY + "Consulta la vida, daño, dificultad, ubicación",
                ChatColor.GRAY + "y drops detallados de cada Jefe y sus Esbirros.",
                "",
                ChatColor.YELLOW + "► Click para abrir el Bestiario"));

        // Fila 5: Sistemas 4.0 (Slots 38, 40, 42)
        gui.setItem(38, createCategory(Material.BEACON, "👑 Sendas de Ascensión Divina",
                "Consagra tus armas Lv.100 a deidades celestiales"));
        gui.setItem(40, createCategory(Material.ENDER_EYE, "🃏 Tarot Arcano del Destino",
                "Roba cartas místicas con bendiciones de 30 min"));
        gui.setItem(42, createCategory(Material.SHIELD, "🏰 Hermandades de Almas",
                "Clanes, bóveda compartida y progresión grupal"));

        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static ItemStack createCategory(Material material, String name, String description) {
        return GUIBuilder.createGlowingItem(
                material,
                ChatColor.GOLD + name,
                "",
                ChatColor.GRAY + description,
                "",
                ChatColor.YELLOW + "► Click para explorar");
    }
}
