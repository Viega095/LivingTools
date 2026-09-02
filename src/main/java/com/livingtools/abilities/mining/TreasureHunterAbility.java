package com.livingtools.abilities.mining;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class TreasureHunterAbility extends Ability {

    private final Random random = new Random();

    public TreasureHunterAbility() {
        super("treasurehunter", "Cazatesoros", "Probabilidad de encontrar tesoros al excavar.", 20);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, LivingTool tool) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.SAND || type == Material.GRAVEL
                || type == Material.CLAY) {
            // Mastery Scaling
            int level = getLevel(tool);
            double chance = 0.03 + (level * 0.01); // Lvl 1: 4%, Lvl 5: 8%

            if (random.nextDouble() < chance) {
                ItemStack treasure;
                double roll = random.nextDouble();

                // Better loot tables at higher levels
                double diamondThreshold = 0.98 - (level * 0.01); // Lvl 1: 97%, Lvl 5: 93%
                double emeraldThreshold = 0.90 - (level * 0.02); // Lvl 1: 88%, Lvl 5: 80%

                if (roll < 0.50) { // 50% Gold Nugget
                    treasure = new ItemStack(Material.GOLD_NUGGET, random.nextInt(3) + 1);
                } else if (roll < 0.80) { // 30% Iron Nugget
                    treasure = new ItemStack(Material.IRON_NUGGET, random.nextInt(3) + 1);
                } else if (roll < emeraldThreshold) { // Emerald
                    treasure = new ItemStack(Material.EMERALD);
                } else if (roll >= diamondThreshold) { // Diamond
                    treasure = new ItemStack(Material.DIAMOND);
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                    event.getPlayer().sendMessage(ChatColor.AQUA + "¡Has encontrado un diamante!");
                } else {
                    // Fallback / Filler
                    treasure = new ItemStack(Material.FLINT);
                }

                block.getWorld().dropItemNaturally(block.getLocation(), treasure);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f,
                        1);

                // Add XP
                addXP(event.getPlayer(), tool, 15);
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SHOVEL");
    }
}
