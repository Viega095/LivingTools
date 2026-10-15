package com.livingtools.gui;

import com.livingtools.gui.BestiaryGUI.BestiaryDrop;
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
 * MobDetailGUI — Vista detallada de 54 slots de una criatura individual del Bestiario.
 * Muestra clasificación (LivingTools / Vanilla), Libro de Invocación/Origen, Esbirros subordinados,
 * cuadrícula interactiva de Drops con porcentajes explícitos y botón de Contrato de Cacería.
 */
public class MobDetailGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_PURPLE + "✦ Detalle: " + ChatColor.GOLD;

    public static void open(Player player, BestiaryEntry entry, BestiaryGUI.BestiaryCategory returnCategory, int returnPage) {
        String title = TITLE_PREFIX + entry.getName();
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Borde decorativo temático
        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Slot 4: Cabecera Épica
        gui.setItem(4, GUIBuilder.createGlowingItem(
                Material.WRITABLE_BOOK,
                ChatColor.GOLD + "✦ " + entry.getName() + " ✦",
                "",
                ChatColor.GRAY + entry.getSubtitle(),
                "",
                ChatColor.DARK_AQUA + "🏷 " + ChatColor.BOLD + entry.getTierName(),
                ChatColor.YELLOW + (entry.isCustomPluginMob() ? "✨ Criatura Exclusiva de LivingTools" : "🌿 Vanilla Potenciado con Habilidades Vivas"),
                ChatColor.YELLOW + "⭐ Dificultad: " + entry.getDifficultyStars(),
                ChatColor.AQUA + "🌍 Origen / Entorno: " + ChatColor.WHITE + entry.getLocation()
        ));

        // Slot 13: Modelo / Icono Central de la Criatura
        gui.setItem(13, entry.createItem());

        // Slot 19: Estadísticas de Combate
        gui.setItem(19, GUIBuilder.createItem(
                Material.DIAMOND_SWORD,
                ChatColor.RED + "⚔ Atributos de Combate",
                "",
                ChatColor.RED + "❤ Salud Máxima: " + ChatColor.WHITE + (int) entry.getMaxHealth() + " HP",
                ChatColor.DARK_RED + "⚔ Daño de Golpe: " + ChatColor.WHITE + (int) entry.getAttackDamage() + " pts",
                ChatColor.GOLD + "🛡 Clasificación: " + (entry.isBoss() ? ChatColor.LIGHT_PURPLE + "Jefe Supremo / Raid" : ChatColor.GRAY + "Élite / Invocación / Esbirro"),
                "",
                ChatColor.YELLOW + "✦ Requiere estrategia, movilidad y armas vivientes de alto rango."
        ));

        // Slot 21: Patrones de Ataque & Comportamiento de IA
        gui.setItem(21, GUIBuilder.createItem(
                Material.BLAZE_POWDER,
                ChatColor.GOLD + "⚡ Tácticas & Comportamiento de IA",
                "",
                ChatColor.YELLOW + "• IA Inteligente Adaptativa en combate",
                ChatColor.YELLOW + "• Inmunidad parcial al retroceso y al fuego",
                ChatColor.YELLOW + "• Habilidades especiales reactivas a la distancia",
                "",
                ChatColor.GRAY + "Pasa el cursor sobre la cuadrícula inferior para examinar el botín."
        ));

        // Slot 23: Libro de Invocación / Método de Obtención
        List<String> summonLore = new ArrayList<>();
        summonLore.add(ChatColor.GRAY + "Origen: " + ChatColor.YELLOW + entry.getOriginType());
        summonLore.add("");
        if (!entry.getSummonGuide().isEmpty()) {
            for (String line : entry.getSummonGuide()) {
                summonLore.add(ChatColor.WHITE + line);
            }
        } else {
            summonLore.add(ChatColor.WHITE + "Se manifiesta naturalmente en el mundo o en eventos sangrientos.");
        }
        summonLore.add("");
        summonLore.add(ChatColor.GOLD + "✦ Consulta este códice antes de partir en expedición.");
        gui.setItem(23, GUIBuilder.createGlowingItem(
                Material.BOOK,
                ChatColor.AQUA + "📖 Códice de Invocación & Origen",
                summonLore.toArray(new String[0])
        ));

        // Slot 25: Esbirros / Subordinados
        if (!entry.getMinionList().isEmpty()) {
            List<String> minionLore = new ArrayList<>();
            minionLore.add(ChatColor.GRAY + "Subordinados que acompañan o protegen a esta criatura:");
            minionLore.add("");
            for (String m : entry.getMinionList()) {
                minionLore.add(ChatColor.LIGHT_PURPLE + "  ► " + m);
            }
            minionLore.add("");
            minionLore.add(ChatColor.RED + "⚠ Elimina primero a los subordinados para quebrar su escudo.");
            gui.setItem(25, GUIBuilder.createItem(
                    Material.ZOMBIE_HEAD,
                    ChatColor.LIGHT_PURPLE + "👾 Esbirros Subordinados (" + entry.getMinionList().size() + ")",
                    minionLore.toArray(new String[0])
            ));
        } else {
            gui.setItem(25, GUIBuilder.createItem(
                    Material.IRON_HELMET,
                    ChatColor.GRAY + "👾 Sin Esbirros Subordinados",
                    "",
                    ChatColor.GRAY + "Esta criatura combate en solitario sin invocar esbirros."
            ));
        }

        // Fila 4 (Slots 29, 30, 31, 32, 33): Cuadrícula de Drops Interactiva con Probabilidades Explícitas
        int[] dropSlots = {29, 30, 31, 32, 33};
        List<BestiaryDrop> drops = entry.getDrops();

        for (int i = 0; i < dropSlots.length; i++) {
            if (i < drops.size()) {
                BestiaryDrop drop = drops.get(i);
                gui.setItem(dropSlots[i], GUIBuilder.createGlowingItem(
                        drop.getIcon(),
                        ChatColor.GOLD + "✦ " + drop.getName() + " " + ChatColor.GREEN + "[" + drop.getChancePercent() + "%]",
                        "",
                        ChatColor.AQUA + "Probabilidad de Caída: " + ChatColor.WHITE + drop.getChancePercent() + "%",
                        ChatColor.GRAY + "Uso en el Plugin:",
                        ChatColor.YELLOW + drop.getUsage(),
                        "",
                        ChatColor.GRAY + "Presiona para ver recetas relacionadas."
                ));
            } else if (i < entry.getDropList().size()) {
                String dropText = entry.getDropList().get(i);
                Material dropMat = resolveDropIcon(dropText);
                gui.setItem(dropSlots[i], GUIBuilder.createGlowingItem(
                        dropMat,
                        ChatColor.GOLD + "✦ " + dropText,
                        "",
                        ChatColor.GRAY + "Botín garantizado o por probabilidad.",
                        ChatColor.AQUA + "Usado en forjas místicas y recetas vivientes."
                ));
            } else {
                gui.setItem(dropSlots[i], GUIBuilder.createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
        }

        // Slot 45: Botón de Invocar para Pruebas (Admin)
        if (player.hasPermission("livingtools.admin")) {
            gui.setItem(45, GUIBuilder.createGlowingItem(
                    Material.NETHER_STAR,
                    ChatColor.GREEN + "⚡ [Admin] Invocar Criatura de Prueba",
                    "",
                    ChatColor.GRAY + "Invoca a esta entidad en tu ubicación",
                    ChatColor.GRAY + "para probar mecánicas y drops en vivo.",
                    "",
                    ChatColor.YELLOW + "► Click para invocar"
            ));
        }

        // Slot 47: Contrato de Cacería Directo
        gui.setItem(47, GUIBuilder.createGlowingItem(
                Material.WRITTEN_BOOK,
                ChatColor.RED + "📜 Iniciar Contrato de Cacería",
                "",
                ChatColor.GRAY + "Firma un contrato sagrado para cazar a:",
                ChatColor.YELLOW + "• " + entry.getName(),
                "",
                ChatColor.AQUA + "Recompensa al abatir:",
                ChatColor.WHITE + "  ► Fragmentos de Alma Viviente",
                ChatColor.WHITE + "  ► +500 XP para tu arma viviente",
                ChatColor.WHITE + "  ► Contribución a la Meta del Servidor",
                "",
                ChatColor.YELLOW + "► Click para aceptar contrato"
        ));

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
                ChatColor.AQUA + "📖 Ver Enciclopedia de Recetas",
                "",
                ChatColor.GRAY + "Abre la guía de crafteos y forjas.",
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
        if (lower.contains("hueso")) return Material.BONE;
        if (lower.contains("arco")) return Material.BOW;
        if (lower.contains("carne")) return Material.ROTTEN_FLESH;
        if (lower.contains("furia")) return Material.REDSTONE_BLOCK;
        if (lower.contains("telaraña")) return Material.COBWEB;
        if (lower.contains("ojo") || lower.contains("veneno")) return Material.SPIDER_EYE;
        if (lower.contains("pólvora")) return Material.GUNPOWDER;
        if (lower.contains("sísmic") || lower.contains("carga")) return Material.FIRE_CHARGE;
        if (lower.contains("choque") || lower.contains("sombra")) return Material.ECHO_SHARD;
        return Material.CHEST;
    }
}
