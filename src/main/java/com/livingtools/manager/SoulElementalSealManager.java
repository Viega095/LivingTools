package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * SoulElementalSealManager — Gestiona los Sellos Elementales Ancestrales que se pueden
 * templar en la Forja Rítmica de Almas.
 */
public class SoulElementalSealManager implements Listener {

    private static NamespacedKey KEY_ELEMENTAL_SEAL;

    public static NamespacedKey getKeyElementalSeal() {
        if (KEY_ELEMENTAL_SEAL == null) {
            KEY_ELEMENTAL_SEAL = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_elemental_seal");
        }
        return KEY_ELEMENTAL_SEAL;
    }

    public enum ElementalSeal {
        VOLCANIC_FURY("Furia Volcánica", ChatColor.RED, Material.MAGMA_CREAM,
                "Auto-funde minerales con +25% de drops e incinera enemigos con fuego de almas."),
        GLACIAL_FROST("Escarcha Glacial", ChatColor.AQUA, Material.BLUE_ICE,
                "Ralentiza y congela enemigos. Convierte agua en hielo y lava en obsidiana al minar."),
        CELESTIAL_THUNDER("Trueno Celestial", ChatColor.YELLOW, Material.LIGHTNING_ROD,
                "Descargas eléctricas en cadena y minería de vetas conectadas (Vein-Strike)."),
        VOID_VORTEX("Vórtice del Vacío", ChatColor.DARK_PURPLE, Material.ENDER_EYE,
                "Atracción magnética de ítems en 8 bloques y +15% de daño a jefes y esbirros.");

        private final String displayName;
        private final ChatColor color;
        private final Material icon;
        private final String description;

        ElementalSeal(String displayName, ChatColor color, Material icon, String description) {
            this.displayName = displayName;
            this.color = color;
            this.icon = icon;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public ChatColor getColor() { return color; }
        public Material getIcon() { return icon; }
        public String getDescription() { return description; }
        public String getFormattedName() { return color + "" + ChatColor.BOLD + "✦ " + displayName; }
    }

    public static ElementalSeal getSeal(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String sealName = pdc.get(getKeyElementalSeal(), PersistentDataType.STRING);
        if (sealName == null) return null;
        try {
            return ElementalSeal.valueOf(sealName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void applySeal(ItemStack item, ElementalSeal seal) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (seal == null) {
            meta.getPersistentDataContainer().remove(getKeyElementalSeal());
        } else {
            meta.getPersistentDataContainer().set(getKeyElementalSeal(), PersistentDataType.STRING, seal.name());
        }
        item.setItemMeta(meta);
        if (LivingTool.isLivingTool(item)) {
            new LivingTool(item).updateLore();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        ElementalSeal seal = getSeal(held);
        if (seal == null) return;

        Block block = event.getBlock();
        Location loc = block.getLocation();

        // 1. VOLCANIC FURY: Auto-Smelt + Bonus
        if (seal == ElementalSeal.VOLCANIC_FURY) {
            Material type = block.getType();
            ItemStack smelted = getSmeltedDrop(type);
            if (smelted != null) {
                event.setDropItems(false);
                int amount = 1;
                if (Math.random() < 0.25) {
                    amount = 2; // +25% bonus drop
                    player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.4f);
                }
                smelted.setAmount(amount);
                loc.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), smelted);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.3, 0.3, 0.3, 0.05);
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.8f);
            }
        }

        // 2. GLACIAL FROST: Lava/Water cooling while mining
        else if (seal == ElementalSeal.GLACIAL_FROST) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block nearby = block.getRelative(x, y, z);
                        if (nearby.getType() == Material.WATER) {
                            nearby.setType(Material.FROSTED_ICE);
                        } else if (nearby.getType() == Material.LAVA) {
                            nearby.setType(Material.OBSIDIAN);
                        }
                    }
                }
            }
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 12, 0.4, 0.4, 0.4, 0.02);
        }

        // 3. CELESTIAL THUNDER: Vein-Strike
        else if (seal == ElementalSeal.CELESTIAL_THUNDER) {
            if (isOre(block.getType()) && Math.random() < 0.20) {
                mineVein(player, block, block.getType(), 0, new HashSet<>());
                loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 25, 0.6, 0.6, 0.6, 0.1);
                player.playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 0.6f, 1.8f);
            }
        }

        // 4. VOID VORTEX: Vacuum pull drops
        else if (seal == ElementalSeal.VOID_VORTEX) {
            Bukkit.getScheduler().runTaskLater(LivingToolsPlugin.getInstance(), () -> {
                if (!player.isOnline()) return;
                Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 8, 8, 8);
                for (Entity e : entities) {
                    if (e instanceof Item) {
                        e.teleport(player.getLocation().add(0, 0.5, 0));
                    }
                }
            }, 2L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        ElementalSeal seal = getSeal(held);
        if (seal == null) return;

        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) event.getEntity();
        Location targetLoc = target.getLocation();

        // 1. VOLCANIC FURY: Soul fire + extra burst
        if (seal == ElementalSeal.VOLCANIC_FURY) {
            target.setFireTicks(100);
            targetLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, targetLoc.add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
            player.playSound(targetLoc, Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.2f);
        }

        // 2. GLACIAL FROST: Slowness IV & Freeze
        else if (seal == ElementalSeal.GLACIAL_FROST) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 3));
            target.setFreezeTicks(140);
            targetLoc.getWorld().spawnParticle(Particle.SNOW_SHOVEL, targetLoc.add(0, 1, 0), 20, 0.4, 0.5, 0.4, 0.05);
            player.playSound(targetLoc, Sound.BLOCK_POWDER_SNOW_BREAK, 0.9f, 1.5f);
        }

        // 3. CELESTIAL THUNDER: Chain Lightning Arc
        else if (seal == ElementalSeal.CELESTIAL_THUNDER) {
            targetLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, targetLoc.add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.1);
            player.playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7f, 1.8f);
            Collection<Entity> nearby = targetLoc.getWorld().getNearbyEntities(targetLoc, 4, 4, 4);
            for (Entity e : nearby) {
                if (e instanceof LivingEntity && !e.equals(player) && !e.equals(target)) {
                    ((LivingEntity) e).damage(4.0, player);
                    e.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, e.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }

        // 4. VOID VORTEX: Boss killer + pull
        else if (seal == ElementalSeal.VOID_VORTEX) {
            boolean isBoss = target.getCustomName() != null && target.getCustomName().contains("✦");
            if (isBoss) {
                event.setDamage(event.getDamage() * 1.15); // +15% Damage against bosses
            }
            targetLoc.getWorld().spawnParticle(Particle.PORTAL, targetLoc.add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.1);
            player.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.4f);
        }
    }

    private static void mineVein(Player player, Block block, Material type, int depth, Set<Block> visited) {
        if (depth > 8 || visited.size() >= 12) return;
        visited.add(block);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block next = block.getRelative(x, y, z);
                    if (next.getType() == type && !visited.contains(next)) {
                        next.breakNaturally(player.getInventory().getItemInMainHand());
                        mineVein(player, next, type, depth + 1, visited);
                    }
                }
            }
        }
    }

    private static boolean isOre(Material mat) {
        String name = mat.name();
        return name.endsWith("_ORE") || name.equals("ANCIENT_DEBRIS");
    }

    private static ItemStack getSmeltedDrop(Material ore) {
        switch (ore) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return new ItemStack(Material.IRON_INGOT);
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case NETHER_GOLD_ORE:
                return new ItemStack(Material.GOLD_INGOT);
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return new ItemStack(Material.COPPER_INGOT);
            case ANCIENT_DEBRIS:
                return new ItemStack(Material.NETHERITE_SCRAP);
            default:
                return null;
        }
    }
}
