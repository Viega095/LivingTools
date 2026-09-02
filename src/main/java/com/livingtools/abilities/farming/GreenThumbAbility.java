package com.livingtools.abilities.farming;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GreenThumbAbility extends Ability {

    public GreenThumbAbility() {
        super("greenthumb", "Mano Verde", "Click derecho para fertilizar un área de 3x3 (Usa Polvo de Hueso).", 20);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        Block center = event.getClickedBlock();

        if (center == null)
            return;

        // Check for Bonemeal
        if (!player.getInventory().containsAtLeast(new ItemStack(Material.BONE_MEAL), 1)) {
            player.sendMessage(ChatColor.RED + "Necesitas Polvo de Hueso en tu inventario.");
            return;
        }

        boolean used = false;

        // Mastery Scaling
        int level = getLevel(tool);
        int radius = 1 + (level / 3); // Lvl 1-2: 1 (3x3), Lvl 3-5: 2 (5x5)
        double saveChance = 0.10 * level; // Lvl 1: 10%, Lvl 5: 50%

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block target = center.getRelative(x, 0, z);
                if (target.getBlockData() instanceof Ageable) {
                    Ageable ageable = (Ageable) target.getBlockData();
                    if (ageable.getAge() < ageable.getMaximumAge()) {
                        ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + 2)); // Grow by 2 stages
                        target.setBlockData(ageable);
                        target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                                target.getLocation().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2);
                        used = true;
                    }
                }
            }
        }

        if (used) {
            // Chance to save bonemeal
            if (Math.random() > saveChance) {
                ItemStack bonemeal = new ItemStack(Material.BONE_MEAL, 1);
                player.getInventory().removeItem(bonemeal);
            } else {
                player.sendMessage(ChatColor.GREEN + "¡Ahorro de fertilizante!");
            }

            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);

            // Add XP
            addXP(player, tool, 5);

            // Cooldown
            addCooldown(player, 2000); // 2 seconds
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_HOE");
    }
}
