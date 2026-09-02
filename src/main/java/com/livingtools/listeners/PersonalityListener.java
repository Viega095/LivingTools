package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.DialogueCooldown;
import com.livingtools.mechanics.Personality;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PersonalityListener implements Listener {

    private final Random random = new Random();

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item))
            return;

        LivingTool tool = new LivingTool(item);

        // Trigger Personality Dialogue
        com.livingtools.manager.PersonalityManager.sayLine(player, tool,
                com.livingtools.manager.PersonalityManager.EventType.MINING);

        String personalityName = tool.getData().getPersonality();
        if (personalityName == null)
            return;

        Personality personality;
        try {
            personality = Personality.valueOf(personalityName);
        } catch (IllegalArgumentException e) {
            return;
        }

        int affinity = tool.getData().getAffinity();

        // Low Affinity Effects
        if (affinity < -20 && random.nextInt(100) < 5) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu herramienta se niega a trabajar. (Afinidad Baja)");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        // High Affinity Effects
        if (affinity > 50 && random.nextInt(100) < 5) {
            // Auto-repair chance
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) item.getItemMeta();
            if (meta.hasDamage()) {
                meta.setDamage(meta.getDamage() - 1);
                item.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Tu herramienta se repara sola por amor.");
            }
        }

        switch (personality) {
            case GLUTTONOUS:
                if (random.nextInt(100) < 10) { // 10% chance
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                    player.setSaturation(Math.min(20, player.getSaturation() + 1));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                }
                break;
            case LAZY:
                if (random.nextInt(100) < 2) { // 2% chance
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.GRAY + "Tu herramienta bosteza y se niega a minar.");
                }
                break;
            case LUCKY:
                if (random.nextInt(100) < 5) { // 5% chance
                    // Drop random material
                    org.bukkit.Material[] rewards = {
                            org.bukkit.Material.IRON_INGOT, org.bukkit.Material.GOLD_INGOT,
                            org.bukkit.Material.DIAMOND, org.bukkit.Material.EMERALD,
                            org.bukkit.Material.COAL
                    };
                    player.getWorld().dropItemNaturally(event.getBlock().getLocation(),
                            new ItemStack(rewards[random.nextInt(rewards.length)]));
                    player.sendMessage(ChatColor.GREEN + "¡Qué suerte! ¡Un regalo!");
                }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            com.livingtools.manager.PersonalityManager.sayLine(player, tool,
                    com.livingtools.manager.PersonalityManager.EventType.KILL_MOB);

            String pName = tool.getData().getPersonality();
            if ("AGGRESSIVE".equals(pName)) {
                player.addPotionEffect(
                        new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 100, 0)); // Strength
                                                                                                                         // I
                                                                                                                         // for
                                                                                                                         // 5s
            } else if ("GLUTTONOUS".equals(pName)) {
                if (random.nextInt(100) < 20) { // 20% chance on kill
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 2));
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                }
            } else if ("LUCKY".equals(pName)) {
                if (random.nextInt(100) < 10) { // 10% chance
                    org.bukkit.Material[] rewards = {
                            org.bukkit.Material.GOLD_NUGGET, org.bukkit.Material.IRON_NUGGET,
                            org.bukkit.Material.EXPERIENCE_BOTTLE
                    };
                    player.getWorld().dropItemNaturally(event.getEntity().getLocation(),
                            new ItemStack(rewards[random.nextInt(rewards.length)]));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (event.getCause() != org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL)
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return;

        LivingTool tool = new LivingTool(item);
        String personality = tool.getData().getPersonality();

        if ("LAZY".equals(personality)) {
            if (DialogueCooldown.tryToolChat(player)) {
                player.sendMessage(ChatColor.GRAY + "[Herramienta]: ¡Auch! ¿Podrías caminar en lugar de saltar?");
            }
        } else if ("HEROIC".equals(personality)) {
            if (player.getHealth() - event.getFinalDamage() < 6) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 100, 0));
                if (DialogueCooldown.tryToolChat(player)) {
                    player.sendMessage(ChatColor.GOLD + "¡Tu herramienta te protege en tu momento de necesidad!");
                }
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item))
            return;
        LivingTool tool = new LivingTool(item);
        String personalityName = tool.getData().getPersonality();
        if (personalityName == null)
            return;

        if ("SARCASTIC".equals(personalityName)) {
            if (random.nextInt(100) < 10) { // 10% chance
                event.setDamage(event.getDamage() + 2.0); // True damage simulation (just extra damage here)
                player.sendMessage(
                        ChatColor.ITALIC + "" + ChatColor.GRAY + "Tu herramienta insulta al enemigo. (+2 Daño)");
            }
        } else if ("AGGRESSIVE".equals(personalityName)) {
            if (random.nextInt(100) < 15) { // 15% chance
                if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                    event.getEntity().setFireTicks(60); // 3 seconds fire
                    player.sendMessage(ChatColor.RED + "¡Tu herramienta prende fuego a tu enemigo!");
                }
            }
        } else if ("GLUTTONOUS".equals(personalityName)) {
            if (random.nextInt(100) < 5) { // 5% chance on hit
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                player.setSaturation(Math.min(20, player.getSaturation() + 1));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                if ("SHY".equals(tool.getData().getPersonality())) {
                    if (random.nextInt(100) < 20) { // 20% chance on sneak
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.INVISIBILITY, 60, 0, false, false));
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SPEED, 60, 1, false, false)); // Speed II
                        player.sendMessage(ChatColor.GRAY + "Te escondes y corres...");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        // Lazy Repair Logic could go here, but better in a runnable to avoid spam.
        // For now, let's skip complex idle checks in MoveEvent to save performance.

        // Fury Logic (Camera Control)
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            if (tool.getMood() == LivingTool.Mood.FURIOUS && random.nextInt(1000) < 5) { // 0.5% chance per move packet
                                                                                         // (rare but noticeable)
                // Find nearby entity
                player.getNearbyEntities(10, 10, 10).stream()
                        .filter(e -> e instanceof org.bukkit.entity.LivingEntity && e != player)
                        .findFirst()
                        .ifPresent(target -> {
                            org.bukkit.Location loc = target.getLocation();
                            org.bukkit.Location playerLoc = player.getLocation();
                            playerLoc.setDirection(loc.toVector().subtract(playerLoc.toVector()));
                            player.teleport(playerLoc);
                            player.sendMessage(ChatColor.DARK_RED + "¡MÁTALO! ¡TU HERRAMIENTA EXIGE SANGRE!");
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 0.5f);
                        });
            }
        }
    }
}
