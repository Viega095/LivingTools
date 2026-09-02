package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class OmniToolManager implements Listener {

    public static final NamespacedKey KEY_IS_OMNI_TOOL = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "is_omni_tool");

    public static ItemStack createOmniTool() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE); // Base item
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&d&l&k||&r &d&lOMNI-HERRAMIENTA &d&l&k||"));

        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7El pináculo de la creación."));
        lore.add(MessageUtils.color("&7Rompe la realidad misma."));
        lore.add("");
        lore.add(MessageUtils.color("&d&lHABILIDADES:"));
        lore.add(MessageUtils.color("&7- &fRompe Bloques Instantáneo"));
        lore.add(MessageUtils.color("&7- &fDurabilidad Infinita"));
        lore.add(MessageUtils.color("&7- &fDaño Masivo"));
        meta.setLore(lore);

        meta.addEnchant(Enchantment.DIG_SPEED, 10, true);
        meta.addEnchant(Enchantment.DAMAGE_ALL, 20, true);
        meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(KEY_IS_OMNI_TOOL, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isOmniTool(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_IS_OMNI_TOOL, PersistentDataType.BYTE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isOmniTool(item)) {
            // Instant break logic is handled by high efficiency, but we can add special
            // effects here
            event.setDropItems(true); // Ensure drops
            player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, event.getBlock().getLocation(), 5);
        }
    }
}
