package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
 * Características:
 *  - Mundo exclusivo generado automáticamente para Grieta Abisal, Raids y Forjas.
 *  - KeepInventory activo al 100% para proteger el equipo en combates de alto riesgo.
 *  - Estructuras pre-generadas: Arena de Grieta Abisal, Altar del Génesis y Santuario de Forjas.
 *  - Registro de posición anterior y retorno seguro al overworld.
 */
public class LivingRealmManager implements Listener {

    public static final String REALM_WORLD_NAME = "livingtools_realm";
    private static final Map<UUID, Location> previousLocations = new ConcurrentHashMap<>();
    private static File storageFile;
    private static YamlConfiguration storageConfig;

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

            // Generar arenas y estructuras si no están construidas
            buildRealmStructures(realm);
        }

        return realm;
    }

    /**
     * Genera las estructuras fijas del reino místico.
     */
    public static void buildRealmStructures(World world) {
        if (world == null) return;

        // 1. Arena Central de la Grieta Abisal en (0, 65, 0)
        buildRiftArena(world, 0, 65, 0, 18);

        // 2. Gran Altar del Avatar del Génesis en (120, 65, 0)
        buildGenesisArena(world, 120, 65, 0, 22);

        // 3. Santuario de Forjas y Pedestales en (-120, 65, 0)
        buildMasterForgeSanctuary(world, -120, 65, 0);

        // 4. Portal de Retorno en (0, 65, 22)
        buildReturnPortal(world, 0, 65, 22);
    }

    private static void buildRiftArena(World world, int cx, int cy, int cz, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    Block b = world.getBlockAt(cx + x, cy, cz + z);
                    if (distSq > (radius - 1) * (radius - 1)) {
                        b.setType(Material.CRYING_OBSIDIAN);
                    } else if (distSq > (radius - 3) * (radius - 3)) {
                        b.setType(Material.POLISHED_BLACKSTONE_BRICKS);
                    } else if ((x + z) % 4 == 0) {
                        b.setType(Material.DEEPSLATE_TILES);
                    } else {
                        b.setType(Material.POLISHED_DEEPSLATE);
                    }

                    // Limpiar espacio aéreo
                    for (int y = 1; y <= 10; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                    }
                }
            }
        }

        // Pilares rúnicos con linternas de almas en los 4 puntos cardinales
        int[][] pillars = {
                {cx + radius - 2, cz},
                {cx - radius + 2, cz},
                {cx, cz + radius - 2},
                {cx, cz - radius + 2}
        };
        for (int[] p : pillars) {
            for (int y = 1; y <= 4; y++) {
                world.getBlockAt(p[0], cy + y, p[1]).setType(Material.POLISHED_BLACKSTONE_WALL);
            }
            world.getBlockAt(p[0], cy + 5, p[1]).setType(Material.SOUL_LANTERN);
        }

        // Centro de invocación
        world.getBlockAt(cx, cy, cz).setType(Material.LODESTONE);
    }

    private static void buildGenesisArena(World world, int cx, int cy, int cz, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    Block b = world.getBlockAt(cx + x, cy, cz + z);
                    if (distSq > (radius - 1) * (radius - 1)) {
                        b.setType(Material.OBSIDIAN);
                    } else if (distSq > (radius - 4) * (radius - 4)) {
                        b.setType(Material.MAGMA_BLOCK);
                    } else {
                        b.setType(Material.GILDED_BLACKSTONE);
                    }

                    for (int y = 1; y <= 15; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                    }
                }
            }
        }

        // Altar central
        world.getBlockAt(cx, cy + 1, cz).setType(Material.NETHERITE_BLOCK);
        world.getBlockAt(cx, cy + 2, cz).setType(Material.BEACON);
    }

    private static void buildMasterForgeSanctuary(World world, int cx, int cy, int cz) {
        int width = 16;
        for (int x = -width; x <= width; x++) {
            for (int z = -width; z <= width; z++) {
                Block b = world.getBlockAt(cx + x, cy, cz + z);
                b.setType((Math.abs(x) == width || Math.abs(z) == width) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.QUARTZ_BLOCK);
                for (int y = 1; y <= 8; y++) {
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                }
            }
        }

        // 1. Mesa de Ensamblaje en (-120, 65, -6)
        world.getBlockAt(cx, cy + 1, cz - 6).setType(Material.IRON_BLOCK);
        world.getBlockAt(cx, cy + 2, cz - 6).setType(Material.CRAFTING_TABLE);

        // 2. Forja de Jefes en (-120, 65, 6)
        world.getBlockAt(cx, cy + 1, cz + 6).setType(Material.MAGMA_BLOCK);
        world.getBlockAt(cx, cy + 2, cz + 6).setType(Material.SMITHING_TABLE);

        // 3. Altar Ancestral en (-120 - 6, 65, cz)
        world.getBlockAt(cx - 6, cy + 1, cz).setType(Material.ENCHANTING_TABLE);

        // 4. Pedestal 3D en (-120 + 6, 65, cz)
        world.getBlockAt(cx + 6, cy + 1, cz).setType(Material.LODESTONE);
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
     * Teletransporta al jugador al Reino Místico guardando su ubicación anterior.
     */
    public static boolean teleportToRealm(Player player, Location targetInRealm) {
        World realm = getOrCreateRealmWorld();
        if (realm == null) {
            player.sendMessage(ChatColor.RED + "Error: No se pudo cargar el Reino de LivingTools.");
            return false;
        }

        // Guardar ubicación previa si no está ya en el reino
        if (!player.getWorld().getName().equals(REALM_WORLD_NAME)) {
            previousLocations.put(player.getUniqueId(), player.getLocation());
            saveStoredLocations();
        }

        Location dest = (targetInRealm != null) ? targetInRealm : getRiftArenaSpawn();
        player.teleport(dest);
        player.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);
        player.spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.2);

        player.sendTitle(ChatColor.DARK_PURPLE + "✦ REINO DE LIVING TOOLS ✦",
                ChatColor.LIGHT_PURPLE + "KeepInventory activado • Usa el portal para regresar", 10, 50, 20);
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
        return new Location(realm, 0.5, 66.0, 0.5, 0f, 0f);
    }

    public static Location getGenesisArenaSpawn() {
        World realm = getOrCreateRealmWorld();
        return new Location(realm, 120.5, 66.0, 0.5, 90f, 0f);
    }

    public static Location getSanctuarySpawn() {
        World realm = getOrCreateRealmWorld();
        return new Location(realm, -120.5, 66.0, 0.5, -90f, 0f);
    }

    public static boolean isInRealm(Player player) {
        return player.getWorld().getName().equals(REALM_WORLD_NAME);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();

        if (isInRealm(player)) {
            Location loc = event.getClickedBlock().getLocation();
            // Portal de retorno en (0, 65..67, 22)
            if (loc.getBlockX() == 0 && (loc.getBlockY() >= 65 && loc.getBlockY() <= 67) && loc.getBlockZ() == 22) {
                event.setCancelled(true);
                returnFromRealm(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isInRealm(player)) {
            // Reaparece en el punto de entrada de la arena con inventario protegido
            event.setRespawnLocation(getRiftArenaSpawn());
            Bukkit.getScheduler().runTaskLater(LivingToolsPlugin.getInstance(), () -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.GOLD + "✦ Has reaparecido en el Reino de Almas con todo tu inventario intacto.");
                    player.sendMessage(ChatColor.YELLOW + "Usa " + ChatColor.AQUA + "/lt rift leave" + ChatColor.YELLOW + " o camina al portal para volver a tu mundo.");
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
