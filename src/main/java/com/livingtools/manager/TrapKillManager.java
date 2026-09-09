package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * TrapKillManager — atribuye kills de trampas a jugadores con living tools.
 *
 * Tipos de trampa soportados:
 *   1. DISPENSER    — el jugador colocó un dispensador. Proyectiles y flechas = kill suyo
 *   2. TRIPWIRE     — tripwire hook colocado. Activa detector, kill atribuido
 *   3. TNT          — jugador encendió TNT (mechero/flecha) → explosión → kill
 *   4. LAVA/FUEGO  — jugador colocó lava, el mob muere en zona adyacente
 *   5. PRESSURE PLATE + mob fall (VOID/FALL) — el jugador colocó la trampa
 *   6. CAÑA DE PESCAR — PlayerFishEvent captura criatura (ataca/atrapa mobs)
 *
 * Sistema de atribución:
 *   - BlockPlaceEvent con bloques trampa → guardamos Location → UUID del player
 *   - EntityDamageEvent/EntityDamageByBlockEvent → si es LAVA/FIRE/ENTITY_EXPLOSION
 *     cerca de una trampa conocida → mapeamos EntityUUID → PlayerUUID
 *   - EntityDeathEvent con getKiller()==null → buscamos en trapOwners → otorgamos XP
 *
 * XP de trampa = 60% del XP de combate normal (con todos los multiplicadores).
 * El sistema muestra frases de la herramienta diferenciadas por trampa (orgullosa/filosófica).
 */
public class TrapKillManager implements Listener {

    // Bloques que se consideran "trampa" cuando los coloca el jugador
    private static final Set<Material> TRAP_BLOCKS = EnumSet.of(
            Material.DISPENSER,
            Material.TRIPWIRE_HOOK,
            Material.TNT,
            Material.LAVA,
            Material.CAMPFIRE,
            Material.MAGMA_BLOCK,
            Material.OBSERVER,         // trampas de pistón
            Material.STONE_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE
    );

    // Causas de daño que son "trampa" en EntityDamageEvent
    private static final Set<EntityDamageEvent.DamageCause> TRAP_CAUSES = EnumSet.of(
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
            EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.PROJECTILE,
            EntityDamageEvent.DamageCause.CONTACT  // Cactus
    );

    // Location (serializada) → UUID del jugador que colocó el bloque trampa
    // Se limpia cuando el bloque es destruido o después de 30 minutos
    private static final Map<String, UUID>  trapOwners    = new LinkedHashMap<>();
    private static final Map<String, Long>  trapTimestamp = new LinkedHashMap<>();

    // EntityUUID → UUID del jugador que le causó daño (para seguimiento de fire_tick etc)
    private static final Map<UUID, UUID> entityDamageOwner = new HashMap<>();
    private static final Map<UUID, Long> entityDamageTime  = new HashMap<>();

    // UUID de proyectiles disparados por dispensadores → owner
    private static final Map<UUID, UUID> projectileOwner = new HashMap<>();

    private static final long TRAP_EXPIRE_MS      = 30 * 60 * 1000L; // 30 min
    private static final long DAMAGE_TRACK_MS     = 10_000L;          // 10 s
    private static final double TRAP_XP_RATIO     = 0.60;             // 60% del XP normal
    private static final int   TRAP_RADIUS        = 8;                // bloques de radio de detección

    // -----------------------------------------------------------------------
    // Eventos de colocación de trampa
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrapPlaced(BlockPlaceEvent event) {
        if (!TRAP_BLOCKS.contains(event.getBlock().getType())) return;
        Player p = event.getPlayer();
        if (!LivingTool.isLivingTool(p.getInventory().getItemInMainHand())) return;

        String key = locKey(event.getBlock().getLocation());
        trapOwners.put(key, p.getUniqueId());
        trapTimestamp.put(key, System.currentTimeMillis());
        cleanOldTraps();
    }

    // -----------------------------------------------------------------------
    // Seguimiento de daño para atribución
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getEntity() instanceof Player) return;
        if (!TRAP_CAUSES.contains(event.getCause())) return;

        LivingEntity victim = (LivingEntity) event.getEntity();

        // ¿Es daño de proyectil? Buscar owner del proyectil
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            Entity damager = edbe.getDamager();

            // Proyectil (flecha de dispensador)
            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                UUID ownerUUID = projectileOwner.get(proj.getUniqueId());
                if (ownerUUID != null) {
                    entityDamageOwner.put(victim.getUniqueId(), ownerUUID);
                    entityDamageTime.put(victim.getUniqueId(), System.currentTimeMillis());
                    return;
                }
            }
        }

        // Buscar trampa más cercana al lugar del daño
        Location victimLoc = victim.getLocation();
        UUID owner = findNearestTrapOwner(victimLoc);
        if (owner != null) {
            entityDamageOwner.put(victim.getUniqueId(), owner);
            entityDamageTime.put(victim.getUniqueId(), System.currentTimeMillis());
        }
    }

    // Seguimiento de proyectiles de dispensadores (no hay evento directo, usamos dispense)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof org.bukkit.projectiles.BlockProjectileSource)) return;
        org.bukkit.projectiles.BlockProjectileSource source =
                (org.bukkit.projectiles.BlockProjectileSource) event.getEntity().getShooter();
        if (source.getBlock().getType() != Material.DISPENSER) return;

        String key = locKey(source.getBlock().getLocation());
        UUID owner = trapOwners.get(key);
        if (owner != null) {
            projectileOwner.put(event.getEntity().getUniqueId(), owner);
        }
    }

    // -----------------------------------------------------------------------
    // Atribución de kill
    // -----------------------------------------------------------------------

    /**
     * Llamado desde ExperienceListener.onEntityDeath cuando getKiller()==null.
     * Retorna el jugador al que atribuir el kill, o null si no es kill de trampa.
     */
    public static Player getTrapKiller(LivingEntity entity) {
        UUID ownerUUID = entityDamageOwner.get(entity.getUniqueId());
        if (ownerUUID == null) return null;

        Long time = entityDamageTime.get(entity.getUniqueId());
        if (time == null || System.currentTimeMillis() - time > DAMAGE_TRACK_MS) {
            entityDamageOwner.remove(entity.getUniqueId());
            entityDamageTime.remove(entity.getUniqueId());
            return null;
        }

        Player player = Bukkit.getPlayer(ownerUUID);
        if (player == null || !player.isOnline()) return null;

        // Cleanup
        entityDamageOwner.remove(entity.getUniqueId());
        entityDamageTime.remove(entity.getUniqueId());
        return player;
    }

    /**
     * Otorga XP de trampa al jugador. Llama a addXP con el 60% del XP de combate normal.
     */
    public static void awardTrapXP(Player player, LivingTool tool, LivingEntity mob, EntityType entityType) {
        ToolData data = tool.getData();

        // XP base de combate escalado por multiplicadores (sin kill streak — trampas son pasivas)
        double combatMult = ConfigManager.getCombatXPMultiplier()
                * WeatherBonusManager.getWeatherMultiplier(player, tool.getItem().getType())
                * ServerEventManager.getCombatXPMultiplier(player.getWorld())
                * SleepBonusManager.getSleepMultiplier(player);

        long trapXP = Math.max(1L, (long)(5 * combatMult * TRAP_XP_RATIO));
        tool.addXP(player, trapXP);

        // Tracking
        data.setMobKills(data.getMobKills() + 1);
        ToolMemoryManager.onKill(player, tool, entityType);
        DailyChallengeManager.onKill(player, tool, entityType);
        DailyBonusManager.checkAndGrant(player, tool);

        // Personalidad — mensaje de trampa
        String[] trapLines = trapLinesByPersonality(data.getPersonality());
        if (trapLines.length > 0 && Math.random() < 0.25) { // 25% chance de comentar
            String line = trapLines[(int)(Math.random() * trapLines.length)];
            String toolName = getToolName(tool);
            player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.ITALIC + line + ChatColor.GRAY + "\"");
        }

        // Action bar con XP ganado
        MessageUtils.sendActionBar(player, ChatColor.YELLOW + "⚙ Trampa! +" + trapXP + " XP");
    }

    // -----------------------------------------------------------------------
    // Caña de pescar — atrapa mob
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishingHit(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        if (!(event.getCaught() instanceof LivingEntity)) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        // Comprobar también la mano secundaria
        if (!LivingTool.isLivingTool(held)) {
            held = player.getInventory().getItemInOffHand();
        }
        if (!LivingTool.isLivingTool(held)) return;

        // Marcar la entidad como "siendo atacada" por el jugador con caña
        LivingEntity caught = (LivingEntity) event.getCaught();
        entityDamageOwner.put(caught.getUniqueId(), player.getUniqueId());
        entityDamageTime.put(caught.getUniqueId(), System.currentTimeMillis());

        // XP menor por atrapar con caña (no mata, solo engancha)
        LivingTool tool = new LivingTool(held);
        long hookXP = 2;
        tool.addXP(player, hookXP);
        MessageUtils.sendActionBar(player, ChatColor.AQUA + "🎣 ¡Enganchado! +" + hookXP + " XP");
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    private static UUID findNearestTrapOwner(Location loc) {
        String world = loc.getWorld().getName();
        UUID nearest = null;
        double nearestDist = TRAP_RADIUS * TRAP_RADIUS;

        for (Map.Entry<String, UUID> entry : trapOwners.entrySet()) {
            String[] parts = entry.getKey().split(":");
            if (parts.length != 4 || !parts[0].equals(world)) continue;
            try {
                double dx = loc.getX() - Double.parseDouble(parts[1]);
                double dy = loc.getY() - Double.parseDouble(parts[2]);
                double dz = loc.getZ() - Double.parseDouble(parts[3]);
                double dist2 = dx*dx + dy*dy + dz*dz;
                if (dist2 < nearestDist) {
                    nearestDist = dist2;
                    nearest = entry.getValue();
                }
            } catch (NumberFormatException ignored) {}
        }
        return nearest;
    }

    private static String locKey(Location loc) {
        return loc.getWorld().getName() + ":"
                + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private static void cleanOldTraps() {
        if (trapTimestamp.size() < 100) return; // evitar limpiar siempre
        long now = System.currentTimeMillis();
        trapOwners.entrySet().removeIf(e -> {
            Long ts = trapTimestamp.get(e.getKey());
            return ts == null || now - ts > TRAP_EXPIRE_MS;
        });
        trapTimestamp.entrySet().removeIf(e -> now - e.getValue() > TRAP_EXPIRE_MS);
    }

    private static String getToolName(LivingTool tool) {
        ItemStack item = tool.getItem();
        return (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                ? item.getItemMeta().getDisplayName() : ChatColor.GOLD + "Tu herramienta";
    }

    private static String[] trapLinesByPersonality(String personality) {
        if (personality == null) personality = "WISE";
        switch (personality) {
            case "AGGRESSIVE":
                return new String[]{
                    "Eficiente. Sin necesidad de ensuciarse las manos.",
                    "Muertos igualmente. La trampa hizo el trabajo.",
                    "¿Quién dijo que necesitamos pelear cara a cara?",
                    "El verdadero depredador no persigue. Espera."
                };
            case "WISE":
                return new String[]{
                    "La inteligencia supera a la fuerza bruta.",
                    "Un buen trampero vale más que diez guerreros.",
                    "La paciencia también es una forma de poder.",
                    "Confucio dijo: prepara la trampa y el enemigo vendrá."
                };
            case "LAZY":
                return new String[]{
                    "Así me gusta. Trabajo sin moverse.",
                    "Las mejores kills son las que no requieren esfuerzo.",
                    "Zzz... ¿Qué? ¿Ya murió? Bien.",
                    "Mi tipo de combate favorito: el que otros hacen por mí."
                };
            default: // CHEERFUL
                return new String[]{
                    "¡La trampa funcionó! ¡Sabía que funcionaría!",
                    "¡Sorpresa! ¡La mejor trampa es la que no ves venir!",
                    "¡Perfecto! Cada trampa es una mini-obra de arte.",
                    "¡Genial! Soy tan inteligente y encantador... y tú también."
                };
        }
    }
}
