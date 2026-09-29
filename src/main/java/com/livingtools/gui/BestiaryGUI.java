package com.livingtools.gui;

import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BestiaryGUI — Bestiario interactivo de Jefes, Criaturas Míticas y Esbirros.
 * Muestra vida, daño, dificultad, ubicación y drops detallados con categorías y paginación.
 */
public class BestiaryGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Bestiario de Criaturas & Jefes ✦";

    public enum BestiaryCategory {
        ALL("Todas las Criaturas", Material.BOOK),
        BOSSES("Jefes Supremos", Material.NETHER_STAR),
        MINIONS("Esbirros & Invocaciones", Material.ZOMBIE_HEAD),
        RAID("Mega-Raid Génesis", Material.DRAGON_EGG);

        private final String displayName;
        private final Material icon;

        BestiaryCategory(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
    }

    public static class BestiaryEntry {
        private final String name;
        private final String subtitle;
        private final Material icon;
        private final double maxHealth;
        private final double attackDamage;
        private final String difficultyStars;
        private final String location;
        private final List<String> dropList;
        private final List<String> minionList;
        private final boolean isBoss;

        public BestiaryEntry(String name, String subtitle, Material icon, double maxHealth,
                             double attackDamage, String difficultyStars, String location,
                             List<String> dropList, List<String> minionList, boolean isBoss) {
            this.name = name;
            this.subtitle = subtitle;
            this.icon = icon;
            this.maxHealth = maxHealth;
            this.attackDamage = attackDamage;
            this.difficultyStars = difficultyStars;
            this.location = location;
            this.dropList = dropList;
            this.minionList = minionList;
            this.isBoss = isBoss;
        }

        public String getName() { return name; }
        public String getSubtitle() { return subtitle; }
        public Material getIcon() { return icon; }
        public double getMaxHealth() { return maxHealth; }
        public double getAttackDamage() { return attackDamage; }
        public String getDifficultyStars() { return difficultyStars; }
        public String getLocation() { return location; }
        public List<String> getDropList() { return dropList; }
        public List<String> getMinionList() { return minionList; }
        public boolean isBoss() { return isBoss; }

        public ItemStack createItem() {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + subtitle);
            lore.add("");
            lore.add(ChatColor.RED + "❤ Salud: " + ChatColor.WHITE + (int) maxHealth + " HP");
            lore.add(ChatColor.DARK_RED + "⚔ Daño de Ataque: " + ChatColor.WHITE + (int) attackDamage + " pts");
            lore.add(ChatColor.YELLOW + "⭐ Dificultad: " + difficultyStars);
            lore.add(ChatColor.AQUA + "🌍 Ubicación: " + ChatColor.WHITE + location);
            lore.add("");
            lore.add(ChatColor.GOLD + "✦ Drops Posibles:");
            for (String drop : dropList) {
                lore.add(ChatColor.WHITE + "  • " + drop);
            }

            if (!minionList.isEmpty()) {
                lore.add("");
                lore.add(ChatColor.LIGHT_PURPLE + "✦ Esbirros Asociados:");
                for (String m : minionList) {
                    lore.add(ChatColor.GRAY + "  ► " + m);
                }
            }

            lore.add("");
            lore.add(ChatColor.YELLOW + "► Click para ver detalles, drops y combate");

            return GUIBuilder.createGlowingItem(icon, (isBoss ? ChatColor.GOLD + "👑 " : ChatColor.YELLOW + "👾 ") + name,
                    lore.toArray(new String[0]));
        }
    }

    public static BestiaryEntry getEntryByName(String rawName) {
        if (rawName == null) return null;
        String clean = ChatColor.stripColor(rawName).replace("👑", "").replace("👾", "").trim();
        for (BestiaryEntry e : ENTRIES) {
            if (clean.equalsIgnoreCase(e.getName()) || clean.contains(e.getName()) || e.getName().contains(clean)) {
                return e;
            }
        }
        return null;
    }

    public static final List<BestiaryEntry> ENTRIES = new ArrayList<>();

    static {
        // 1. The Smith
        ENTRIES.add(new BestiaryEntry(
                "The Smith (Living Boss)",
                "Avatar incandescente del fuego y la forja primordial.",
                Material.MAGMA_BLOCK, 500.0, 20.0,
                ChatColor.YELLOW + "★★★☆☆ Media",
                "Estructura Volcánica / Altar de Fuego (/lt admin spawnboss SMITH)",
                Arrays.asList(
                        ChatColor.GOLD + "Núcleo de Magma (100% - Forja de Expansor)",
                        ChatColor.YELLOW + "Lingotes de Netherite & Diamantes",
                        ChatColor.RED + "Bolsa de Botín Living"
                ),
                Arrays.asList(
                        "Living Boss Minion (40 HP - Lanza fuego e inmola)"
                ),
                true
        ));

        // 2. Serafín Celestial
        ENTRIES.add(new BestiaryEntry(
                "Serafín Celestial",
                "Entidad alada consagrada a la luz suprema del cielo.",
                Material.FEATHER, 750.0, 25.0,
                ChatColor.GOLD + "★★★★☆ Difícil",
                "Fortaleza Aérea / Cielo Y>200 (/lt admin spawnboss SERAPHIM)",
                Arrays.asList(
                        ChatColor.YELLOW + "Pluma de Serafín (100% - Alas del Serafín)",
                        ChatColor.WHITE + "Polvo de Ángel (60% - Halo Celestial)",
                        ChatColor.GOLD + "Esencia Estelar (35% - Ascensión Astral)"
                ),
                Arrays.asList(
                        "Querubines Sagrados (30 HP - Proyectiles de Luz divina)"
                ),
                true
        ));

        // 3. Titán de las Sombras
        ENTRIES.add(new BestiaryEntry(
                "Titán de las Sombras",
                "Coloso milenario moldeado con la energía oscura del vacío.",
                Material.OBSIDIAN, 1000.0, 30.0,
                ChatColor.DARK_PURPLE + "★★★★☆ Difícil",
                "Forja Maldita / Grieta del Vacío (/lt admin spawnboss TITAN)",
                Arrays.asList(
                        ChatColor.DARK_PURPLE + "Fragmento de Titán (100% - Runa del Titán)",
                        ChatColor.DARK_GRAY + "Escama del Vacío (75% - Kit de Reparación)",
                        ChatColor.BLACK + "Esencia de Sombra (40% - Sendas de Sombra)"
                ),
                Arrays.asList(
                        "Espectros del Vacío (45 HP - Ceguera y marchitez)",
                        "Parásitos del Vacío (50 HP - Absorbe durabilidad y vida)"
                ),
                true
        ));

        // 4. Dríada Corrupta
        ENTRIES.add(new BestiaryEntry(
                "Dríada Corrupta",
                "Espíritu vegetal ancestral enloquecido por la plaga.",
                Material.OAK_SAPLING, 800.0, 22.0,
                ChatColor.GREEN + "★★★☆☆ Media",
                "Arena Forestal (/lt admin spawnarena DRYAD)",
                Arrays.asList(
                        ChatColor.DARK_GREEN + "Corazón del Bosque (100% - Dryad Relic)",
                        ChatColor.GREEN + "Relicario del Bosque Forjado",
                        ChatColor.AQUA + "Bastón del Bosque (Arma Mítica 3x3)"
                ),
                Arrays.asList(
                        "Brotes Vivientes & Ents (35 HP - Enraizamiento y veneno)"
                ),
                true
        ));

        // 5. Wyrm de las Arenas
        ENTRIES.add(new BestiaryEntry(
                "Wyrm de las Arenas",
                "Sierpe gigantesca excavadora que domina el desierto árido.",
                Material.SAND, 1200.0, 28.0,
                ChatColor.GOLD + "★★★★☆ Difícil",
                "Arena Desértica (/lt admin spawnarena WYRM)",
                Arrays.asList(
                        ChatColor.GOLD + "Núcleo de Arena (100% - Wyrm Relic)",
                        ChatColor.YELLOW + "Sello del Desierto Forjado",
                        ChatColor.GOLD + "Colmillo del Desierto (Sand Fang)"
                ),
                Arrays.asList(
                        "Crías de Wyrm (40 HP - Torbellino de Arena y lentitud)"
                ),
                true
        ));

        // 6. Gran Leviatán
        ENTRIES.add(new BestiaryEntry(
                "Gran Leviatán",
                "Monstruosidad de las profundidades oceánicas insondables.",
                Material.PRISMARINE_CRYSTALS, 1500.0, 32.0,
                ChatColor.DARK_AQUA + "★★★★☆ Difícil",
                "Arena Oceánica (/lt admin spawnarena LEVIATHAN)",
                Arrays.asList(
                        ChatColor.DARK_AQUA + "Núcleo Abisal (100% - Leviathan Relic)",
                        ChatColor.AQUA + "Núcleo Abisal Forjado",
                        ChatColor.BLUE + "Ancla Abisal (Abyss Anchor)"
                ),
                Arrays.asList(
                        "Guardianes Abisales (60 HP - Rayos Láser y ahogamiento)"
                ),
                true
        ));

        // 7. Mega-Raid Boss: Avatar del Génesis
        ENTRIES.add(new BestiaryEntry(
                "El Avatar del Génesis [Mega-Boss]",
                "Deidad primigenia de 3 fases continuas: Magma, Escarcha y Vacío.",
                Material.NETHER_STAR, 3000.0, 35.0,
                ChatColor.RED + "★★★★★ MÍTICO / RAID",
                "Invocación Mítica de Raid (/lt genesis / /lt incursion)",
                Arrays.asList(
                        ChatColor.GOLD + "Esencia de la Creación (100% - Ascensión Divina)",
                        ChatColor.YELLOW + "Corona del Génesis (Casco Legendario)",
                        ChatColor.LIGHT_PURPLE + "Gema de Estrella del Génesis (+35% Daño)"
                ),
                Arrays.asList(
                        "Cristales Rúnicos de Escarcha (150 HP - Escudo Inmune)"
                ),
                true
        ));

        // 8. Esbirros: Living Boss Minion
        ENTRIES.add(new BestiaryEntry(
                "Living Boss Minion",
                "Esbirro volcánico invocado para defender a The Smith.",
                Material.MAGMA_CREAM, 40.0, 10.0,
                ChatColor.GRAY + "★☆☆☆☆ Esbirro",
                "Arena Volcánica / Invocación de The Smith",
                Arrays.asList(
                        ChatColor.GOLD + "Magma Cream (100%)",
                        ChatColor.GRAY + "Pólvora & Fragmentos de Fuego"
                ),
                new ArrayList<>(),
                false
        ));

        // 9. Esbirros: Querubín Sagrado (Cherub)
        ENTRIES.add(new BestiaryEntry(
                "Querubín Sagrado (Cherub)",
                "Invocación alada que lanza ráfagas de luz celestial.",
                Material.SUGAR, 30.0, 8.0,
                ChatColor.GRAY + "★☆☆☆☆ Esbirro",
                "Fortaleza Aérea / Invocación de Serafín",
                Arrays.asList(
                        ChatColor.WHITE + "Polvo de Ángel (30% - Halo Sagrado)",
                        ChatColor.YELLOW + "Pepitas de Oro & Plumas"
                ),
                new ArrayList<>(),
                false
        ));

        // 10. Esbirros: Parásito del Vacío (Void Parasite)
        ENTRIES.add(new BestiaryEntry(
                "Parásito del Vacío (Void Parasite)",
                "Criatura ágil de la sombra que succiona durabilidad y salud.",
                Material.SCUTE, 50.0, 12.0,
                ChatColor.DARK_GRAY + "★★☆☆☆ Esbirro Élite",
                "Grietas Abisales / Invocación de Titán",
                Arrays.asList(
                        ChatColor.DARK_GRAY + "Escama del Vacío (40% - Kit de Reparación)",
                        ChatColor.DARK_PURPLE + "Fragmentos de Sombra"
                ),
                new ArrayList<>(),
                false
        ));

        // 11. Esbirros: Espectro del Vacío
        ENTRIES.add(new BestiaryEntry(
                "Espectro del Vacío",
                "Alma atrapada en las sombras que inflige ceguera cósmica.",
                Material.PHANTOM_MEMBRANE, 45.0, 14.0,
                ChatColor.DARK_PURPLE + "★★☆☆☆ Esbirro",
                "Grietas Abisales / Invocación de Titán",
                Arrays.asList(
                        ChatColor.DARK_PURPLE + "Esencia de Sombra (25%)",
                        ChatColor.WHITE + "Membrana de Fantasma & Lágrima de Ghast"
                ),
                new ArrayList<>(),
                false
        ));

        // 12. Esbirros: Brote Viviente / Ent
        ENTRIES.add(new BestiaryEntry(
                "Brote Viviente (Ent Sagrado)",
                "Guardián silvano que enraíza a los intrusos del bosque.",
                Material.OAK_LEAVES, 35.0, 10.0,
                ChatColor.GREEN + "★☆☆☆☆ Esbirro",
                "Arena Forestal / Invocación de Dríada",
                Arrays.asList(
                        ChatColor.GREEN + "Madera Sagrada del Bosque (50%)",
                        ChatColor.GOLD + "Manzanas Doradas & Semillas"
                ),
                new ArrayList<>(),
                false
        ));

        // 13. Esbirros: Cría de Wyrm de Arena
        ENTRIES.add(new BestiaryEntry(
                "Cría de Wyrm de Arena",
                "Serpiente subterránea que ciega con tormentas de polvo.",
                Material.RABBIT_FOOT, 40.0, 12.0,
                ChatColor.GOLD + "★☆☆☆☆ Esbirro",
                "Arena Desértica / Invocación de Wyrm",
                Arrays.asList(
                        ChatColor.YELLOW + "Arena Fósil Reforzada (50%)",
                        ChatColor.GRAY + "Huesos del Desierto"
                ),
                new ArrayList<>(),
                false
        ));

        // 14. Esbirros: Guardián Abisal
        ENTRIES.add(new BestiaryEntry(
                "Guardián Abisal Marino",
                "Defensor de las corrientes oceánicas con rayos de prisma.",
                Material.PRISMARINE_SHARD, 60.0, 16.0,
                ChatColor.AQUA + "★★☆☆☆ Esbirro",
                "Arena Oceánica / Invocación de Leviatán",
                Arrays.asList(
                        ChatColor.AQUA + "Fragmento de Prisma (50%)",
                        ChatColor.DARK_AQUA + "Ojo Abisal de las Mareas"
                ),
                new ArrayList<>(),
                false
        ));

        // 15. Esbirros: Cristal Rúnico de Escarcha
        ENTRIES.add(new BestiaryEntry(
                "Cristal Rúnico de Escarcha",
                "Tótem gélido que otorga invulnerabilidad absoluta al Avatar del Génesis.",
                Material.PACKED_ICE, 150.0, 0.0,
                ChatColor.AQUA + "★★★☆☆ Tótem Raid",
                "Fase 2 del Avatar del Génesis",
                Arrays.asList(
                        ChatColor.AQUA + "Fragmento de Hielo Ancestral (100%)"
                ),
                new ArrayList<>(),
                false
        ));
    }

    public static void open(Player player, int page, BestiaryCategory category) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);
        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Fila Superior: Filtros de Categoría
        gui.setItem(1, GUIBuilder.createGlowingItem(
                Material.BOOK,
                (category == BestiaryCategory.ALL ? ChatColor.GREEN + "▶ " : "") + ChatColor.GOLD + "✦ Todas las Criaturas",
                "",
                ChatColor.GRAY + "Ver Jefes, Esbirros y Raids completos.",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        gui.setItem(2, GUIBuilder.createGlowingItem(
                Material.NETHER_STAR,
                (category == BestiaryCategory.BOSSES ? ChatColor.GREEN + "▶ " : "") + ChatColor.RED + "👑 Jefes Supremos",
                "",
                ChatColor.GRAY + "The Smith, Serafín, Titán, Dríada, Wyrm, Leviatán.",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        // Header Central (Slot 4)
        gui.setItem(4, GUIBuilder.createGlowingItem(Material.WRITABLE_BOOK,
                ChatColor.GOLD + "✦ Bestiario Mítico de Almas ✦",
                ChatColor.GRAY + "Categoría: " + ChatColor.LIGHT_PURPLE + category.getDisplayName(),
                ChatColor.GRAY + "Página: " + ChatColor.YELLOW + (page + 1),
                "",
                ChatColor.AQUA + "Conoce la salud, dificultad, ubicación y drops",
                ChatColor.AQUA + "de cada Jefe y sus Esbirros subordinados."));

        gui.setItem(6, GUIBuilder.createGlowingItem(
                Material.ZOMBIE_HEAD,
                (category == BestiaryCategory.MINIONS ? ChatColor.GREEN + "▶ " : "") + ChatColor.DARK_PURPLE + "👾 Esbirros & Minions",
                "",
                ChatColor.GRAY + "Minions volcánicos, querubines, parásitos...",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        gui.setItem(7, GUIBuilder.createGlowingItem(
                Material.DRAGON_EGG,
                (category == BestiaryCategory.RAID ? ChatColor.GREEN + "▶ " : "") + ChatColor.LIGHT_PURPLE + "🐉 Mega-Raid Génesis",
                "",
                ChatColor.GRAY + "Avatar del Génesis y Cristales de Escarcha.",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        // Filtrar entradas
        List<BestiaryEntry> filtered = new ArrayList<>();
        for (BestiaryEntry e : ENTRIES) {
            if (category == BestiaryCategory.ALL) {
                filtered.add(e);
            } else if (category == BestiaryCategory.BOSSES && e.isBoss && !e.getName().contains("Génesis")) {
                filtered.add(e);
            } else if (category == BestiaryCategory.MINIONS && !e.isBoss) {
                filtered.add(e);
            } else if (category == BestiaryCategory.RAID && (e.getName().contains("Génesis") || e.getName().contains("Escarcha"))) {
                filtered.add(e);
            }
        }

        int pageSize = 21;
        int maxPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        int safePage = Math.max(0, Math.min(page, maxPages - 1));

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        int startIndex = safePage * pageSize;
        for (int i = 0; i < slots.length; i++) {
            int entryIndex = startIndex + i;
            if (entryIndex < filtered.size()) {
                gui.setItem(slots[i], filtered.get(entryIndex).createItem());
            }
        }

        // Barra de navegación inferior
        if (safePage > 0) {
            gui.setItem(45, GUIBuilder.createItem(Material.ARROW, ChatColor.YELLOW + "« Página Anterior (" + safePage + ")"));
        }

        gui.setItem(49, GUIBuilder.createItem(Material.BARRIER, ChatColor.RED + "« Volver al Menú Principal"));

        if (safePage < maxPages - 1) {
            gui.setItem(53, GUIBuilder.createItem(Material.ARROW, ChatColor.YELLOW + "Página Siguiente (" + (safePage + 2) + ") »"));
        }

        player.openInventory(gui);
    }
}
