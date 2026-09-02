package com.livingtools.abilities.utility;

import com.livingtools.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class TelepathyAbility extends Ability {

    public TelepathyAbility() {
        super("telepathy", "Telepatía", "Los ítems van directamente a tu inventario.", 15);
    }

    @Override
    public boolean isCompatible(Material type) {
        return true; // Compatible with all tools
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, com.livingtools.data.LivingTool tool) {
        Player player = event.getPlayer();
        event.setDropItems(false);

        Collection<ItemStack> drops = event.getBlock().getDrops(player.getInventory().getItemInMainHand());
        for (ItemStack drop : drops) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(drop);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
        // Give XP directly
        player.giveExp(event.getExpToDrop());
        event.setExpToDrop(0);
    }

    // Note: EntityDeathEvent doesn't have a direct "setDropItems" method in older
    // versions easily,
    // but we can clear drops and add them to inventory.
    // However, Ability class doesn't have onEntityDeath hook yet, only
    // onEntityDamage.
    // We need to add onEntityDeath to Ability class or handle it in a listener.
    // For now, let's stick to BlockBreak or add the hook.
}
