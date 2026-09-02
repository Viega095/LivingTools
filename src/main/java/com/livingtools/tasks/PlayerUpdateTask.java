package com.livingtools.tasks;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.SynergyManager;
import com.livingtools.runes.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerUpdateTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        // Check Main Hand
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            handleToolEffects(player, tool, item);
        }

        // Check Armor
        handleArmorEffects(player);

        // Synergy Check (Set Bonus)
        SynergyManager.applySetBonus(player);
    }

    private void handleToolEffects(Player player, LivingTool tool, ItemStack item) {
        // Runes (Celeritas)
        for (RuneType rune : tool.getData().getRunes()) {
            if (rune == RuneType.CELERITAS) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
            }
        }

        // Visual Effects
        if (tool.hasAbility("infernal_infusion")) {
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 0.5, 0), 2, 0.2, 0.2, 0.2,
                    0.02);
        }

        int level = tool.getData().getLevel();
        if (level >= 200) { // Mythic / Prestige
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 5, 0.3, 0.5,
                    0.3, 0.05);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.5, 0), 1, 0.2, 0.2, 0.2,
                    0.01);
        } else if (level >= 100) {
            player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.1);
        } else if (level >= 50) {
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 3, 0.5, 0.5,
                    0.5, 0.1);
        }

        // Lazy Personality (Passive Repair)
        if ("LAZY".equals(tool.getData().getPersonality()) && !player.isSprinting() && !player.isSneaking()) {
            if (Math.random() < 0.05) {
                Damageable meta = (Damageable) item.getItemMeta();
                if (meta.hasDamage()) {
                    meta.setDamage(meta.getDamage() - 1);
                    item.setItemMeta(meta);
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 1, 0.5, 0.5, 0.5,
                            0.1);
                }
            }
        }
    }

    private void handleArmorEffects(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (LivingArmor.isLivingArmor(armor)) {
                LivingArmor livingArmor = new LivingArmor(armor);
                for (String abilityId : livingArmor.getAbilities()) {
                    Ability ability = AbilityRegistry.getAbility(abilityId);
                    if (ability != null && ability.isCompatible(armor.getType())) {
                        ability.onHold(player);
                    }
                }
            }
        }
    }

    public static void start() {
        new PlayerUpdateTask().runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 20L);
    }
}
