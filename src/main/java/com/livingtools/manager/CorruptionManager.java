package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CorruptionManager {

    private static final Random random = new Random();

    public static void handleHit(Player attacker, LivingTool tool, Entity victim) {
        int corruption = tool.getData().getCorruption();
        if (corruption <= 0)
            return;

        // 1. Power Effects (Risk/Reward)
        if (corruption >= 20) {
            // 10% chance for double damage
            if (random.nextInt(100) < 10) {
                if (victim instanceof LivingEntity) {
                    LivingEntity livingVictim = (LivingEntity) victim;
                    livingVictim.damage(5.0); // Bonus damage
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 0.5f);
                    attacker.sendMessage(ChatColor.DARK_PURPLE + "¡Golpe Corrupto!");
                }
            }
        }

        if (corruption >= 50) {
            // 5% chance to apply Wither
            if (random.nextInt(100) < 5) {
                if (victim instanceof LivingEntity) {
                    ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                }
            }
        }

        // 2. Betrayal (Risk)
        // Chance increases with corruption: 1% at 20, 5% at 100
        double betrayalChance = (corruption / 20.0);
        if (random.nextDouble() * 100 < betrayalChance) {
            attacker.damage(2.0);
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.5f);
            attacker.sendMessage(ChatColor.DARK_RED + "¡Tu herramienta te traiciona por su corrupción!");
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
        }
    }

    public static void checkCorruption(Player player, LivingTool tool) {
        int corruption = tool.getData().getCorruption();
        if (corruption >= 100) {
            transformToDemonTool(player, tool);
        }
    }

    public static void transformToDemonTool(Player player, LivingTool tool) {
        // Visual effects
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 0.5f);
        player.sendTitle(ChatColor.DARK_RED + "¡CORRUPCIÓN TOTAL!",
                ChatColor.RED + "Tu herramienta ha sido consumida...", 10, 70, 20);

        // Lore update handled by tool.updateLore() usually, but we might want a special
        // title
        tool.getData().setTitle(ChatColor.DARK_RED + "Herramienta Demoníaca");
        tool.updateLore();
    }

    public static void handleDemonToolEffects(Player player, LivingTool tool) {
        if (tool.getData().getCorruption() < 100)
            return;

        // Passive drain
        if (Math.random() < 0.05) { // 5% chance per tick/event
            player.damage(1.0); // 0.5 hearts
            player.sendMessage(ChatColor.DARK_RED + "Tu herramienta consume tu vitalidad...");
        }
    }

    public static void spawnShadowMob(org.bukkit.Location loc, Player target) {
        org.bukkit.entity.Vex vex = (org.bukkit.entity.Vex) loc.getWorld().spawnEntity(loc,
                org.bukkit.entity.EntityType.VEX);
        vex.setCustomName(ChatColor.DARK_GRAY + "Sombra Viviente");
        vex.setCustomNameVisible(true);
        vex.setTarget(target);
        vex.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 0));
        vex.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_SWORD));

        loc.getWorld().spawnParticle(org.bukkit.Particle.SQUID_INK, loc, 20, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_VEX_CHARGE, 1, 0.5f);
    }
}
