package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;

public class DecapitateAbility extends Ability {

    private final Random random = new Random();

    public DecapitateAbility() {
        super("decapitate", "Decapitador", "Probabilidad de cortar cabezas de enemigos.", 35);
    }

    @Override
    public void onKill(EntityDeathEvent event, LivingTool tool) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        // Mastery Scaling
        int level = getLevel(tool);
        double chance = 0.05 + (level * 0.02); // Lvl 1: 7%, Lvl 5: 15%

        if (random.nextDouble() > chance)
            return;

        // Add XP on success
        addXP(killer, tool, 50);

        ItemStack head = null;

        if (victim instanceof Skeleton) {
            if (victim instanceof WitherSkeleton) {
                head = new ItemStack(Material.WITHER_SKELETON_SKULL);
            } else {
                head = new ItemStack(Material.SKELETON_SKULL);
            }
        } else if (victim instanceof Zombie) {
            head = new ItemStack(Material.ZOMBIE_HEAD);
        } else if (victim instanceof Creeper) {
            head = new ItemStack(Material.CREEPER_HEAD);
        } else if (victim instanceof Player) {
            head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer((Player) victim);
            head.setItemMeta(meta);
        }

        if (head != null) {
            victim.getWorld().dropItemNaturally(victim.getLocation(), head);
            if (killer != null) {
                killer.sendMessage(ChatColor.GOLD + "¡Has decapitado a tu enemigo!");
                killer.playSound(killer.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0.5f);
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE");
    }
}
