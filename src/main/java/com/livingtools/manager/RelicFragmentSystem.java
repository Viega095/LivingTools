package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * RelicFragmentSystem — fragmentos de reliquia que dropean mobs raros.
 *
 * Hay 5 tipos de fragmento (FUEGO, HIELO, TRUENO, SOMBRA, LUZ).
 * Al completar un set de 5 fragmentos distintos, el jugador puede
 * usar /lt relic para fusionarlos en una Reliquia Ancestral que otorga
 * un bonus permanente de +20% XP durante 24h de juego acumuladas.
 *
 * Drop rates:
 *   - Mobs normales: 0.3% si la herramienta tiene prestige ≥ 1
 *   - Mobs boss (Wither, EnderDragon): 100% (múltiples fragmentos)
 *   - Mobs hostiles en noche: 0.5%
 *
 * Fragmentos por personalidad del mob:
 *   - FUEGO  → Blazes, Magma, Ghasts
 *   - HIELO  → Snow Golems, Strays, Frozen biomes mobs
 *   - TRUENO → Wither, Charged Creeper, Lightning-hit mobs
 *   - SOMBRA → Phantoms, Warden, Endermen, Shulkers
 *   - LUZ    → Witches con luz solar, Bees, Allays
 */
public class RelicFragmentSystem implements Listener {

    public enum RelicType {
        FUEGO   ("Fragmento de Fuego",   Material.BLAZE_POWDER,    ChatColor.RED,         "🔥"),
        HIELO   ("Fragmento de Hielo",   Material.BLUE_ICE,        ChatColor.AQUA,        "❄"),
        TRUENO  ("Fragmento de Trueno",  Material.LIGHTNING_ROD,   ChatColor.YELLOW,      "⚡"),
        SOMBRA  ("Fragmento de Sombra",  Material.ECHO_SHARD,      ChatColor.DARK_PURPLE, "☽"),
        LUZ     ("Fragmento de Luz",     Material.END_CRYSTAL,     ChatColor.WHITE,       "✦");

        final String itemName;
        final Material material;
        final ChatColor color;
        final String symbol;

        RelicType(String itemName, Material material, ChatColor color, String symbol) {
            this.itemName = itemName;
            this.material = material;
            this.color = color;
            this.symbol = symbol;
        }
    }

    // PDC keys
    private static NamespacedKey KEY_RELIC_TYPE;
    private static NamespacedKey keyRelicType() {
        if (KEY_RELIC_TYPE == null)
            KEY_RELIC_TYPE = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "relic_fragment_type");
        return KEY_RELIC_TYPE;
    }
    private static NamespacedKey KEY_RELIC_BONUS_EXPIRY;
    public static NamespacedKey keyRelicBonusExpiry() {
        if (KEY_RELIC_BONUS_EXPIRY == null)
            KEY_RELIC_BONUS_EXPIRY = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "relic_bonus_expiry_ms");
        return KEY_RELIC_BONUS_EXPIRY;
    }

    private static final Random random = new Random();
    private static final double RELIC_BONUS_XP = 0.20; // 20% extra XP

    // -----------------------------------------------------------------------
    // Drop logic
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack held = killer.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        LivingTool tool = new LivingTool(held);
        EntityType type = event.getEntityType();

        // Determinar tipo de fragmento según el mob
        RelicType relicType = getRelicTypeForMob(type, event.getEntity());
        if (relicType == null) return;

        // Calcular probabilidad de drop
        double dropChance = getDropChance(tool.getData(), type, killer);
        if (random.nextDouble() > dropChance) return;

        // Soltar fragmento
        ItemStack fragment = createFragment(relicType);
        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), fragment);

        MessageUtils.sendActionBar(killer,
                relicType.color + relicType.symbol + " ¡Fragmento de Reliquia! " + ChatColor.WHITE + relicType.itemName);
        killer.playSound(killer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.8f);
    }

    // -----------------------------------------------------------------------
    // /lt relic fusion command
    // -----------------------------------------------------------------------

    /**
     * Intenta fusionar los fragmentos del inventario del jugador en una Reliquia.
     * Retorna true si se fusionó con éxito.
     */
    public static boolean tryFuseRelic(Player player, LivingTool tool) {
        Map<RelicType, Integer> found = collectFragments(player);

        // ¿Tiene al menos 1 de cada tipo?
        for (RelicType type : RelicType.values()) {
            if (!found.containsKey(type) || found.get(type) == 0) {
                player.sendMessage(ChatColor.RED + "✗ Te falta el " + type.color + type.itemName
                        + ChatColor.RED + " para completar el set.");
                showMissingFragments(player, found);
                return false;
            }
        }

        // Consumir 1 de cada tipo
        consumeFragments(player, found);

        // Aplicar bonus a la herramienta (24h = 86400000ms)
        ItemStack item = tool.getItem();
        if (!item.hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        long expiry = System.currentTimeMillis() + 86_400_000L;
        meta.getPersistentDataContainer().set(keyRelicBonusExpiry(), PersistentDataType.LONG, expiry);
        item.setItemMeta(meta);

        // Efectos
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦✦ ¡" + ChatColor.GOLD + "RELIQUIA ANCESTRAL" + ChatColor.DARK_PURPLE + " activada! ✦✦");
        player.sendMessage(ChatColor.GRAY + "  +" + (int)(RELIC_BONUS_XP * 100) + "% XP durante 24 horas.");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 0.8f);
        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 80, 1, 1, 1, 0.3);
        return true;
    }

    /**
     * Retorna el multiplicador de XP de la reliquia (1.0 si expiró o no tiene).
     */
    public static double getRelicXPMultiplier(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return 1.0;
        Long expiry = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(keyRelicBonusExpiry(), PersistentDataType.LONG);
        if (expiry == null || System.currentTimeMillis() > expiry) return 1.0;
        return 1.0 + RELIC_BONUS_XP;
    }

    /**
     * Tiempo restante del bonus de reliquia en minutos (0 si no activo).
     */
    public static long getRelicRemainingMinutes(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return 0;
        Long expiry = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(keyRelicBonusExpiry(), PersistentDataType.LONG);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining / 60_000L);
    }

    // -----------------------------------------------------------------------
    // Item creation
    // -----------------------------------------------------------------------

    public static ItemStack createFragment(RelicType type) {
        ItemStack item = new ItemStack(type.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(type.color + "" + ChatColor.BOLD + type.symbol + " " + type.itemName);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "Tipo: " + type.color + type.name());
        lore.add(ChatColor.GRAY + "Colecciona los 5 fragmentos para");
        lore.add(ChatColor.GRAY + "forjar una " + ChatColor.GOLD + "Reliquia Ancestral" + ChatColor.GRAY + ".");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Usa /lt relic con el set completo.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(keyRelicType(), PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isRelicFragment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(keyRelicType(), PersistentDataType.STRING);
    }

    public static RelicType getFragmentType(ItemStack item) {
        if (!isRelicFragment(item)) return null;
        String s = item.getItemMeta().getPersistentDataContainer()
                .get(keyRelicType(), PersistentDataType.STRING);
        try { return RelicType.valueOf(s); } catch (Exception e) { return null; }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static RelicType getRelicTypeForMob(EntityType type, Entity entity) {
        if (type == EntityType.ENDER_DRAGON) {
            return RelicType.values()[new Random().nextInt(RelicType.values().length)];
        }
        if (type == EntityType.BLAZE || type == EntityType.MAGMA_CUBE || type == EntityType.GHAST) {
            return RelicType.FUEGO;
        }
        if (type == EntityType.SNOWMAN || type == EntityType.STRAY) {
            return RelicType.HIELO;
        }
        if (type == EntityType.WITHER || type == EntityType.CREEPER) {
            return RelicType.TRUENO;
        }
        if (type == EntityType.PHANTOM || type == EntityType.WARDEN
                || type == EntityType.ENDERMAN || type == EntityType.SHULKER) {
            return RelicType.SOMBRA;
        }
        if (type == EntityType.BEE || type == EntityType.ALLAY) {
            return RelicType.LUZ;
        }
        return null;
    }

    private static double getDropChance(ToolData data, EntityType type, Player player) {
        // Mobs especiales
        if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON) return 1.0;

        double base = data.getPrestige() > 0 ? 0.005 : 0.003;
        // Bonus de noche
        long time = player.getWorld().getTime();
        if (time > 13000 || time < 500) base += 0.002;
        return base;
    }

    private static Map<RelicType, Integer> collectFragments(Player player) {
        Map<RelicType, Integer> found = new EnumMap<>(RelicType.class);
        for (ItemStack item : player.getInventory().getContents()) {
            if (!isRelicFragment(item)) continue;
            RelicType t = getFragmentType(item);
            if (t != null) found.merge(t, item.getAmount(), Integer::sum);
        }
        return found;
    }

    private static void consumeFragments(Player player, Map<RelicType, Integer> found) {
        for (RelicType type : RelicType.values()) {
            int toRemove = 1;
            for (ItemStack item : player.getInventory().getContents()) {
                if (toRemove <= 0) break;
                if (!isRelicFragment(item)) continue;
                if (getFragmentType(item) != type) continue;
                int remove = Math.min(toRemove, item.getAmount());
                item.setAmount(item.getAmount() - remove);
                toRemove -= remove;
            }
        }
    }

    private static void showMissingFragments(Player player, Map<RelicType, Integer> found) {
        player.sendMessage(ChatColor.GRAY + "  Fragmentos actuales:");
        for (RelicType type : RelicType.values()) {
            int qty = found.getOrDefault(type, 0);
            String status = qty > 0 ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ";
            player.sendMessage("    " + status + type.color + type.symbol + " " + type.itemName
                    + ChatColor.GRAY + (qty > 0 ? " ×" + qty : " (falta)"));
        }
    }
}
