package com.livingtools.gui;

import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * AdminTestingGUI — Suite completa interactiva para probar y verificar todas las interfaces,
 * arenas, animaciones de meteoritos, forja rítmica y jefes del plugin en 1 click.
 */
public class AdminTestingGUI {

    public static final String TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "✦ Panel de Pruebas & Verificación ✦";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);
        GUIBuilder.setBorder54(gui, Material.RED_STAINED_GLASS_PANE);

        // Header
        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.COMMAND_BLOCK,
                ChatColor.RED + "✦ SUITE DE PRUEBAS LIVING TOOLS ✦",
                "",
                ChatColor.GRAY + "Verifica cada interfaz, invoca jefes, prueba animaciones",
                ChatColor.GRAY + "de meteoritos, repara herramientas en la forja y viaja al reino.",
                "",
                ChatColor.YELLOW + "Selecciona una acción para probarla al instante:"));

        // Fila 2: Verificador de GUIs Principales
        gui.setItem(10, GUIBuilder.createItem(Material.NETHER_STAR, ChatColor.GOLD + "1. Dashboard Principal", "", ChatColor.GRAY + "Abre el menú interactivo de la herramienta.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(11, GUIBuilder.createItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "2. Árbol de Talentos", "", ChatColor.GRAY + "Abre la interfaz de talentos pasivos.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(12, GUIBuilder.createItem(Material.WRITABLE_BOOK, ChatColor.LIGHT_PURPLE + "3. Bestiario Mítico", "", ChatColor.GRAY + "Abre la lista de 15 criaturas y detalles.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(13, GUIBuilder.createItem(Material.CRAFTING_TABLE, ChatColor.AQUA + "4. Menú de Recetas", "", ChatColor.GRAY + "Abre la enciclopedia de crafteos.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(14, GUIBuilder.createItem(Material.BEACON, ChatColor.YELLOW + "5. Ascensión Divina", "", ChatColor.GRAY + "Abre el altar de las 3 sendas divinas.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(15, GUIBuilder.createItem(Material.ENDER_EYE, ChatColor.DARK_PURPLE + "6. Tarot del Destino", "", ChatColor.GRAY + "Abre la tirada de cartas arcanas.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(16, GUIBuilder.createItem(Material.EMERALD, ChatColor.GREEN + "7. Sockets de Gemas", "", ChatColor.GRAY + "Abre el engarce de gemas y Fénix.", "", ChatColor.YELLOW + "► Click para abrir"));

        // Fila 3: Verificador de GUIs de Modos & Forjas
        gui.setItem(19, GUIBuilder.createItem(Material.END_CRYSTAL, ChatColor.LIGHT_PURPLE + "8. Grieta Abisal (Rift)", "", ChatColor.GRAY + "Abre el panel del Abismo Endless.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(20, GUIBuilder.createItem(Material.ANVIL, ChatColor.GOLD + "9. Forja Rítmica (Minijuego)", "", ChatColor.GRAY + "Abre el minijuego de forja al compás.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(21, GUIBuilder.createItem(Material.MAP, ChatColor.YELLOW + "10. Contratos de Caza", "", ChatColor.GRAY + "Abre los contratos diarios de monstruos.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(22, GUIBuilder.createItem(Material.SMITHING_TABLE, ChatColor.AQUA + "11. Forja de Reparación", "", ChatColor.GRAY + "Abre el menú de mantenimiento y reforge.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(23, GUIBuilder.createItem(Material.LODESTONE, ChatColor.DARK_AQUA + "12. Fusión de Almas", "", ChatColor.GRAY + "Abre la mesa de fusión de 2 armas.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(24, GUIBuilder.createItem(Material.BLAZE_POWDER, ChatColor.RED + "13. Estelas Cosméticas", "", ChatColor.GRAY + "Abre el selector de auras y partículas.", "", ChatColor.YELLOW + "► Click para abrir"));
        gui.setItem(25, GUIBuilder.createItem(Material.CHEST, ChatColor.GOLD + "14. Bóveda de Clan", "", ChatColor.GRAY + "Abre la bóveda comunitaria de hermandad.", "", ChatColor.YELLOW + "► Click para abrir"));

        // Fila 4: Pruebas de Animaciones, Reino y Jefes
        gui.setItem(28, GUIBuilder.createGlowingItem(Material.FIRE_CHARGE, ChatColor.GOLD + "☄ Probar Animación de Meteorito", "", ChatColor.GRAY + "Dispara la caída cinemática diagonal con estela,", ChatColor.GRAY + "destello, mini-explosión y cráter aquí mismo.", "", ChatColor.YELLOW + "► Click para probar en vivo"));
        gui.setItem(29, GUIBuilder.createGlowingItem(Material.OBSIDIAN, ChatColor.DARK_PURPLE + "🌌 Reino: Arena de Grieta Abisal", "", ChatColor.GRAY + "Teletransporte a la arena en livingtools_realm.", "", ChatColor.YELLOW + "► Click para viajar"));
        gui.setItem(30, GUIBuilder.createGlowingItem(Material.GILDED_BLACKSTONE, ChatColor.RED + "👑 Reino: Gran Arena Génesis", "", ChatColor.GRAY + "Teletransporte al altar del Avatar del Génesis.", "", ChatColor.YELLOW + "► Click para viajar"));
        gui.setItem(31, GUIBuilder.createGlowingItem(Material.CRYING_OBSIDIAN, ChatColor.AQUA + "🔨 Reino: Templo de la Forja Rítmica", "", ChatColor.GRAY + "Teletransporte al templo de la forja.", "", ChatColor.YELLOW + "► Click para viajar"));
        gui.setItem(32, GUIBuilder.createGlowingItem(Material.DRAGON_EGG, ChatColor.LIGHT_PURPLE + "🐉 Invocar Mega-Boss Génesis", "", ChatColor.GRAY + "Invoca al Avatar de 3000 HP y 3 fases.", "", ChatColor.YELLOW + "► Click para invocar"));
        gui.setItem(33, GUIBuilder.createGlowingItem(Material.WITHER_SKELETON_SKULL, ChatColor.RED + "👑 Invocar Jefe The Smith", "", ChatColor.GRAY + "Invoca al jefe volcánico y sus esbirros.", "", ChatColor.YELLOW + "► Click para invocar"));
        gui.setItem(34, GUIBuilder.createGlowingItem(Material.DIAMOND_CHESTPLATE, ChatColor.GREEN + "🎁 Dar Kit de Pruebas Máximo", "", ChatColor.GRAY + "Entrega arma Lv.100, armadura completa,", ChatColor.GRAY + "gemas celestiales, runas y tarot.", "", ChatColor.YELLOW + "► Click para recibir kit"));

        // Fila 5: Nuevas Pruebas de Forja y Utilidades
        gui.setItem(37, GUIBuilder.createGlowingItem(Material.LODESTONE, ChatColor.AQUA + "✦ Dar Núcleo de Forja Rítmica", "", ChatColor.GRAY + "Recibe el ítem que construye la forja.", "", ChatColor.YELLOW + "► Click para recibir"));
        gui.setItem(38, GUIBuilder.createGlowingItem(Material.DAMAGED_ANVIL, ChatColor.DARK_RED + "💥 Dañar / Romper Herramienta en Mano", "", ChatColor.GRAY + "Pone la durabilidad al 1% o rota para probar", ChatColor.GRAY + "la reparación instantánea en la Forja.", "", ChatColor.YELLOW + "► Click para dañar"));
        gui.setItem(39, GUIBuilder.createGlowingItem(Material.MAGMA_CREAM, ChatColor.RED + "🔥 Sello: Furia Volcánica", "", ChatColor.GRAY + "Aplica el Sello Ígneo a la herramienta.", "", ChatColor.YELLOW + "► Click para aplicar"));
        gui.setItem(40, GUIBuilder.createGlowingItem(Material.BLUE_ICE, ChatColor.AQUA + "❄ Sello: Escarcha Glacial", "", ChatColor.GRAY + "Aplica el Sello Criogénico.", "", ChatColor.YELLOW + "► Click para aplicar"));
        gui.setItem(41, GUIBuilder.createGlowingItem(Material.LIGHTNING_ROD, ChatColor.YELLOW + "⚡ Sello: Trueno Celestial", "", ChatColor.GRAY + "Aplica el Sello de Relámpagos.", "", ChatColor.YELLOW + "► Click para aplicar"));
        gui.setItem(42, GUIBuilder.createGlowingItem(Material.ENDER_EYE, ChatColor.DARK_PURPLE + "🌑 Sello: Vórtice del Vacío", "", ChatColor.GRAY + "Aplica el Sello de Succión y Boss-Killer.", "", ChatColor.YELLOW + "► Click para aplicar"));

        // Fila 6: Controles inferiores
        gui.setItem(45, GUIBuilder.createItem(Material.BOOK, ChatColor.YELLOW + "📋 Auditoría de Recetas", "", ChatColor.GRAY + "Ejecuta /lt checkrecipes."));
        gui.setItem(49, GUIBuilder.createCloseButton());
        gui.setItem(53, GUIBuilder.createItem(Material.COMPASS, ChatColor.AQUA + "🧭 Dar Brújula de Almas", "", ChatColor.GRAY + "Recibe una brújula rastreadora."));

        player.openInventory(gui);
    }
}
