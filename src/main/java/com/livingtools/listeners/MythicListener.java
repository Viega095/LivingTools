package com.livingtools.listeners;

import com.livingtools.abilities.mythic.MythicSlayerAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MythicListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item))
            return;
        LivingTool tool = new LivingTool(item);

        if (tool.hasAbility("mythic_slayer") && MythicSlayerAbility.isMythic(event.getEntityType())) {
            event.setDamage(event.getDamage() * 1.5); // +50% Damage
            player.getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_WITHER_HURT, 0.5f, 2.0f);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!MythicSlayerAbility.isMythic(event.getEntityType()))
            return;

        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (LivingTool.isLivingTool(item)) {
                // Drop Mythic Essence
                ItemStack essence = new ItemStack(Material.NETHER_STAR);
                ItemMeta meta = essence.getItemMeta();
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Esencia Mítica");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Material de crafteo legendario."));
                essence.setItemMeta(meta);

                event.getDrops().add(essence);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "¡Has obtenido Esencia Mítica!");
            }
        }
    }
}
