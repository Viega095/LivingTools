package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.data.LivingArmor;
import com.livingtools.manager.ArtifactManager;
import com.livingtools.manager.AetherialManager;
import com.livingtools.manager.BossDropType;
import com.livingtools.manager.BossWeaponManager;
import com.livingtools.manager.SleepingManager;
import com.livingtools.runes.RuneManager;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AssemblyGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "Mesa de Ensamblaje";

    // Grid 3x3 — mismos slots que RecipeGUI (guía /living recipes)
    public static final int[] GRID_SLOTS = {11, 12, 13, 20, 21, 22, 29, 30, 31};
    public static final int CONFIRM_SLOT = 23;
    public static final int OUTPUT_SLOT = 24;
    public static final int INFO_SLOT = 40;
    private static final String CONFIRM_LABEL = "Ensamblar";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        // Fill border and filler
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, filler);
        }

        // Set grid inputs to empty (null)
        for (int slot : GRID_SLOTS) {
            inv.setItem(slot, null);
        }

        // Preview + confirm
        setPreview(inv, null);

        // Crafting Info Indicator
        ItemStack info = new ItemStack(Material.ANVIL);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "\u2692 Forja Ancestral \u2692");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Coloca los ingredientes en la cuadr\u00edcula de 3x3.",
                ChatColor.GRAY + "El resultado aparece a la derecha.",
                ChatColor.GRAY + "Usa el bot\u00f3n " + ChatColor.GREEN + CONFIRM_LABEL + ChatColor.GRAY + " para craftear.",
                "",
                ChatColor.YELLOW + "Estructura: Mesa de Herrer\u00eda sobre Bloque de Hierro",
                ChatColor.YELLOW + "Gu\u00eda: /livingtool structure assembly"
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(INFO_SLOT, info);

        player.openInventory(inv);
    }

    public static boolean isGridSlot(int slot) {
        for (int s : GRID_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    public static boolean isProtectedSlot(int slot) {
        return slot == CONFIRM_SLOT || slot == OUTPUT_SLOT || slot == INFO_SLOT;
    }

    public static boolean tryCraft(Inventory inv, Player player) {
        updateResult(inv);
        ItemStack result = inv.getItem(OUTPUT_SLOT);
        if (result == null || result.getType() == Material.AIR) {
            return false;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Inventario lleno.");
            return false;
        }

        player.getInventory().addItem(result.clone());
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
        player.sendMessage(ChatColor.GREEN + "\u00a1Objeto ensamblado con \u00e9xito!");

        consumeGrid(inv);
        updateResult(inv);
        return true;
    }

    private static void consumeGrid(Inventory inv) {
        for (int gridSlot : GRID_SLOTS) {
            ItemStack ingredient = inv.getItem(gridSlot);
            if (ingredient == null || ingredient.getType() == Material.AIR) {
                continue;
            }
            int remaining = ingredient.getAmount() - 1;
            if (remaining <= 0) {
                inv.setItem(gridSlot, null);
            } else {
                ingredient.setAmount(remaining);
                inv.setItem(gridSlot, ingredient);
            }
        }
    }

    private static ItemStack createConfirmButton(boolean enabled) {
        if (enabled) {
            return GUIBuilder.createGlowingItem(
                    Material.LIME_DYE,
                    ChatColor.GREEN + "\u2714 " + CONFIRM_LABEL,
                    ChatColor.GRAY + "Clic para confirmar el ensamblaje.");
        }
        return GUIBuilder.createItem(
                Material.GRAY_DYE,
                ChatColor.DARK_GRAY + CONFIRM_LABEL,
                ChatColor.DARK_GRAY + "Coloca los ingredientes correctos.");
    }

    private static void updateConfirmButton(Inventory inv) {
        ItemStack result = inv.getItem(OUTPUT_SLOT);
        boolean valid = result != null && result.getType() != Material.AIR;
        inv.setItem(CONFIRM_SLOT, createConfirmButton(valid));
    }

    public static void updateResult(Inventory inv) {
        ItemStack s11 = inv.getItem(11);
        ItemStack s12 = inv.getItem(12);
        ItemStack s13 = inv.getItem(13);
        ItemStack s20 = inv.getItem(20);
        ItemStack s21 = inv.getItem(21);
        ItemStack s22 = inv.getItem(22);
        ItemStack s29 = inv.getItem(29);
        ItemStack s30 = inv.getItem(30);
        ItemStack s31 = inv.getItem(31);

        // === Reliquias y armas de jefes nuevos (Mesa 3x3) ===

        // Dryad Relic: _ E _ / H H H / _ E _
        if (isEmpty(s11) && isType(s12, Material.EMERALD) && isEmpty(s13) &&
            isDrop(s20, BossDropType.DRYAD_HEARTWOOD) && isDrop(s21, BossDropType.DRYAD_HEARTWOOD) && isDrop(s22, BossDropType.DRYAD_HEARTWOOD) &&
            isEmpty(s29) && isType(s30, Material.EMERALD) && isEmpty(s31)) {
            setPreview(inv, createBossRelic(Material.EMERALD, "&2&lRelicario del Bosque"));
            return;
        }

        // Wyrm Relic: _ S _ / G W G / _ S _
        if (isEmpty(s11) && isVanilla(s12, Material.SAND) && isEmpty(s13) &&
            isType(s20, Material.GOLD_INGOT) && isDrop(s21, BossDropType.WYRM_CORE) && isType(s22, Material.GOLD_INGOT) &&
            isEmpty(s29) && isVanilla(s30, Material.SAND) && isEmpty(s31)) {
            setPreview(inv, createBossRelic(Material.GOLD_INGOT, "&6&lSello del Desierto"));
            return;
        }

        // Leviathan Relic: _ P _ / C L C / _ P _
        if (isEmpty(s11) && isVanilla(s12, Material.PRISMARINE_SHARD) && isEmpty(s13) &&
            isVanilla(s20, Material.PRISMARINE_CRYSTALS) && isDrop(s21, BossDropType.LEVIATHAN_CORE) && isVanilla(s22, Material.PRISMARINE_CRYSTALS) &&
            isEmpty(s29) && isVanilla(s30, Material.PRISMARINE_SHARD) && isEmpty(s31)) {
            setPreview(inv, createBossRelic(Material.PRISMARINE_CRYSTALS, "&3&lNúcleo Abisal Forjado"));
            return;
        }

        // Bastón del Bosque (Dryad): _ H _ / H S H / _ E _
        if (isEmpty(s11) && isDrop(s12, BossDropType.DRYAD_HEARTWOOD) && isEmpty(s13) &&
            isDrop(s20, BossDropType.DRYAD_HEARTWOOD) && isType(s21, Material.STICK) && isDrop(s22, BossDropType.DRYAD_HEARTWOOD) &&
            isEmpty(s29) && isType(s30, Material.EMERALD) && isEmpty(s31)) {
            setPreview(inv, createThemedWeapon(Material.STICK, "&2&lBastón del Bosque",
                    "Canaliza la esencia de la Dríade Corrupta."));
            return;
        }

        // Bastón Ancestral: _ Relicario _ / _ Bastón _ / _ Bloque Esmeralda _
        if (isEmpty(s11) && isNamed(s12, Material.EMERALD, "Relicario") && isEmpty(s13) &&
            isEmpty(s20) && isNamed(s21, Material.STICK, "Bastón") && isEmpty(s22) &&
            isEmpty(s29) && isType(s30, Material.EMERALD_BLOCK) && isEmpty(s31)) {
            setPreview(inv, BossWeaponManager.create(BossWeaponManager.FinalWeapon.VERDANT_STAFF));
            return;
        }

        // Colmillo Real: _ Sello _ / _ Colmillo _ / _ Bloque Oro _
        if (isEmpty(s11) && isNamed(s12, Material.GOLD_INGOT, "Sello") && isEmpty(s13) &&
            isEmpty(s20) && isNamed(s21, Material.GOLDEN_SWORD, "Colmillo") && isEmpty(s22) &&
            isEmpty(s29) && isType(s30, Material.GOLD_BLOCK) && isEmpty(s31)) {
            setPreview(inv, BossWeaponManager.create(BossWeaponManager.FinalWeapon.SANDSTORM_FANG));
            return;
        }

        // Ancla del Abismo: _ Núcleo Forjado _ / _ Ancla _ / _ Ladrillos _
        if (isEmpty(s11) && isNamed(s12, Material.PRISMARINE_CRYSTALS, "Forjado") && isEmpty(s13) &&
            isEmpty(s20) && isNamed(s21, Material.TRIDENT, "Ancla") && isEmpty(s22) &&
            isEmpty(s29) && isType(s30, Material.PRISMARINE_BRICKS) && isEmpty(s31)) {
            setPreview(inv, BossWeaponManager.create(BossWeaponManager.FinalWeapon.ABYSS_ANCHOR));
            return;
        }

        // === Artifact Recipes ===

        // Living Charm: _ E _ / _ G _ / _ S _
        if (isEmpty(s11) && isType(s12, Material.EMERALD) && isEmpty(s13) &&
            isEmpty(s20) && isType(s21, Material.GOLD_INGOT) && isEmpty(s22) &&
            isEmpty(s29) && isType(s30, Material.STRING) && isEmpty(s31)) {
            setPreview(inv, ArtifactManager.createArtifact(ArtifactManager.ArtifactType.LIVING_CHARM));
            return;
        }

        // Soul Gem: _ A _ / _ S _ / _ G _
        if (isEmpty(s11) && isVanilla(s12, Material.AMETHYST_SHARD) && isEmpty(s13) &&
            isEmpty(s20) && isType(s21, Material.SOUL_SAND) && isEmpty(s22) &&
            isEmpty(s29) && isType(s30, Material.GLASS) && isEmpty(s31)) {
            setPreview(inv, ArtifactManager.createArtifact(ArtifactManager.ArtifactType.SOUL_GEM));
            return;
        }

        // Rune Pouch: _ L _ / _ S _ / _ G _
        if (isEmpty(s11) && isType(s12, Material.LEATHER) && isEmpty(s13) &&
            isEmpty(s20) && isType(s21, Material.STRING) && isEmpty(s22) &&
            isEmpty(s29) && isType(s30, Material.GOLD_NUGGET) && isEmpty(s31)) {
            setPreview(inv, ArtifactManager.createArtifact(ArtifactManager.ArtifactType.RUNE_POUCH));
            return;
        }

        // Rune Geode: _ A _ / G S G / _ A _
        if (isEmpty(s11) && isVanilla(s12, Material.AMETHYST_SHARD) && isEmpty(s13) &&
            isType(s20, Material.GOLD_NUGGET) && isType(s21, Material.STONE) && isType(s22, Material.GOLD_NUGGET) &&
            isEmpty(s29) && isVanilla(s30, Material.AMETHYST_SHARD) && isEmpty(s31)) {
            setPreview(inv, RuneManager.createGeode());
            return;
        }

        // === Living Tool/Armor Recipes ===
        if (s21 != null && s21.getType() != Material.AIR) {
            Material baseMat = s21.getType();
            boolean isTool = baseMat.name().contains("PICKAXE") || baseMat.name().contains("SWORD") ||
                             baseMat.name().contains("_AXE") || baseMat.name().contains("SHOVEL") ||
                             baseMat.name().contains("HOE");
            boolean isArmor = baseMat.name().contains("HELMET") || baseMat.name().contains("CHESTPLATE") ||
                              baseMat.name().contains("LEGGINGS") || baseMat.name().contains("BOOTS");

            if (isTool || isArmor) {
                // Diamond Recipe: F _ _ / T X T / F _ _
                if (isDrop(s11, BossDropType.SERAPHIM_FEATHER) && isEmpty(s12) && isEmpty(s13) &&
                    isDrop(s20, BossDropType.TITAN_SHARD) && isDrop(s22, BossDropType.TITAN_SHARD) &&
                    isDrop(s29, BossDropType.SERAPHIM_FEATHER) && isEmpty(s30) && isEmpty(s31)) {
                    setPreview(inv, isTool ? createLivingTool(baseMat) : createLivingArmor(baseMat));
                    return;
                }

                // Netherite Recipe: M _ _ / V X V / M _ _
                if (isDrop(s11, BossDropType.MAGMA_CORE) && isEmpty(s12) && isEmpty(s13) &&
                    isDrop(s20, BossDropType.VOID_SCALE) && isDrop(s22, BossDropType.VOID_SCALE) &&
                    isDrop(s29, BossDropType.MAGMA_CORE) && isEmpty(s30) && isEmpty(s31)) {
                    setPreview(inv, isTool ? createLivingTool(baseMat) : createLivingArmor(baseMat));
                    return;
                }

                // Sleeping Tool (Only tools): A _ _ / S X S / A _ _
                if (isTool && isDrop(s11, BossDropType.ANGEL_DUST) && isEmpty(s12) && isEmpty(s13) &&
                    isStarlightEssence(s20) && isStarlightEssence(s22) &&
                    isDrop(s29, BossDropType.ANGEL_DUST) && isEmpty(s30) && isEmpty(s31)) {
                    setPreview(inv, SleepingManager.createSleepingTool(baseMat));
                    return;
                }

                // Iron/Gold/Stone/Basic Tier1: _ I _ / _ X _ / _ _ _
                if (isEmpty(s11) && isEmpty(s13) && isEmpty(s20) && isEmpty(s22) &&
                    isEmpty(s29) && isEmpty(s30) && isEmpty(s31)) {
                    if (isType(s12, Material.IRON_BLOCK)) {
                        setPreview(inv, isTool ? createLivingTool(baseMat) : createLivingArmor(baseMat));
                        return;
                    }
                    if (isType(s12, Material.GOLD_BLOCK)) {
                        setPreview(inv, isTool ? createLivingTool(baseMat) : createLivingArmor(baseMat));
                        return;
                    }
                    if (isTool && isType(s12, Material.COAL)) {
                        setPreview(inv, createLivingTool(baseMat));
                        return;
                    }
                    if (isTool && isType(s12, Material.WHEAT_SEEDS)) {
                        setPreview(inv, createLivingTool(baseMat));
                        return;
                    }
                }
            }
        }

        // No recipe matched
        setPreview(inv, null);
    }

    private static void setPreview(Inventory inv, ItemStack result) {
        inv.setItem(OUTPUT_SLOT, result);
        updateConfirmButton(inv);
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private static boolean isType(ItemStack item, Material mat) {
        if (mat == Material.AIR) {
            return isEmpty(item);
        }
        return item != null && item.getType() == mat;
    }

    private static boolean isStarlightEssence(ItemStack item) {
        return BossDropType.STARLIGHT_ESSENCE.matches(item)
                || com.livingtools.manager.AetherialManager.isStarlightEssence(item);
    }

    private static boolean isDrop(ItemStack item, BossDropType type) {
        return BossDropType.matchesAmount(item, type, 1);
    }

    private static boolean isVanilla(ItemStack item, Material mat) {
        return BossDropType.isVanillaMaterial(item, mat);
    }

    private static boolean isNamed(ItemStack item, Material mat, String namePart) {
        if (item == null || item.getType() != mat) {
            return false;
        }
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains(namePart);
    }

    private static ItemStack createLivingTool(Material mat) {
        ItemStack result = new ItemStack(mat);
        LivingTool tool = new LivingTool(result);
        tool.getData().initialize();
        tool.updateLore();
        return result;
    }

    private static ItemStack createLivingArmor(Material mat) {
        ItemStack result = new ItemStack(mat);
        LivingArmor armor = new LivingArmor(result);
        armor.initialize();
        armor.updateLore();
        return result;
    }

    private static ItemStack createBossRelic(Material mat, String name) {
        ItemStack result = new ItemStack(mat);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Recompensa forjada a partir de un boss.",
                    ChatColor.GRAY + "Se puede usar en progresiones futuras.",
                    ChatColor.DARK_GRAY + "Solo para crafteo"));
            result.setItemMeta(meta);
        }
        return result;
    }

    private static ItemStack createThemedWeapon(Material mat, String name, String flavor) {
        ItemStack result = new ItemStack(mat);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + flavor,
                    ChatColor.DARK_GRAY + "Arma temática de jefe",
                    ChatColor.DARK_GRAY + "Solo para crafteo"));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            result.setItemMeta(meta);
        }
        return result;
    }
}
