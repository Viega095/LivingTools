package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import com.livingtools.visuals.DamageIndicatorManager;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * AbyssalRiftEngine — Modo de Incursión Infinita en el Abismo Profundo (Endless Rift).
 */
public class AbyssalRiftEngine implements Listener {

    public enum RiftModifier {
        LOW_GRAVITY(ChatColor.AQUA + "☁ Baja Gravedad", "La gravedad disminuye al saltar."),
        VOID_STORM(ChatColor.DARK_PURPLE + "⚡ Tormenta del Vacío", "Rayos sombríos caen periódicamente."),
        VAMPIRISM(ChatColor.RED + "🩸 Mobs Vampíricos", "Los enemigos regeneran salud al golpear."),
        VOLATILE_FLOOR(ChatColor.GOLD + "💥 Suelo Volátil", "Detonaciones si permaneces inmóvil.");

        private final String displayName;
        private final String description;

        RiftModifier(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public static class RiftSession {
        private final UUID playerId;
        private final Location center;
        private int currentWave = 1;
        private final List<LivingEntity> activeMobs = new ArrayList<>();
        private final Set<RiftModifier> activeModifiers = new HashSet<>();
        private BukkitTask waveLoopTask;
        private long startTime;

        public RiftSession(UUID playerId, Location center) {
            this.playerId = playerId;
            this.center = center;
            this.startTime = System.currentTimeMillis();
        }

        public UUID getPlayerId() { return playerId; }
        public Location getCenter() { return center; }
        public int getCurrentWave() { return currentWave; }
        public void setCurrentWave(int currentWave) { this.currentWave = currentWave; }
        public List<LivingEntity> getActiveMobs() { return activeMobs; }
        public Set<RiftModifier> getActiveModifiers() { return activeModifiers; }
    }

    private static final Map<UUID, RiftSession> activeSessions = new HashMap<>();
    private static final Map<UUID, Integer> highScores = new HashMap<>();
    private static final Random random = new Random();

    public static boolean startRift(Player player) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ya te encuentras dentro de una Incursión Abisal.");
            return false;
        }

        // Teletransportar al Reino Dedicado de LivingTools
        LivingRealmManager.teleportToRealm(player, LivingRealmManager.getRiftArenaSpawn());
        Location center = LivingRealmManager.getRiftArenaSpawn();
        RiftSession session = new RiftSession(player.getUniqueId(), center);
        activeSessions.put(player.getUniqueId(), session);

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "╔══════════════════════════════════════════════╗");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "║   🌌 ¡ENTRADA AL ABISMO PROFUNDO (ENDLESS)! ║");
        player.sendMessage(ChatColor.GRAY + "║  Sobrevive a tantas oleadas como puedas.      ║");
        player.sendMessage(ChatColor.GRAY + "║  KeepInventory activo • Sal con /lt rift leave║");
        player.sendMessage(ChatColor.DARK_PURPLE + "╚══════════════════════════════════════════════╝");
        player.sendMessage("");
        player.playSound(center, Sound.BLOCK_PORTAL_TRIGGER, 1.2f, 0.8f);

        spawnWave(session);
        return true;
    }

    public static void leaveRift(Player player) {
        RiftSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            endRift(session, false);
        }
        LivingRealmManager.returnFromRealm(player);
    }

    private static void spawnWave(RiftSession session) {
        Player player = Bukkit.getPlayer(session.getPlayerId());
        if (player == null || !player.isOnline()) {
            endRift(session, false);
            return;
        }

        World world = session.getCenter().getWorld();
        if (world == null) return;

        int wave = session.getCurrentWave();

        // Asignar modificadores aleatorios según la oleada
        session.getActiveModifiers().clear();
        if (wave >= 3) {
            RiftModifier[] mods = RiftModifier.values();
            session.getActiveModifiers().add(mods[random.nextInt(mods.length)]);
            if (wave >= 8) {
                session.getActiveModifiers().add(mods[random.nextInt(mods.length)]);
            }
        }

        player.sendMessage(ChatColor.GOLD + "⚔ " + ChatColor.BOLD + "¡OLEADA " + wave + " COMIENZA! " + ChatColor.GOLD + "⚔");
        for (RiftModifier mod : session.getActiveModifiers()) {
            player.sendMessage(ChatColor.GRAY + "  [Modificador] " + mod.getDisplayName() + ChatColor.DARK_GRAY + " - " + mod.getDescription());
        }

        int mobCount = 3 + (wave * 2);
        for (int i = 0; i < mobCount; i++) {
            double angle = (2 * Math.PI * i) / mobCount;
            double radius = 7.0 + (random.nextDouble() * 3.0);
            Location spawnLoc = session.getCenter().clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

            EntityType type = (wave % 5 == 0 && i == 0) ? EntityType.IRON_GOLEM
                    : (wave > 5 && random.nextBoolean()) ? EntityType.WITHER_SKELETON : EntityType.ZOMBIE;

            LivingEntity mob = (LivingEntity) world.spawnEntity(spawnLoc, type);
            mob.setCustomName(ChatColor.DARK_PURPLE + "Enemigo Abisal Lv." + wave);
            mob.setCustomNameVisible(true);
            mob.setRemoveWhenFarAway(false);

            if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                double hp = 30.0 + (wave * 12.0);
                mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
                mob.setHealth(hp);
            }

            session.getActiveMobs().add(mob);
            world.spawnParticle(Particle.PORTAL, spawnLoc.clone().add(0, 1, 0), 20, 0.4, 0.4, 0.4, 0.1);
        }

        // Ticker de modificadores durante la oleada
        if (session.waveLoopTask != null) session.waveLoopTask.cancel();
        session.waveLoopTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    endRift(session, false);
                    return;
                }

                // Baja gravedad
                if (session.getActiveModifiers().contains(RiftModifier.LOW_GRAVITY)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 2, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false));
                }

                // Tormenta de vacío cada 100 ticks
                if (session.getActiveModifiers().contains(RiftModifier.VOID_STORM) && ticks % 100 == 0) {
                    Location strikeLoc = player.getLocation().add((random.nextDouble() - 0.5) * 8, 0, (random.nextDouble() - 0.5) * 8);
                    world.strikeLightningEffect(strikeLoc);
                    world.spawnParticle(Particle.SPELL_WITCH, strikeLoc, 30, 0.5, 1, 0.5, 0.1);
                }

                // Suelo volátil cada 80 ticks
                if (session.getActiveModifiers().contains(RiftModifier.VOLATILE_FLOOR) && ticks % 80 == 0) {
                    world.spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
                    player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.6f, 1.8f);
                }

                ticks++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 10L, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        for (RiftSession session : activeSessions.values()) {
            if (session.getActiveMobs().contains(entity)) {
                session.getActiveMobs().remove(entity);

                Player player = Bukkit.getPlayer(session.getPlayerId());
                if (player != null && player.isOnline()) {
                    MessageUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "👾 Mobs restantes: " + session.getActiveMobs().size());
                }

                // Si se despejó la oleada
                if (session.getActiveMobs().isEmpty()) {
                    if (session.waveLoopTask != null) session.waveLoopTask.cancel();

                    if (player != null && player.isOnline()) {
                        int completedWave = session.getCurrentWave();
                        highScores.merge(player.getUniqueId(), completedWave, Math::max);

                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 1.2f);
                        player.sendMessage(ChatColor.GREEN + "✔ ¡Oleada " + completedWave + " superada con éxito!");

                        // Recompensa cada 5 oleadas
                        if (completedWave % 5 == 0) {
                            grantWaveMilestoneLoot(player, completedWave);
                        }

                        // Siguiente oleada tras 4 segundos
                        session.setCurrentWave(completedWave + 1);
                        Bukkit.getScheduler().runTaskLater(LivingToolsPlugin.getInstance(), () -> {
                            if (activeSessions.containsKey(player.getUniqueId())) {
                                spawnWave(session);
                            }
                        }, 80L);
                    }
                }
                break;
            }
        }
    }

    private static void grantWaveMilestoneLoot(Player player, int wave) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "🎁 ¡RECOMPENSA DE HITO: OLEADA " + wave + "!");
        player.sendMessage(ChatColor.YELLOW + "Has obtenido botín abisal de alto rango.");
        player.sendMessage("");

        ItemStack held = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(held)) {
            LivingTool tool = new LivingTool(held);
            tool.addXP(player, 500L * (wave / 5));
        }

        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 2 * (wave / 5)));
        player.getInventory().addItem(new ItemStack(Material.NETHERITE_SCRAP, wave / 5));
    }

    public static void endRift(RiftSession session, boolean broadcast) {
        if (session.waveLoopTask != null) session.waveLoopTask.cancel();
        for (LivingEntity e : session.getActiveMobs()) {
            if (e != null && e.isValid()) e.remove();
        }
        session.getActiveMobs().clear();
        activeSessions.remove(session.getPlayerId());

        Player player = Bukkit.getPlayer(session.getPlayerId());
        if (player != null && player.isOnline()) {
            int wave = session.getCurrentWave();
            player.sendMessage(ChatColor.DARK_PURPLE + "✦ Fin de la Incursión Abisal. Alcanzaste la Oleada: " + ChatColor.GOLD + wave);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        RiftSession session = activeSessions.get(event.getPlayer().getUniqueId());
        if (session != null) endRift(session, false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        RiftSession session = activeSessions.get(event.getEntity().getUniqueId());
        if (session != null) endRift(session, false);
    }

    public static int getHighScore(UUID uuid) {
        return highScores.getOrDefault(uuid, 0);
    }

    public static void cleanup(UUID uuid) {
        RiftSession s = activeSessions.remove(uuid);
        if (s != null) endRift(s, false);
    }
}
