package com.livingtools.abilities.passive;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class MagnetismAbility extends Ability {

    public MagnetismAbility() {
        super("magnetism", "Magnetismo", "Atrae items cercanos hacia ti.", 15);
    }

    // We can use onHold or similar, but Ability class doesn't have a tick hook yet.
    // We can simulate it via PlayerMoveEvent or BlockBreakEvent, but ideally we
    // need a tick task.
    // For now, let's hook into onBlockBreak to trigger a pull, or rely on a new
    // hook if available.
    // Actually, let's use a simple check in onBlockBreak for now as it's a
    // mining/farming tool mostly.
    // Wait, user wants it for ALL tools.
    // Let's implement a static tick method or similar in LivingToolsPlugin if
    // needed,
    // but for simplicity let's use onBlockBreak and onAttack to trigger "pulses" of
    // magnetism.

    // Better yet, let's use the existing onInteract to toggle it? No, it's passive.
    // Let's stick to onBlockBreak/onAttack for now to avoid performance heavy
    // runnables for every tool.

    @Override
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event, LivingTool tool) {
        pullItems(event.getPlayer(), tool);
    }

    @Override
    public void onAttack(org.bukkit.event.entity.EntityDamageByEntityEvent event, LivingTool tool) {
        if (event.getDamager() instanceof Player) {
            pullItems((Player) event.getDamager(), tool);
        }
    }

    private void pullItems(Player player, LivingTool tool) {
        int level = getLevel(tool);
        double range = 3.0 + level; // Lvl 1: 4 blocks, Lvl 5: 8 blocks

        boolean pulled = false;
        for (Entity e : player.getNearbyEntities(range, range, range)) {
            if (e instanceof Item) {
                Item item = (Item) e;
                if (item.getPickupDelay() > 0)
                    continue;

                item.setVelocity(player.getLocation().toVector().subtract(item.getLocation().toVector()).normalize()
                        .multiply(0.5));
                pulled = true;
            }
        }

        if (pulled) {
            // Tiny XP gain for utility
            if (Math.random() < 0.1)
                addXP(player, tool, 1);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        // Compatible with everything
        return true;
    }
}
