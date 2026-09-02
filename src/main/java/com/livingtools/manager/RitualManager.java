package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RitualManager {

    public static boolean isValidAltar(Block altarCenter) {
        return checkStructure(altarCenter);
    }

    public static boolean tryPerformRitual(Player player, Block altarCenter) {
        if (altarCenter.getType() != Material.ENCHANTING_TABLE) {
            return false;
        }

        // Check Structure
        if (!checkStructure(altarCenter)) {
            player.sendMessage(ConfigManager.getMessage("ritual_invalid_structure"));
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return false;
        }

        LivingTool tool = new LivingTool(item);

        // Corruption Check (Phase 22)
        if (tool.getData().getCorruption() > 75) {
            if (Math.random() < 0.5) { // 50% chance to fail
                player.sendMessage(
                        org.bukkit.ChatColor.DARK_RED + "¡La corrupción de tu herramienta interfiere con el ritual!");
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1, 0.5f);

                // Spawn Corrupt Spirit
                Location spawnLoc = altarCenter.getLocation().add(0.5, 1, 0.5);
                org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) spawnLoc.getWorld().spawnEntity(spawnLoc,
                        org.bukkit.entity.EntityType.ZOMBIE);
                zombie.setCustomName(org.bukkit.ChatColor.DARK_RED + "Espíritu Corrupto");
                zombie.setCustomNameVisible(true);
                zombie.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                zombie.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                zombie.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 9999, 1));
                zombie.addPotionEffect(
                        new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 9999, 1));

                return false;
            }
        }

        // Perform Ritual
        // Check for Infusion Catalysts
        String infusionId = detectInfusion(altarCenter);
        if (infusionId != null) {
            if (infusionId.equals("ascension")) {
                return performAscension(player, tool, altarCenter.getLocation().add(0.5, 1, 0.5));
            }
            if (InfusionManager.applyInfusion(player, tool, infusionId)) {
                playRitualEffects(altarCenter.getLocation().add(0.5, 1, 0.5));
                return true;
            }
        }

        // Check for Enchantment Sacrifice (Book)
        if (performEnchantmentRitual(player, tool, altarCenter.getLocation().add(0.5, 1, 0.5))) {
            return true;
        }

        // Default Ritual: Repair + XP -> DISABLED to prevent farming
        // Only specific rituals (Enchanting, Infusion, Ascension) are allowed now.

        player.sendMessage(org.bukkit.ChatColor.GRAY
                + "El altar permanece en silencio... Quizás necesites un catalizador o un sacrificio.");
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 0.5f);

        return false;
    }

    private static boolean performEnchantmentRitual(Player player, LivingTool tool, Location center) {
        // Scan for Enchanted Book
        for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, 1, 1, 1)) {
            if (entity instanceof org.bukkit.entity.Item) {
                ItemStack item = ((org.bukkit.entity.Item) entity).getItemStack();
                if (item.getType() == Material.ENCHANTED_BOOK) {
                    org.bukkit.inventory.meta.EnchantmentStorageMeta meta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) item
                            .getItemMeta();
                    if (meta.getStoredEnchants().isEmpty())
                        continue;

                    // Take the first enchantment found
                    java.util.Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry = meta.getStoredEnchants()
                            .entrySet().iterator().next();
                    org.bukkit.enchantments.Enchantment enchant = entry.getKey();
                    int level = entry.getValue();

                    // Check compatibility
                    if (!enchant.canEnchantItem(tool.getItem())) {
                        player.sendMessage(
                                org.bukkit.ChatColor.RED + "Este encantamiento no es compatible con tu herramienta.");
                        return false;
                    }

                    // Apply to Tool
                    tool.getItem().addUnsafeEnchantment(enchant, level);

                    // Consume Book
                    entity.remove();

                    // Visuals
                    playRitualEffects(center);
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "¡Ritual de Encantamiento completado! "
                            + org.bukkit.ChatColor.AQUA + enchant.getKey().getKey() + " " + level);
                    return true;
                } else if (item.getType() == Material.WITHER_SKELETON_SKULL) {
                    // Soul Reaper Ritual
                    if (tool.getData().hasAbility("soul_reaper")) {
                        player.sendMessage(org.bukkit.ChatColor.RED + "Ya tienes Soul Reaper.");
                        return false;
                    }
                    tool.getData().addAbility("soul_reaper");
                    entity.remove();
                    playRitualEffects(center);
                    player.sendMessage(
                            org.bukkit.ChatColor.DARK_PURPLE + "¡Has imbuido tu herramienta con Soul Reaper!");
                    return true;
                } else if (item.getType() == Material.LIGHTNING_ROD) {
                    // Thunderlord Ritual
                    if (tool.getData().hasAbility("thunderlord")) {
                        player.sendMessage(org.bukkit.ChatColor.RED + "Ya tienes Thunderlord.");
                        return false;
                    }
                    tool.getData().addAbility("thunderlord");
                    entity.remove();
                    playRitualEffects(center);
                    player.sendMessage(org.bukkit.ChatColor.GOLD + "¡Has imbuido tu herramienta con Thunderlord!");
                    return true;
                }
            }
        }
        return false;
    }

    private static String detectInfusion(Block center) {
        // Check blocks UNDER the candles (y-1 relative to candle, so y-1 relative to
        // center)
        // Center is Enchanting Table. Candles are at y=0 relative to center.
        // So blocks under candles are at y=-1 relative to center?
        // Wait, checkStructure checks candles at y=0.
        // So let's check blocks at y=-1 under the candles.

        Block c1 = getRelative(center, 1, -1, 1);
        Block c2 = getRelative(center, 1, -1, -1);
        Block c3 = getRelative(center, -1, -1, 1);
        Block c4 = getRelative(center, -1, -1, -1);

        if (isMaterial(c1, c2, c3, c4, Material.MAGMA_BLOCK))
            return "fire_infusion";
        if (isMaterial(c1, c2, c3, c4, Material.BLUE_ICE))
            return "ice_infusion";
        if (isMaterial(c1, c2, c3, c4, Material.LIGHTNING_ROD))
            return "lightning_infusion";
        if (isMaterial(c1, c2, c3, c4, Material.BEACON))
            return "ascension";

        return null;
    }

    private static boolean performAscension(Player player, LivingTool tool, Location loc) {
        if (tool.getData().getLevel() < 100 || tool.getData().getPrestige() < 1) {
            player.sendMessage(org.bukkit.ChatColor.RED
                    + "Tu herramienta no es digna de la Ascensión (Req: Nvl 100 + Prestigio 1).");
            return false;
        }
        if (tool.getData().isDivine()) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Tu herramienta ya ha ascendido.");
            return false;
        }

        tool.getData().setDivine(true);
        tool.updateLore();

        // Visuals
        loc.getWorld().strikeLightningEffect(loc);
        loc.getWorld().spawnParticle(Particle.TOTEM, loc, 200, 1, 2, 1, 0.5);
        loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0.1f);

        org.bukkit.Bukkit.broadcastMessage(
                org.bukkit.ChatColor.GOLD + "¡" + player.getName() + " ha ascendido su herramienta a DIVINA!");
        return true;
    }

    private static boolean isMaterial(Block b1, Block b2, Block b3, Block b4, Material mat) {
        return b1.getType() == mat && b2.getType() == mat && b3.getType() == mat && b4.getType() == mat;
    }

    private static boolean checkStructure(Block center) {
        // Cross: Redstone Wire
        if (getRelative(center, 1, 0, 0).getType() != Material.REDSTONE_WIRE)
            return false;
        if (getRelative(center, -1, 0, 0).getType() != Material.REDSTONE_WIRE)
            return false;
        if (getRelative(center, 0, 0, 1).getType() != Material.REDSTONE_WIRE)
            return false;
        if (getRelative(center, 0, 0, -1).getType() != Material.REDSTONE_WIRE)
            return false;

        // Corners: Candles (must be lit?) - Let's just check for any candle for now
        if (!isCandle(getRelative(center, 1, 0, 1)))
            return false;
        if (!isCandle(getRelative(center, 1, 0, -1)))
            return false;
        if (!isCandle(getRelative(center, -1, 0, 1)))
            return false;
        if (!isCandle(getRelative(center, -1, 0, -1)))
            return false;

        return true;
    }

    private static Block getRelative(Block block, int x, int y, int z) {
        return block.getRelative(x, y, z);
    }

    private static boolean isCandle(Block block) {
        return block.getType().name().endsWith("CANDLE");
    }

    private static void playRitualEffects(Location loc) {
        loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 100, 1, 1, 1, 0.5);
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 50, 0.5, 0.5, 0.5, 0.05);
        loc.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.5f);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2, 1.5f);
    }
}
