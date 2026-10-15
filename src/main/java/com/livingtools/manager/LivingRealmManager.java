package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LivingRealmManager — Administrador del Mundo Dedicado de LivingTools ("livingtools_realm").
 *
 * Mejoras de Aislamiento e Inmersión:
 *  - Estructuras hiper-separadas a 500+ bloques entre sí para que sea imposible verlas entre sí.
 *  - Muros perimetrales rúnicos y cúpulas de 14 bloques de altura que bloquean la línea de visión al horizonte.
 *  - Protección total anti-construcción: Prohibido colocar, romper o verter bloques en el reino para no puentear.
 *  - KeepInventory activo al 100% y portal de retorno seguro.
 */
public class LivingRealmManager implements Listener {

    public static final String REALM_WORLD_NAME = "livingtools_realm";
    private static final Map<UUID, Location> previousLocations = new ConcurrentHashMap<>();
    private static File storageFile;
    private static YamlConfiguration storageConfig;

    // Coordenadas aisladas (a 500+ bloques de distancia)
    public static final int RIFT_X = 0, RIFT_Z = 0;
    public static final int GENESIS_X = 500, GENESIS_Z = 500;
    public static final int SANCTUARY_X = -500, SANCTUARY_Z = -500;
    public static final int FORGE_X = 0, FORGE_Z = -500;

    public static void init() {
        storageFile = new File(LivingToolsPlugin.getInstance().getDataFolder(), "realm_locations.yml");
        loadStoredLocations();
        getOrCreateRealmWorld();
    }

    /**
     * Obtiene o crea el mundo dedicado con las reglas místicas requeridas.
     */
    public static World getOrCreateRealmWorld() {
        World realm = Bukkit.getWorld(REALM_WORLD_NAME);
        if (realm == null) {
            LivingToolsPlugin.getInstance().getLogger().info("✦ Creando / Cargando el Reino Místico de LivingTools (" + REALM_WORLD_NAME + ")...");
            WorldCreator creator = new WorldCreator(REALM_WORLD_NAME);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            realm = creator.createWorld();
        }

        if (realm != null) {
            realm.setGameRule(GameRule.KEEP_INVENTORY, true);
            realm.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            realm.setGameRule(GameRule.MOB_GRIEFING, false);
            realm.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            realm.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            realm.setTime(18000L); // Noche mística estrellada
            realm.setStorm(false);

            // Generar arenas aisladas con muros altos
            buildRealmStructures(realm);
        }

        return realm;
    }

    /**
     * Genera las estructuras fijas del reino místico totalmente amuralladas e hiper-separadas.
     */
    public static void buildRealmStructures(World world) {
        if (world == null) return;

        // 1. Arena de la Grieta Abisal en (0, 65, 0)
        buildRiftArena(world, RIFT_X, 65, RIFT_Z, 20);

        // 2. Gran Altar del Avatar del Génesis en (500, 65, 500)
        buildGenesisArena(world, GENESIS_X, 65, GENESIS_Z, 26);

        // 3. Santuario de Forjas y Pedestales en (-500, 65, -500)
        buildMasterForgeSanctuary(world, SANCTUARY_X, 65, SANCTUARY_Z);

        // 4. Templo de la Forja Rítmica de Almas en (0, 65, -500)
        buildRhythmicForgeTemple(world, FORGE_X, 65, FORGE_Z);

        // 5. Portal de Retorno seguro en la Grieta
        buildReturnPortal(world, RIFT_X, 65, RIFT_Z + 16);
    }

    private static void buildRiftArena(World world, int cx, int cy, int cz, int radius) {
        // Suelo y espacio aéreo
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    Block b = world.getBlockAt(cx + x, cy, cz + z);
                    if (distSq > (radius - 2) * (radius - 2)) {
                        b.setType(Material.CRYING_OBSIDIAN);
                    } else if (distSq > (radius - 4) * (radius - 4)) {
                        b.setType(Material.POLISHED_BLACKSTONE_BRICKS);
                    } else if ((x + z) % 4 == 0) {
                        b.setType(Material.DEEPSLATE_TILES);
                    } else {
                        b.setType(Material.POLISHED_DEEPSLATE);
                    }

                    // Limpiar interior
                    for (int y = 1; y <= 16; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                    }
                }
            }
        }

        // Muralla Perimetral Rúnica de 14 bloques de altura para impedir ver el exterior
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq >= (radius - 1) * (radius - 1) && distSq <= radius * radius) {
                    for (int y = 1; y <= 14; y++) {
                        Material wallMat = (y % 4 == 0) ? Material.CRYING_OBSIDIAN : Material.POLISHED_BLACKSTONE_BRICKS;
                        if (y == 14) wallMat = Material.DEEPSLATE_TILES;
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(wallMat);
                    }
                }
            }
        }

        // Pilares de energía y linternas
        int[][] pillars = {
                {cx + radius - 4, cz},
                {cx - radius + 4, cz},
                {cx, cz + radius - 4},
                {cx, cz - radius + 4}
        };
        for (int[] p : pillars) {
            for (int y = 1; y <= 6; y++) {
                world.getBlockAt(p[0], cy + y, p[1]).setType(Material.POLISHED_BLACKSTONE_WALL);
            }
            world.getBlockAt(p[0], cy + 7, p[1]).setType(Material.SOUL_LANTERN);
        }

        // Centro de invocación
        world.getBlockAt(cx, cy, cz).setType(Material.LODESTONE);
    }

    private static void buildGenesisArena(World world, int cx, int cy, int cz, int radius) {
        // Suelo y aire
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    Block b = world.getBlockAt(cx + x, cy, cz + z);
                    if (distSq > (radius - 2) * (radius - 2)) {
                        b.setType(Material.OBSIDIAN);
                    } else if (distSq > (radius - 5) * (radius - 5)) {
                        b.setType(Material.MAGMA_BLOCK);
                    } else {
                        b.setType(Material.GILDED_BLACKSTONE);
                    }

                    for (int y = 1; y <= 18; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                    }
                }
            }
        }

        // Muralla Perimetral Volcánica de 16 bloques de altura
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq >= (radius - 1) * (radius - 1) && distSq <= radius * radius) {
                    for (int y = 1; y <= 16; y++) {
                        Material wallMat = (y % 3 == 0) ? Material.MAGMA_BLOCK : Material.POLISHED_BLACKSTONE_BRICKS;
                        if (y == 16) wallMat = Material.OBSIDIAN;
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(wallMat);
                    }
                }
            }
        }

        // Altar central supremo
        world.getBlockAt(cx, cy + 1, cz).setType(Material.NETHERITE_BLOCK);
        world.getBlockAt(cx, cy + 2, cz).setType(Material.BEACON);
    }

    private static void buildMasterForgeSanctuary(World world, int cx, int cy, int cz) {
        int width = 16;
        for (int x = -width; x <= width; x++) {
            for (int z = -width; z <= width; z++) {
                Block b = world.getBlockAt(cx + x, cy, cz + z);
                b.setType((Math.abs(x) == width || Math.abs(z) == width) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.QUARTZ_BLOCK);
                for (int y = 1; y <= 14; y++) {
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                }
            }
        }

        // Muros de recinto sagrado de 14 bloques
        for (int x = -width; x <= width; x++) {
            for (int z = -width; z <= width; z++) {
                if (Math.abs(x) == width || Math.abs(z) == width) {
                    for (int y = 1; y <= 14; y++) {
                        Material wallMat = (y % 2 == 0) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.CHISELED_POLISHED_BLACKSTONE;
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(wallMat);
                    }
                }
            }
        }

        // Estaciones de forja
        world.getBlockAt(cx, cy + 1, cz - 6).setType(Material.IRON_BLOCK);
        world.getBlockAt(cx, cy + 2, cz - 6).setType(Material.CRAFTING_TABLE);

        world.getBlockAt(cx, cy + 1, cz + 6).setType(Material.MAGMA_BLOCK);
        world.getBlockAt(cx, cy + 2, cz + 6).setType(Material.SMITHING_TABLE);

        world.getBlockAt(cx - 6, cy + 1, cz).setType(Material.ENCHANTING_TABLE);
        world.getBlockAt(cx + 6, cy + 1, cz).setType(Material.LODESTONE);
    }

    private static void buildRhythmicForgeTemple(World world, int cx, int cy, int cz) {
        int radius = 12;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    Block b = world.getBlockAt(cx + x, cy, cz + z);
                    if (distSq > (radius - 2) * (radius - 2)) {
                        b.setType(Material.CRYING_OBSIDIAN);
                    } else {
                        b.setType(Material.DEEPSLATE_BRICKS);
                    }

                    for (int y = 1; y <= 14; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                    }
                }
            }
        }

        // Muro perimetral rítmico de 14 bloques
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq >= (radius - 1) * (radius - 1) && distSq <= radius * radius) {
                    for (int y = 1; y <= 14; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.POLISHED_BLACKSTONE_BRICKS);
                    }
                }
            }
        }

        // Forja rítmica en el centro
        RhythmicForgeManager.buildStructure(new Location(world, cx, cy + 1, cz));
    }

    private static void buildReturnPortal(World world, int cx, int cy, int cz) {
        world.getBlockAt(cx, cy + 1, cz).setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(cx, cy + 2, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);

        world.getBlockAt(cx - 1, cy + 1, cz).setType(Material.POLISHED_DEEPSLATE_WALL);
        world.getBlockAt(cx + 1, cy + 1, cz).setType(Material.POLISHED_DEEPSLATE_WALL);
        world.getBlockAt(cx - 1, cy + 2, cz).setType(Material.SOUL_TORCH);
        world.getBlockAt(cx + 1, cy + 2, cz).setType(Material.SOUL_TORCH);
    }

    /**
     * Teletransporta al jugador al Reino Místico guardando su ubicación previa.
     */
    public static boolean teleportToRealm(Player player, Location targetInRealm) {
        World realm = getOrCreateRealmWorld();
        if (realm == null) {
            player.sendMessage(ChatColor.RED + "Error: No se pudo cargar el Reino de LivingTools.");
            return false;
        }

        if (!player.getWorld().getName().equals(REALM_WORLD_NAME)) {
            previousLocations.put(player.getUniqueId(), player.getLocation());
            saveStoredLocations();
        }

        Location dest = (targetInRealm != null) ? targetInRealm : getRiftArenaSpawn();
        player.teleport(dest);
        player.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);
        player.spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.2);

        player.sendTitle(ChatColor.DARK_PURPLE + "✦ REINO DE LIVING TOOLS ✦",
                ChatColor.LIGHT_PURPLE + "KeepInventory activo • Arenas selladas y aisladas", 10, 50, 20);
        return true;
    }

    /**
     * Regresa al jugador a su última posición en el mundo normal o a su spawn.
     */
    public static boolean returnFromRealm(Player player) {
        Location prev = previousLocations.remove(player.getUniqueId());
        saveStoredLocations();

        Location returnLoc = null;
        if (prev != null && prev.getWorld() != null) {
            returnLoc = prev;
        } else if (player.getBedSpawnLocation() != null) {
            returnLoc = player.getBedSpawnLocation();
        } else {
            World mainWorld = Bukkit.getWorlds().get(0);
            returnLoc = mainWorld.getSpawnLocation();
        }

        player.teleport(returnLoc);
        player.playSound(returnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        player.spawnParticle(Particle.FIREWORKS_SPARK, returnLoc.clone().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.1);
        player.sendMessage(ChatColor.GREEN + "✦ Has regresado sano y salvo a tu mundo anterior.");
        return true;
    }

    public static Location getRiftArenaSpawn() {
        World realm = getOrCreateRealmWorld();
        return new Location(realm, RIFT_X + 0.5, 66.0, RIFT_Z + 0.5, 0f, 0f);
    }

    public static Location getGenesisArenaSpawn() {
        World realm = getOrCreateRealmWorld();
        return new Location(realm, GENESIS_X + 0.5, 66.0, GENESIS_Z + 0.5, 90f, 0f);
    }

    public static Location getSanctuarySpawn() {
        World realm = getOrCreateRealmWorld();
        return new Location(realm, SANCTUARY_X + 0.5, 66.0, SANCTUARY_Z + 0.5, -90f, 0f);
    }

    public static Location getForgeTempleSpawn() {
        World realm = getOrCreateRealmWorld();
        return new Location(realm, FORGE_X + 0.5, 66.0, FORGE_Z + 0.5, -45f, 0f);
    }

    public static boolean isInRealm(Player player) {
        return player.getWorld().getName().equals(REALM_WORLD_NAME);
    }

    // -------------------------------------------------------------------------
    // PROTECCIÓN TOTAL ANTI-CONSTRUCCIÓN EN EL REINO
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isInRealm(player) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "✦ El Reino Sagrado es indestructible; no se permite colocar bloques.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isInRealm(player) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "✦ El Reino Sagrado es indestructible; no se permite romper bloques.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (isInRealm(player) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "✦ No se permite verter líquidos en el Reino Sagrado.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (isInRealm(player) && !player.isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();

        if (isInRealm(player)) {
            Location loc = event.getClickedBlock().getLocation();
            // Portal de retorno
            if (loc.getBlockX() == RIFT_X && (loc.getBlockY() >= 65 && loc.getBlockY() <= 67) && loc.getBlockZ() == (RIFT_Z + 16)) {
                event.setCancelled(true);
                returnFromRealm(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isInRealm(player)) {
            event.setRespawnLocation(getRiftArenaSpawn());
            Bukkit.getScheduler().runTaskLater(LivingToolsPlugin.getInstance(), () -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.GOLD + "✦ Has reaparecido en el Reino de Almas con todo tu inventario intacto.");
                    player.sendMessage(ChatColor.YELLOW + "Usa " + ChatColor.AQUA + "/lt realm leave" + ChatColor.YELLOW + " o camina al portal para volver a tu mundo.");
                }
            }, 10L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        saveStoredLocations();
    }

    private static void loadStoredLocations() {
        if (!storageFile.exists()) return;
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
        for (String key : storageConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Location loc = storageConfig.getLocation(key);
                if (loc != null) {
                    previousLocations.put(uuid, loc);
                }
            } catch (Exception ignored) {}
        }
    }

    public static void saveStoredLocations() {
        if (storageConfig == null) storageConfig = new YamlConfiguration();
        for (Map.Entry<UUID, Location> entry : previousLocations.entrySet()) {
            storageConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            storageConfig.save(storageFile);
        } catch (IOException ignored) {}
    }
}
