package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class SynergyManager {

    private static final String META_MINER_RAGE = "livingtools_miner_rage";
    private static final String META_WARRIOR_FOCUS = "livingtools_warrior_focus";

    public static boolean checkHotbarSynergy(Player player) {
        String personality = null;
        int count = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                String p = tool.getData().getPersonality();
                if (p != null) {
                    if (personality == null) {
                        personality = p;
                        count++;
                    } else if (personality.equals(p)) {
                        count++;
                    }
                }
            }
        }

        return count >= 2;
    }

    public static void addMinerRageStack(Player player) {
        int current = 0;
        if (player.hasMetadata(META_MINER_RAGE)) {
            current = player.getMetadata(META_MINER_RAGE).get(0).asInt();
        }
        current++;

        if (current == 10) {
            player.sendMessage(ChatColor.RED + "¡Furia Minera Activada! Tu próximo golpe será devastador.");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 0.5f);
        }

        player.setMetadata(META_MINER_RAGE,
                new FixedMetadataValue(com.livingtools.LivingToolsPlugin.getInstance(), current));
    }

    public static boolean consumeMinerRage(Player player) {
        if (player.hasMetadata(META_MINER_RAGE)) {
            int current = player.getMetadata(META_MINER_RAGE).get(0).asInt();
            if (current >= 10) {
                player.removeMetadata(META_MINER_RAGE, com.livingtools.LivingToolsPlugin.getInstance());
                return true;
            }
        }
        return false;
    }

    public static void addWarriorFocusStack(Player player) {
        int current = 0;
        if (player.hasMetadata(META_WARRIOR_FOCUS)) {
            current = player.getMetadata(META_WARRIOR_FOCUS).get(0).asInt();
        }
        current++;

        if (current == 5) {
            player.sendMessage(ChatColor.GOLD + "¡Enfoque Guerrero Activado! Tu próxima minería será instantánea.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1, 2.0f);
        }

        player.setMetadata(META_WARRIOR_FOCUS,
                new FixedMetadataValue(com.livingtools.LivingToolsPlugin.getInstance(), current));
    }

    public static boolean consumeWarriorFocus(Player player) {
        if (player.hasMetadata(META_WARRIOR_FOCUS)) {
            int current = player.getMetadata(META_WARRIOR_FOCUS).get(0).asInt();
            if (current >= 5) {
                player.removeMetadata(META_WARRIOR_FOCUS, com.livingtools.LivingToolsPlugin.getInstance());
                return true;
            }
        }
        return false;
    }

    public static boolean hasLivingToolInHotbar(Player player) {
        return getLivingToolFromHotbar(player) != null;
    }

    public static LivingTool getLivingToolFromHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (LivingTool.isLivingTool(item)) {
                return new LivingTool(item);
            }
        }
        return null;
    }

    public static boolean checkFullSetBonus(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();

        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) {
                return false;
            }
        }

        for (ItemStack piece : armor) {
            if (!com.livingtools.data.LivingArmor.isLivingArmor(piece)) {
                return false;
            }
        }

        return hasLivingToolInHotbar(player);
    }

    public static void applySetBonus(Player player) {
        if (checkFullSetBonus(player)) {
            // Regeneration II
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION,
                    40, 1, false, false));
            // Strength I
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false));

            // Visuals
            if (Math.random() < 0.1) {
                player.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0),
                        5, 0.3, 0.5, 0.3, 0.05);
            }
        }
    }
}
