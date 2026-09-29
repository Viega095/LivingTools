package com.livingtools.gui;

import com.livingtools.gui.BestiaryGUI.BestiaryEntry;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * MobDetailGUI — Vista detallada de una criatura individual del Bestiario.
 * Muestra modelo central, estadísticas, habilidades, cuadrícula de drops interactiva y opciones de prueba.
 */
public class MobDetailGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_PURPLE + "✦ Detalle: " + ChatColor.GOLD;

    public static void open(Player player, BestiaryEntry entry, BestiaryGUI.BestiaryCategory returnCategory, int returnPage) {
        String title = TITLE_PREFIX + entry.getName();
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Borde decorativo
        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Slot 4: Cabecera Épica
        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.WRITABLE_BOOK,
                ChatColor.GOLD + "✦ " + entry.getName() + " ✦",
                "",
                ChatColor.GRAY + entry.getSubtitle(),
                "",
                ChatColor.YELLOW + "⭐ Dificultad: " + entry.getDifficultyStars(),
                ChatColor.AQUA + "🌍 Ubicación: " + ChatColor.WHITE + entry.getLocation()
        ));

        // Slot 13: Modelo / Icono Central del Mob
        gui.setItem(13, entry.createItem());

        // Slot 19: Estadísticas de Combate
        gui.setItem(19, GUIBuilder.createItem(
                Material.DIAMOND_SWORD,
                ChatColor.RED + "⚔ Atributos de Combate",
                "",
                ChatColor.RED + "❤ Salud Máxima: " + ChatColor.WHITE + (int) entry.getMaxHealth() + " HP",
                ChatColor.DARK_RED + "⚔ Daño de Golpe: " + ChatColor.WHITE + (int) entry.getAttackDamage() + " pts",
                ChatColor.GOLD + "🛡 Tipo: " + (entry.isBoss() ? ChatColor.LIGHT_PURPLE + "Jefe Supremo / Raid" : ChatColor.GRAY + "Esbirro / Invocación"),
                "",
                ChatColor.YELLOW + "✦ Requiere estrategia y armas vivientes de alto rango."
        ));

        // Slot 21: Patrones de Ataque & Habilidades
        gui.setItem(21, GUIBuilder.createItem(
                Material.BLAZE_POWDER,
                ChatColor.GOLD + "⚡ Habilidades Especiales",
                "",
                ChatColor.YELLOW + "• Ataques telegrafiados en el suelo",
                ChatColor.YELLOW + "• Invocación periódica de esbirros",
                ChatColor.YELLOW + "• Resistencia al retroceso e inmunidad elemental",
                "",
                ChatColor.GRAY + "Pasa el cursor sobre los drops para conocer sus usos."
        ));

        // Fila 4 (Slots 29, 30, 31, 32, 33): Drops y Recompensas
        int[] dropSlots = {29, 30, 31, 32, 33};
        List<String> drops = entry.getDropList();
        for (int i = 0; i < dropSlots.length; i++) {
            if (i < drops.size()) {
                String dropText = drops.get(i);
                Material dropMat = resolveDropIcon(dropText);
                gui.setItem(dropSlots[i], GUIBuilder.createGlowingItem(
                        dropMat,
                        ChatColor.GOLD + "✦ Botín " + (i + 1),
                        "",
                        dropText,
                        "",
                        ChatColor.AQUA + "Usado en forjas místicas y recetas de ascensión."
                ));
            } else {
                gui.setItem(dropSlots[i], GUIBuilder.createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
        }

        // Slot 25: Esbirros / Invocaciones asociadas
        if (!entry.getMinionList().isEmpty()) {
            List<String> minionLore = new ArrayList<>();
            minionLore.add(ChatColor.GRAY + "Criaturas que lo acompañan en batalla:");
            minionLore.add("");
            for (String m : entry.getMinionList()) {
                minionLore.add(ChatColor.LIGHT_PURPLE + "  ► " + m);
            }
            gui.setItem(25, GUIBuilder.createItem(Material.ZOMBIE_HEAD, ChatColor.LIGHT_PURPLE + "👾 Esbirros Subordinados", minionLore.toArray(new String[0])));
        }

        // Slot 45: Botón de Invocar para Pruebas (Admin)
        if (player.hasPermission("livingtools.admin")) {
            gui.setItem(45, GUIBuilder.createGlowingItem(
                    Material.NETHER_STAR,
                    ChatColor.GREEN + "⚡ [Admin] Probar / Invocar",
                    "",
                    ChatColor.GRAY + "Invoca a esta criatura en tu posición para",
                    ChatColor.GRAY + "probar su combate y animaciones.",
                    "",
                    ChatColor.YELLOW + "► Click para invocar"
            ));
        }

        // Slot 49: Volver al Bestiario
        gui.setItem(49, GUIBuilder.createItem(
                Material.BARRIER,
                ChatColor.RED + "« Volver al Bestiario",
                "",
                ChatColor.GRAY + "Regresar a la lista de criaturas."
        ));

        // Slot 53: Ver en Recetas
        gui.setItem(53, GUIBuilder.createItem(
                Material.CRAFTING_TABLE,
                ChatColor.AQUA + "📖 Ver Crafteos Relacionados",
                "",
                ChatColor.GRAY + "Abre la enciclopedia de recetas.",
                "",
                ChatColor.YELLOW + "► Click para abrir recetas"
        ));

        player.openInventory(gui);
    }

    private static Material resolveDropIcon(String dropText) {
        String lower = dropText.toLowerCase();
        if (lower.contains("magma")) return Material.MAGMA_CREAM;
        if (lower.contains("pluma")) return Material.FEATHER;
        if (lower.contains("ángel") || lower.contains("polvo")) return Material.SUGAR;
        if (lower.contains("titán") || lower.contains("obsidiana")) return Material.CRYING_OBSIDIAN;
        if (lower.contains("vacío") || lower.contains("escama")) return Material.SCUTE;
        if (lower.contains("bosque") || lower.contains("corazón")) return Material.OAK_SAPLING;
        if (lower.contains("arena") || lower.contains("colmillo")) return Material.SAND;
        if (lower.contains("abisal") || lower.contains("ancla")) return Material.PRISMARINE_CRYSTALS;
        if (lower.contains("génesis") || lower.contains("creación")) return Material.NETHER_STAR;
        if (lower.contains("corona")) return Material.GOLDEN_HELMET;
        if (lower.contains("gema")) return Material.EMERALD;
        if (lower.contains("diamante")) return Material.DIAMOND;
        if (lower.contains("netherite")) return Material.NETHERITE_INGOT;
        return Material.CHEST;
    }
}
