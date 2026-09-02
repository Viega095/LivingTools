package com.livingtools.abilities;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SelfRepairAbility extends Ability implements org.bukkit.event.Listener {

    public SelfRepairAbility() {
        super("self_repair", "Auto-Reparación", "Usa tu XP para reparar la herramienta.", 10, 5000);
    }

    @Override
    public boolean isCompatible(Material type) {
        return true; // Compatible with all tools
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            if (tool.hasAbility(getId()) && tool.getData().isAbilityActive(getId())) {
                repairItem(player, item, event.getAmount(), tool);
            }
        }

        // Also check offhand or armor? For now just main hand for simplicity
    }

    private void repairItem(Player player, ItemStack item, int xpAmount, LivingTool tool) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable))
            return;

        Damageable damageable = (Damageable) meta;
        if (damageable.getDamage() > 0) {
            int repairAmount = xpAmount * 2; // 1 XP = 2 Durability
            int newDamage = Math.max(0, damageable.getDamage() - repairAmount);

            damageable.setDamage(newDamage);
            item.setItemMeta((ItemMeta) damageable);

            if (tool.isBroken()) {
                tool.setBroken(false);
                player.sendMessage(ChatColor.GREEN + "¡Tu herramienta ha revivido!");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
            }
        }
    }
}
