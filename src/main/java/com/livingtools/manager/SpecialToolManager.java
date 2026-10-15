package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * SpecialToolManager — Gestiona herramientas míticas vivientes avanzadas:
 * 1. Hoz Segadora de Almas (Cosecha 5x5 con re-siembra y drenaje de vida en combate)
 * 2. Martillo Tectónico Viviente (Minería 3x3x3 y Fisura Sísmica por impacto)
 */
public class SpecialToolManager implements Listener {

    private static final NamespacedKey KEY_SPECIAL_TOOL = new NamespacedKey(LivingToolsPlugin.getInstance(), "livingtool_special_type");
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    public enum SpecialToolType {
        SOUL_SCYTHE(
                Material.NETHERITE_HOE,
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Hoz Segadora de Almas ✦",
                Arrays.asList(
                        ChatColor.GRAY + "Herramienta viviente forjada en los confines del Reino Místico.",
                        "",
                        ChatColor.LIGHT_PURPLE + "✦ Cosecha Áurea 5x5: " + ChatColor.WHITE + "Siega y replanta cultivos maduros al instante.",
                        ChatColor.DARK_AQUA + "✦ Segador Espiritual: " + ChatColor.WHITE + "Drena 20% del daño infligido como vida propia.",
                        ChatColor.YELLOW + "✦ Click Derecho: " + ChatColor.GOLD + "Torbellino de Almas (Radio 6 bloques).",
                        "",
                        ChatColor.GOLD + "★ Calidad: Reliquia Mítica Viviente"
                )
        ),
        TECTONIC_HAMMER(
                Material.NETHERITE_PICKAXE,
                ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Martillo Tectónico Viviente ✦",
                Arrays.asList(
                        ChatColor.GRAY + "Imbuido con el núcleo resonante de las profundidades tectónicas.",
                        "",
                        ChatColor.YELLOW + "✦ Demolición Sísmica 3x3x3: " + ChatColor.WHITE + "Excava túneles cúbicos al romper roca.",
                        ChatColor.RED + "✦ Furia Sísmica: " + ChatColor.WHITE + "Golpes críticos causan ondas de choque a enemigos.",
                        ChatColor.AQUA + "✦ Click Derecho al Suelo: " + ChatColor.DARK_AQUA + "Fisura Tectónica (Lanza enemigos por el aire).",
                        "",
                        ChatColor.GOLD + "★ Calidad: Reliquia Mítica Viviente"
                )
        );

        private final Material material;
        private final String name;
        private final List<String> lore;

        SpecialToolType(Material material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }
    }

    public static ItemStack createSpecialTool(SpecialToolType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getName());
            meta.setLore(type.getLore());
            meta.getPersistentDataContainer().set(KEY_SPECIAL_TOOL, PersistentDataType.STRING, type.name());
            item.setItemMeta(meta);
        }

        // Registrar como LivingTool nativa con nivel 1 y experiencia
        ToolData td = new ToolData(item);
        td.initialize();
        td.setCustomName(type.getName());
        LivingTool lt = new LivingTool(item);
        lt.updateLore();

        return item;
    }

    public static SpecialToolType getSpecialType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String raw = pdc.get(KEY_SPECIAL_TOOL, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return SpecialToolType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        SpecialToolType type = getSpecialType(held);
        if (type == null) return;

        Block broken = event.getBlock();

        if (type == SpecialToolType.SOUL_SCYTHE) {
            handleScytheHarvest(player, broken);
        } else if (type == SpecialToolType.TECTONIC_HAMMER) {
            handleHammerMine(player, broken, event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        SpecialToolType type = getSpecialType(held);
        if (type == null) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long cd = COOLDOWNS.getOrDefault(uuid, 0L);
        if (now < cd) {
            long remaining = (cd - now) / 1000;
            player.sendMessage(ChatColor.RED + "⌛ Habilidad especial en enfriamiento (" + remaining + "s restantes).");
            return;
        }

        if (type == SpecialToolType.SOUL_SCYTHE) {
            // Torbellino de Almas
            COOLDOWNS.put(uuid, now + 12000L); // 12s cd
            triggerSoulWhirlwind(player);
        } else if (type == SpecialToolType.TECTONIC_HAMMER) {
            // Fisura Sísmica
            COOLDOWNS.put(uuid, now + 10000L); // 10s cd
            triggerTectonicFissure(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack held = player.getInventory().getItemInMainHand();
        SpecialToolType type = getSpecialType(held);
        if (type == null) return;

        if (type == SpecialToolType.SOUL_SCYTHE) {
            // Drenaje espiritual de vida
            double heal = Math.min(event.getFinalDamage() * 0.20, 4.0);
            double newHp = Math.min(player.getHealth() + heal, player.getMaxHealth());
            player.setHealth(newHp);
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_TAKEN, 0.7f, 1.5f);
        } else if (type == SpecialToolType.TECTONIC_HAMMER) {
            // Golpe sísmico en combate
            event.getEntity().setVelocity(player.getLocation().getDirection().multiply(1.2).setY(0.4));
            player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, event.getEntity().getLocation().add(0, 0.5, 0), 1);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.8f, 0.6f);
        }
    }

    private void handleScytheHarvest(Player player, Block center) {
        World world = center.getWorld();
        int harvested = 0;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block b = center.getRelative(x, 0, z);
                if (b.getBlockData() instanceof Ageable) {
                    Ageable age = (Ageable) b.getBlockData();
                    if (age.getAge() >= age.getMaximumAge()) {
                        Material cropType = b.getType();
                        b.breakNaturally(player.getInventory().getItemInMainHand());
                        b.setType(cropType);
                        Ageable reset = (Ageable) b.getBlockData();
                        reset.setAge(0);
                        b.setBlockData(reset);
                        harvested++;
                    }
                }
            }
        }

        if (harvested > 0) {
            world.playSound(center.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            world.spawnParticle(Particle.VILLAGER_HAPPY, center.getLocation().add(0.5, 0.5, 0.5), 15, 1.2, 0.5, 1.2, 0);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "🌾 ¡Cosechados " + harvested + " cultivos mágicos y resembrados automáticamente!");
        }
    }

    private void handleHammerMine(Player player, Block center, BlockBreakEvent originalEvent) {
        Material mat = center.getType();
        if (!isMineableStone(mat)) return;

        World world = center.getWorld();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block neighbor = center.getRelative(x, y, z);
                    if (isMineableStone(neighbor.getType())) {
                        neighbor.breakNaturally(player.getInventory().getItemInMainHand());
                    }
                }
            }
        }
        world.playSound(center.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.8f);
        world.spawnParticle(Particle.BLOCK_CRACK, center.getLocation().add(0.5, 0.5, 0.5), 25, 0.6, 0.6, 0.6, mat.createBlockData());
    }

    private boolean isMineableStone(Material m) {
        return m == Material.STONE || m == Material.COBBLESTONE || m == Material.DEEPSLATE ||
                m == Material.COBBLED_DEEPSLATE || m == Material.ANDESITE || m == Material.DIORITE ||
                m == Material.GRANITE || m == Material.TUFF || m == Material.NETHERRACK ||
                m == Material.BASALT || m == Material.BLACKSTONE || m == Material.END_STONE;
    }

    private void triggerSoulWhirlwind(Player player) {
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.8f);
        world.spawnParticle(Particle.SOUL, player.getLocation().add(0, 1.0, 0), 60, 2.5, 0.5, 2.5, 0.05);

        for (Entity e : player.getNearbyEntities(6, 4, 6)) {
            if (e instanceof LivingEntity && !(e instanceof Player)) {
                LivingEntity le = (LivingEntity) e;
                le.damage(16.0, player);
                le.setVelocity(new Vector(0, 0.6, 0));
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, le.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.02);
            }
        }
        player.sendMessage(ChatColor.DARK_PURPLE + "🌀 ¡Has liberado el Torbellino de Almas Ancestral!");
    }

    private void triggerTectonicFissure(Player player) {
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().setY(0).normalize();

        world.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2f, 0.5f);

        for (int i = 1; i <= 8; i++) {
            Location loc = player.getLocation().clone().add(dir.clone().multiply(i));
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.add(0, 0.2, 0), 8, 0.2, 0.1, 0.2, 0.02);
            world.spawnParticle(Particle.BLOCK_CRACK, loc, 15, 0.3, 0.3, 0.3, Material.COBBLED_DEEPSLATE.createBlockData());

            for (Entity e : world.getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                if (e instanceof LivingEntity && !e.equals(player)) {
                    LivingEntity le = (LivingEntity) e;
                    le.damage(18.0, player);
                    le.setVelocity(new Vector(0, 0.9, 0).add(dir.clone().multiply(0.5)));
                }
            }
        }
        player.sendMessage(ChatColor.GOLD + "💥 ¡Has desatado una Fisura Tectónica destructiva!");
    }
}
