package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DualWieldManager implements Listener {

    public static void init() {
        LivingToolsPlugin.getInstance().getServer().getPluginManager().registerEvents(new DualWieldManager(),
                LivingToolsPlugin.getInstance());
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (LivingTool.isLivingTool(offhand)) {
            // Apply 50% damage from offhand tool
            // This is a simplified implementation; real dual wield needs animation/packet
            // handling to look good
            // But for stats, we can just boost the damage
            event.setDamage(event.getDamage() * 1.5);

            // Give XP to offhand tool (reduced)
            LivingTool tool = new LivingTool(offhand);
            tool.addXP(player, 5); // Flat amount for now
        }
    }
}
