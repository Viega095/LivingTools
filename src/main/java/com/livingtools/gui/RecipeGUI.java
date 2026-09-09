package com.livingtools.gui;

import com.livingtools.gui.category.ArmorCategoryGUI;
import com.livingtools.gui.category.ArtifactsCategoryGUI;
import com.livingtools.gui.category.BossForgeRecipesGUI;
import com.livingtools.gui.category.BossRelicsCategoryGUI;
import com.livingtools.gui.category.MainCategoryGUI;
import com.livingtools.gui.category.ToolsCategoryGUI;
import com.livingtools.gui.category.WeaponsCategoryGUI;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Vistas de receta (solo lectura). La navegación por categorías vive en gui/category/.
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

        ItemStack slotBg = pane(Material.WHITE_STAINED_GLASS_PANE);
        int[] gridSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        for (int slot : gridSlots) {
            gui.setItem(slot, slotBg);
        }

        String matPrefix = resolveMatPrefix(context);
        populateRecipe(gui, recipeType, context, matPrefix);

        gui.setItem(4, GUIBuilder.createInfoItem(
                "Mesa de Ensamblaje",
                "Esta receta se craftea en la",
                "estructura 3x3 del mundo.",
                "Usa /livingtool structure assembly"));

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Volver");
        back.setItemMeta(backMeta);
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
        } else if (title.contains("ARTIFACT")) {
            ArtifactsCategoryGUI.open(player);
        } else if (title.contains("FORGE")) {
            BossForgeRecipesGUI.open(player);
        } else if (title.contains("BOSS")) {
            BossRelicsCategoryGUI.open(player);
        } else {
            MainCategoryGUI.open(player);
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private static void populateRecipe(Inventory gui, String recipeType, String context, String matPrefix) {
        switch (recipeType) {
            case "Basic":
                gui.setItem(12, new ItemStack(Material.WHEAT_SEEDS));
                gui.setItem(21, baseItem(context, matPrefix, "WOODEN"));
                setResult(gui, baseItem(context, matPrefix, "WOODEN"), ChatColor.GREEN + "Herramienta Viviente de Madera");
                break;
            case "Stone":
                gui.setItem(12, new ItemStack(Material.COAL));
                gui.setItem(21, baseItem(context, matPrefix, "STONE"));
                setResult(gui, baseItem(context, matPrefix, "STONE"), ChatColor.GRAY + "Herramienta Viviente de Piedra");
                break;
            case "Iron":
            case "IronArmor":
                gui.setItem(12, new ItemStack(Material.IRON_BLOCK));
                gui.setItem(21, resolveCenterItem(context, matPrefix, "IRON"));
                setResult(gui, resolveCenterItem(context, matPrefix, "IRON"), ChatColor.WHITE + "Equipo Viviente de Hierro");
                break;
            case "Gold":
            case "GoldArmor":
                gui.setItem(12, new ItemStack(Material.GOLD_BLOCK));
                gui.setItem(21, resolveCenterItem(context, matPrefix, "GOLDEN"));
                setResult(gui, resolveCenterItem(context, matPrefix, "GOLDEN"), ChatColor.GOLD + "Equipo Viviente de Oro");
                break;
            case "Advanced":
            case "DiamondArmor":
                gui.setItem(11, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
                gui.setItem(21, resolveCenterItem(context, matPrefix, "DIAMOND"));
                gui.setItem(29, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
                gui.setItem(20, named(Material.AMETHYST_SHARD, ChatColor.DARK_PURPLE + "Fragmento de Titán"));
                gui.setItem(22, named(Material.AMETHYST_SHARD, ChatColor.DARK_PURPLE + "Fragmento de Titán"));
                setResult(gui, resolveCenterItem(context, matPrefix, "DIAMOND"), ChatColor.AQUA + "Equipo Viviente de Diamante");
                break;
            case "Netherite":
            case "NetheriteArmor":
                gui.setItem(11, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
                gui.setItem(21, resolveCenterItem(context, matPrefix, "NETHERITE"));
                gui.setItem(29, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
                gui.setItem(20, named(Material.SCUTE, ChatColor.DARK_GRAY + "Escama del Vacío"));
                gui.setItem(22, named(Material.SCUTE, ChatColor.DARK_GRAY + "Escama del Vacío"));
                setResult(gui, resolveCenterItem(context, matPrefix, "NETHERITE"), ChatColor.DARK_PURPLE + "Equipo Viviente de Netherite");
                break;
            case "Sleeping":
                gui.setItem(11, named(Material.SUGAR, ChatColor.WHITE + "Polvo de Ángel"));
                gui.setItem(21, baseItem(context, matPrefix, "DIAMOND"));
                gui.setItem(29, named(Material.SUGAR, ChatColor.WHITE + "Polvo de Ángel"));
                gui.setItem(20, named(Material.NETHER_STAR, ChatColor.YELLOW + "Esencia Estelar"));
                gui.setItem(22, named(Material.NETHER_STAR, ChatColor.YELLOW + "Esencia Estelar"));
                setResult(gui, baseItem(context, matPrefix, "DIAMOND"), ChatColor.DARK_GRAY + "Herramienta Dormida");
                break;
            case "Charm":
                gui.setItem(12, new ItemStack(Material.EMERALD));
                gui.setItem(21, new ItemStack(Material.GOLD_INGOT));
                gui.setItem(30, new ItemStack(Material.STRING));
                setResult(gui, Material.EMERALD, ChatColor.GREEN + "Amuleto Viviente");
                break;
            case "SoulGem":
                gui.setItem(12, new ItemStack(Material.AMETHYST_SHARD));
                gui.setItem(21, new ItemStack(Material.SOUL_SAND));
                gui.setItem(30, new ItemStack(Material.GLASS));
                setResult(gui, Material.AMETHYST_SHARD, ChatColor.LIGHT_PURPLE + "Gema de Almas");
                break;
            case "RunePouch":
                gui.setItem(12, new ItemStack(Material.LEATHER));
                gui.setItem(21, new ItemStack(Material.STRING));
                gui.setItem(30, new ItemStack(Material.GOLD_NUGGET));
                setResult(gui, Material.BUNDLE, ChatColor.GOLD + "Bolsa de Runas");
                break;
            case "Geode":
                gui.setItem(12, new ItemStack(Material.AMETHYST_SHARD));
                gui.setItem(20, new ItemStack(Material.GOLD_NUGGET));
                gui.setItem(21, new ItemStack(Material.STONE));
                gui.setItem(22, new ItemStack(Material.GOLD_NUGGET));
                gui.setItem(30, new ItemStack(Material.AMETHYST_SHARD));
                setResult(gui, Material.AMETHYST_CLUSTER, ChatColor.LIGHT_PURPLE + "Geoda Rúnica");
                break;
            case "SocketExpander":
                showForgeCrossRecipe(gui, Material.CONDUIT, ChatColor.AQUA + "Expansor de Zócalos");
                break;
            case "RepairKit":
                gui.setItem(12, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
                gui.setItem(21, new ItemStack(Material.DIAMOND_BLOCK));
                gui.setItem(30, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
                gui.setItem(20, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
                gui.setItem(22, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
                setResult(gui, Material.ANVIL, ChatColor.RED + "Kit de Reparación Viva");
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
            case "DryadRelic":
                gui.setItem(12, new ItemStack(Material.EMERALD));
                gui.setItem(20, named(Material.OAK_SAPLING, ChatColor.DARK_GREEN + "Corazón del Bosque"));
                gui.setItem(21, named(Material.OAK_SAPLING, ChatColor.DARK_GREEN + "Corazón del Bosque"));
                gui.setItem(22, named(Material.OAK_SAPLING, ChatColor.DARK_GREEN + "Corazón del Bosque"));
                gui.setItem(30, new ItemStack(Material.EMERALD));
                setResult(gui, Material.EMERALD, ChatColor.DARK_GREEN + "Relicario del Bosque");
                break;
            case "WyrmRelic":
                gui.setItem(12, new ItemStack(Material.SAND));
                gui.setItem(20, new ItemStack(Material.GOLD_INGOT));
                gui.setItem(21, named(Material.SAND, ChatColor.GOLD + "Núcleo de Arena"));
                gui.setItem(22, new ItemStack(Material.GOLD_INGOT));
                gui.setItem(30, new ItemStack(Material.SAND));
                setResult(gui, Material.GOLD_INGOT, ChatColor.GOLD + "Sello del Desierto");
                break;
            case "LeviathanRelic":
                gui.setItem(12, new ItemStack(Material.PRISMARINE_SHARD));
                gui.setItem(20, new ItemStack(Material.PRISMARINE_CRYSTALS));
                gui.setItem(21, named(Material.PRISMARINE_CRYSTALS, ChatColor.AQUA + "Núcleo Abisal"));
                gui.setItem(22, new ItemStack(Material.PRISMARINE_CRYSTALS));
                gui.setItem(30, new ItemStack(Material.PRISMARINE_SHARD));
                setResult(gui, Material.PRISMARINE_CRYSTALS, ChatColor.AQUA + "Núcleo Abisal Forjado");
                break;
            case "DryadStaff":
                gui.setItem(12, named(Material.OAK_SAPLING, ChatColor.DARK_GREEN + "Corazón del Bosque"));
                gui.setItem(20, named(Material.OAK_SAPLING, ChatColor.DARK_GREEN + "Corazón del Bosque"));
                gui.setItem(21, new ItemStack(Material.STICK));
                gui.setItem(22, named(Material.OAK_SAPLING, ChatColor.DARK_GREEN + "Corazón del Bosque"));
                gui.setItem(30, new ItemStack(Material.EMERALD));
                setResult(gui, Material.STICK, ChatColor.DARK_GREEN + "Bastón del Bosque");
                break;
            case "SandFang":
                gui.setItem(12, new ItemStack(Material.SAND));
                gui.setItem(20, new ItemStack(Material.GOLD_INGOT));
                gui.setItem(21, named(Material.SAND, ChatColor.GOLD + "Núcleo de Arena"));
                gui.setItem(22, new ItemStack(Material.GOLD_INGOT));
                gui.setItem(30, new ItemStack(Material.SAND));
                setResult(gui, Material.GOLDEN_SWORD, ChatColor.GOLD + "Colmillo del Desierto");
                break;
            case "AbyssAnchor":
                gui.setItem(12, new ItemStack(Material.PRISMARINE_SHARD));
                gui.setItem(20, new ItemStack(Material.PRISMARINE_CRYSTALS));
                gui.setItem(21, named(Material.PRISMARINE_CRYSTALS, ChatColor.AQUA + "Núcleo Abisal"));
                gui.setItem(22, new ItemStack(Material.PRISMARINE_CRYSTALS));
                gui.setItem(30, new ItemStack(Material.PRISMARINE_SHARD));
                setResult(gui, Material.TRIDENT, ChatColor.AQUA + "Ancla Abisal");
                break;
            case "VerdantAscension":
                gui.setItem(12, named(Material.EMERALD, ChatColor.DARK_GREEN + "Relicario del Bosque"));
                gui.setItem(21, named(Material.STICK, ChatColor.DARK_GREEN + "Bastón del Bosque"));
                gui.setItem(30, new ItemStack(Material.EMERALD_BLOCK));
                setResult(gui, Material.STICK, ChatColor.DARK_GREEN + "Bastón Ancestral del Bosque");
                break;
            case "SandstormCrown":
                gui.setItem(12, named(Material.GOLD_INGOT, ChatColor.GOLD + "Sello del Desierto"));
                gui.setItem(21, named(Material.GOLDEN_SWORD, ChatColor.GOLD + "Colmillo del Desierto"));
                gui.setItem(30, new ItemStack(Material.GOLD_BLOCK));
                setResult(gui, Material.GOLDEN_SWORD, ChatColor.GOLD + "Colmillo Real del Wyrm");
                break;
            case "TidalDominion":
                gui.setItem(12, named(Material.PRISMARINE_CRYSTALS, ChatColor.AQUA + "Núcleo Abisal Forjado"));
                gui.setItem(21, named(Material.TRIDENT, ChatColor.AQUA + "Ancla Abisal"));
                gui.setItem(30, new ItemStack(Material.PRISMARINE_BRICKS));
                setResult(gui, Material.TRIDENT, ChatColor.AQUA + "Ancla del Abismo");
                break;
            default:
                break;
        }
    }

    private static void showForgeCrossRecipe(Inventory gui, Material resultMat, String resultName) {
        gui.setItem(4, GUIBuilder.createInfoItem(
                "Forja de Jefes",
                "Esta receta usa la estructura",
                "en cruz del mundo.",
                "Usa /livingtool structure bossforge"));
        if (resultMat == Material.CONDUIT) {
            gui.setItem(12, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
            gui.setItem(20, named(Material.MAGMA_CREAM, ChatColor.GOLD + "Núcleo de Magma"));
            gui.setItem(21, new ItemStack(Material.DIAMOND));
            gui.setItem(22, named(Material.AMETHYST_SHARD, ChatColor.DARK_PURPLE + "Fragmento de Titán"));
            gui.setItem(30, named(Material.SCUTE, ChatColor.DARK_GRAY + "Escama del Vacío"));
        } else if (resultMat == Material.ELYTRA) {
            gui.setItem(12, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
            gui.setItem(20, named(Material.SUGAR, ChatColor.WHITE + "Polvo de Ángel"));
            gui.setItem(21, new ItemStack(Material.ELYTRA));
            gui.setItem(22, named(Material.SUGAR, ChatColor.WHITE + "Polvo de Ángel"));
            gui.setItem(30, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
        } else if (resultMat == Material.GOLDEN_HELMET) {
            gui.setItem(12, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
            gui.setItem(20, new ItemStack(Material.GOLDEN_HELMET));
            gui.setItem(21, named(Material.NETHER_STAR, ChatColor.YELLOW + "Esencia Estelar"));
            gui.setItem(22, new ItemStack(Material.GOLDEN_HELMET));
            gui.setItem(30, named(Material.FEATHER, ChatColor.YELLOW + "Pluma de Serafín"));
        } else if (resultMat == Material.NETHERITE_CHESTPLATE) {
            gui.setItem(12, named(Material.SCUTE, ChatColor.DARK_GRAY + "Escama del Vacío"));
            gui.setItem(20, named(Material.AMETHYST_SHARD, ChatColor.DARK_PURPLE + "Fragmento de Titán"));
            gui.setItem(21, new ItemStack(Material.PAPER));
            gui.setItem(22, named(Material.AMETHYST_SHARD, ChatColor.DARK_PURPLE + "Fragmento de Titán"));
            gui.setItem(30, named(Material.SCUTE, ChatColor.DARK_GRAY + "Escama del Vacío"));
        }
        setResult(gui, resultMat, resultName);
    }

    private static String resolveMatPrefix(String context) {
        if ("SWORD".equals(context)) {
            return "_SWORD";
        }
        if ("PICKAXE".equals(context)) {
            return "_PICKAXE";
        }
        if ("AXE".equals(context)) {
            return "_AXE";
        }
        if ("SHOVEL".equals(context)) {
            return "_SHOVEL";
        }
        if ("HOE".equals(context)) {
            return "_HOE";
        }
        return "";
    }

    private static ItemStack baseItem(String context, String matPrefix, String tier) {
        if (matPrefix.isEmpty()) {
            return new ItemStack(Material.STONE);
        }
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
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static void setResult(Inventory gui, Material mat, String name) {
        setResult(gui, new ItemStack(mat), name);
    }

    private static void setResult(Inventory gui, ItemStack template, String name) {
        ItemStack result = template.clone();
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(name);
        result.setItemMeta(meta);
        gui.setItem(24, result);
    }
}
