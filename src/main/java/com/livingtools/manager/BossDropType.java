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
 * Nombres, materiales y orígenes de drops de jefes y esbirros (español).
 */
public enum BossDropType {

    MAGMA_CORE(Material.MAGMA_CREAM, "magma_core", "&6&lNúcleo de Magma",
            "&7Núcleo incandescente de calor primigenio.",
            "&6✦ Jefe: &fThe Smith / Avatar del Génesis",
            "&e✦ Esbirros: &fEsbirro Ígneo (Living Boss Minion)",
            "&b✦ Estación: &fForja de Jefes & Mesa de Ensamblaje"),

    SERAPHIM_FEATHER(Material.FEATHER, "seraphim_feather", "&e&lPluma de Serafín",
            "&7Pluma celestial impregnada de luz pura.",
            "&6✦ Jefe: &fSerafín Celestial",
            "&e✦ Esbirros: &fQuerubines Sagrados (Cherub)",
            "&b✦ Estación: &fForja de Jefes & Mesa de Ensamblaje"),

    TITAN_SHARD(Material.AMETHYST_SHARD, "titan_shard", "&5&lFragmento de Titán",
            "&7Cristal de amatista cósmica del vacío profundo.",
            "&6✦ Jefe: &fTitán de las Sombras",
            "&e✦ Esbirros: &fEspectros del Vacío",
            "&b✦ Estación: &fForja de Jefes & Mesa de Ensamblaje"),

    VOID_SCALE(Material.SCUTE, "void_scale", "&8&lEscama del Vacío",
            "&7Coraza endurecida de criaturas de la penumbra.",
            "&6✦ Jefe: &fTitán de las Sombras / Incursión Abisal",
            "&e✦ Esbirros: &fParásitos del Vacío (Void Parasite)",
            "&b✦ Estación: &fForja de Jefes & Mesa de Ensamblaje"),

    ANGEL_DUST(Material.SUGAR, "angel_dust", "&f&lPolvo de Ángel",
            "&7Polvo etéreo destellante de querubines celestiales.",
            "&6✦ Jefe: &fSerafín Celestial",
            "&e✦ Esbirros: &fQuerubín Sagrado (Cherub)",
            "&b✦ Estación: &fForja de Jefes & Mesa de Ensamblaje"),

    STARLIGHT_ESSENCE(Material.NETHER_STAR, "starlight_essence", "&e&lEsencia Estelar",
            "&7Fragmento de estrella caído del firmamento.",
            "&6✦ Jefe: &fSerafín Celestial / Meteoritos Celestiales",
            "&e✦ Esbirros: &fCentinelas de Luz",
            "&b✦ Estación: &fForja de Jefes & Altar Celestial"),

    SHADOW_ESSENCE(Material.COAL, "shadow_essence", "&8&lEsencia de Sombra",
            "&7Oscuridad pura condensada en materia física.",
            "&6✦ Jefe: &fTitán de las Sombras",
            "&e✦ Esbirros: &fCriaturas de Sombra (Shadow Creature)",
            "&b✦ Estación: &fForja de Jefes & Grieta Abisal"),

    DRYAD_HEARTWOOD(Material.OAK_SAPLING, "dryad_heartwood", "&2&lCorazón del Bosque",
            "&7Brote viviente arrancado de la Dríada Corrupta.",
            "&6✦ Jefe: &fDríada Corrupta (Arena Bosque)",
            "&e✦ Esbirros: &fBrotes Vivientes y Ents",
            "&b✦ Estación: &fMesa de Ensamblaje"),

    WYRM_CORE(Material.SAND, "wyrm_core", "&6&lNúcleo de Arena",
            "&7Poder ígneo y terráqueo del Señor de las Dunas.",
            "&6✦ Jefe: &fWyrm de las Arenas (Arena Desierto)",
            "&e✦ Esbirros: &fCrías de Wyrm",
            "&b✦ Estación: &fMesa de Ensamblaje"),

    LEVIATHAN_CORE(Material.PRISMARINE_CRYSTALS, "leviathan_core", "&3&lNúcleo Abisal",
            "&7Latido marino de la gran bestia de las profundidades.",
            "&6✦ Jefe: &fGran Leviatán (Arena Oceánica)",
            "&e✦ Esbirros: &fGuardianes Abisales",
            "&b✦ Estación: &fMesa de Ensamblaje"),

    GENESIS_ESSENCE(Material.NETHER_STAR, "genesis_essence", "&6&lEsencia de la Creación",
            "&7Poder primigenio supremo destilado del Avatar del Génesis.",
            "&6✦ Jefe: &fEl Avatar del Génesis (Fase 3)",
            "&e✦ Esbirros: &fCristales Rúnicos de Escarcha",
            "&b✦ Estación: &fAltar de Ascensión Divina (/lt ascend)"),

    GENESIS_STAR_GEM(Material.EMERALD, "genesis_star_gem", "&d&lGema de Estrella del Génesis",
            "&7Engarce celestial que amplifica todo el arsenal.",
            "&6✦ Jefe: &fEl Avatar del Génesis",
            "&e✦ Esbirros: &fIncursiones Míticas",
            "&b✦ Estación: &fEngarces Celestiales (/lt sockets)");

    private static NamespacedKey dropKey() {
        return new NamespacedKey(LivingToolsPlugin.getInstance(), "boss_drop_type");
    }

    private static final String CRAFTING_LORE = MessageUtils.color("&cSolo para crafteo");

    private final Material material;
    private final String id;
    private final String displayName;
    private final String flavorLore;
    private final String bossSource;
    private final String minionSource;
    private final String stationLore;

    BossDropType(Material material, String id, String displayName, String flavorLore,
                 String bossSource, String minionSource, String stationLore) {
        this.material = material;
        this.id = id;
        this.displayName = displayName;
        this.flavorLore = flavorLore;
        this.bossSource = bossSource;
        this.minionSource = minionSource;
        this.stationLore = stationLore;
    }

    public Material getMaterial() {
        return material;
    }

    public String getId() {
        return id;
    }

    public String getColoredName() {
        return MessageUtils.color(displayName);
    }

    public String getPlainName() {
        return ChatColor.stripColor(getColoredName());
    }

    public String getBossSource() {
        return bossSource;
    }

    public String getMinionSource() {
        return minionSource;
    }

    public String getStationLore() {
        return stationLore;
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
        if (bossSource != null && !bossSource.isEmpty()) {
            lore.add(MessageUtils.color(bossSource));
        }
        if (minionSource != null && !minionSource.isEmpty()) {
            lore.add(MessageUtils.color(minionSource));
        }
        if (stationLore != null && !stationLore.isEmpty()) {
            lore.add(MessageUtils.color(stationLore));
        }
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
