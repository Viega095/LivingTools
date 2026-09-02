package com.livingtools.listeners;

import com.livingtools.manager.BloodMoonManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class BloodMoonListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!BloodMoonManager.isBloodMoonActive())
            return;

        if (event.getEntity() instanceof Monster) {
            LivingEntity entity = event.getEntity();
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0)); // Strength
                                                                                                              // I
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0)); // Speed I

            // Visual indicator
            entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(org.bukkit.Color.RED, 1));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!BloodMoonManager.isBloodMoonActive())
            return;

        if (event.getEntity() instanceof Monster) {
            if (Math.random() < 0.20) { // 20% chance
                event.getDrops().add(createBloodEssence());
            }
        }
    }

    public static ItemStack createBloodEssence() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Esencia de Sangre");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Una esencia concentrada de pura maldad.");
        lore.add(ChatColor.GRAY + "Obtenida durante la Luna de Sangre.");
        lore.add(ChatColor.RED + "Solo para crafteo");
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
