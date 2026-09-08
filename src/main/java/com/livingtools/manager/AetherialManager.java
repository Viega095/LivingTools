package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class AetherialManager {

    public static final NamespacedKey KEY_STARLIGHT_ESSENCE = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "starlight_essence");
    public static final NamespacedKey KEY_IS_AETHERIAL = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "is_aetherial");

    public static ItemStack createStarlightEssence() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&e&lEsencia Estelar"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Brilla con la luz de mil soles.");
        lore.add(ChatColor.GRAY + "Se usa para ascender herramientas a lo Etéreo.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(KEY_STARLIGHT_ESSENCE, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isStarlightEssence(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_STARLIGHT_ESSENCE, PersistentDataType.BYTE);
    }

    public static void upgradeToAetherial(Player player, LivingTool tool) {
        if (isAetherial(tool)) {
            player.sendMessage(ChatColor.RED + "Esta herramienta ya es Etérea.");
            return;
        }

        if (com.livingtools.manager.AbyssalManager.isAbyssal(tool)) {
            player.sendMessage(ChatColor.RED + "No puedes ascender una herramienta corrupta. Purifícala primero.");
            return;
        }

        if (tool.getData().getLevel() < 200) {
            player.sendMessage(ChatColor.RED + "La herramienta debe ser Nivel 200 para alcanzar la Luz.");
            return;
        }

        // Apply Upgrade
        ItemMeta meta = tool.getItem().getItemMeta();
        meta.getPersistentDataContainer().set(KEY_IS_AETHERIAL, PersistentDataType.BYTE, (byte) 1);

        // Visuals
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.add(0, ChatColor.YELLOW + "" + ChatColor.BOLD + "☀ ETÉREA ☀");
        }
        meta.setLore(lore);
        tool.getItem().setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0.5f);
        player.spawnParticle(org.bukkit.Particle.TOTEM, player.getLocation(), 50);
        player.sendMessage(MessageUtils.color("&e&l¡TU HERRAMIENTA HA SIDO BENDECIDA POR LA LUZ!"));

        // Grant Passive Buff
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 6000, 0));
    }

    public static boolean isAetherial(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return false;
        return tool.getItem().getItemMeta().getPersistentDataContainer().has(KEY_IS_AETHERIAL, PersistentDataType.BYTE);
    }
}
