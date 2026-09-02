package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetBonusManager {

    private static final Map<UUID, Boolean> activeBonuses = new HashMap<>();

    public static void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkSetBonus(player);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 40L); // Check every 2 seconds
    }

    private static void checkSetBonus(Player player) {
        boolean hasFullArmor = true;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || !LivingArmor.isLivingArmor(armor)) {
                hasFullArmor = false;
                break;
            }
        }

        boolean hasTool = LivingTool.isLivingTool(player.getInventory().getItemInMainHand());

        if (hasFullArmor && hasTool) {
            if (!activeBonuses.getOrDefault(player.getUniqueId(), false)) {
                // Bonus Activated
                activeBonuses.put(player.getUniqueId(), true);
                player.sendMessage(MessageUtils
                        .color("&5&l[Living Tools] &d¡Set Completo! &7Has despertado el poder de la Sinergia."));
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
            }

            // Apply Effects
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 0, false, false, true));

            // Particles
            player.getWorld().spawnParticle(org.bukkit.Particle.SPELL_WITCH, player.getLocation().add(0, 1, 0), 2, 0.3,
                    0.5, 0.3, 0);

        } else {
            if (activeBonuses.getOrDefault(player.getUniqueId(), false)) {
                // Bonus Deactivated
                activeBonuses.put(player.getUniqueId(), false);
                player.sendMessage(MessageUtils.color("&5&l[Living Tools] &7Sinergia perdida."));
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.5f);
            }
        }
    }
}
