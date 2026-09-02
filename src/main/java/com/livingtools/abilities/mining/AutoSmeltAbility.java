package com.livingtools.abilities.mining;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class AutoSmeltAbility extends Ability {

    public AutoSmeltAbility() {
        super("autosmelt", "Auto-Fundición", "Cocina automáticamente los minerales extraídos.", 10);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_PICKAXE") || type.name().endsWith("_SHOVEL");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, com.livingtools.data.LivingTool tool) {
        Block block = event.getBlock();
        Material drop = null;

        switch (block.getType()) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case RAW_IRON_BLOCK:
                drop = Material.IRON_INGOT;
                break;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case RAW_GOLD_BLOCK:
                drop = Material.GOLD_INGOT;
                break;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
            case RAW_COPPER_BLOCK:
                drop = Material.COPPER_INGOT;
                break;
            case SAND:
            case RED_SAND:
                drop = Material.GLASS;
                break;
            case COBBLESTONE:
            case STONE:
                drop = Material.STONE;
                break;
            case ANCIENT_DEBRIS:
                drop = Material.NETHERITE_SCRAP;
                break;
            case CLAY:
                drop = Material.BRICK;
                break;
            case NETHERRACK:
                drop = Material.NETHER_BRICK;
                break;
            case CACTUS:
                drop = Material.GREEN_DYE;
                break;
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
                drop = Material.CHARCOAL;
                break;
            default:
                return;
        }

        if (drop != null) {
            event.setDropItems(false);

            // Mastery Scaling: Chance to double drops
            int level = getLevel(tool);
            int amount = 1;
            double doubleChance = 0.10 * level; // Lvl 1: 10%, Lvl 5: 50%

            if (Math.random() < doubleChance) {
                amount = 2;
            }

            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(drop, amount));

            // Add XP (2 per smelt)
            addXP(event.getPlayer(), tool, 2);

            // Add vanilla XP
            event.setExpToDrop(event.getExpToDrop() + 1);
        }
    }
}
