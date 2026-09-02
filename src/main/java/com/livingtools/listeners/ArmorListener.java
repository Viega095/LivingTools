package com.livingtools.listeners;

import com.livingtools.data.LivingArmor;
import com.livingtools.manager.DialogueCooldown;
import com.livingtools.manager.SynergyManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();

        // Grant XP to all equipped living armor
        boolean armorPersonalityHandled = false;

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (!LivingArmor.isLivingArmor(item)) {
                continue;
            }
            LivingArmor armor = new LivingArmor(item);

            for (String abilityId : armor.getAbilities()) {
                com.livingtools.abilities.Ability ability = com.livingtools.abilities.AbilityRegistry
                        .getAbility(abilityId);
                if (ability != null) {
                    ability.onDamageTaken(event, player);
                    if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
                        ability.onDamageTakenByEntity((org.bukkit.event.entity.EntityDamageByEntityEvent) event,
                                player);
                    }
                }
            }

            if (armorPersonalityHandled) {
                continue;
            }

            String pName = armor.getPersonality();
            if (pName == null) {
                continue;
            }

            try {
                com.livingtools.mechanics.ArmorPersonality personality = com.livingtools.mechanics.ArmorPersonality
                        .valueOf(pName);

                if (personality == com.livingtools.mechanics.ArmorPersonality.COWARDLY) {
                    if (player.getHealth() < 6) {
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SPEED, 60, 1));
                        if (!armorPersonalityHandled && sendArmorChat(player)) {
                            player.sendMessage(ChatColor.GRAY + "[Armadura]: ¡Corre! ¡Huye!");
                            armorPersonalityHandled = true;
                        }
                    }
                } else if (personality == com.livingtools.mechanics.ArmorPersonality.MASOCHISTIC) {
                    if (Math.random() < 0.1) {
                        player.setHealth(Math.min(player.getHealth() + 1, player
                                .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
                        if (!armorPersonalityHandled && sendArmorChat(player)) {
                            player.sendMessage(ChatColor.GOLD + "[Armadura]: Mmm... más...");
                            armorPersonalityHandled = true;
                        }
                    }
                } else if (personality == com.livingtools.mechanics.ArmorPersonality.GUARDIAN) {
                    if (Math.random() < 0.2) {
                        for (org.bukkit.entity.Entity e : player.getNearbyEntities(10, 5, 10)) {
                            if (e instanceof org.bukkit.entity.Mob) {
                                ((org.bukkit.entity.Mob) e).setTarget(player);
                            }
                        }
                        if (!armorPersonalityHandled && sendArmorChat(player)) {
                            player.sendMessage(ChatColor.GOLD + "[Armadura]: ¡Venid a mí! ¡Yo os protegeré!");
                            armorPersonalityHandled = true;
                        }
                    }
                } else if (personality == com.livingtools.mechanics.ArmorPersonality.THORNED) {
                    if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
                        org.bukkit.event.entity.EntityDamageByEntityEvent edbe =
                                (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
                        if (edbe.getDamager() instanceof org.bukkit.entity.LivingEntity) {
                            ((org.bukkit.entity.LivingEntity) edbe.getDamager())
                                    .damage(event.getFinalDamage() * 0.5, player);
                            if (!armorPersonalityHandled && sendArmorChat(player)) {
                                player.sendMessage(ChatColor.GREEN + "[Armadura]: ¡El dolor se comparte!");
                                armorPersonalityHandled = true;
                            }
                        }
                    }
                } else if (personality == com.livingtools.mechanics.ArmorPersonality.GHOSTLY) {
                    if (Math.random() < 0.1) {
                        event.setCancelled(true);
                        if (!armorPersonalityHandled && sendArmorChat(player)) {
                            player.sendMessage(ChatColor.AQUA + "[Armadura]: Intangible...");
                            armorPersonalityHandled = true;
                        }
                        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 5, 0.5,
                                0.5, 0.5, 0.1);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        checkSetBonus(player);
    }

    private static boolean sendArmorChat(Player player) {
        return DialogueCooldown.tryArmorChat(player);
    }

    private void checkSetBonus(Player player) {
        int livingPieces = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (LivingArmor.isLivingArmor(item)) {
                livingPieces++;
            }
        }

        if (livingPieces == 4) {
            player.addPotionEffect(
                    new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 100, 1));
            player.addPotionEffect(
                    new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 100, 0));

            if (Math.random() < 0.05) {
                player.getWorld().strikeLightningEffect(player.getLocation());
                for (org.bukkit.entity.Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                        ((org.bukkit.entity.LivingEntity) e).damage(5);
                    }
                }
            }

            com.livingtools.data.LivingTool hotbarTool = SynergyManager.getLivingToolFromHotbar(player);
            if (hotbarTool != null) {
                com.livingtools.manager.GuideManager.triggerStep(player, hotbarTool,
                        com.livingtools.manager.GuideManager.TutorialStep.FULL_SET);
            }
        }
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (LivingArmor.isLivingArmor(item)) {
                LivingArmor armor = new LivingArmor(item);
                for (String abilityId : armor.getAbilities()) {
                    com.livingtools.abilities.Ability ability = com.livingtools.abilities.AbilityRegistry
                            .getAbility(abilityId);
                    if (ability != null) {
                        ability.onHold(player);
                    }
                }
            }
        }
    }
}
