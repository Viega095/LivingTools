package com.livingtools.mechanics;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.AbyssalManager;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PurificationSystem {

    public static void purifyTool(Player player, LivingTool tool) {
        if (!AbyssalManager.isAbyssal(tool)) {
            player.sendMessage(ChatColor.RED + "Esta herramienta no está corrupta.");
            return;
        }

        // Remove Abyssal Tag
        ItemMeta meta = tool.getItem().getItemMeta();
        meta.getPersistentDataContainer().remove(AbyssalManager.KEY_IS_ABYSSAL);

        // Update Lore (Remove Abyssal line)
        java.util.List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            lore.removeIf(line -> line.contains("ABISAL"));
        }
        meta.setLore(lore);
        tool.getItem().setItemMeta(meta);

        // Visuals
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
        player.spawnParticle(Particle.END_ROD, player.getLocation(), 50);
        player.sendMessage(MessageUtils.color("&e&l¡TU HERRAMIENTA HA SIDO PURIFICADA!"));

        // Reduce Player Corruption
        CorruptionSystem.decreaseCorruption(player, 50);
    }

    public static void createSanctuary(Location location) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 600) { // 30 seconds
                    this.cancel();
                    return;
                }

                // Visuals
                location.getWorld().spawnParticle(Particle.COMPOSTER, location, 10, 3, 1, 3, 0);

                // Effects
                for (org.bukkit.entity.Entity entity : location.getWorld().getNearbyEntities(location, 5, 5, 5)) {
                    if (entity instanceof Player) {
                        ((Player) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.REGENERATION, 40, 1));
                    } else if (entity instanceof Monster) {
                        ((Monster) entity).damage(2);
                        entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation(), 5);
                    }
                }
                ticks += 20;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 20);
    }
}
