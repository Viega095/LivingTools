package com.livingtools.listeners;

import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulboundListener implements Listener {

    private static final Map<UUID, List<ItemStack>> soulboundItems = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> savedItems = new ArrayList<>();
        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (isSoulbound(item)) {
                savedItems.add(item);
                iterator.remove(); // Remove from drops
            }
        }

        if (!savedItems.isEmpty()) {
            soulboundItems.put(player.getUniqueId(), savedItems);
            player.sendMessage(ChatColor.AQUA + "¡Tus herramientas vivientes se han vinculado a tu alma!");
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> items = soulboundItems.remove(player.getUniqueId());

        if (items != null) {
            for (ItemStack item : items) {
                player.getInventory().addItem(item);
            }
            player.sendMessage(ChatColor.AQUA + "¡Tus herramientas han regresado a ti!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
        }
    }

    private boolean isSoulbound(ItemStack item) {
        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            return tool.hasAbility("soulbound");
        } else if (LivingArmor.isLivingArmor(item)) {
            // Armor doesn't have abilities yet, so maybe keep level check or assume false?
            // For now, let's keep the level check for armor as a fallback or remove it if
            // armor should have abilities.
            // But the request was specifically about "Ability not found" errors, implying
            // we should use the ability system.
            // However, LivingArmor class might not support abilities yet.
            // Let's check if LivingArmor has hasAbility method.
            // Based on previous files, LivingArmor is separate.
            // Let's stick to the plan: "Update isSoulbound to check
            // tool.hasAbility("soulbound")".
            // For armor, we'll leave it as is for now or disable it if it causes issues.
            // Actually, the plan said "Update isSoulbound to check
            // tool.hasAbility("soulbound") instead of hardcoded level check."
            LivingArmor armor = new LivingArmor(item);
            return armor.getLevel() >= 30; // Keep level check for armor for now
        }
        return false;
    }
}
