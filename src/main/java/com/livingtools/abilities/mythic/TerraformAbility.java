package com.livingtools.abilities.mythic;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class TerraformAbility extends Ability {

    public TerraformAbility() {
        super("terraform", "Terraformar", "Transforma el entorno a tu voluntad. (Coste: 50 Durabilidad)", 50,
                AbilityType.ACTIVE);
    }

    @Override
    public void onTrigger(Player player, Event event) {
        if (!checkCooldown(player))
            return;

        // Cost
        LivingTool tool = new LivingTool(player.getInventory().getItemInMainHand());
        org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) tool.getItem().getItemMeta();

        if (meta.getDamage() > tool.getItem().getType().getMaxDurability() - 50) {
            player.sendMessage("§c¡Herramienta demasiado dañada!");
            return;
        }

        // Apply Durability Cost
        meta.setDamage(meta.getDamage() + 50);
        tool.getItem().setItemMeta(meta);

        // Effect: Convert surroundings
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = player.getLocation().getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE) {
                        block.setType(Material.GRASS_BLOCK);
                        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1, 0.5),
                                1);
                    } else if (block.getType() == Material.NETHERRACK) {
                        block.setType(Material.WARPED_NYLIUM);
                    }
                }
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1, 0.5f);
        player.sendMessage("§a¡La naturaleza responde a tu llamado!");

        addCooldown(player, 10000); // 10s cooldown
    }

    private boolean checkCooldown(Player player) {
        return !isOnCooldown(player);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_PICKAXE") || type.name().endsWith("_SHOVEL");
    }
}
