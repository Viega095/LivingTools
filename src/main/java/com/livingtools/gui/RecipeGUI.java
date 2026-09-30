package com.livingtools.gui;

import com.livingtools.gui.category.ArmorCategoryGUI;
import com.livingtools.gui.category.ArtifactsCategoryGUI;
import com.livingtools.gui.category.BossForgeRecipesGUI;
import com.livingtools.gui.category.BossRelicsCategoryGUI;
import com.livingtools.gui.category.MainCategoryGUI;
import com.livingtools.gui.category.ToolsCategoryGUI;
import com.livingtools.gui.category.WeaponsCategoryGUI;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.BossDropType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RecipeGUI — Vista interactiva y detallada de recetas con descripción completa
 * del origen de cada material (Recursos Vanilla, Drops de Jefes, Esbirros y Estaciones).
 */
public class RecipeGUI {

    public static final String TITLE_RECIPE = "Receta: ";

    public static void open(Player player) {
        MainCategoryGUI.open(player);
    }

    public static void openMainMenu(Player player) {
        MainCategoryGUI.open(player);
    }

    public static void openRecipeView(Player player, String recipeType, String context) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_RECIPE + recipeType + " (" + context + ")");

        ItemStack filler = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        ItemStack slotBg = pane(Material.GRAY_STAINED_GLASS_PANE);
        int[] gridSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        for (int slot : gridSlots) {
            gui.setItem(slot, slotBg);
        }

        String matPrefix = resolveMatPrefix(context);
        populateRecipe(gui, recipeType, context, matPrefix);

        // Estación requerida y consejos de crafteo
        String stationTitle = "Mesa de Crafteo / Ensamblaje";
        String stationDesc = "Craftea en Mesa de Ensamblaje (Mesa sobre Bloque de Hierro)";
        String cmdHint = "Usa /livingtool structure assembly";

        if (recipeType.equals("SocketExpander") || recipeType.equals("RepairKit") ||
            recipeType.equals("AngelWings") || recipeType.equals("SeraphimHalo") || recipeType.equals("TitanRune")) {
            stationTitle = "Forja de Jefes (Estructura en Cruz)";
            stationDesc = "Plataforma 5x3 de Blackstone + Magma + Mesa de Herrería";
            cmdHint = "Usa /livingtool structure bossforge";
        }

        gui.setItem(4, GUIBuilder.createInfoItem(
                stationTitle,
                stationDesc,
                "Pasa el cursor sobre los ingredientes para ver su origen.",
                cmdHint));

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "« Volver a Categorías");
            List<String> bLore = new ArrayList<>();
            bLore.add(ChatColor.GRAY + "Clic para regresar al menú anterior.");
            backMeta.setLore(bLore);
            back.setItemMeta(backMeta);
        }
        gui.setItem(40, back);

        player.openInventory(gui);
    }

    public static void handleRecipeViewClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.BARRIER) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE_RECIPE)) {
            return;
        }

        if (title.contains("SWORD")) {
            WeaponsCategoryGUI.open(player);
        } else if (title.contains("PICKAXE") || title.contains("AXE")
                || title.contains("SHOVEL") || title.contains("HOE")) {
            ToolsCategoryGUI.open(player);
        } else if (title.contains("HELMET") || title.contains("CHESTPLATE")
                || title.contains("LEGGINGS") || title.contains("BOOTS")) {
            ArmorCategoryGUI.open(player);
        } else if (title.contains("ARTIFACT") || title.contains("Charm") || title.contains("SoulGem") || title.contains("RunePouch") || title.contains("Geode")) {
            ArtifactsCategoryGUI.open(player);
        } else if (title.contains("SocketExpander") || title.contains("RepairKit") || title.contains("AngelWings") || title.contains("SeraphimHalo") || title.contains("TitanRune")) {
            BossForgeRecipesGUI.open(player);
        } else if (title.contains("Relic") || title.contains("Staff") || title.contains("Fang") || title.contains("Anchor")) {
            BossRelicsCategoryGUI.open(player);
        } else {
            MainCategoryGUI.open(player);
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private static void populateRecipe(Inventory gui, String recipeType, String context, String matPrefix) {
        switch (recipeType) {
            case "Basic":
                gui.setItem(12, ingredient(Material.WHEAT_SEEDS, "Semillas de Trigo",
                        "Recurso común de pasto y agricultura.", "✦ Origen: Pasto / Cultivos del Mundo"));
                gui.setItem(21, baseIngredient(context, matPrefix, "WOODEN", "Madera común"));
                setResult(gui, baseItem(context, matPrefix, "WOODEN"), ChatColor.GREEN + "Herramienta Viviente de Madera",
                        "Despierta con personalidad al craftearse.");
                break;
            case "Stone":
                gui.setItem(12, ingredient(Material.COAL, "Carbón",
                        "Combustible y mineral fósil común.", "✦ Origen: Minería superficial"));
                gui.setItem(21, baseIngredient(context, matPrefix, "STONE", "Piedra cocinada"));
                setResult(gui, baseItem(context, matPrefix, "STONE"), ChatColor.GRAY + "Herramienta Viviente de Piedra",
                        "Evoluciona y gana experiencia al usarse.");
                break;
            case "Iron":
            case "IronArmor":
                gui.setItem(12, ingredient(Material.IRON_BLOCK, "Bloque de Hierro",
                        "9 lingotes de hierro comprimidos.", "✦ Origen: Fundición de mineral de hierro"));
                gui.setItem(21, resolveCenterIngredient(context, matPrefix, "IRON", "Equipo de Hierro Vanilla"));
                setResult(gui, resolveCenterItem(context, matPrefix, "IRON"), ChatColor.WHITE + "Equipo Viviente de Hierro",
                        "Otorga afinidad y progreso constante.");
                break;
            case "Gold":
            case "GoldArmor":
                gui.setItem(12, ingredient(Material.GOLD_BLOCK, "Bloque de Oro",
                        "9 lingotes de oro puro.", "✦ Origen: Minería de oro en Nether / Badlands"));
                gui.setItem(21, resolveCenterIngredient(context, matPrefix, "GOLDEN", "Equipo de Oro Vanilla"));
                setResult(gui, resolveCenterItem(context, matPrefix, "GOLDEN"), ChatColor.GOLD + "Equipo Viviente de Oro",
                        "Mayor velocidad de ataque y afinidad mágica.");
                break;
            case "Advanced":
            case "DiamondArmor":
                ItemStack feather = bossIngredient(BossDropType.SERAPHIM_FEATHER);
                ItemStack shard = bossIngredient(BossDropType.TITAN_SHARD);
                gui.setItem(11, feather);
                gui.setItem(21, resolveCenterIngredient(context, matPrefix, "DIAMOND", "Equipo de Diamante Vanilla"));
                gui.setItem(29, feather);
                gui.setItem(20, shard);
                gui.setItem(22, shard);
                setResult(gui, resolveCenterItem(context, matPrefix, "DIAMOND"), ChatColor.AQUA + "Equipo Viviente de Diamante",
                        "Desbloquea habilidades activas y ranuras de runas.");
                break;
            case "Netherite":
            case "NetheriteArmor":
                ItemStack magma = bossIngredient(BossDropType.MAGMA_CORE);
                ItemStack scute = bossIngredient(BossDropType.VOID_SCALE);
                gui.setItem(11, magma);
                gui.setItem(21, resolveCenterIngredient(context, matPrefix, "NETHERITE", "Equipo de Netherite"));
                gui.setItem(29, magma);
                gui.setItem(20, scute);
                gui.setItem(22, scute);
                setResult(gui, resolveCenterItem(context, matPrefix, "NETHERITE"), ChatColor.DARK_PURPLE + "Equipo Viviente de Netherite",
                        "Poder supremo indestructible y resistente al fuego.");
                break;
            case "Sleeping":
                ItemStack dust = bossIngredient(BossDropType.ANGEL_DUST);
                ItemStack star = bossIngredient(BossDropType.STARLIGHT_ESSENCE);
                gui.setItem(11, dust);
                gui.setItem(21, baseIngredient(context, matPrefix, "DIAMOND", "Herramienta de Diamante"));
                gui.setItem(29, dust);
                gui.setItem(20, star);
                gui.setItem(22, star);
                setResult(gui, baseItem(context, matPrefix, "DIAMOND"), ChatColor.DARK_GRAY + "Herramienta Dormida",
                        "Acumula poder pasivo mientras duermes.");
                break;
            case "Charm":
                gui.setItem(12, ingredient(Material.EMERALD, "Esmeralda", "Gema comercial.", "✦ Origen: Minería / Aldeanos"));
                gui.setItem(21, ingredient(Material.GOLD_INGOT, "Lingote de Oro", "Metal noble.", "✦ Origen: Minería / Nether"));
                gui.setItem(30, ingredient(Material.STRING, "Hilo", "Fibra flexible.", "✦ Origen: Arañas"));
                setResult(gui, new ItemStack(Material.EMERALD), ChatColor.GREEN + "Amuleto Viviente",
                        "Otorga bonificación de suerte y afinidad.");
                break;
            case "SoulGem":
                gui.setItem(12, ingredient(Material.AMETHYST_SHARD, "Fragmento de Amatista", "Cristal resonante.", "✦ Origen: Geodas subterráneas"));
                gui.setItem(21, ingredient(Material.SOUL_SAND, "Arena de Almas", "Tierra mística con almas atrapadas.", "✦ Origen: Valle de Almas (Nether)"));
                gui.setItem(30, ingredient(Material.GLASS, "Cristal", "Vidrio fundido.", "✦ Origen: Horno (Arena)"));
                setResult(gui, new ItemStack(Material.AMETHYST_SHARD), ChatColor.LIGHT_PURPLE + "Gema de Almas",
                        "Almacena almas y potencia habilidades.");
                break;
            case "RunePouch":
                gui.setItem(12, ingredient(Material.LEATHER, "Cuero", "Piel tratada.", "✦ Origen: Ganado / Caza"));
                gui.setItem(21, ingredient(Material.STRING, "Hilo", "Fibra de unión.", "✦ Origen: Arañas"));
                gui.setItem(30, ingredient(Material.GOLD_NUGGET, "Pepita de Oro", "Pequeño fragmento áureo.", "✦ Origen: Piglins / Oro"));
                setResult(gui, new ItemStack(Material.BUNDLE), ChatColor.GOLD + "Bolsa de Runas",
                        "Almacena hasta 16 runas elementales.");
                break;
            case "Geode":
                gui.setItem(12, ingredient(Material.AMETHYST_SHARD, "Fragmento de Amatista", "Cristal de geoda.", "✦ Origen: Geodas"));
                gui.setItem(20, ingredient(Material.GOLD_NUGGET, "Pepita de Oro", "Oro puro.", "✦ Origen: Minería"));
                gui.setItem(21, ingredient(Material.STONE, "Piedra", "Piedra lisa.", "✦ Origen: Horno"));
                gui.setItem(22, ingredient(Material.GOLD_NUGGET, "Pepita de Oro", "Oro puro.", "✦ Origen: Minería"));
                gui.setItem(30, ingredient(Material.AMETHYST_SHARD, "Fragmento de Amatista", "Cristal de geoda.", "✦ Origen: Geodas"));
                setResult(gui, new ItemStack(Material.AMETHYST_CLUSTER), ChatColor.LIGHT_PURPLE + "Geoda Rúnica",
                        "Rómpela para extraer runas elementales aleatorias.");
                break;
            case "SocketExpander":
                showForgeCrossRecipe(gui, Material.CONDUIT, ChatColor.AQUA + "Expansor de Zócalos");
                break;
            case "RepairKit":
                ItemStack magmaK = bossIngredient(BossDropType.MAGMA_CORE);
                gui.setItem(12, magmaK);
                gui.setItem(21, ingredient(Material.DIAMOND_BLOCK, "Bloque de Diamante", "9 diamantes.", "✦ Origen: Minería profunda"));
                gui.setItem(30, magmaK);
                gui.setItem(20, magmaK);
                gui.setItem(22, magmaK);
                setResult(gui, new ItemStack(Material.ANVIL), ChatColor.RED + "Kit de Reparación Viva",
                        "Restaura el 50% de durabilidad instantáneamente.");
                break;
            case "AngelWings":
                showForgeCrossRecipe(gui, Material.ELYTRA, ChatColor.WHITE + "Alas de Ángel");
                break;
            case "SeraphimHalo":
                showForgeCrossRecipe(gui, Material.GOLDEN_HELMET, ChatColor.GOLD + "Halo de Serafín");
                break;
            case "TitanRune":
                showForgeCrossRecipe(gui, Material.NETHERITE_CHESTPLATE, ChatColor.DARK_PURPLE + "Runa del Titán");
                break;
            case "SoulForgeCore":
                gui.setItem(12, ingredient(Material.CRYING_OBSIDIAN, "Obsidiana Llorosa", "Roca con lágrimas arcanas.", "✦ Origen: Nether / Portales en ruinas"));
                gui.setItem(20, ingredient(Material.POLISHED_BLACKSTONE_BRICKS, "Ladrillos de Blackstone", "Piedra negra pulida.", "✦ Origen: Nether / Crafteo"));
                gui.setItem(21, ingredient(Material.SOUL_CAMPFIRE, "Fogata de Almas", "Fuego espiritual eterno.", "✦ Origen: Crafteo con Soul Soil"));
                gui.setItem(22, ingredient(Material.POLISHED_BLACKSTONE_BRICKS, "Ladrillos de Blackstone", "Piedra negra pulida.", "✦ Origen: Nether / Crafteo"));
                gui.setItem(30, ingredient(Material.CRYING_OBSIDIAN, "Obsidiana Llorosa", "Roca con lágrimas arcanas.", "✦ Origen: Nether / Portales en ruinas"));
                setResult(gui, com.livingtools.manager.RhythmicForgeManager.createSoulForgeCore(),
                        ChatColor.AQUA + "✦ Núcleo de la Forja Rítmica ✦",
                        "Construye la Forja Rítmica en el mundo para reparar y templar tus armas.");
                break;
            case "DryadRelic":
                ItemStack heart = bossIngredient(BossDropType.DRYAD_HEARTWOOD);
                gui.setItem(12, ingredient(Material.EMERALD, "Esmeralda", "Gema pura.", "✦ Origen: Aldeanos / Minería"));
                gui.setItem(20, heart);
                gui.setItem(21, heart);
                gui.setItem(22, heart);
                gui.setItem(30, ingredient(Material.EMERALD, "Esmeralda", "Gema pura.", "✦ Origen: Aldeanos / Minería"));
                setResult(gui, new ItemStack(Material.EMERALD), ChatColor.DARK_GREEN + "Relicario del Bosque",
                        "Otorga regeneración en biomas forestales.");
                break;
            case "WyrmRelic":
                ItemStack wCore = bossIngredient(BossDropType.WYRM_CORE);
                gui.setItem(12, ingredient(Material.SAND, "Arena", "Arena del desierto.", "✦ Origen: Desiertos"));
                gui.setItem(20, ingredient(Material.GOLD_INGOT, "Lingote de Oro", "Oro.", "✦ Origen: Fundición"));
                gui.setItem(21, wCore);
                gui.setItem(22, ingredient(Material.GOLD_INGOT, "Lingote de Oro", "Oro.", "✦ Origen: Fundición"));
                gui.setItem(30, ingredient(Material.SAND, "Arena", "Arena del desierto.", "✦ Origen: Desiertos"));
                setResult(gui, new ItemStack(Material.GOLD_INGOT), ChatColor.GOLD + "Sello del Desierto",
                        "Otorga resistencia al fuego en desiertos.");
                break;
            case "LeviathanRelic":
                ItemStack lCore = bossIngredient(BossDropType.LEVIATHAN_CORE);
                gui.setItem(12, ingredient(Material.PRISMARINE_SHARD, "Fragmento de Prismarina", "Restos marinos.", "✦ Origen: Monumentos Marinos"));
                gui.setItem(20, ingredient(Material.PRISMARINE_CRYSTALS, "Cristal de Prismarina", "Luz marina.", "✦ Origen: Guardianes del Océano"));
                gui.setItem(21, lCore);
                gui.setItem(22, ingredient(Material.PRISMARINE_CRYSTALS, "Cristal de Prismarina", "Luz marina.", "✦ Origen: Guardianes del Océano"));
                gui.setItem(30, ingredient(Material.PRISMARINE_SHARD, "Fragmento de Prismarina", "Restos marinos.", "✦ Origen: Monumentos Marinos"));
                setResult(gui, new ItemStack(Material.PRISMARINE_CRYSTALS), ChatColor.AQUA + "Núcleo Abisal Forjado",
                        "Otorga gracia del delfín y respiración acuática.");
                break;
            case "DryadStaff":
                ItemStack dHeart = bossIngredient(BossDropType.DRYAD_HEARTWOOD);
                gui.setItem(12, dHeart);
                gui.setItem(20, dHeart);
                gui.setItem(21, ingredient(Material.STICK, "Palo", "Madera común.", "✦ Origen: Crafteo de madera"));
                gui.setItem(22, dHeart);
                gui.setItem(30, ingredient(Material.EMERALD, "Esmeralda", "Gema.", "✦ Origen: Minería"));
                setResult(gui, new ItemStack(Material.STICK), ChatColor.DARK_GREEN + "Bastón del Bosque",
                        "Arma temática de la Dríada Corrupta.");
                break;
            case "SandFang":
                ItemStack sCore = bossIngredient(BossDropType.WYRM_CORE);
                gui.setItem(12, sCore);
                gui.setItem(20, ingredient(Material.GOLD_INGOT, "Lingote de Oro", "Oro puro.", "✦ Origen: Fundición"));
                gui.setItem(21, ingredient(Material.GOLDEN_SWORD, "Espada de Oro", "Base de la hoja.", "✦ Origen: Mesa de Crafteo"));
                gui.setItem(22, ingredient(Material.GOLD_INGOT, "Lingote de Oro", "Oro puro.", "✦ Origen: Fundición"));
                gui.setItem(30, sCore);
                setResult(gui, new ItemStack(Material.GOLDEN_SWORD), ChatColor.GOLD + "Colmillo del Desierto",
                        "Espada rúnica imbuida con la furia del Wyrm.");
                break;
            case "AbyssAnchor":
                ItemStack aCore = bossIngredient(BossDropType.LEVIATHAN_CORE);
                gui.setItem(12, aCore);
                gui.setItem(20, ingredient(Material.PRISMARINE_CRYSTALS, "Cristal de Prismarina", "Luz marina.", "✦ Origen: Guardianes"));
                gui.setItem(21, ingredient(Material.TRIDENT, "Tridente", "Arma de las profundidades.", "✦ Origen: Ahogados (Drowned)"));
                gui.setItem(22, ingredient(Material.PRISMARINE_CRYSTALS, "Cristal de Prismarina", "Luz marina.", "✦ Origen: Guardianes"));
                gui.setItem(30, aCore);
                setResult(gui, new ItemStack(Material.TRIDENT), ChatColor.AQUA + "Ancla Abisal",
                        "Tridente imbuido con el poder del Leviatán.");
                break;
            case "VerdantAscension":
                gui.setItem(12, ingredient(Material.EMERALD, "Relicario del Bosque", "Reliquia forjada.", "✦ Origen: Mesa de Ensamblaje"));
                gui.setItem(21, ingredient(Material.STICK, "Bastón del Bosque", "Arma de la Dríada.", "✦ Origen: Mesa de Ensamblaje"));
                gui.setItem(30, ingredient(Material.EMERALD_BLOCK, "Bloque de Esmeralda", "Poder vegetal.", "✦ Origen: 9 Esmeraldas"));
                setResult(gui, new ItemStack(Material.STICK), ChatColor.DARK_GREEN + "Bastón Ancestral del Bosque",
                        "Arma legendaria definitiva con regeneración y fuerza.");
                break;
            case "SandstormCrown":
                gui.setItem(12, ingredient(Material.GOLD_INGOT, "Sello del Desierto", "Reliquia forjada.", "✦ Origen: Mesa de Ensamblaje"));
                gui.setItem(21, ingredient(Material.GOLDEN_SWORD, "Colmillo del Desierto", "Espada del Wyrm.", "✦ Origen: Mesa de Ensamblaje"));
                gui.setItem(30, ingredient(Material.GOLD_BLOCK, "Bloque de Oro", "Oro concentrado.", "✦ Origen: 9 Lingotes de Oro"));
                setResult(gui, new ItemStack(Material.GOLDEN_SWORD), ChatColor.GOLD + "Colmillo Real del Wyrm",
                        "Espada legendaria definitiva con fuego y empuje del desierto.");
                break;
            case "TidalDominion":
                gui.setItem(12, ingredient(Material.PRISMARINE_CRYSTALS, "Núcleo Abisal Forjado", "Reliquia forjada.", "✦ Origen: Mesa de Ensamblaje"));
                gui.setItem(21, ingredient(Material.TRIDENT, "Ancla Abisal", "Tridente del Leviatán.", "✦ Origen: Mesa de Ensamblaje"));
                gui.setItem(30, ingredient(Material.PRISMARINE_BRICKS, "Ladrillos de Prismarina", "Piedra de monumento.", "✦ Origen: Monumento Marino"));
                setResult(gui, new ItemStack(Material.TRIDENT), ChatColor.AQUA + "Ancla del Abismo",
                        "Tridente legendario definitivo con dominio del océano.");
                break;
            default:
                break;
        }
    }

    private static void showForgeCrossRecipe(Inventory gui, Material resultMat, String resultName) {
        if (resultMat == Material.CONDUIT) {
            gui.setItem(12, bossIngredient(BossDropType.SERAPHIM_FEATHER));
            gui.setItem(20, bossIngredient(BossDropType.MAGMA_CORE));
            gui.setItem(21, ingredient(Material.DIAMOND, "Diamante", "Gema preciosa.", "✦ Origen: Minería profunda"));
            gui.setItem(22, bossIngredient(BossDropType.TITAN_SHARD));
            gui.setItem(30, bossIngredient(BossDropType.VOID_SCALE));
            setResult(gui, new ItemStack(Material.CONDUIT), resultName, "Añade 1 ranura adicional de runas a tu herramienta.");
        } else if (resultMat == Material.ELYTRA) {
            gui.setItem(12, bossIngredient(BossDropType.SERAPHIM_FEATHER));
            gui.setItem(20, bossIngredient(BossDropType.ANGEL_DUST));
            gui.setItem(21, ingredient(Material.ELYTRA, "Élitros Vanilla", "Alas del End.", "✦ Origen: Barco de End City"));
            gui.setItem(22, bossIngredient(BossDropType.ANGEL_DUST));
            gui.setItem(30, bossIngredient(BossDropType.SERAPHIM_FEATHER));
            setResult(gui, new ItemStack(Material.ELYTRA), resultName, "Alas indestructibles que dejan un rastro celestial.");
        } else if (resultMat == Material.GOLDEN_HELMET) {
            gui.setItem(12, bossIngredient(BossDropType.SERAPHIM_FEATHER));
            gui.setItem(20, ingredient(Material.GOLDEN_HELMET, "Casco de Oro", "Casco dorado.", "✦ Origen: Crafteo Vanilla"));
            gui.setItem(21, bossIngredient(BossDropType.STARLIGHT_ESSENCE));
            gui.setItem(22, ingredient(Material.GOLDEN_HELMET, "Casco de Oro", "Casco dorado.", "✦ Origen: Crafteo Vanilla"));
            gui.setItem(30, bossIngredient(BossDropType.SERAPHIM_FEATHER));
            setResult(gui, new ItemStack(Material.GOLDEN_HELMET), resultName, "Otorga visión nocturna constante y caída lenta.");
        } else if (resultMat == Material.NETHERITE_CHESTPLATE) {
            gui.setItem(12, bossIngredient(BossDropType.VOID_SCALE));
            gui.setItem(20, bossIngredient(BossDropType.TITAN_SHARD));
            gui.setItem(21, ingredient(Material.PAPER, "Papel", "Pergamino arcano.", "✦ Origen: Caña de Azúcar"));
            gui.setItem(22, bossIngredient(BossDropType.TITAN_SHARD));
            gui.setItem(30, bossIngredient(BossDropType.VOID_SCALE));
            setResult(gui, new ItemStack(Material.NETHERITE_CHESTPLATE), resultName, "Otorga Fuerza II y Resistencia I permanente a tu armadura.");
        }
    }

    private static ItemStack ingredient(Material mat, String name, String desc, String origin) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + desc);
            lore.add("");
            lore.add(ChatColor.AQUA + origin);
            lore.add(ChatColor.DARK_GRAY + "Material de Crafteo");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack bossIngredient(BossDropType drop) {
        ItemStack item = drop.create();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GOLD + "✦ Drop Especial:");
            lore.add(ChatColor.WHITE + "  " + drop.getBossSource());
            lore.add(ChatColor.WHITE + "  " + drop.getMinionSource());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack baseIngredient(String context, String matPrefix, String tier, String origin) {
        ItemStack base = baseItem(context, matPrefix, tier);
        ItemMeta meta = base.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "Herramienta Base (" + tier + ")");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Crafteo tradicional en mesa de trabajo.");
            lore.add(ChatColor.AQUA + "✦ Origen: " + origin);
            meta.setLore(lore);
            base.setItemMeta(meta);
        }
        return base;
    }

    private static ItemStack resolveCenterIngredient(String context, String matPrefix, String tier, String origin) {
        ItemStack item = resolveCenterItem(context, matPrefix, tier);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "Equipo Base (" + tier + ")");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Pieza base necesaria para la infusión viva.");
            lore.add(ChatColor.AQUA + "✦ Origen: " + origin);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String resolveMatPrefix(String context) {
        if ("SWORD".equals(context)) return "_SWORD";
        if ("PICKAXE".equals(context)) return "_PICKAXE";
        if ("AXE".equals(context)) return "_AXE";
        if ("SHOVEL".equals(context)) return "_SHOVEL";
        if ("HOE".equals(context)) return "_HOE";
        return "";
    }

    private static ItemStack baseItem(String context, String matPrefix, String tier) {
        if (matPrefix.isEmpty()) return new ItemStack(Material.STONE);
        try {
            return new ItemStack(Material.valueOf(tier + matPrefix));
        } catch (Exception e) {
            return new ItemStack(Material.STONE_PICKAXE);
        }
    }

    private static ItemStack resolveCenterItem(String context, String matPrefix, String tier) {
        if (context.equals("HELMET") || context.equals("CHESTPLATE")
                || context.equals("LEGGINGS") || context.equals("BOOTS")) {
            try {
                return new ItemStack(Material.valueOf(tier + "_" + context));
            } catch (Exception e) {
                return new ItemStack(Material.IRON_CHESTPLATE);
            }
        }
        return baseItem(context, matPrefix, tier);
    }

    private static ItemStack pane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void setResult(Inventory gui, ItemStack template, String name, String benefit) {
        ItemStack result = template.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + benefit);
            lore.add("");
            lore.add(ChatColor.GREEN + "✔ Resultado del crafteo");
            meta.setLore(lore);
            result.setItemMeta(meta);
        }
        gui.setItem(24, result);
    }
}
