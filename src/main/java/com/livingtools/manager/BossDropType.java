package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Nombres y materiales canónicos de drops de jefe (español).
 */
public enum BossDropType {

    MAGMA_CORE(Material.MAGMA_CREAM, "magma_core", "&6Núcleo de Magma", "&7Núcleo de calor puro."),
    SERAPHIM_FEATHER(Material.FEATHER, "seraphim_feather", "&ePluma de Serafín", "&7Pluma divina."),
    TITAN_SHARD(Material.AMETHYST_SHARD, "titan_shard", "&5Fragmento de Titán", "&7Fragmento del vacío."),
    VOID_SCALE(Material.SCUTE, "void_scale", "&8Escama del Vacío", "&7Escama de una criatura del vacío."),
    ANGEL_DUST(Material.SUGAR, "angel_dust", "&fPolvo de Ángel", "&7Polvo brillante de un querubín."),
    STARLIGHT_ESSENCE(Material.NETHER_STAR, "starlight_essence", "&e&lEsencia Estelar", "&7Esencia celestial."),
    SHADOW_ESSENCE(Material.COAL, "shadow_essence", "&8Esencia de Sombra", "&7Oscuridad concentrada."),
    DRYAD_HEARTWOOD(Material.OAK_SAPLING, "dryad_heartwood", "&2&lCorazón del Bosque",
            "&7Esencia viva arrancada de la Dríada Corrupta."),
    WYRM_CORE(Material.SAND, "wyrm_core", "&6&lNúcleo de Arena", "&7Calor y fuerza del Wyrm de las Arenas."),
    LEVIATHAN_CORE(Material.PRISMARINE_CRYSTALS, "leviathan_core", "&3&lNúcleo Abisal",
            "&7Latido profundo del Leviatán.");

    private static NamespacedKey dropKey() {
        return new NamespacedKey(LivingToolsPlugin.getInstance(), "boss_drop_type");
    }

    private static final String CRAFTING_LORE = MessageUtils.color("&cSolo para crafteo");

    private final Material material;
    private final String id;
    private final String displayName;
    private final String flavorLore;

    BossDropType(Material material, String id, String displayName, String flavorLore) {
        this.material = material;
        this.id = id;
        this.displayName = displayName;
        this.flavorLore = flavorLore;
    }

    public Material getMaterial() {
        return material;
    }

    public String getColoredName() {
        return MessageUtils.color(displayName);
    }

    public String getPlainName() {
        return ChatColor.stripColor(getColoredName());
    }

    public ItemStack create() {
        return create(1);
    }

    public ItemStack create(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(getColoredName());
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color(flavorLore));
        lore.add(CRAFTING_LORE);
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(dropKey(), PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public boolean matches(ItemStack item) {
        if (item == null || item.getType() != material) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();

        if (meta.getPersistentDataContainer().has(dropKey(), PersistentDataType.STRING)) {
            String stored = meta.getPersistentDataContainer().get(dropKey(), PersistentDataType.STRING);
            return id.equals(stored);
        }

        if (meta.hasDisplayName() && displayNameMatches(meta.getDisplayName())) {
            return true;
        }

        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (displayNameMatches(line) || flavorMatches(line)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean displayNameMatches(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String normalized = normalize(text);
        String expected = normalize(getColoredName());
        String plain = normalize(getPlainName());
        return normalized.equals(expected)
                || normalized.equals(plain)
                || normalized.contains(plain);
    }

    private boolean flavorMatches(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return normalize(text).equals(normalize(flavorLore));
    }

    private static String normalize(String text) {
        return ChatColor.stripColor(MessageUtils.color(text)).toLowerCase().trim();
    }

    public static boolean hasCraftingLore(ItemMeta meta) {
        if (meta == null || !meta.hasLore()) {
            return false;
        }
        for (String line : meta.getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped != null && stripped.contains("Solo para crafteo")) {
                return true;
            }
        }
        return false;
    }

    /** Material vanilla (no es un drop de jefe con el mismo material base). */
    public static boolean isVanillaMaterial(ItemStack item, Material material) {
        if (item == null || item.getType() != material) {
            return false;
        }
        return fromItem(item) == null;
    }

    public static BossDropType fromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String stored = item.getItemMeta().getPersistentDataContainer().get(dropKey(), PersistentDataType.STRING);
        if (stored != null) {
            for (BossDropType type : values()) {
                if (type.id.equals(stored)) {
                    return type;
                }
            }
        }
        for (BossDropType type : values()) {
            if (type.matches(item)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isCraftingOnlyDrop(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        if (fromItem(item) != null) {
            return true;
        }
        return hasCraftingLore(item.getItemMeta());
    }

    public static boolean matchesAmount(ItemStack item, BossDropType type, int required) {
        return item != null && item.getAmount() >= required && type.matches(item);
    }

    public static boolean matchesAmount(ItemStack item, int required) {
        return item != null && item.getAmount() >= required && matchesAny(item);
    }

    private static boolean matchesAny(ItemStack item) {
        for (BossDropType type : values()) {
            if (type.matches(item)) {
                return true;
            }
        }
        return false;
    }
}
