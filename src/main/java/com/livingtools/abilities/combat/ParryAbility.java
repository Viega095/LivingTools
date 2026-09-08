package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class ParryAbility extends Ability {

    private final Random random = new Random();

    public ParryAbility() {
        super("parry", "Parada", "Probabilidad de negar daño al bloquear (agacharse).", 25);
    }

    // We use onDamageTaken because this is a defensive ability when holding the
    // tool
    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player victim) {
        if (!victim.isSneaking())
            return;

        // Check if facing the attacker roughly
        // Check if facing the attacker roughly
        if (event.getDamager() != null) {
            org.bukkit.util.Vector dirToAttacker = event.getDamager().getLocation().toVector()
                    .subtract(victim.getLocation().toVector()).normalize();
            org.bukkit.util.Vector playerDir = victim.getLocation().getDirection();

            // Dot product > 0 means roughly facing same direction (wait, no)
            // Dot product > 0.5 means within ~60 degrees
            if (playerDir.dot(dirToAttacker) > 0.5) {

                org.bukkit.inventory.ItemStack heldItem = victim.getInventory().getItemInMainHand();
                if (!com.livingtools.data.LivingTool.isLivingTool(heldItem)) return;
                LivingTool tool = new LivingTool(heldItem);
                int level = getLevel(tool);
                double chance = 0.22 + (level * 0.02); // Lvl 1: 24%, Lvl 5: 32%

                if (random.nextDouble() < chance) {
                    event.setDamage(0);
                    victim.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation().add(playerDir.multiply(0.5)),
                            5);
                    victim.sendMessage(ChatColor.GREEN + "¡Parada exitosa!");

                    // Add XP
                    addXP(victim, tool, 10);
                }
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_SWORD");
    }
}
