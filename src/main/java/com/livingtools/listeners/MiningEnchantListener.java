package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.CustomEnchantManager;
import com.livingtools.manager.CustomEnchantManager.LivingEnchant;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * MiningEnchantListener — implementa todos los encantamientos de minería de Living Tools:
 *
 *  TREASURE      — al minar un ore/piedra, ~0.3% base (+0.2% por nivel) de abrir un GUI de tesoro
 *  VEIN_BREAKER  — mina vetas enteras de ore (BFS hasta 16/32/64 bloques según nivel)
 *  ORE_ECHO      — detecta ores en radio 6/10/16 bloques y los lista en el chat (cooldown 30s)
 *  AUTO_SMELT    — funde ores en el inventario al romperlos (carbón = oro, hierro = lingote, etc.)
 *  EXPLOSIVE_PICK — chance 3% por nivel de romper un cuadrado 3×3 alrededor del bloque
 *  SOUL_HARVEST  — mobs matados con el pico tienen +15% por nivel de soltar su drop raro
 */
public class MiningEnchantListener implements Listener {

    private static final Random random = new Random();

    // Cooldowns en memoria
    private static final Map<UUID, Long> oreEchoCooldown   = new HashMap<>();
    private static final Map<UUID, Long> treasureCooldown  = new HashMap<>();
    private static final long ORE_ECHO_COOLDOWN_MS  = 30_000L;
    private static final long TREASURE_COOLDOWN_MS  = 10_000L; // evitar spam

    // Ores que activan VEIN_BREAKER y TREASURE
    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS
    );

    // Mapeo de auto-smelt: ore → drop fundido
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();
    static {
        SMELT_MAP.put(Material.IRON_ORE,              Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE,    Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE,              Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE,    Material.GOLD_INGOT);
        SMELT_MAP.put(Material.NETHER_GOLD_ORE,       Material.GOLD_NUGGET);
        SMELT_MAP.put(Material.COPPER_ORE,            Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE,  Material.COPPER_INGOT);
        SMELT_MAP.put(Material.COAL_ORE,              Material.COAL);
        SMELT_MAP.put(Material.DEEPSLATE_COAL_ORE,    Material.COAL);
        SMELT_MAP.put(Material.NETHER_QUARTZ_ORE,     Material.QUARTZ);
    }

    // -----------------------------------------------------------------------
    // Main event
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;

        LivingTool tool = new LivingTool(item);
        Block block = event.getBlock();

        // ── TREASURE ─────────────────────────────────────────────────────────
        if (CustomEnchantManager.hasEnchant(tool, LivingEnchant.TREASURE)) {
            handleTreasure(player, tool, block);
        }

        // ── VEIN_BREAKER ─────────────────────────────────────────────────────
        if (CustomEnchantManager.hasEnchant(tool, LivingEnchant.VEIN_BREAKER) && ORES.contains(block.getType())) {
            handleVeinBreaker(event, player, tool, block);
        }

        // ── ORE_ECHO ──────────────────────────────────────────────────────────
        if (CustomEnchantManager.hasEnchant(tool, LivingEnchant.ORE_ECHO)) {
            handleOreEcho(player, tool, block);
        }

        // ── AUTO_SMELT ────────────────────────────────────────────────────────
        if (CustomEnchantManager.hasEnchant(tool, LivingEnchant.AUTO_SMELT) && SMELT_MAP.containsKey(block.getType())) {
            handleAutoSmelt(event, player, block);
        }

        // ── EXPLOSIVE_PICK ────────────────────────────────────────────────────
        if (CustomEnchantManager.hasEnchant(tool, LivingEnchant.EXPLOSIVE_PICK)) {
            handleExplosivePick(player, tool, block);
        }
    }

    // -----------------------------------------------------------------------
    // TREASURE
    // -----------------------------------------------------------------------

    private void handleTreasure(Player player, LivingTool tool, Block block) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - treasureCooldown.getOrDefault(uuid, 0L) < TREASURE_COOLDOWN_MS) return;

        int level = CustomEnchantManager.getEnchantLevel(tool, LivingEnchant.TREASURE);
        // Probabilidad: 0.3% base + 0.2% por nivel (Lv5 = 1.3%)
        double chance = 0.003 + (level * 0.002);
        // Ores tienen el doble de chance
        if (ORES.contains(block.getType())) chance *= 2.0;

        if (random.nextDouble() > chance) return;
        treasureCooldown.put(uuid, now);

        // Efectos visuales
        block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
        block.getWorld().spawnParticle(Particle.TOTEM, block.getLocation().add(0.5, 1.5, 0.5), 30, 0.5, 1, 0.5, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);

        // Abrir GUI de Tesoro
        openTreasureGUI(player, level, block.getLocation());
    }

    private void openTreasureGUI(Player player, int enchantLevel, Location loc) {
        String title = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ ¡Tesoro Descubierto! ✦";
        org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 27, title);

        // Borde de cristal dorado
        ItemStack border = createItem(Material.ORANGE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) gui.setItem(i, border);
        for (int i = 18; i < 27; i++) gui.setItem(i, border);
        gui.setItem(9, border); gui.setItem(17, border);

        // Generar loot aleatorio
        List<ItemStack> loot = generateTreasureLoot(enchantLevel);
        int[] lootSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < Math.min(loot.size(), lootSlots.length); i++) {
            gui.setItem(lootSlots[i], loot.get(i));
        }

        // Mensaje y sonido por nivel de loot
        String rarity = enchantLevel >= 4 ? ChatColor.DARK_PURPLE + "LEGENDARIO" :
                         enchantLevel >= 3 ? ChatColor.GOLD + "ÉPICO" :
                         enchantLevel >= 2 ? ChatColor.AQUA + "RARO" : ChatColor.GREEN + "POCO COMÚN";

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ¡Encontraste un " + rarity + ChatColor.GOLD + " Tesoro!");
        player.sendMessage(ChatColor.GRAY + "  Profundidad: Y=" + (int)loc.getY()
                + "  Bioma: " + loc.getBlock().getBiome().name().replace("_", " ").toLowerCase());
        player.sendMessage("");

        // Dar los items directamente al inventario (más usable que un GUI)
        for (ItemStack item : loot) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            // Si no cabe, tirar al suelo
            for (ItemStack leftItem : leftover.values()) {
                loc.getWorld().dropItemNaturally(loc, leftItem);
            }
        }

        // Partículas de tesoro permanentes en la ubicación
        Bukkit.getScheduler().runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
            loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0.5, 0.5, 0.5), 40, 0.3, 0.5, 0.3, 0.05);
        }, 10L);
    }

    /**
     * Genera loot escalado por nivel del encantamiento y profundidad.
     * Nivel 1: materiales básicos / Nivel 5: ítems muy raros
     */
    private List<ItemStack> generateTreasureLoot(int level) {
        List<ItemStack> loot = new ArrayList<>();
        int itemCount = 2 + level; // 3 items en lv1, 7 en lv5

        // Pool de loot por nivel
        List<ItemStack[]> pools = buildLootPools(level);

        Set<Integer> usedPools = new HashSet<>();
        for (int i = 0; i < itemCount; i++) {
            // Seleccionar pool aleatoria (sin repetir la misma pool seguida)
            int poolIdx;
            int attempts = 0;
            do {
                poolIdx = random.nextInt(pools.size());
                attempts++;
            } while (usedPools.contains(poolIdx) && attempts < 5);
            usedPools.add(poolIdx);
            if (usedPools.size() >= pools.size()) usedPools.clear();

            ItemStack[] pool = pools.get(poolIdx);
            loot.add(pool[random.nextInt(pool.length)].clone());
        }
        return loot;
    }

    private List<ItemStack[]> buildLootPools(int level) {
        List<ItemStack[]> pools = new ArrayList<>();

        // Pool básica (siempre disponible)
        pools.add(new ItemStack[]{
                new ItemStack(Material.GOLD_INGOT, 2 + random.nextInt(4)),
                new ItemStack(Material.IRON_INGOT, 3 + random.nextInt(6)),
                new ItemStack(Material.COAL, 4 + random.nextInt(8)),
                new ItemStack(Material.LAPIS_LAZULI, 3 + random.nextInt(5)),
        });

        // Pool nivel 2+
        if (level >= 2) {
            pools.add(new ItemStack[]{
                    new ItemStack(Material.DIAMOND, 1 + random.nextInt(2)),
                    new ItemStack(Material.GOLD_INGOT, 4 + random.nextInt(8)),
                    new ItemStack(Material.EMERALD, 1),
                    new ItemStack(Material.REDSTONE, 8 + random.nextInt(16)),
            });
            pools.add(new ItemStack[]{
                    createNamedItem(Material.EXPERIENCE_BOTTLE, ChatColor.AQUA + "Frasco de Sabiduría",
                            ChatColor.GRAY + "Otorga 50 XP de herramienta al usarlo"),
                    new ItemStack(Material.EXPERIENCE_BOTTLE, 3 + random.nextInt(5)),
            });
        }

        // Pool nivel 3+
        if (level >= 3) {
            pools.add(new ItemStack[]{
                    new ItemStack(Material.DIAMOND, 2 + random.nextInt(3)),
                    new ItemStack(Material.NETHERITE_SCRAP, 1),
                    createNamedItem(Material.BOOK, ChatColor.LIGHT_PURPLE + "Tomo del Subsuelo",
                            ChatColor.GRAY + "Conocimiento de las profundidades",
                            ChatColor.YELLOW + "Lleva al RuneForge para desbloquearlo"),
            });
            pools.add(new ItemStack[]{
                    new ItemStack(Material.ANCIENT_DEBRIS, 1),
                    new ItemStack(Material.DIAMOND, 1 + random.nextInt(4)),
                    new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(2)),
            });
        }

        // Pool nivel 4+
        if (level >= 4) {
            pools.add(new ItemStack[]{
                    new ItemStack(Material.NETHERITE_INGOT, 1),
                    createNamedItem(Material.NETHER_STAR, ChatColor.GOLD + "" + ChatColor.BOLD + "Fragmento Celestial",
                            ChatColor.GRAY + "Un shard de poder antiguo",
                            ChatColor.YELLOW + "⚡ Otorga 500 XP a tu herramienta"),
                    new ItemStack(Material.DIAMOND, 3 + random.nextInt(5)),
            });
        }

        // Pool nivel 5 (épica)
        if (level >= 5) {
            pools.add(new ItemStack[]{
                    createNamedItem(Material.DIAMOND_BLOCK, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Núcleo de Profundidad",
                            ChatColor.GRAY + "Cristalizado de millones de años",
                            ChatColor.LIGHT_PURPLE + "Objeto único de Nivel Legendario"),
                    new ItemStack(Material.NETHERITE_INGOT, 1 + random.nextInt(2)),
                    createNamedItem(Material.ENCHANTED_BOOK, ChatColor.DARK_PURPLE + "Libro del Vacío",
                            ChatColor.GRAY + "Encantamientos del mundo subterráneo"),
            });
        }

        return pools;
    }

    // -----------------------------------------------------------------------
    // VEIN_BREAKER
    // -----------------------------------------------------------------------

    private void handleVeinBreaker(BlockBreakEvent event, Player player, LivingTool tool, Block origin) {
        int level = CustomEnchantManager.getEnchantLevel(tool, LivingEnchant.VEIN_BREAKER);
        int maxBlocks = level == 1 ? 16 : level == 2 ? 32 : 64;
        Material oreType = origin.getType();

        // BFS para encontrar bloques del mismo tipo conectados
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && visited.size() < maxBlocks) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && neighbor.getType() == oreType) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Romper todos los bloques encontrados (excepto el original que ya lo rompe el evento)
        int broken = 0;
        for (Block veinBlock : visited) {
            if (veinBlock.equals(origin)) continue;
            veinBlock.breakNaturally(player.getInventory().getItemInMainHand());
            broken++;
        }

        if (broken > 0) {
            MessageUtils.sendActionBar(player, ChatColor.YELLOW + "⛏ Rompevenas: +" + broken + " bloques");
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 0.5f, 0.8f);
        }
    }

    // -----------------------------------------------------------------------
    // ORE_ECHO
    // -----------------------------------------------------------------------

    private void handleOreEcho(Player player, LivingTool tool, Block block) {
        // Solo al romper piedra/deepslate (no en ores — sería demasiado frecuente)
        if (block.getType() != Material.STONE && block.getType() != Material.DEEPSLATE
                && block.getType() != Material.COBBLESTONE && block.getType() != Material.TUFF) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - oreEchoCooldown.getOrDefault(uuid, 0L) < ORE_ECHO_COOLDOWN_MS) return;
        oreEchoCooldown.put(uuid, now);

        int level = CustomEnchantManager.getEnchantLevel(tool, LivingEnchant.ORE_ECHO);
        int radius = level == 1 ? 6 : level == 2 ? 10 : 16;

        // Contar ores en radio
        Map<Material, Integer> oreCounts = new LinkedHashMap<>();
        Location center = block.getLocation();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block nearby = center.getWorld().getBlockAt(
                            center.getBlockX() + dx,
                            center.getBlockY() + dy,
                            center.getBlockZ() + dz);
                    if (ORES.contains(nearby.getType())) {
                        oreCounts.merge(nearby.getType(), 1, Integer::sum);
                    }
                }
            }
        }

        if (oreCounts.isEmpty()) {
            MessageUtils.sendActionBar(player, ChatColor.GRAY + "Eco: sin ores en " + radius + " bloques");
            return;
        }

        // Mostrar resultados
        player.sendMessage(ChatColor.AQUA + "🔊 Eco de Minas (radio " + radius + " bloques):");
        oreCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(6)
                .forEach(e -> {
                    String name = e.getKey().name().replace("_ORE", "").replace("DEEPSLATE_", "")
                            .replace("_", " ").toLowerCase();
                    String color = getOreColor(e.getKey());
                    player.sendMessage("  " + color + name + ChatColor.GRAY + ": " + ChatColor.WHITE + e.getValue());
                });

        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.5f);
    }

    // -----------------------------------------------------------------------
    // AUTO_SMELT
    // -----------------------------------------------------------------------

    private void handleAutoSmelt(BlockBreakEvent event, Player player, Block block) {
        Material smelted = SMELT_MAP.get(block.getType());
        if (smelted == null) return;

        // Cancelar el drop normal y dar el item fundido
        event.setExpToDrop(0);
        event.getBlock().setType(Material.AIR);
        // Drops ya caen del evento — los cancelamos y damos el fundido
        Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().dropItemNaturally(dropLoc, new ItemStack(smelted, 1));
        event.setDropItems(false);

        MessageUtils.sendActionBar(player, ChatColor.GOLD + "🔥 Fundición Viva: " + smelted.name().replace("_", " ").toLowerCase());
    }

    // -----------------------------------------------------------------------
    // EXPLOSIVE_PICK
    // -----------------------------------------------------------------------

    private void handleExplosivePick(Player player, LivingTool tool, Block center) {
        int level = CustomEnchantManager.getEnchantLevel(tool, LivingEnchant.EXPLOSIVE_PICK);
        // 3% base por nivel
        if (random.nextDouble() > level * 0.03) return;

        Location loc = center.getLocation();
        player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.8f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc.add(0.5, 0.5, 0.5), 8, 0.3, 0.3, 0.3, 0.1);

        // Romper radio 3×3 de bloques sólidos (excluir bedrock, obsidiana fuerte, etc.)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    Block b = center.getRelative(dx, dy, dz);
                    if (isMineableByPick(b.getType())) {
                        b.breakNaturally(player.getInventory().getItemInMainHand());
                    }
                }
            }
        }
        MessageUtils.sendActionBar(player, ChatColor.RED + "💥 Pico Explosivo activado!");
    }

    // -----------------------------------------------------------------------
    // SOUL_HARVEST — se maneja en EntityDeath
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeathSoulHarvest(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;

        LivingTool tool = new LivingTool(item);
        if (!CustomEnchantManager.hasEnchant(tool, LivingEnchant.SOUL_HARVEST)) return;

        int level = CustomEnchantManager.getEnchantLevel(tool, LivingEnchant.SOUL_HARVEST);
        double bonusChance = level * 0.15; // +15% por nivel

        if (random.nextDouble() < bonusChance) {
            // Duplicar los drops del mob
            List<ItemStack> extraDrops = new ArrayList<>(event.getDrops());
            event.getDrops().addAll(extraDrops);
            MessageUtils.sendActionBar(player, ChatColor.DARK_PURPLE + "☽ Cosecha de Almas: ¡Drops dobles!");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static boolean isMineableByPick(Material m) {
        return m == Material.STONE || m == Material.COBBLESTONE || m == Material.DEEPSLATE
                || m == Material.TUFF || m == Material.GRANITE || m == Material.DIORITE
                || m == Material.ANDESITE || m == Material.GRAVEL || m == Material.DIRT
                || ORES.contains(m);
    }

    private static String getOreColor(Material m) {
        String n = m.name();
        if (n.contains("DIAMOND"))  return "" + ChatColor.AQUA;
        if (n.contains("EMERALD"))  return "" + ChatColor.GREEN;
        if (n.contains("GOLD"))     return "" + ChatColor.GOLD;
        if (n.contains("IRON"))     return "" + ChatColor.WHITE;
        if (n.contains("REDSTONE")) return "" + ChatColor.RED;
        if (n.contains("LAPIS"))    return "" + ChatColor.DARK_BLUE;
        if (n.contains("COAL"))     return "" + ChatColor.DARK_GRAY;
        if (n.contains("COPPER"))   return "" + ChatColor.GOLD;
        if (n.contains("ANCIENT"))  return "" + ChatColor.DARK_RED;
        if (n.contains("QUARTZ"))   return "" + ChatColor.WHITE;
        return "" + ChatColor.GRAY;
    }

    private static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    private static ItemStack createNamedItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
