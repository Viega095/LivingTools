package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlinkAbility extends ActiveAbility {

    // Charge tracking per player
    private static final Map<UUID, Integer> charges = new HashMap<>();
    private static final Map<UUID, Long> lastChargeRegen = new HashMap<>();

    // Combat tracking
    private static final Map<UUID, Long> damageBoostExpiry = new HashMap<>();
    private static final NamespacedKey BLINK_BOOST_KEY = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "blink_damage_boost");

    public BlinkAbility() {
        super("blink", "Blink", "Click Derecho: Teletransporte corto con cargas múltiples.", 10000);
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        // Check charges
        int currentCharges = getCharges(player);
        int maxCharges = getMaxCharges(tool);

        if (currentCharges <= 0) {
            if (!checkCooldown(player)) {
                return;
            }
            // Reset charges after cooldown
            setCharges(player, maxCharges);
            currentCharges = maxCharges;
        }

        // Check durability
        if (ConfigManager.getBoolean("abilities.blink.durability-cost-enabled")) {
            int durabilityCost = ConfigManager.getInt("abilities.blink.durability-cost");
            if (tool.getItem().getType().getMaxDurability() > 0) {
                org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) tool.getItem()
                        .getItemMeta();
                if (meta != null) {
                    int currentDamage = meta.getDamage();
                    int maxDurability = tool.getItem().getType().getMaxDurability();
                    if (currentDamage + durabilityCost >= maxDurability) {
                        player.sendMessage(ChatColor.RED + "¡Tu herramienta está demasiado dañada!");
                        return;
                    }
                }
            }
        }

        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();

        // Calculate max range with scaling
        double baseRange = ConfigManager.getDouble("abilities.blink.max-range");
        int toolLevel = tool.getData().getLevel();
        int prestige = tool.getData().getPrestige();
        double rangePerLevel = ConfigManager.getDouble("abilities.blink.level-scaling.range-per-level");
        double prestigeBonus = ConfigManager.getDouble("abilities.blink.level-scaling.prestige-bonus");

        double maxRange = baseRange + (toolLevel * rangePerLevel) + (prestige * prestigeBonus);

        // Enhanced raytrace with validation
        Location target = findValidDestination(start, direction, maxRange);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "¡Destino no válido!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
            return;
        }

        // Particle trail
        createParticleTrail(start, target, tool);

        // Teleport
        player.teleport(target.setDirection(player.getLocation().getDirection()));

        // Apply safe-fall effect
        int safeFallDuration = ConfigManager.getInt("abilities.blink.safe-fall-duration");
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_FALLING, safeFallDuration, 0, false, false, true));

        // Combat features
        applyCombatEffects(player, target);

        // Effects
        int particleDensity = ConfigManager.getInt("abilities.blink.particle-density");
        Particle particleType = getParticleType(toolLevel, prestige);

        player.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.2f);
        player.getWorld().spawnParticle(particleType, start, particleDensity, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(particleType, target, particleDensity, 0.5, 1, 0.5, 0.1);

        // Consume charge
        currentCharges--;
        setCharges(player, currentCharges);

        // Apply durability cost
        if (ConfigManager.getBoolean("abilities.blink.durability-cost-enabled")) {
            consumeDurability(tool, ConfigManager.getInt("abilities.blink.durability-cost"));
        }

        // Update action bar
        updateActionBar(player, currentCharges, maxCharges);

        // Track charge regeneration
        lastChargeRegen.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private Location findValidDestination(Location start, Vector direction, double maxRange) {
        Location target = start.clone();
        Location lastValid = start.clone();

        for (double i = 0; i < maxRange; i += 0.5) {
            Location next = target.clone().add(direction.clone().multiply(0.5));

            // Check if hit solid block
            if (next.getBlock().getType().isSolid()) {
                break;
            }

            // Enhanced validation
            if (isValidDestination(next)) {
                lastValid = next.clone();
                target = next;
            } else {
                break; // Stop if we hit invalid space
            }
        }

        // Final validation
        if (isValidDestination(lastValid) && lastValid.distance(start) > 1.0) {
            return lastValid;
        }

        return null;
    }

    private boolean isValidDestination(Location loc) {
        // Check for 2 blocks of clearance
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();

        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }

        // Check ground below (within 3 blocks)
        boolean hasGround = false;
        for (int i = 0; i < 4; i++) {
            Block below = loc.clone().subtract(0, i, 0).getBlock();
            if (below.getType().isSolid() && below.getType() != Material.LAVA) {
                hasGround = true;
                break;
            }
        }

        if (!hasGround) {
            return false; // Prevent teleporting into void
        }

        // Check for dangerous blocks
        Material feetType = feet.getType();
        if (feetType == Material.LAVA || feetType == Material.FIRE ||
                feetType == Material.SOUL_FIRE || feetType == Material.CACTUS) {
            return false;
        }

        return true;
    }

    private void createParticleTrail(Location start, Location end, LivingTool tool) {
        int particleDensity = ConfigManager.getInt("abilities.blink.particle-density");
        Particle particleType = getParticleType(tool.getData().getLevel(), tool.getData().getPrestige());

        Vector direction = end.toVector().subtract(start.toVector());
        double distance = start.distance(end);

        // Spawn particles every 0.5 blocks
        for (double i = 0; i < distance; i += 0.5) {
            Location point = start.clone().add(direction.clone().normalize().multiply(i));
            point.getWorld().spawnParticle(particleType, point, Math.max(1, particleDensity / 10), 0.1, 0.1, 0.1, 0);
        }
    }

    private Particle getParticleType(int level, int prestige) {
        if (prestige > 0) {
            return Particle.SOUL_FIRE_FLAME;
        } else if (level >= 75) {
            return Particle.DRAGON_BREATH;
        } else if (level >= 50) {
            return Particle.ENCHANTMENT_TABLE;
        } else if (level >= 25) {
            return Particle.PORTAL;
        }
        return Particle.PORTAL;
    }

    private void applyCombatEffects(Player player, Location target) {
        // AoE knockback
        double knockbackPower = ConfigManager.getDouble("abilities.blink.knockback-power");
        for (Entity entity : target.getWorld().getNearbyEntities(target, 3, 3, 3)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector knockback = entity.getLocation().toVector()
                        .subtract(target.toVector()).normalize().multiply(knockbackPower);
                knockback.setY(0.3);
                entity.setVelocity(knockback);
            }
        }

        // Brief invulnerability
        int invulnDuration = ConfigManager.getInt("abilities.blink.invulnerability-duration");
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE, invulnDuration, 2, false, false, false));

        // Set damage boost window
        int bonusWindow = ConfigManager.getInt("abilities.blink.damage-bonus-window");
        long expiryTime = System.currentTimeMillis() + (bonusWindow * 50); // Convert ticks to ms
        damageBoostExpiry.put(player.getUniqueId(), expiryTime);

        // Store in PDC for listener access
        player.getPersistentDataContainer().set(BLINK_BOOST_KEY,
                PersistentDataType.DOUBLE, ConfigManager.getDouble("abilities.blink.damage-bonus"));
    }

    private void consumeDurability(LivingTool tool, int amount) {
        if (tool.getItem().getType().getMaxDurability() > 0) {
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) tool.getItem()
                    .getItemMeta();
            if (meta != null) {
                meta.setDamage(meta.getDamage() + amount);
                tool.getItem().setItemMeta((org.bukkit.inventory.meta.ItemMeta) meta);
            }
        }
    }

    private int getCharges(Player player) {
        regenerateCharges(player);
        return charges.getOrDefault(player.getUniqueId(), 0);
    }

    private void setCharges(Player player, int amount) {
        charges.put(player.getUniqueId(), amount);
    }

    private int getMaxCharges(LivingTool tool) {
        int baseCharges = ConfigManager.getInt("abilities.blink.charges");
        int level = tool.getData().getLevel();

        // Scale with level: +1 charge every 50 levels
        int bonusCharges = level / 50;
        return Math.min(baseCharges + bonusCharges, 3); // Max 3 charges
    }

    private void regenerateCharges(Player player) {
        UUID uuid = player.getUniqueId();
        if (!lastChargeRegen.containsKey(uuid)) {
            return;
        }

        long lastRegen = lastChargeRegen.get(uuid);
        long now = System.currentTimeMillis();
        int regenTime = ConfigManager.getInt("abilities.blink.charge-regen-time") * 1000;

        int currentCharges = charges.getOrDefault(uuid, 0);
        int chargesToAdd = (int) ((now - lastRegen) / regenTime);

        if (chargesToAdd > 0 && currentCharges < 3) {
            currentCharges = Math.min(currentCharges + chargesToAdd, 3);
            charges.put(uuid, currentCharges);
            lastChargeRegen.put(uuid, now);
        }
    }

    private void updateActionBar(Player player, int current, int max) {
        StringBuilder display = new StringBuilder(ChatColor.AQUA + "Blink: ");

        // Show charges as symbols
        for (int i = 0; i < max; i++) {
            if (i < current) {
                display.append(ChatColor.GREEN).append("◆");
            } else {
                display.append(ChatColor.GRAY).append("◇");
            }
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(display.toString()));
    }

    // Static method to check if player has damage boost (called from damage
    // listener)
    public static boolean hasDamageBoost(Player player) {
        Long expiry = damageBoostExpiry.get(player.getUniqueId());
        if (expiry != null && System.currentTimeMillis() < expiry) {
            return true;
        }
        // Clean up expired boost
        if (expiry != null) {
            damageBoostExpiry.remove(player.getUniqueId());
            player.getPersistentDataContainer().remove(BLINK_BOOST_KEY);
        }
        return false;
    }

    public static double getDamageBoost(Player player) {
        if (hasDamageBoost(player)) {
            Double boost = player.getPersistentDataContainer()
                    .get(BLINK_BOOST_KEY, PersistentDataType.DOUBLE);
            return boost != null ? boost : 0.0;
        }
        return 0.0;
    }

    public static void consumeDamageBoost(Player player) {
        damageBoostExpiry.remove(player.getUniqueId());
        player.getPersistentDataContainer().remove(BLINK_BOOST_KEY);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD");
    }
}
