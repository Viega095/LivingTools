package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.VoidManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class VoidListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item))
            return;
        LivingTool tool = new LivingTool(item);

        if (event.getEntityType() == EntityType.ENDERMAN) {
            VoidManager.checkVoidTouch(player, tool);
        }

        // If tool is already Void-Touched, chance to spawn Rift
        if ("VOID_TOUCHED".equals(tool.getData().getPersonality())) {
            if (Math.random() < 0.05) { // 5% chance
                VoidManager.triggerVoidRift(event.getEntity().getLocation());
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return;
        LivingTool tool = new LivingTool(item);

        if ("VOID_TOUCHED".equals(tool.getData().getPersonality())) {
            // 10% Chance to teleport when hit
            if (Math.random() < 0.10) {
                Location loc = player.getLocation();
                Vector dir = loc.getDirection();
                // Teleport behind or to side
                loc.add(dir.multiply(-1).add(new Vector((Math.random() - 0.5) * 5, 0, (Math.random() - 0.5) * 5)));
                // Ensure safe landing (simple check)
                if (loc.getBlock().getType().isSolid()) {
                    loc.add(0, 1, 0);
                }

                player.teleport(loc);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                event.setCancelled(true); // Dodge the damage!
            }
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking())
            return; // Only when starting to sneak
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item))
            return;
        LivingTool tool = new LivingTool(item);

        if ("VOID_TOUCHED".equals(tool.getData().getPersonality())) {
            // Night Vision in dark areas
            if (player.getLocation().getBlock().getLightLevel() < 8) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, false, false, true));
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.5f, 2.0f);
            }
        }
    }
}
