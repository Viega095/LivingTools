package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * BossRaidDungeonManager — Administrador de Incursiones Rúnicas y Mazmorras de Jefes.
 *
 * Fases de una Incursión:
 *  1. Invocación de Oleada: 4 Centinelas Rúnicos (Runic Sentinels) armados y con auras mágicas.
 *  2. Aparición Cinemática del Jefe: Despertar con BossCinematicManager.
 *  3. Combate de Incursión: Ataques telegrafiados en el suelo.
 *  4. Cofre Ancestral Mítico: Cofre interactivo con apertura cinemática y recompensas legendarias.
 */
public class BossRaidDungeonManager implements Listener {

    public static class IncursionSession {
        private final UUID playerId;
        private final Location centerLocation;
        private final long startTime;
        private int stage; // 1 = Sentinels, 2 = Boss, 3 = Chest / Finished
        private final List<LivingEntity> spawnedEntities = new ArrayList<>();
        private Location chestLocation;
        private boolean chestClaimed = false;
        private BukkitTask tickerTask;

        public IncursionSession(UUID playerId, Location centerLocation) {
            this.playerId = playerId;
            this.centerLocation = centerLocation;
            this.startTime = System.currentTimeMillis();
            this.stage = 1;
        }

        public UUID getPlayerId() { return playerId; }
        public Location getCenterLocation() { return centerLocation; }
        public int getStage() { return stage; }
        public void setStage(int stage) { this.stage = stage; }
        public List<LivingEntity> getSpawnedEntities() { return spawnedEntities; }
        public Location getChestLocation() { return chestLocation; }
        public void setChestLocation(Location chestLocation) { this.chestLocation = chestLocation; }
        public boolean isChestClaimed() { return chestClaimed; }
        public void setChestClaimed(boolean chestClaimed) { this.chestClaimed = chestClaimed; }
    }

    private static final Map<UUID, IncursionSession> activeIncursions = new HashMap<>();
    private static final Map<Location, UUID> activeChests = new HashMap<>();
    private static final Random random = new Random();

    public static void init() {
        // Ticker de chequeo de límites y partículas de mazmorra
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, IncursionSession>> it = activeIncursions.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<UUID, IncursionSession> entry = it.next();
                    IncursionSession session = entry.getValue();
                    Player player = Bukkit.getPlayer(session.getPlayerId());

                    // Timeout después de 12 minutos
                    if (now - session.startTime > 12 * 60 * 1000L || player == null || !player.isOnline()) {
                        cleanupSession(session);
                        it.remove();
                        if (player != null && player.isOnline()) {
                            player.sendMessage(ChatColor.RED + "⏳ La Incursión Rúnica ha expirado.");
                        }
                        continue;
                    }

                    // Partículas de perímetro de la arena
                    ParticleOptimizer.spawnCircle(session.centerLocation, 14.0, Particle.SOUL_FIRE_FLAME, 20, null);

                    // Si el cofre está esperando reclamo, emitir partículas de baliza
                    if (session.stage == 3 && session.chestLocation != null && !session.chestClaimed) {
                        session.chestLocation.getWorld().spawnParticle(Particle.END_ROD, session.chestLocation.clone().add(0.5, 0.8, 0.5), 5, 0.2, 0.3, 0.2, 0.02);
                        session.chestLocation.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, session.chestLocation.clone().add(0.5, 1.2, 0.5), 10, 0.4, 0.4, 0.4, 0.1);
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 20L);
    }

    public static boolean startIncursion(Player player) {
        if (activeIncursions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ya tienes una Incursión Rúnica activa.");
            return false;
        }

        Location startLoc = player.getLocation();
        IncursionSession session = new IncursionSession(player.getUniqueId(), startLoc);
        activeIncursions.put(player.getUniqueId(), session);

        player.playSound(startLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 0.8f);
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "╔════════════════════════════════════════╗");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "║   ⚔ ¡INCURSIÓN RÚNICA INICIADA! ⚔     ║");
        player.sendMessage(ChatColor.GRAY + "║  Fase 1: Derrota a los Centinelas Rúnicos");
        player.sendMessage(ChatColor.DARK_PURPLE + "╚════════════════════════════════════════╝");
        player.sendMessage("");

        // Invocar 4 Centinelas Rúnicos
        spawnSentinels(session, startLoc);
        return true;
    }

    private static void spawnSentinels(IncursionSession session, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        double[][] offsets = {{4, 0}, {-4, 0}, {0, 4}, {0, -4}};
        for (double[] off : offsets) {
            Location spawnLoc = center.clone().add(off[0], 0, off[1]);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

            WitherSkeleton sentinel = (WitherSkeleton) world.spawnEntity(spawnLoc, EntityType.WITHER_SKELETON);
            sentinel.setCustomName(ChatColor.LIGHT_PURPLE + "⚡ " + ChatColor.BOLD + "Centinela Rúnico" + ChatColor.LIGHT_PURPLE + " ⚡");
            sentinel.setCustomNameVisible(true);
            sentinel.setRemoveWhenFarAway(false);

            if (sentinel.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                sentinel.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(120.0);
                sentinel.setHealth(120.0);
            }
            if (sentinel.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                sentinel.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(12.0);
            }
            if (sentinel.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
                sentinel.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.7);
            }

            // Equipamiento
            ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
            if (sentinel.getEquipment() != null) {
                sentinel.getEquipment().setHelmet(helmet);
                sentinel.getEquipment().setItemInMainHand(sword);
                sentinel.getEquipment().setHelmetDropChance(0f);
                sentinel.getEquipment().setItemInMainHandDropChance(0f);
            }

            sentinel.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            session.getSpawnedEntities().add(sentinel);

            world.spawnParticle(Particle.SPELL_WITCH, spawnLoc.clone().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.1);
            world.playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.8f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSentinelDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getCustomName() == null || !entity.getCustomName().contains("Centinela Rúnico")) return;

        // Buscar a qué sesión pertenece
        for (IncursionSession session : activeIncursions.values()) {
            if (session.getStage() == 1 && session.getSpawnedEntities().contains(entity)) {
                session.getSpawnedEntities().remove(entity);

                Player p = Bukkit.getPlayer(session.getPlayerId());
                if (p != null && p.isOnline()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.5f);
                    p.sendMessage(ChatColor.GOLD + "✦ Centinela caído. Restantes: " + ChatColor.YELLOW + session.getSpawnedEntities().size());
                }

                // Si todos los centinelas murieron → Fase 2: Spawn Boss Cinemático
                if (session.getSpawnedEntities().isEmpty()) {
                    session.setStage(2);
                    triggerBossPhase(session);
                }
                break;
            }
        }
    }

    private static void triggerBossPhase(IncursionSession session) {
        Player player = Bukkit.getPlayer(session.getPlayerId());
        if (player == null || !player.isOnline()) return;

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_RED + "☠ ¡LOS SELLOS RÚNICOS SE HAN ROTO!");
        player.sendMessage(ChatColor.GOLD + "Prepárate: La Bestia Ancestral se aproxima...");
        player.sendMessage("");

        // Invocación Cinemática
        Location bossLoc = session.getCenterLocation();
        BossCinematicManager.playSpawnCinematic(bossLoc, "Guardián de la Cripta", () -> {
            World world = bossLoc.getWorld();
            if (world == null) return;

            IronGolem boss = (IronGolem) world.spawnEntity(bossLoc, EntityType.IRON_GOLEM);
            boss.setCustomName(ChatColor.DARK_PURPLE + "⚡ " + ChatColor.BOLD + "GUARDIÁN DE LA CRIPTA" + ChatColor.DARK_PURPLE + " ⚡");
            boss.setCustomNameVisible(true);
            boss.setRemoveWhenFarAway(false);

            if (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(800.0);
                boss.setHealth(800.0);
            }
            if (boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(24.0);
            }
            if (boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
                boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
            }

            BossDropManager.tagTitan(boss);
            BossBarManager.addBoss(boss, "&5&lGuardián de la Cripta", org.bukkit.boss.BarColor.PURPLE);
            session.getSpawnedEntities().add(boss);

            // Tarea de ataques telegrafiados periódicos
            session.tickerTask = new BukkitRunnable() {
                int tickCount = 0;

                @Override
                public void run() {
                    if (boss.isDead() || !boss.isValid()) {
                        cancel();
                        return;
                    }

                    tickCount++;
                    // Cada 8 segundos (160 ticks), realizar ataque telegrafiado
                    if (tickCount % 160 == 0 && player.isOnline()) {
                        Location targetLoc = player.getLocation();
                        player.sendMessage(ChatColor.RED + "⚠ ¡El Guardián prepara una Ondas de Choque destructiva! ¡Esquiva!");

                        BossCinematicManager.playTelegraphedAttack(targetLoc, 4.0, 40, () -> {
                            // Al explotar tras 2 segundos
                            for (Entity near : targetLoc.getWorld().getNearbyEntities(targetLoc, 4.0, 4.0, 4.0)) {
                                if (near instanceof Player) {
                                    ((Player) near).damage(18.0, boss);
                                    near.setVelocity(new Vector(0, 0.6, 0));
                                }
                            }
                        });
                    }
                }
            }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 1L);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDefeat(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getCustomName() == null || !entity.getCustomName().contains("GUARDIÁN DE LA CRIPTA")) return;

        for (IncursionSession session : activeIncursions.values()) {
            if (session.getStage() == 2 && session.getSpawnedEntities().contains(entity)) {
                if (session.tickerTask != null) session.tickerTask.cancel();
                session.getSpawnedEntities().clear();
                session.setStage(3);

                // Cinemática de Muerte
                BossCinematicManager.playDeathCinematic(entity, null);

                // Crear Cofre Ancestral Mítico en el centro
                spawnAncientChest(session);
                break;
            }
        }
    }

    private static void spawnAncientChest(IncursionSession session) {
        Location chestLoc = session.getCenterLocation().getBlock().getLocation();
        Block block = chestLoc.getBlock();
        block.setType(Material.CHEST);
        session.setChestLocation(chestLoc);
        activeChests.put(chestLoc, session.getPlayerId());

        Player player = Bukkit.getPlayer(session.getPlayerId());
        if (player != null && player.isOnline()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦══════════════════════════════════════════✦");
            player.sendMessage(ChatColor.YELLOW + "  ¡VICTORIA EN LA INCURSIÓN RÚNICA! ");
            player.sendMessage(ChatColor.AQUA + "  Un " + ChatColor.GOLD + "Cofre Ancestral Mítico" + ChatColor.AQUA + " ha aparecido en el centro.");
            player.sendMessage(ChatColor.GRAY + "  Haz click derecho en el cofre para reclamar tu botín legendario.");
            player.sendMessage(ChatColor.GOLD + "✦══════════════════════════════════════════✦");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1f);
        }

        // Efectos del cofre
        ParticleOptimizer.spawnHelix(chestLoc.clone().add(0.5, 0, 0.5), 1.5, 3.0, Particle.TOTEM, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST) return;

        Location loc = event.getClickedBlock().getLocation();
        if (!activeChests.containsKey(loc)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID ownerId = activeChests.get(loc);

        if (!player.getUniqueId().equals(ownerId) && !player.hasPermission("livingtools.admin")) {
            player.sendMessage(ChatColor.RED + "Este cofre ancestral pertenece a otro guerrero.");
            return;
        }

        IncursionSession session = activeIncursions.get(ownerId);
        if (session != null && !session.isChestClaimed()) {
            session.setChestClaimed(true);
            activeChests.remove(loc);
            loc.getBlock().setType(Material.AIR);

            // Efectos cinematográficos de apertura
            World world = loc.getWorld();
            world.spawnParticle(Particle.EXPLOSION_LARGE, loc.clone().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.05);
            world.spawnParticle(Particle.TOTEM, loc.clone().add(0.5, 1, 0.5), 60, 0.5, 0.8, 0.5, 0.1);
            world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 1.2f);
            world.playSound(loc, Sound.BLOCK_CHEST_OPEN, 1.0f, 0.8f);

            // Otorgar botín épico
            grantMythicLoot(player, loc);
            player.sendMessage(ChatColor.GREEN + "✦ ¡Has reclamado las riquezas del Cofre Ancestral!");

            // Limpieza de sesión
            cleanupSession(session);
            activeIncursions.remove(ownerId);
        }
    }

    private static void grantMythicLoot(Player player, Location loc) {
        List<ItemStack> rewards = new ArrayList<>();

        // 1. Núcleo o fragmentos
        rewards.add(new ItemStack(Material.NETHERITE_INGOT, 1 + random.nextInt(2)));
        rewards.add(new ItemStack(Material.DIAMOND, 4 + random.nextInt(6)));
        rewards.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 8 + random.nextInt(8)));

        // 2. Fragmentos de Reliquia
        rewards.add(RelicFragmentSystem.createFragment(1 + random.nextInt(3)));

        // 3. XP Masivo para Living Tool
        ItemStack held = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(held)) {
            LivingTool tool = new LivingTool(held);
            tool.addXP(player, 1200L);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "⚡ +1200 XP otorgados a tu herramienta viviente!");
        }

        for (ItemStack item : rewards) {
            Map<Integer, ItemStack> left = player.getInventory().addItem(item);
            for (ItemStack l : left.values()) {
                loc.getWorld().dropItemNaturally(loc, l);
            }
        }
    }

    private static void cleanupSession(IncursionSession session) {
        if (session.tickerTask != null) session.tickerTask.cancel();
        for (LivingEntity e : session.getSpawnedEntities()) {
            if (e != null && e.isValid()) e.remove();
        }
        session.getSpawnedEntities().clear();
        if (session.getChestLocation() != null) {
            Block b = session.getChestLocation().getBlock();
            if (b.getType() == Material.CHEST) {
                b.setType(Material.AIR);
            }
            activeChests.remove(session.getChestLocation());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        IncursionSession session = activeIncursions.remove(event.getPlayer().getUniqueId());
        if (session != null) {
            cleanupSession(session);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        IncursionSession session = activeIncursions.get(p.getUniqueId());
        if (session != null) {
            p.sendMessage(ChatColor.RED + "☠ Has caído en la Incursión Rúnica...");
            cleanupSession(session);
            activeIncursions.remove(p.getUniqueId());
        }
    }

    public static void cleanup(UUID uuid) {
        IncursionSession session = activeIncursions.remove(uuid);
        if (session != null) {
            cleanupSession(session);
        }
    }
}
