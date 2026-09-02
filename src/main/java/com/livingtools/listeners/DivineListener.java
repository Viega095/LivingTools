package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DivineListener implements Listener {

    private static final long COOLDOWN_MS = 3600000; // 1 Hour

    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {
        if (event.isCancelled() && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                if (tool.getData().isDivine()) {
                    long lastUse = tool.getData().getDivineCooldown();
                    if (System.currentTimeMillis() - lastUse >= COOLDOWN_MS) {
                        // Trigger Divine Intervention
                        event.setCancelled(false); // Allow resurrection

                        // Effects
                        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        player.setHealth(maxHealth / 2); // Restore 50% HP
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1)); // 45s Regen II
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0)); // 40s Fire
                                                                                                            // Res
                        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1)); // 5s Absorption
                                                                                                       // II

                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
                        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation(), 100, 0.5, 1, 0.5, 0.5);

                        player.sendMessage(org.bukkit.ChatColor.GOLD
                                + "¡INTERVENCIÓN DIVINA! Tu herramienta te ha salvado de la muerte.");

                        // Set Cooldown
                        tool.getData().setDivineCooldown(System.currentTimeMillis());
                    }
                }
            }
        }
    }
}
