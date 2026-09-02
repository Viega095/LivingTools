package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.api.events.LivingToolLevelUpEvent;
import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class SoulBindingManager implements Listener {

    private final NamespacedKey BOUND_TO_KEY = new NamespacedKey(LivingToolsPlugin.getInstance(), "soulbound_to");
    private final NamespacedKey BOUND_NAME_KEY = new NamespacedKey(LivingToolsPlugin.getInstance(), "soulbound_name");

    @EventHandler
    public void onLevelUp(LivingToolLevelUpEvent event) {
        if (event.getNewLevel() == 10) {
            LivingTool tool = event.getTool();
            if (!isBound(tool.getItem())) {
                bindTool(tool.getItem(), event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        if (isBound(item)) {
            UUID ownerUUID = getBoundOwner(item);
            if (ownerUUID != null && !ownerUUID.equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking())
            return;
        if (event.getItem() == null)
            return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (LivingTool.isLivingTool(mainHand) && isBound(mainHand)) {
            if (isPurifiedEssence(offHand)) {
                unbindTool(mainHand, player);
                offHand.setAmount(offHand.getAmount() - 1);
                event.setCancelled(true);
            }
        }
    }

    private void bindTool(ItemStack item, Player player) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(BOUND_TO_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
        meta.getPersistentDataContainer().set(BOUND_NAME_KEY, PersistentDataType.STRING, player.getName());

        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.add(ChatColor.GOLD + "Soulbound to: " + player.getName());
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        player.sendMessage(MessageUtils.color("&6&l¡Tu herramienta se ha vinculado a tu alma!"));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
    }

    private void unbindTool(ItemStack item, Player player) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(BOUND_TO_KEY);
        meta.getPersistentDataContainer().remove(BOUND_NAME_KEY);

        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.removeIf(line -> line.contains("Soulbound to:"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        player.sendMessage(MessageUtils.color("&a&l¡Tu herramienta ha sido liberada!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    private boolean isBound(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(BOUND_TO_KEY, PersistentDataType.STRING);
    }

    private UUID getBoundOwner(ItemStack item) {
        String uuidStr = item.getItemMeta().getPersistentDataContainer().get(BOUND_TO_KEY, PersistentDataType.STRING);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }

    private boolean isPurifiedEssence(ItemStack item) {
        return item != null && item.getType() == Material.GHAST_TEAR && item.hasItemMeta()
                && item.getItemMeta().getDisplayName().contains("Purified Essence");
    }
}
