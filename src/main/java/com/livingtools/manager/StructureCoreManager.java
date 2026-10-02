package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StructureCoreManager — Administrador central de Núcleos de Estructura Desplegables,
 * Persistencia, Auto-Desmantelamiento en Reversa y Recuperación de Núcleos (Refund).
 */
public class StructureCoreManager {

    private static NamespacedKey KEY_STRUCTURE_CORE;

    public static NamespacedKey getKeyStructureCore() {
        if (KEY_STRUCTURE_CORE == null) {
            KEY_STRUCTURE_CORE = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_structure_core");
        }
        return KEY_STRUCTURE_CORE;
    }

    public enum StructureType {
        ASSEMBLY_TABLE(
                "Mesa de Ensamblaje",
                ChatColor.GREEN,
                Material.CRAFTING_TABLE,
                "Estructura básica para forjar herramientas y armaduras vivientes.",
                "Básica (Hierro y Madera)"
        ),
        SOUL_PEDESTAL(
                "Pedestal de Almas 3D",
                ChatColor.AQUA,
                Material.END_PORTAL_FRAME,
                "Pedestal de exhibición que otorga XP de Descanso continua.",
                "Fácil (Piedra y Amatista)"
        ),
        RUNE_FORGE(
                "Forja Rúnica",
                ChatColor.LIGHT_PURPLE,
                Material.AMETHYST_CLUSTER,
                "Altar de amatista para imbuir runas elementales y súper sinergias.",
                "Media (Amatista y Oro)"
        ),
        RHYTHMIC_FORGE(
                "Forja Rítmica de Almas",
                ChatColor.DARK_AQUA,
                Material.LODESTONE,
                "Forja de precisión para reparar al 100% y templar con Obra Maestra.",
                "Media (Fogata de Almas y Blackstone)"
        ),
        CURSED_FORGE(
                "Forja Maldita",
                ChatColor.DARK_RED,
                Material.CRYING_OBSIDIAN,
                "Forja oscura de almas para transferir maldiciones y forjas arcanas.",
                "Media-Alta (Obsidiana y Velas Rojas)"
        ),
        SOUL_FUSION_CRUCIBLE(
                "Crisol de Fusión de Almas",
                ChatColor.GOLD,
                Material.CONDUIT,
                "Crisol para fusionar dos armas vivientes combinando niveles y habilidades.",
                "Alta (Obsidiana Llorosa y Diamantes)"
        ),
        BOSS_FORGE(
                "Forja de Jefes (Cruz)",
                ChatColor.RED,
                Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                "Forja monumental 5x3 para craftear reliquias y armamento supremo.",
                "Alta (Magma Core y Blackstone Pulida)"
        ),
        RITUAL_ALTAR(
                "Gran Altar Celestial",
                ChatColor.YELLOW,
                Material.BEACON,
                "Altar cósmico para rituales de ascensión divina y encantamientos mayores.",
                "Mítica (Mesa de Encantamiento y Cuarzo)"
        );

        private final String displayName;
        private final ChatColor color;
        private final Material icon;
        private final String description;
        private final String difficulty;

        StructureType(String displayName, ChatColor color, Material icon, String description, String difficulty) {
            this.displayName = displayName;
            this.color = color;
            this.icon = icon;
            this.description = description;
            this.difficulty = difficulty;
        }

        public String getDisplayName() { return displayName; }
        public ChatColor getColor() { return color; }
        public Material getIcon() { return icon; }
        public String getDescription() { return description; }
        public String getDifficulty() { return difficulty; }
        public String getFormattedName() { return color + "" + ChatColor.BOLD + "✦ " + displayName + " ✦"; }
    }

    /**
     * Modelo de Estructura Desplegada en el mundo.
     */
    public static class DeployedStructure {
        private final UUID id;
        private final StructureType type;
        private final UUID ownerUUID;
        private final String ownerName;
        private final Location centerLocation;
        private final Set<Location> blocks = new HashSet<>();
        private final long deployedAt;

        public DeployedStructure(UUID id, StructureType type, UUID ownerUUID, String ownerName,
                                 Location centerLocation, Set<Location> blocks, long deployedAt) {
            this.id = id;
            this.type = type;
            this.ownerUUID = ownerUUID;
            this.ownerName = ownerName != null ? ownerName : "Desconocido";
            this.centerLocation = centerLocation.getBlock().getLocation();
            if (blocks != null) {
                for (Location loc : blocks) {
                    this.blocks.add(loc.getBlock().getLocation());
                }
            }
            this.deployedAt = deployedAt;
        }

        public UUID getId() { return id; }
        public StructureType getType() { return type; }
        public UUID getOwnerUUID() { return ownerUUID; }
        public String getOwnerName() { return ownerName; }
        public Location getCenterLocation() { return centerLocation; }
        public Set<Location> getBlocks() { return blocks; }
        public long getDeployedAt() { return deployedAt; }

        public boolean containsBlock(Location loc) {
            if (loc == null || loc.getWorld() == null || centerLocation.getWorld() == null) return false;
            if (!loc.getWorld().equals(centerLocation.getWorld())) return false;
            return blocks.contains(loc.getBlock().getLocation());
        }
    }

    // Almacenamiento en memoria para acceso O(1)
    private static final Map<Location, DeployedStructure> blockToStructureMap = new ConcurrentHashMap<>();
    private static final Map<UUID, DeployedStructure> activeStructures = new ConcurrentHashMap<>();

    private static File structuresFile;
    private static FileConfiguration structuresConfig;

    /**
     * Inicializa el gestor de estructuras y carga las estructuras activas desde structures.yml.
     */
    public static void init(LivingToolsPlugin plugin) {
        structuresFile = new File(plugin.getDataFolder(), "structures.yml");
        if (!structuresFile.exists()) {
            try {
                structuresFile.getParentFile().mkdirs();
                structuresFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("No se pudo crear structures.yml: " + e.getMessage());
            }
        }
        structuresConfig = YamlConfiguration.loadConfiguration(structuresFile);
        loadStructures();
        registerBukkitRecipes();
    }

    /**
     * Carga las estructuras desplegadas guardadas.
     */
    public static synchronized void loadStructures() {
        blockToStructureMap.clear();
        activeStructures.clear();

        if (structuresConfig == null) return;
        ConfigurationSection section = structuresConfig.getConfigurationSection("structures");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String typeName = section.getString(key + ".type");
                StructureType type = StructureType.valueOf(typeName);

                String ownerUUIDStr = section.getString(key + ".owner-uuid");
                UUID ownerUUID = ownerUUIDStr != null ? UUID.fromString(ownerUUIDStr) : null;
                String ownerName = section.getString(key + ".owner-name", "Desconocido");

                String worldName = section.getString(key + ".world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                int x = section.getInt(key + ".x");
                int y = section.getInt(key + ".y");
                int z = section.getInt(key + ".z");
                Location center = new Location(world, x, y, z);
                long deployedAt = section.getLong(key + ".deployed-at", System.currentTimeMillis());

                Set<Location> blocks = calculateStructureBlocks(center, type);
                DeployedStructure struct = new DeployedStructure(id, type, ownerUUID, ownerName, center, blocks, deployedAt);

                activeStructures.put(id, struct);
                for (Location bLoc : blocks) {
                    blockToStructureMap.put(bLoc, struct);
                }
            } catch (Exception e) {
                LivingToolsPlugin.getInstance().getLogger().warning("Error cargando estructura guardada (" + key + "): " + e.getMessage());
            }
        }
        LivingToolsPlugin.getInstance().getLogger().info("Estructuras desplegadas cargadas: " + activeStructures.size());
    }

    /**
     * Guarda todas las estructuras activas en structures.yml.
     */
    public static synchronized void saveStructures() {
        if (structuresFile == null) return;
        structuresConfig = new YamlConfiguration();
        ConfigurationSection section = structuresConfig.createSection("structures");

        for (DeployedStructure struct : activeStructures.values()) {
            String key = struct.getId().toString();
            section.set(key + ".type", struct.getType().name());
            section.set(key + ".owner-uuid", struct.getOwnerUUID() != null ? struct.getOwnerUUID().toString() : "");
            section.set(key + ".owner-name", struct.getOwnerName());
            section.set(key + ".world", struct.getCenterLocation().getWorld().getName());
            section.set(key + ".x", struct.getCenterLocation().getBlockX());
            section.set(key + ".y", struct.getCenterLocation().getBlockY());
            section.set(key + ".z", struct.getCenterLocation().getBlockZ());
            section.set(key + ".deployed-at", struct.getDeployedAt());
        }

        try {
            structuresConfig.save(structuresFile);
        } catch (IOException e) {
            LivingToolsPlugin.getInstance().getLogger().warning("Error al guardar structures.yml: " + e.getMessage());
        }
    }

    /**
     * Devuelve la estructura desplegada que contiene el bloque indicado, o null.
     */
    public static DeployedStructure getStructureAt(Location loc) {
        if (loc == null) return null;
        return blockToStructureMap.get(loc.getBlock().getLocation());
    }

    /**
     * Comprueba si un bloque pertenece a una estructura desplegada activa.
     */
    public static boolean isStructureBlock(Location loc) {
        return getStructureAt(loc) != null;
    }

    public static Collection<DeployedStructure> getActiveStructures() {
        return Collections.unmodifiableCollection(activeStructures.values());
    }

    /**
     * Crea el ítem del Núcleo Desplegable correspondiente.
     */
    public static ItemStack createCoreItem(StructureType type) {
        ItemStack item = GUIBuilder.createGlowingItem(
                type.getIcon(),
                type.getColor() + "" + ChatColor.BOLD + "✦ Núcleo de " + type.getDisplayName() + " ✦",
                "",
                ChatColor.GRAY + type.getDescription(),
                "",
                ChatColor.YELLOW + "✦ Dificultad de Crafteo: " + ChatColor.WHITE + type.getDifficulty(),
                ChatColor.AQUA + "✦ Click Derecho en el suelo:" + ChatColor.WHITE + " Auto-construye la estructura.",
                ChatColor.GREEN + "✦ Romper 1 bloque / Shift+Click:" + ChatColor.WHITE + " Desmantela y devuelve el Núcleo.",
                ChatColor.LIGHT_PURPLE + "✦ Sistema:" + ChatColor.GRAY + " Estructuras Desplegables LivingTools"
        );

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getKeyStructureCore(), PersistentDataType.STRING, type.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Detecta si un ítem es un Núcleo de Estructura Desplegable.
     */
    public static StructureType getStructureType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String raw = pdc.get(getKeyStructureCore(), PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return StructureType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Calcula la lista exacta de bloques que ocupará una estructura dada su posición central.
     */
    public static Set<Location> calculateStructureBlocks(Location center, StructureType type) {
        Set<Location> blocks = new HashSet<>();
        World world = center.getWorld();
        if (world == null) return blocks;

        Location origin = center.getBlock().getLocation();
        Location baseCenter = origin.clone().add(0, -1, 0);

        switch (type) {
            case ASSEMBLY_TABLE:
            case SOUL_PEDESTAL:
                blocks.add(baseCenter);
                blocks.add(origin);
                break;

            case RUNE_FORGE:
            case RHYTHMIC_FORGE:
            case SOUL_FUSION_CRUCIBLE:
                // Base 3x3 en y = -1
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(baseCenter.clone().add(x, 0, z));
                    }
                }
                // Centro y = 0
                blocks.add(origin);
                // 4 Esquinas en y = 0 (velas, linternas o antorchas)
                int[][] corners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : corners) {
                    blocks.add(origin.clone().add(c[0], 0, c[1]));
                }
                break;

            case CURSED_FORGE:
                // Base 3x3 en y = -1
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(baseCenter.clone().add(x, 0, z));
                    }
                }
                // Centro y = 0
                blocks.add(origin);
                // 4 Esquinas en y = 0 (crying obsidian)
                int[][] cursedMid = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : cursedMid) {
                    blocks.add(origin.clone().add(c[0], 0, c[1]));
                    // 4 Velas en y = 1
                    blocks.add(origin.clone().add(c[0], 1, c[1]));
                }
                break;

            case BOSS_FORGE:
                // Base 5x3 en y = -1
                for (int x = -2; x <= 2; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(baseCenter.clone().add(x, 0, z));
                    }
                }
                // Centro y = 0
                blocks.add(origin);
                break;

            case RITUAL_ALTAR:
                // Base 5x5 en y = -1
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        blocks.add(baseCenter.clone().add(x, 0, z));
                    }
                }
                // Centro y = 0
                blocks.add(origin);
                // Cruz de Redstone en y = 0
                blocks.add(origin.clone().add(1, 0, 0));
                blocks.add(origin.clone().add(-1, 0, 0));
                blocks.add(origin.clone().add(0, 0, 1));
                blocks.add(origin.clone().add(0, 0, -1));
                // 4 Esquinas con Velas en y = 0
                int[][] ritualCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : ritualCorners) {
                    blocks.add(origin.clone().add(c[0], 0, c[1]));
                }
                break;
        }

        return blocks;
    }

    /**
     * Comprueba si el área requerida está despejada y es válida para colocar la estructura.
     */
    public static boolean canDeployAt(Location center, StructureType type, Player player) {
        Set<Location> requiredBlocks = calculateStructureBlocks(center, type);

        for (Location loc : requiredBlocks) {
            Block block = loc.getBlock();
            // Comprobar si solapa con otra estructura viva
            if (isStructureBlock(loc)) {
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "❌ No puedes desplegar la estructura aquí: solapa con otra estructura activa.");
                }
                return false;
            }
            // Comprobar si hay bloques irrompibles como Bedrock o Portales
            Material mat = block.getType();
            if (mat == Material.BEDROCK || mat == Material.BARRIER || mat == Material.END_PORTAL || mat == Material.END_PORTAL_FRAME) {
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "❌ No puedes desplegar la estructura sobre bloques protegidos/indestructibles.");
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Despliega la estructura con animación cinemática ascendente y la registra en el sistema.
     */
    public static void deployStructure(Player player, Location targetLoc, StructureType type) {
        World world = targetLoc.getWorld();
        if (world == null) return;

        Location center = targetLoc.getBlock().getLocation();

        // 1. Calcular y registrar la estructura
        Set<Location> blocks = calculateStructureBlocks(center, type);
        UUID structureId = UUID.randomUUID();
        UUID ownerUUID = player != null ? player.getUniqueId() : null;
        String ownerName = player != null ? player.getName() : "Consola";

        DeployedStructure struct = new DeployedStructure(structureId, type, ownerUUID, ownerName, center, blocks, System.currentTimeMillis());
        activeStructures.put(structureId, struct);
        for (Location bLoc : blocks) {
            blockToStructureMap.put(bLoc, struct);
        }
        saveStructures();

        // 2. Banner informativo
        if (player != null) {
            player.sendMessage("");
            player.sendMessage(type.getColor() + "╔════════════════════════════════════════════════════════════╗");
            player.sendMessage(ChatColor.YELLOW + "  🏗 ¡DESPLEGANDO ESTRUCTURA MÍSTICA!");
            player.sendMessage(ChatColor.WHITE + "  Erigiendo: " + type.getFormattedName());
            player.sendMessage(ChatColor.GRAY + "  " + type.getDescription());
            player.sendMessage(ChatColor.AQUA + "  💡 Rompe 1 bloque o haz Shift+Click para recuperar tu Núcleo.");
            player.sendMessage(type.getColor() + "╚════════════════════════════════════════════════════════════╝");
            player.sendMessage("");
        }

        // Sonido inicial de activación
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 1.2f);
        world.spawnParticle(Particle.FLASH, center.clone().add(0.5, 1.0, 0.5), 3);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                switch (step) {
                    case 0:
                        // Capa 0: Base / Suelo
                        buildLayerBase(world, center, type);
                        world.playSound(center, Sound.BLOCK_STONE_PLACE, 1.0f, 0.9f);
                        world.spawnParticle(Particle.SMOKE_LARGE, center.clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.2, 0.5, 0.05);
                        break;
                    case 1:
                        // Capa 1: Mesa central y bloques funcionales
                        buildLayerMiddle(world, center, type);
                        world.playSound(center, Sound.BLOCK_ANVIL_PLACE, 1.2f, 1.1f);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0.5, 1.0, 0.5), 25, 0.4, 0.4, 0.4, 0.05);
                        break;
                    case 2:
                        // Capa 2: Velas, linternas, antorchas y detalles
                        buildLayerTop(world, center, type);
                        playFinalEffects(world, center, type, player);
                        cancel();
                        return;
                }
                step++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 2L, 4L);
    }

    /**
     * Desmantela completamente una estructura desplegada, limpia todos sus bloques a AIR
     * y le devuelve el Núcleo intacto al jugador (o lo suelta en el suelo si no hay jugador).
     *
     * @param struct Estructura a desmantelar.
     * @param player Jugador que ejecuta el desmantelamiento (puede ser null).
     * @param refundCore Si es true, entrega/devuelve el Núcleo de la estructura.
     * @param animated Si es true, ejecuta sonidos y partículas de desmontaje.
     */
    public static synchronized void dismantleStructure(DeployedStructure struct, Player player, boolean refundCore, boolean animated) {
        if (struct == null) return;

        Location center = struct.getCenterLocation();
        World world = center.getWorld();

        if (world != null && animated) {
            world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.2f, 1.2f);
            world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.2f, 1.4f);
            world.spawnParticle(Particle.FLASH, center.clone().add(0.5, 1.0, 0.5), 2);
            world.spawnParticle(Particle.SMOKE_LARGE, center.clone().add(0.5, 0.5, 0.5), 35, 0.8, 0.8, 0.8, 0.08);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0.5, 0.8, 0.5), 25, 0.6, 0.6, 0.6, 0.05);
        }

        // 1. Limpiar todos los bloques de la estructura dejándolos como AIR
        if (world != null) {
            for (Location bLoc : struct.getBlocks()) {
                Block b = bLoc.getBlock();
                b.setType(Material.AIR);
            }
        }

        // 2. Reembolsar el Núcleo
        if (refundCore) {
            ItemStack core = createCoreItem(struct.getType());
            if (player != null && player.isOnline()) {
                Map<Integer, ItemStack> overflow = player.getInventory().addItem(core);
                if (!overflow.isEmpty() && world != null) {
                    world.dropItemNaturally(center.clone().add(0.5, 0.5, 0.5), core);
                }

                player.sendMessage("");
                player.sendMessage(struct.getType().getColor() + "╔════════════════════════════════════════════════════════════╗");
                player.sendMessage(ChatColor.GREEN + "  ✔ " + ChatColor.BOLD + "¡ESTRUCTURA DESMANTELADA LIMPIAMENTE!");
                player.sendMessage(ChatColor.WHITE + "  Estructura: " + struct.getType().getFormattedName());
                player.sendMessage(ChatColor.YELLOW + "  ✦ Se ha devuelto el Núcleo a tu inventario para volver a colocarlo.");
                player.sendMessage(struct.getType().getColor() + "╚════════════════════════════════════════════════════════════╝");
                player.sendMessage("");

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.4f);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.8f);

                try {
                    net.md_5.bungee.api.chat.TextComponent tc = new net.md_5.bungee.api.chat.TextComponent(
                            ChatColor.GREEN + "✔ Núcleo de " + struct.getType().getDisplayName() + " recuperado.");
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, tc);
                } catch (Throwable ignored) {}
            } else if (world != null) {
                world.dropItemNaturally(center.clone().add(0.5, 0.5, 0.5), core);
            }
        }

        // 3. Desregistrar de memoria y persistencia
        activeStructures.remove(struct.getId());
        for (Location bLoc : struct.getBlocks()) {
            blockToStructureMap.remove(bLoc);
        }
        saveStructures();
    }

    /**
     * Desmantela la estructura presente en la ubicación especificada.
     */
    public static boolean dismantleAt(Location loc, Player player, boolean refundCore) {
        DeployedStructure struct = getStructureAt(loc);
        if (struct == null) return false;
        dismantleStructure(struct, player, refundCore, true);
        return true;
    }

    private static void buildLayerBase(World world, Location center, StructureType type) {
        Location base = center.clone().add(0, -1, 0);

        switch (type) {
            case ASSEMBLY_TABLE:
                base.getBlock().setType(Material.IRON_BLOCK);
                break;
            case SOUL_PEDESTAL:
                base.getBlock().setType(Material.POLISHED_DEEPSLATE);
                break;
            case RUNE_FORGE:
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        base.clone().add(x, 0, z).getBlock().setType(Material.AMETHYST_BLOCK);
                    }
                }
                break;
            case RHYTHMIC_FORGE:
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block b = base.clone().add(x, 0, z).getBlock();
                        boolean isCorner = Math.abs(x) == 1 && Math.abs(z) == 1;
                        boolean isCenter = (x == 0 && z == 0);
                        b.setType(isCenter ? Material.SOUL_CAMPFIRE : (isCorner ? Material.CRYING_OBSIDIAN : Material.POLISHED_BLACKSTONE_BRICKS));
                    }
                }
                break;
            case CURSED_FORGE:
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        base.clone().add(x, 0, z).getBlock().setType(Material.POLISHED_BLACKSTONE);
                    }
                }
                break;
            case SOUL_FUSION_CRUCIBLE:
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block b = base.clone().add(x, 0, z).getBlock();
                        boolean isCorner = Math.abs(x) == 1 && Math.abs(z) == 1;
                        b.setType(isCorner ? Material.NETHERITE_BLOCK : Material.CRYING_OBSIDIAN);
                    }
                }
                break;
            case BOSS_FORGE:
                for (int x = -2; x <= 2; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block b = base.clone().add(x, 0, z).getBlock();
                        boolean isCorner = Math.abs(x) == 2 && Math.abs(z) == 1;
                        b.setType(isCorner ? Material.MAGMA_BLOCK : Material.POLISHED_BLACKSTONE_BRICKS);
                    }
                }
                break;
            case RITUAL_ALTAR:
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        Block b = base.clone().add(x, 0, z).getBlock();
                        boolean isCorner = Math.abs(x) == 2 && Math.abs(z) == 2;
                        b.setType(isCorner ? Material.SEA_LANTERN : Material.QUARTZ_BLOCK);
                    }
                }
                break;
        }
    }

    private static void buildLayerMiddle(World world, Location center, StructureType type) {
        switch (type) {
            case ASSEMBLY_TABLE:
                center.getBlock().setType(Material.SMITHING_TABLE);
                break;
            case SOUL_PEDESTAL:
                center.getBlock().setType(Material.LODESTONE);
                break;
            case RUNE_FORGE:
                center.getBlock().setType(Material.SMITHING_TABLE);
                break;
            case RHYTHMIC_FORGE:
                center.getBlock().setType(Material.ANVIL);
                break;
            case CURSED_FORGE:
                center.getBlock().setType(Material.ANVIL);
                int[][] cursedCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : cursedCorners) {
                    center.clone().add(c[0], 0, c[1]).getBlock().setType(Material.CRYING_OBSIDIAN);
                }
                break;
            case SOUL_FUSION_CRUCIBLE:
                center.getBlock().setType(Material.LODESTONE);
                break;
            case BOSS_FORGE:
                center.getBlock().setType(Material.SMITHING_TABLE);
                break;
            case RITUAL_ALTAR:
                center.getBlock().setType(Material.ENCHANTING_TABLE);
                // Cruz de Redstone
                center.clone().add(1, 0, 0).getBlock().setType(Material.REDSTONE_WIRE);
                center.clone().add(-1, 0, 0).getBlock().setType(Material.REDSTONE_WIRE);
                center.clone().add(0, 0, 1).getBlock().setType(Material.REDSTONE_WIRE);
                center.clone().add(0, 0, -1).getBlock().setType(Material.REDSTONE_WIRE);
                break;
        }
    }

    private static void buildLayerTop(World world, Location center, StructureType type) {
        switch (type) {
            case ASSEMBLY_TABLE:
            case SOUL_PEDESTAL:
                break;
            case RUNE_FORGE:
                int[][] runeCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : runeCorners) {
                    Block b = center.clone().add(c[0], 0, c[1]).getBlock();
                    b.setType(Material.PURPLE_CANDLE);
                    if (b.getBlockData() instanceof Candle) {
                        Candle candle = (Candle) b.getBlockData();
                        candle.setLit(true);
                        b.setBlockData(candle);
                    }
                }
                break;
            case RHYTHMIC_FORGE:
                int[][] soulCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : soulCorners) {
                    center.clone().add(c[0], 0, c[1]).getBlock().setType(Material.SOUL_LANTERN);
                }
                break;
            case CURSED_FORGE:
                int[][] cursedCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : cursedCorners) {
                    Block b = center.clone().add(c[0], 1, c[1]).getBlock();
                    b.setType(Material.RED_CANDLE);
                    if (b.getBlockData() instanceof Candle) {
                        Candle candle = (Candle) b.getBlockData();
                        candle.setLit(true);
                        b.setBlockData(candle);
                    }
                }
                break;
            case SOUL_FUSION_CRUCIBLE:
                int[][] fusionCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : fusionCorners) {
                    center.clone().add(c[0], 0, c[1]).getBlock().setType(Material.SOUL_TORCH);
                }
                break;
            case BOSS_FORGE:
                break;
            case RITUAL_ALTAR:
                int[][] ritualCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int[] c : ritualCorners) {
                    Block b = center.clone().add(c[0], 0, c[1]).getBlock();
                    b.setType(Material.WHITE_CANDLE);
                    if (b.getBlockData() instanceof Candle) {
                        Candle candle = (Candle) b.getBlockData();
                        candle.setLit(true);
                        b.setBlockData(candle);
                    }
                }
                break;
        }
    }

    private static void playFinalEffects(World world, Location center, StructureType type, Player player) {
        Location top = center.clone().add(0.5, 1.2, 0.5);

        world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 1.2f);
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.5f, 1.4f);

        switch (type) {
            case ASSEMBLY_TABLE:
                world.spawnParticle(Particle.VILLAGER_HAPPY, top, 30, 0.5, 0.5, 0.5, 0.1);
                break;
            case RUNE_FORGE:
                world.spawnParticle(Particle.END_ROD, top, 40, 0.6, 0.6, 0.6, 0.1);
                break;
            case RHYTHMIC_FORGE:
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, top, 50, 0.7, 0.7, 0.7, 0.08);
                break;
            case CURSED_FORGE:
                world.spawnParticle(Particle.PORTAL, top, 60, 0.8, 0.8, 0.8, 0.2);
                break;
            case SOUL_FUSION_CRUCIBLE:
                world.spawnParticle(Particle.TOTEM, top, 50, 0.6, 0.8, 0.6, 0.2);
                break;
            case BOSS_FORGE:
                world.spawnParticle(Particle.LAVA, top, 35, 0.8, 0.5, 0.8, 0.05);
                break;
            case RITUAL_ALTAR:
                world.spawnParticle(Particle.TOTEM, top, 100, 1.0, 1.5, 1.0, 0.3);
                world.strikeLightningEffect(top);
                break;
            default:
                world.spawnParticle(Particle.CRIT_MAGIC, top, 30, 0.5, 0.5, 0.5, 0.1);
                break;
        }

        if (player != null) {
            player.sendTitle(type.getColor() + type.getDisplayName(),
                    ChatColor.YELLOW + "¡Estructura lista para usarse!", 10, 40, 15);
        }
    }

    /**
     * Registra las recetas de crafteo de todos los núcleos en Bukkit de forma segura.
     */
    public static void registerBukkitRecipes() {
        // 1. Núcleo de Mesa de Ensamblaje (Fácil)
        try {
            NamespacedKey key1 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_assembly_table");
            Bukkit.removeRecipe(key1);
            ShapedRecipe r1 = new ShapedRecipe(key1, createCoreItem(StructureType.ASSEMBLY_TABLE));
            r1.shape(" I ", "ICI", " S ");
            r1.setIngredient('I', Material.IRON_INGOT);
            r1.setIngredient('C', Material.CRAFTING_TABLE);
            r1.setIngredient('S', Material.SMOOTH_STONE);
            Bukkit.addRecipe(r1);
        } catch (Exception ignored) {}

        // 2. Núcleo de Pedestal de Almas (Fácil)
        try {
            NamespacedKey key2 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_soul_pedestal");
            Bukkit.removeRecipe(key2);
            ShapedRecipe r2 = new ShapedRecipe(key2, createCoreItem(StructureType.SOUL_PEDESTAL));
            r2.shape(" A ", "SLS", "SSS");
            r2.setIngredient('A', Material.AMETHYST_SHARD);
            r2.setIngredient('S', Material.SMOOTH_STONE_SLAB);
            r2.setIngredient('L', Material.LODESTONE);
            Bukkit.addRecipe(r2);
        } catch (Exception ignored) {}

        // 3. Núcleo de Forja Rúnica (Media)
        try {
            NamespacedKey key3 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_rune_forge");
            Bukkit.removeRecipe(key3);
            ShapedRecipe r3 = new ShapedRecipe(key3, createCoreItem(StructureType.RUNE_FORGE));
            r3.shape("PAP", "ASA", " G ");
            r3.setIngredient('P', Material.PURPLE_CANDLE);
            r3.setIngredient('A', Material.AMETHYST_BLOCK);
            r3.setIngredient('S', Material.SMITHING_TABLE);
            r3.setIngredient('G', Material.GOLD_INGOT);
            Bukkit.addRecipe(r3);
        } catch (Exception ignored) {}

        // 4. Núcleo de Forja Rítmica (Media)
        try {
            NamespacedKey key4 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_rhythmic_forge");
            Bukkit.removeRecipe(key4);
            ShapedRecipe r4 = new ShapedRecipe(key4, createCoreItem(StructureType.RHYTHMIC_FORGE));
            r4.shape("OLO", "BCB", "OAO");
            r4.setIngredient('O', Material.CRYING_OBSIDIAN);
            r4.setIngredient('L', Material.SOUL_LANTERN);
            r4.setIngredient('B', Material.POLISHED_BLACKSTONE_BRICKS);
            r4.setIngredient('C', Material.SOUL_CAMPFIRE);
            r4.setIngredient('A', Material.ANVIL);
            Bukkit.addRecipe(r4);
        } catch (Exception ignored) {}

        // 5. Núcleo de Forja Maldita (Media-Alta)
        try {
            NamespacedKey key5 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_cursed_forge");
            Bukkit.removeRecipe(key5);
            ShapedRecipe r5 = new ShapedRecipe(key5, createCoreItem(StructureType.CURSED_FORGE));
            r5.shape("RCR", "BAB", " B ");
            r5.setIngredient('R', Material.RED_CANDLE);
            r5.setIngredient('C', Material.CRYING_OBSIDIAN);
            r5.setIngredient('B', Material.POLISHED_BLACKSTONE);
            r5.setIngredient('A', Material.ANVIL);
            Bukkit.addRecipe(r5);
        } catch (Exception ignored) {}

        // 6. Núcleo del Crisol de Fusión (Alta)
        try {
            NamespacedKey key6 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_soul_fusion");
            Bukkit.removeRecipe(key6);
            ShapedRecipe r6 = new ShapedRecipe(key6, createCoreItem(StructureType.SOUL_FUSION_CRUCIBLE));
            r6.shape("TDT", "DLD", "TDT");
            r6.setIngredient('T', Material.SOUL_TORCH);
            r6.setIngredient('D', Material.DIAMOND);
            r6.setIngredient('L', Material.LODESTONE);
            Bukkit.addRecipe(r6);
        } catch (Exception ignored) {}

        // 7. Núcleo de Forja de Jefes (Alta)
        try {
            NamespacedKey key7 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_boss_forge");
            Bukkit.removeRecipe(key7);
            ShapedRecipe r7 = new ShapedRecipe(key7, createCoreItem(StructureType.BOSS_FORGE));
            r7.shape("MBM", "BSB", " D ");
            r7.setIngredient('M', Material.MAGMA_BLOCK);
            r7.setIngredient('B', Material.POLISHED_BLACKSTONE_BRICKS);
            r7.setIngredient('S', Material.SMITHING_TABLE);
            r7.setIngredient('D', Material.DIAMOND_BLOCK);
            Bukkit.addRecipe(r7);
        } catch (Exception ignored) {}

        // 8. Núcleo del Gran Altar Celestial (Mítica)
        try {
            NamespacedKey key8 = new NamespacedKey(LivingToolsPlugin.getInstance(), "core_ritual_altar");
            Bukkit.removeRecipe(key8);
            ShapedRecipe r8 = new ShapedRecipe(key8, createCoreItem(StructureType.RITUAL_ALTAR));
            r8.shape("WRW", "RER", " B ");
            r8.setIngredient('W', Material.WHITE_CANDLE);
            r8.setIngredient('R', Material.REDSTONE);
            r8.setIngredient('E', Material.ENCHANTING_TABLE);
            r8.setIngredient('B', Material.BEACON);
            Bukkit.addRecipe(r8);
        } catch (Exception ignored) {}
    }
}
