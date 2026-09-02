package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.runes.RuneType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class RuneListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                java.util.List<RuneType> runes = tool.getData().getRunes();
                java.util.List<com.livingtools.runes.RuneManager.RuneTier> tiers = tool.getData()
                        .getRuneTiers();

                for (int i = 0; i < runes.size(); i++) {
                    RuneType rune = runes.get(i);
                    com.livingtools.runes.RuneManager.RuneTier tier = (i < tiers.size()) ? tiers.get(i)
                            : com.livingtools.runes.RuneManager.RuneTier.COMMON;

                    if (rune == RuneType.VAMPIRISM) {
                        double healAmount = 1.0; // Common
                        if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                            healAmount = 2.0;
                        if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                            healAmount = 3.0;

                        double newHealth = Math.min(player.getHealth() + healAmount,
                                player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                        player.setHealth(newHealth);
                        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 2, 0),
                                1);
                    } else if (rune == RuneType.LUNAR) {
                        long time = player.getWorld().getTime();
                        if (time > 13000 && time < 23000) { // Night time
                            double bonus = 1.25; // Common (+25%)
                            if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                                bonus = 1.50; // +50%
                            if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                                bonus = 1.75; // +75%

                            event.setDamage(event.getDamage() * bonus);
                            player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD,
                                    event.getEntity().getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                        }
                    } else if (rune == RuneType.IGNIS) {
                        double bonus = 1.10; // Common (+10%)
                        if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                            bonus = 1.20; // +20%
                        if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                            bonus = 1.30; // +30%
                        event.setDamage(event.getDamage() * bonus);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Geode Drop (Mining) - 0.2% Chance
        if (Math.random() < 0.002) {
            player.getWorld().dropItemNaturally(event.getBlock().getLocation(),
                    com.livingtools.runes.RuneManager.createGeode());
        }

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            java.util.List<RuneType> runes = tool.getData().getRunes();
            java.util.List<com.livingtools.runes.RuneManager.RuneTier> tiers = tool.getData()
                    .getRuneTiers();

            for (int i = 0; i < runes.size(); i++) {
                RuneType rune = runes.get(i);
                com.livingtools.runes.RuneManager.RuneTier tier = (i < tiers.size()) ? tiers.get(i)
                        : com.livingtools.runes.RuneManager.RuneTier.COMMON;

                if (rune == RuneType.FORTUNA) {
                    double chance = 0.10; // Common
                    if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                        chance = 0.20;
                    if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                        chance = 0.30;

                    if (Math.random() < chance) {
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(),
                                new ItemStack(event.getBlock().getType()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Geode Drop (Mobs) - 0.1% Chance
        if (Math.random() < 0.001) {
            player.getWorld().dropItemNaturally(event.getEntity().getLocation(),
                    com.livingtools.runes.RuneManager.createGeode());
        }

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            java.util.List<RuneType> runes = tool.getData().getRunes();
            java.util.List<com.livingtools.runes.RuneManager.RuneTier> tiers = tool.getData()
                    .getRuneTiers();

            for (int i = 0; i < runes.size(); i++) {
                RuneType rune = runes.get(i);
                com.livingtools.runes.RuneManager.RuneTier tier = (i < tiers.size()) ? tiers.get(i)
                        : com.livingtools.runes.RuneManager.RuneTier.COMMON;

                if (rune == RuneType.GREED || rune == RuneType.FORTUNA) {
                    double chance = 0.10; // Common
                    if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                        chance = 0.20;
                    if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                        chance = 0.30;

                    if (Math.random() < chance) {
                        for (ItemStack drop : event.getDrops()) {
                            player.getWorld().dropItemNaturally(event.getEntity().getLocation(), drop.clone());
                        }
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                    }
                }
            }
        }
    }
}
