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
 * BestiaryGUI — Bestiario interactivo de Jefes, Criaturas Míticas, Esbirros y Mobs Inteligentes.
 * Categorizado por Dificultad/Tiers con fichas informativas de 54 slots, drops con probabilidades y guías de invocación.
 */
public class BestiaryGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Bestiario de Criaturas & Jefes ✦";

    public enum BestiaryCategory {
        ALL("Todas las Criaturas", Material.BOOK),
        BOSSES("Jefes Supremos [Tier 5]", Material.NETHER_STAR),
        BLOOD_MOON("Luna de Sangre & Élite [Tier 3]", Material.REDSTONE),
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

    public static class BestiaryDrop {
        private final String name;
        private final Material icon;
        private final int chancePercent;
        private final String usage;

        public BestiaryDrop(String name, Material icon, int chancePercent, String usage) {
            this.name = name;
            this.icon = icon;
            this.chancePercent = chancePercent;
            this.usage = usage;
        }

        public String getName() { return name; }
        public Material getIcon() { return icon; }
        public int getChancePercent() { return chancePercent; }
        public String getUsage() { return usage; }
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
        private final String tierName;
        private final String originType;
        private final List<String> summonGuide;
        private final boolean isCustomPluginMob;
        private final List<BestiaryDrop> drops;

        // Constructor legacy
        public BestiaryEntry(String name, String subtitle, Material icon, double maxHealth,
                             double attackDamage, String difficultyStars, String location,
                             List<String> dropList, List<String> minionList, boolean isBoss) {
            this(name, subtitle, icon, maxHealth, attackDamage, difficultyStars, location,
                 dropList, minionList, isBoss,
                 isBoss ? "Tier 5: Jefe Supremo" : "Tier 2: Esbirro Elemental",
                 "Invocación / Mundo Abierto",
                 Arrays.asList("Se manifiesta en estructuras o mediante rituales en el mundo."),
                 true,
                 new ArrayList<>());
        }

        // Constructor completo
        public BestiaryEntry(String name, String subtitle, Material icon, double maxHealth,
                             double attackDamage, String difficultyStars, String location,
                             List<String> dropList, List<String> minionList, boolean isBoss,
                             String tierName, String originType, List<String> summonGuide,
                             boolean isCustomPluginMob, List<BestiaryDrop> drops) {
            this.name = name;
            this.subtitle = subtitle;
            this.icon = icon;
            this.maxHealth = maxHealth;
            this.attackDamage = attackDamage;
            this.difficultyStars = difficultyStars;
            this.location = location;
            this.dropList = dropList != null ? dropList : new ArrayList<>();
            this.minionList = minionList != null ? minionList : new ArrayList<>();
            this.isBoss = isBoss;
            this.tierName = tierName;
            this.originType = originType;
            this.summonGuide = summonGuide != null ? summonGuide : new ArrayList<>();
            this.isCustomPluginMob = isCustomPluginMob;
            this.drops = drops != null ? drops : new ArrayList<>();
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
        public String getTierName() { return tierName; }
        public String getOriginType() { return originType; }
        public List<String> getSummonGuide() { return summonGuide; }
        public boolean isCustomPluginMob() { return isCustomPluginMob; }
        public List<BestiaryDrop> getDrops() { return drops; }

        public ItemStack createItem() {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + subtitle);
            lore.add("");
            lore.add(ChatColor.DARK_AQUA + "🏷 " + ChatColor.BOLD + tierName);
            lore.add(ChatColor.YELLOW + (isCustomPluginMob ? "✨ Criatura de LivingTools" : "🌿 Vanilla Potenciado"));
            lore.add("");
            lore.add(ChatColor.RED + "❤ Salud: " + ChatColor.WHITE + (int) maxHealth + " HP");
            lore.add(ChatColor.DARK_RED + "⚔ Daño: " + ChatColor.WHITE + (int) attackDamage + " pts");
            lore.add(ChatColor.YELLOW + "⭐ Dificultad: " + difficultyStars);
            lore.add(ChatColor.AQUA + "🌍 Origen: " + ChatColor.WHITE + originType);
            lore.add("");
            lore.add(ChatColor.GOLD + "✦ Drops Principales:");
            if (!drops.isEmpty()) {
                for (BestiaryDrop d : drops) {
                    lore.add(ChatColor.WHITE + "  • " + d.getName() + ChatColor.GREEN + " [" + d.getChancePercent() + "%]");
                }
            } else {
                for (String drop : dropList) {
                    lore.add(ChatColor.WHITE + "  • " + drop);
                }
            }

            if (!minionList.isEmpty()) {
                lore.add("");
                lore.add(ChatColor.LIGHT_PURPLE + "✦ Esbirros:");
                for (String m : minionList) {
                    lore.add(ChatColor.GRAY + "  ► " + m);
                }
            }

            lore.add("");
            lore.add(ChatColor.YELLOW + "► Click para abrir ficha interactiva y cacería");

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
        // ---------------------------------------------------------------------
        // TIER 5: JEFES SUPREMOS
        // ---------------------------------------------------------------------

        // 1. The Smith
        ENTRIES.add(new BestiaryEntry(
                "The Smith (Living Boss)",
                "Avatar incandescente del fuego y la forja primordial.",
                Material.MAGMA_BLOCK, 500.0, 20.0,
                ChatColor.YELLOW + "★★★☆☆ Media",
                "Estructura Volcánica / Altar de Fuego (/lt admin spawnboss SMITH)",
                Arrays.asList("Núcleo de Magma [100%]", "Lingote de Netherite [60%]", "Bolsa de Botín Living [100%]"),
                Arrays.asList("Living Boss Minion (40 HP - Lanza bolas de fuego e inmola)"),
                true,
                "Tier 5: Jefe Supremo",
                "Altar de Fuego / Invocación Rúnica",
                Arrays.asList(
                        "Para invocar a The Smith, encuentra una Forja Volcánica",
                        "en el Nether o en el Reino Sagrado. Coloca un Bloque",
                        "de Magma coronado con una Estrella del Nether y haz",
                        "Shift + Click con una herramienta viviente despierta."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Núcleo de Magma", Material.MAGMA_CREAM, 100, "Material esencial para la Forja de Expansor y Armas del Vacío."),
                        new BestiaryDrop("Lingote de Netherite", Material.NETHERITE_INGOT, 60, "Refuerzo legendario para herramientas vivientes."),
                        new BestiaryDrop("Bolsa de Botín Living", Material.BUNDLE, 100, "Contiene gemas cósmicas, orbes y experiencia de almas.")
                )
        ));

        // 2. Serafín Celestial
        ENTRIES.add(new BestiaryEntry(
                "Serafín Celestial",
                "Entidad alada consagrada a la luz suprema del cielo.",
                Material.FEATHER, 750.0, 25.0,
                ChatColor.GOLD + "★★★★☆ Difícil",
                "Fortaleza Aérea / Cielo Y>200 (/lt admin spawnboss SERAPHIM)",
                Arrays.asList("Pluma de Serafín [100%]", "Polvo de Ángel [60%]", "Esencia Estelar [35%]"),
                Arrays.asList("Querubines Sagrados (30 HP - Proyectiles de Luz Divina)"),
                true,
                "Tier 5: Jefe Supremo",
                "Fortaleza Celeste (Y > 200)",
                Arrays.asList(
                        "Asciende por encima de Y=200 durante el día con buen clima.",
                        "Encuentra las plataformas de cuarzo flotantes y activa",
                        "el Beacon Divino con un Lingote de Oro y Pluma Rúnica."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Pluma de Serafín", Material.FEATHER, 100, "Otorga salto divino y planeo etéreo en armaduras vivientes."),
                        new BestiaryDrop("Polvo de Ángel", Material.SUGAR, 60, "Ingrediente para pociones de purificación y bendición solar."),
                        new BestiaryDrop("Esencia Estelar", Material.GLOWSTONE_DUST, 35, "Catalizador de ascensión al rango celestial.")
                )
        ));

        // 3. Titán de las Sombras
        ENTRIES.add(new BestiaryEntry(
                "Titán de las Sombras",
                "Coloso milenario moldeado con la energía oscura del vacío.",
                Material.OBSIDIAN, 1000.0, 30.0,
                ChatColor.DARK_PURPLE + "★★★★☆ Difícil",
                "Forja Maldita / Grieta del Vacío (/lt admin spawnboss TITAN)",
                Arrays.asList("Fragmento de Titán [100%]", "Escama del Vacío [75%]", "Esencia de Sombra [40%]"),
                Arrays.asList(
                        "Espectros del Vacío (45 HP - Ceguera y marchitez)",
                        "Parásitos del Vacío (50 HP - Absorbe durabilidad y vida)"
                ),
                true,
                "Tier 5: Jefe Supremo",
                "Grieta Abisal Subterránea",
                Arrays.asList(
                        "Desciende a las cuevas de Deepslate (Y < -40) durante",
                        "una Noche Corrupta. Rompe un Núcleo de Grieta Abisal",
                        "usando un pico viviente imbuido con poder oscuro."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Fragmento de Titán", Material.CRYING_OBSIDIAN, 100, "Base para forjar la Runa del Titán y armaduras pesadas."),
                        new BestiaryDrop("Escama del Vacío", Material.SCUTE, 75, "Permite elaborar Kits de Reparación Instantánea."),
                        new BestiaryDrop("Esencia de Sombra", Material.ECHO_SHARD, 40, "Desbloquea el talento Sendas de Sombra en armas.")
                )
        ));

        // 4. Dríada Corrupta
        ENTRIES.add(new BestiaryEntry(
                "Dríada Corrupta",
                "Espíritu vegetal ancestral enloquecido por la plaga.",
                Material.OAK_SAPLING, 800.0, 22.0,
                ChatColor.GREEN + "★★★☆☆ Media",
                "Arena Forestal (/lt admin spawnarena DRYAD)",
                Arrays.asList("Corazón del Bosque [100%]", "Relicario del Bosque [70%]", "Bastón del Bosque [30%]"),
                Arrays.asList("Brotes Vivientes & Ents (35 HP - Enraizamiento y veneno)"),
                true,
                "Tier 5: Jefe Elemental",
                "Santuario de Bosque Antiguo",
                Arrays.asList(
                        "Localiza un roble gigante en biomas de bosque oscuro.",
                        "Planta 4 brotes de roble oscuro rodeados de musgo sagrado",
                        "y vierte un balde de savia rúnica a medianoche."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Corazón del Bosque", Material.OAK_SAPLING, 100, "Crea la reliquia Relic of the Ancient Forest."),
                        new BestiaryDrop("Relicario Forjado", Material.CHEST, 70, "Contiene amuletos de regeneración pasiva de vida."),
                        new BestiaryDrop("Bastón del Bosque", Material.STICK, 30, "Arma mítica con capacidad de excavar en área 3x3.")
                )
        ));

        // 5. Wyrm de las Arenas
        ENTRIES.add(new BestiaryEntry(
                "Wyrm de las Arenas",
                "Sierpe gigantesca excavadora que domina el desierto árido.",
                Material.SAND, 1200.0, 28.0,
                ChatColor.GOLD + "★★★★☆ Difícil",
                "Arena Desértica (/lt admin spawnarena WYRM)",
                Arrays.asList("Núcleo de Arena [100%]", "Sello del Desierto [70%]", "Colmillo del Desierto [25%]"),
                Arrays.asList("Crías de Wyrm (40 HP - Torbellino de Arena y lentitud)"),
                true,
                "Tier 5: Jefe Elemental",
                "Templo del Desierto Olvidado",
                Arrays.asList(
                        "Encuentra la fosa de arena movediza en el desierto central.",
                        "Lanza 3 Cristales de Fósforo a la arena ardiente para",
                        "hacer emerger al coloso de las profundidades."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Núcleo de Arena", Material.SAND, 100, "Otorga inmunidad a la asfixia y arena movediza."),
                        new BestiaryDrop("Sello del Desierto", Material.GOLD_INGOT, 70, "Incrustación dorada para forjas de velocidad solar."),
                        new BestiaryDrop("Colmillo del Desierto", Material.RABBIT_FOOT, 25, "Material para la daga mítica Sand Fang.")
                )
        ));

        // 6. Gran Leviatán
        ENTRIES.add(new BestiaryEntry(
                "Gran Leviatán",
                "Monstruosidad de las profundidades oceánicas insondables.",
                Material.PRISMARINE_CRYSTALS, 1500.0, 32.0,
                ChatColor.DARK_AQUA + "★★★★☆ Difícil",
                "Arena Oceánica (/lt admin spawnarena LEVIATHAN)",
                Arrays.asList("Núcleo Abisal [100%]", "Ancla Abisal [25%]", "Fragmento de Prisma [60%]"),
                Arrays.asList("Guardianes Abisales (60 HP - Rayos Láser y ahogamiento)"),
                true,
                "Tier 5: Jefe Elemental",
                "Fosa Oceánica Abisal",
                Arrays.asList(
                        "Sumérgete en el océano profundo a una fosa abisal marina.",
                        "Coloca un conducto submarino y actívalo con un Corazón del Mar",
                        "rodeado de prismarina oscura para desafiar al Leviatán."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Núcleo Abisal", Material.PRISMARINE_CRYSTALS, 100, "Crea la reliquia del dominio acuático y respiración infinita."),
                        new BestiaryDrop("Ancla Abisal", Material.IRON_BLOCK, 25, "Arma pesada con daño demoledor en área acuática."),
                        new BestiaryDrop("Fragmento de Prisma", Material.PRISMARINE_SHARD, 60, "Encantamientos de proyectil oceánico perforante.")
                )
        ));

        // 7. Ender Dragon Potenciado
        ENTRIES.add(new BestiaryEntry(
                "Ender Dragon Ancestral",
                "Emperatriz del End fortalecida con ondas de energía viviente.",
                Material.DRAGON_HEAD, 1000.0, 26.0,
                ChatColor.LIGHT_PURPLE + "★★★★☆ Difícil",
                "Isla Central del End",
                Arrays.asList("Núcleo del Dragón [100%]", "Aliento de Dragón Purificado [80%]", "Huevo del Vacío [100%]"),
                Arrays.asList("Cristales de End Vivientes (Inmunidad mientras sigan activos)"),
                true,
                "Tier 5: Jefe Vanilla Potenciado",
                "Dimensión del End / Altar de Cristales",
                Arrays.asList(
                        "Revive al Dragón en la Isla Central del End colocando",
                        "4 Cristales de End sobre el portal de regreso.",
                        "La versión de LivingTools posee mayor velocidad y aliento flamígero."
                ),
                false,
                Arrays.asList(
                        new BestiaryDrop("Núcleo del Dragón", Material.DRAGON_EGG, 100, "Núcleo de energía máxima para armas de nivel mítico."),
                        new BestiaryDrop("Aliento Purificado", Material.DRAGON_BREATH, 80, "Poción cósmica para el despertar de armaduras vivientes.")
                )
        ));

        // 8. Wither Ancestral
        ENTRIES.add(new BestiaryEntry(
                "Wither Ancestral",
                "Terror de 3 cabezas reforzado con proyectiles de marchitez oscura.",
                Material.WITHER_SKELETON_SKULL, 900.0, 30.0,
                ChatColor.DARK_GRAY + "★★★★☆ Difícil",
                "Invocación en el Nether / Mundo Superior",
                Arrays.asList("Estrella del Nether Rúnica [100%]", "Rosa del Wither Eterna [50%]"),
                Arrays.asList("Esqueletos Wither Escoltas (Invoca 2 al 50% HP)"),
                true,
                "Tier 5: Jefe Vanilla Potenciado",
                "Ritual con 4 Arenas de Almas + 3 Calaveras Wither",
                Arrays.asList(
                        "Coloca 4 bloques de Arena de Almas en forma de T",
                        "y corona con 3 Calaveras de Esqueleto Wither.",
                        "En LivingTools activa un aura de escudo sónico al entrar en fase 2."
                ),
                false,
                Arrays.asList(
                        new BestiaryDrop("Estrella del Nether Rúnica", Material.NETHER_STAR, 100, "Esencia fundamental para forjar la Forja Rúnica de Almas."),
                        new BestiaryDrop("Rosa del Wither Eterna", Material.WITHER_ROSE, 50, "Aplica efecto pasivo de marchitez a los atacantes.")
                )
        ));

        // ---------------------------------------------------------------------
        // TIER 3: SMART MOBS & LUNA DE SANGRE (CRIATURAS INTELIGENTES)
        // ---------------------------------------------------------------------

        // 9. Esqueleto Gladiador (Smart Mob)
        ENTRIES.add(new BestiaryEntry(
                "Esqueleto Gladiador",
                "Arquero táctico que cambia a espada oscura con robo de vida si te acercas.",
                Material.SKELETON_SKULL, 60.0, 14.0,
                ChatColor.RED + "★★★☆☆ Élite Inteligente",
                "Luna de Sangre / Noche Corrupta / Cuevas Y < 0",
                Arrays.asList("Hueso Rúnico [100%]", "Arco del Gladiador [25%]", "Fragmento de Alma [40%]"),
                new ArrayList<>(),
                false,
                "Tier 3: Élite / Luna de Sangre",
                "Aparición Táctica en Eventos Sangrientos",
                Arrays.asList(
                        "Aparece de forma natural durante la Luna de Sangre",
                        "o en las profundidades de cuevas (Y < 0).",
                        "¡Cuidado! Si te acercas a menos de 4 bloques desenvaina una",
                        "espada oscura con 25% de robo de vida."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Hueso Rúnico", Material.BONE, 100, "Utilizado en ensamblaje de mangos reforzados para armas vivientes."),
                        new BestiaryDrop("Arco del Gladiador", Material.BOW, 25, "Arco con probabilidad de infligir sangrado al impactar."),
                        new BestiaryDrop("Fragmento de Alma", Material.AMETHYST_SHARD, 40, "Recarga la barra de energía Overdrive (Shift+F).")
                )
        ));

        // 10. Zombie Berserker (Smart Mob)
        ENTRIES.add(new BestiaryEntry(
                "Zombie Berserker",
                "No-muerto colérico que entra en furia al 50% HP y realiza saltos de embestida.",
                Material.ZOMBIE_HEAD, 80.0, 16.0,
                ChatColor.RED + "★★★☆☆ Élite Inteligente",
                "Luna de Sangre / Noche Corrupta / Cuevas Y < 0",
                Arrays.asList("Carne Corrupta Pura [100%]", "Corazón de Furia [30%]", "Pepita de Netherite [35%]"),
                new ArrayList<>(),
                false,
                "Tier 3: Élite / Luna de Sangre",
                "Aparición Natural en Luna de Sangre",
                Arrays.asList(
                        "Se manifiesta durante la noche bajo el resplandor rojo.",
                        "Al caer por debajo del 50% de su vida entra en cólera:",
                        "Gana Fuerza, Velocidad II y salta por el aire hacia el jugador."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Carne Corrupta Pura", Material.ROTTEN_FLESH, 100, "Purificable en la Mesa de Ensamblaje para abono rúnico."),
                        new BestiaryDrop("Corazón de Furia", Material.REDSTONE_BLOCK, 30, "Aumenta el daño de ataque crítico en armas vivientes."),
                        new BestiaryDrop("Pepita de Netherite", Material.NETHERITE_SCRAP, 35, "Recurso de forja y templado.")
                )
        ));

        // 11. Araña Neurotóxica (Smart Mob)
        ENTRIES.add(new BestiaryEntry(
                "Araña Neurotóxica",
                "Depredador arácnido que escupe telarañas temporales paralizantes y veneno.",
                Material.SPIDER_EYE, 70.0, 14.0,
                ChatColor.RED + "★★★☆☆ Élite Inteligente",
                "Luna de Sangre / Grietas del Reino",
                Arrays.asList("Glándula de Veneno [60%]", "Telaraña Rúnica [50%]", "Ojo Fermentado [35%]"),
                new ArrayList<>(),
                false,
                "Tier 3: Élite / Luna de Sangre",
                "Aparición Natural en Biomas Pantanosos / Luna de Sangre",
                Arrays.asList(
                        "Merodea en la oscuridad. Cuando tiene a un jugador en la mira,",
                        "escupe una telaraña efímera a sus pies que dura 3 segundos,",
                        "aplicando Veneno y Lentitud extrema para atraparlo."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Glándula de Veneno", Material.SPIDER_EYE, 60, "Permite destilar veneno paralizante para flechas vivientes."),
                        new BestiaryDrop("Telaraña Rúnica", Material.COBWEB, 50, "Crea trampas defensivas alrededor de territorios."),
                        new BestiaryDrop("Ojo Fermentado", Material.FERMENTED_SPIDER_EYE, 35, "Material de transmutación alquímica.")
                )
        ));

        // 12. Creeper de Pulso Sísmico (Smart Mob)
        ENTRIES.add(new BestiaryEntry(
                "Creeper de Pulso Sísmico",
                "Criatura inestable que emite una onda sónica anti-escudos antes de detonar.",
                Material.CREEPER_HEAD, 50.0, 25.0,
                ChatColor.DARK_RED + "★★★★☆ Élite Inteligente",
                "Luna de Sangre / Reino Viviente",
                Arrays.asList("Pólvora Concentrada [100%]", "Carga Sísmica [40%]", "Fragmento de Choque [25%]"),
                new ArrayList<>(),
                false,
                "Tier 3: Élite / Luna de Sangre",
                "Infiltración en Eventos del Mundo",
                Arrays.asList(
                        "Si intentas bloquear su explosión con escudo, emitirá un pulso",
                        "sónico a 5 bloques de distancia 1.5 segundos antes de detonar.",
                        "El pulso desactiva tu escudo por 3 segundos completos."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Pólvora Concentrada", Material.GUNPOWDER, 100, "Combustible de alta densidad para excavación explosiva."),
                        new BestiaryDrop("Carga Sísmica", Material.FIRE_CHARGE, 40, "Otorga efecto de onda expansiva al Martillo Sísmico."),
                        new BestiaryDrop("Fragmento de Choque", Material.ECHO_SHARD, 25, "Material para runas sónicas protectoras.")
                )
        ));

        // ---------------------------------------------------------------------
        // MEGA-RAID & ESBIRROS (TIER 4 & TIER 5+)
        // ---------------------------------------------------------------------

        // 13. Avatar del Génesis [Mega-Boss]
        ENTRIES.add(new BestiaryEntry(
                "El Avatar del Génesis [Mega-Boss]",
                "Deidad primigenia de 3 fases continuas: Magma, Escarcha y Vacío.",
                Material.NETHER_STAR, 3000.0, 35.0,
                ChatColor.RED + "★★★★★ MÍTICO / RAID",
                "Invocación Mítica de Raid (/lt genesis / /lt incursion)",
                Arrays.asList("Esencia de la Creación [100%]", "Corona del Génesis [50%]", "Gema del Génesis [35%]"),
                Arrays.asList("Cristales Rúnicos de Escarcha (150 HP - Escudo Inmune)"),
                true,
                "Tier 5+: Mega-Raid Boss",
                "Ritual del Génesis en el Reino",
                Arrays.asList(
                        "Reúne los 3 Núcleos Elementales (Magma, Escarcha y Vacío).",
                        "Dirígete al Altar Supremo del Reino Viviente y activa",
                        "la Incursión Génesis con el comando /lt genesis.",
                        "Requiere un equipo coordinado de cazadores armados."
                ),
                true,
                Arrays.asList(
                        new BestiaryDrop("Esencia de la Creación", Material.NETHER_STAR, 100, "Desbloquea la habilidad máxima de Ascensión Divina."),
                        new BestiaryDrop("Corona del Génesis", Material.GOLDEN_HELMET, 50, "Casco legendario con regeneración de vida infinita."),
                        new BestiaryDrop("Gema del Génesis", Material.EMERALD, 35, "Incrustación mística con +35% daño total a toda el arma.")
                )
        ));

        // 14. Living Boss Minion
        ENTRIES.add(new BestiaryEntry(
                "Living Boss Minion",
                "Esbirro volcánico invocado para defender a The Smith.",
                Material.MAGMA_CREAM, 40.0, 10.0,
                ChatColor.GRAY + "★☆☆☆☆ Esbirro",
                "Arena Volcánica / Invocación de The Smith",
                Arrays.asList("Magma Cream [100%]", "Pólvora de Fuego [50%]"),
                new ArrayList<>(),
                false,
                "Tier 2: Esbirro Elemental",
                "Invocación durante combate con The Smith",
                Arrays.asList("Aparece periódicamente en oleadas cuando The Smith baja de 75% HP."),
                true,
                Arrays.asList(
                        new BestiaryDrop("Magma Cream", Material.MAGMA_CREAM, 100, "Material de templado."),
                        new BestiaryDrop("Pólvora de Fuego", Material.BLAZE_POWDER, 50, "Pólvora ígnea.")
                )
        ));

        // 15. Querubín Sagrado
        ENTRIES.add(new BestiaryEntry(
                "Querubín Sagrado (Cherub)",
                "Invocación alada que lanza ráfagas de luz celestial.",
                Material.SUGAR, 30.0, 8.0,
                ChatColor.GRAY + "★☆☆☆☆ Esbirro",
                "Fortaleza Aérea / Invocación de Serafín",
                Arrays.asList("Polvo de Ángel [30%]", "Pepitas de Oro [60%]"),
                new ArrayList<>(),
                false,
                "Tier 2: Esbirro Celestial",
                "Invocación durante combate con Serafín Celestial",
                Arrays.asList("Vuela alrededor de la arena disparando rayos solares bendecidos."),
                true,
                Arrays.asList(
                        new BestiaryDrop("Polvo de Ángel", Material.SUGAR, 30, "Polvo curativo."),
                        new BestiaryDrop("Pepita de Oro", Material.GOLD_NUGGET, 60, "Oro sagrado.")
                )
        ));

        // 16. Cristal Rúnico de Escarcha
        ENTRIES.add(new BestiaryEntry(
                "Cristal Rúnico de Escarcha",
                "Tótem gélido que otorga invulnerabilidad absoluta al Avatar del Génesis.",
                Material.PACKED_ICE, 150.0, 0.0,
                ChatColor.AQUA + "★★★☆☆ Tótem Raid",
                "Fase 2 del Avatar del Génesis",
                Arrays.asList("Fragmento de Hielo Ancestral [100%]"),
                new ArrayList<>(),
                false,
                "Tier 4: Tótem de Raid",
                "Manifestación en Fase 2 de Mega-Raid Génesis",
                Arrays.asList("Debe ser destruido con picos vivientes para quebrar la barrera del Avatar."),
                true,
                Arrays.asList(
                        new BestiaryDrop("Hielo Ancestral", Material.PACKED_ICE, 100, "Núcleo de congelación absoluta.")
                )
        ));
    }

    public static void open(Player player, int page, BestiaryCategory category) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);
        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Fila Superior: Filtros de Categoría
        gui.setItem(1, GUIBuilder.createGlowingItem(
                Material.BOOK,
                (category == BestiaryCategory.ALL ? ChatColor.GREEN + "▶ " : "") + ChatColor.GOLD + "✦ Todas",
                "",
                ChatColor.GRAY + "Ver catálogo completo de criaturas.",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        gui.setItem(2, GUIBuilder.createGlowingItem(
                Material.NETHER_STAR,
                (category == BestiaryCategory.BOSSES ? ChatColor.GREEN + "▶ " : "") + ChatColor.RED + "👑 Jefes [Tier 5]",
                "",
                ChatColor.GRAY + "The Smith, Serafín, Titán, Dríada, Wyrm...",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        gui.setItem(3, GUIBuilder.createGlowingItem(
                Material.REDSTONE,
                (category == BestiaryCategory.BLOOD_MOON ? ChatColor.GREEN + "▶ " : "") + ChatColor.DARK_RED + "🩸 Élite & Luna [Tier 3]",
                "",
                ChatColor.GRAY + "Esqueleto Gladiador, Zombie Berserker...",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        // Header Central (Slot 4)
        gui.setItem(4, GUIBuilder.createGlowingItem(Material.WRITABLE_BOOK,
                ChatColor.GOLD + "✦ Bestiario Mítico de Criaturas ✦",
                ChatColor.GRAY + "Categoría: " + ChatColor.LIGHT_PURPLE + category.getDisplayName(),
                ChatColor.GRAY + "Página: " + ChatColor.YELLOW + (page + 1),
                "",
                ChatColor.AQUA + "Consulta atributos, tácticas, esbirros,",
                ChatColor.AQUA + "método de invocación y cuadrícula de botín."));

        gui.setItem(5, GUIBuilder.createGlowingItem(
                Material.ZOMBIE_HEAD,
                (category == BestiaryCategory.MINIONS ? ChatColor.GREEN + "▶ " : "") + ChatColor.DARK_PURPLE + "👾 Esbirros",
                "",
                ChatColor.GRAY + "Invocaciones de combate secundarias.",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        gui.setItem(7, GUIBuilder.createGlowingItem(
                Material.DRAGON_EGG,
                (category == BestiaryCategory.RAID ? ChatColor.GREEN + "▶ " : "") + ChatColor.LIGHT_PURPLE + "🐉 Mega-Raid",
                "",
                ChatColor.GRAY + "Avatar del Génesis & Tótems.",
                "",
                ChatColor.YELLOW + "► Click para filtrar"));

        // Filtrar entradas
        List<BestiaryEntry> filtered = new ArrayList<>();
        for (BestiaryEntry e : ENTRIES) {
            if (category == BestiaryCategory.ALL) {
                filtered.add(e);
            } else if (category == BestiaryCategory.BOSSES && e.isBoss && !e.getName().contains("Génesis")) {
                filtered.add(e);
            } else if (category == BestiaryCategory.BLOOD_MOON && (e.getTierName().contains("Tier 3") || e.getLocation().contains("Luna"))) {
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
