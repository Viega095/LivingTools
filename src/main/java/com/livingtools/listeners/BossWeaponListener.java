package com.livingtools.listeners;

import com.livingtools.manager.BossWeaponManager;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BossWeaponListener implements Listener {

    @EventHandler
    public void onForestRegen(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!BossWeaponManager.is(BossWeaponManager.FinalWeapon.VERDANT_STAFF, hand)) {
            return;
        }
        Biome biome = event.getTo().getBlock().getBiome();
        String biomeName = biome.name();
        if (!biomeName.contains("FOREST") && !biomeName.contains("JUNGLE") && !biomeName.contains("TAIGA")
                && !biomeName.contains("BIRCH") && !biomeName.contains("DARK_FOREST")) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false));
    }

    @EventHandler
    public void onWeaponHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        ItemStack hand = player.getInventory().getItemInMainHand();
        BossWeaponManager.FinalWeapon weapon = BossWeaponManager.fromItem(hand);
        if (weapon == null) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) event.getEntity();

        if (weapon == BossWeaponManager.FinalWeapon.SANDSTORM_FANG) {
            Biome biome = player.getLocation().getBlock().getBiome();
            if (biome.name().contains("DESERT") || biome.name().contains("BADLANDS")
                    || biome.name().contains("SAVANNA")) {
                target.setFireTicks(60);
                Vector knock = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize()
                        .multiply(0.8);
                knock.setY(0.35);
                target.setVelocity(knock);
            }
        } else if (weapon == BossWeaponManager.FinalWeapon.ABYSS_ANCHOR && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, true, false));
        }
    }
}
