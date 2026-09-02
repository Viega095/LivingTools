package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SleepingManager {

    private static final NamespacedKey KEY_SLEEPING = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_sleeping");

    public static ItemStack createSleepingTool(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "Herramienta Dormida");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Esta herramienta duerme profundamente...");
        lore.add(ChatColor.GRAY + "Para despertar esta herramienta tienes que construir un altar.");
        lore.add(ChatColor.GRAY + "Puedes ver cómo hacerlo con: " + ChatColor.YELLOW + "/livingtool altar");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(KEY_SLEEPING, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSleepingTool(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_SLEEPING, PersistentDataType.BYTE);
    }

    public static void tryAwaken(Player player, ItemStack item) {
        if (!isSleepingTool(item))
            return;

        if (player.getLevel() < 30) {
            player.sendMessage(ChatColor.RED + "Necesitas 30 niveles de experiencia para despertar esta herramienta.");
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
            return;
        }

        // Consume XP
        player.setLevel(player.getLevel() - 30);

        // Transform Item
        LivingTool tool = new LivingTool(item);

        // Remove Sleeping Tag
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(KEY_SLEEPING);
        item.setItemMeta(meta);

        // Initialize Living Tool
        tool.getData().initialize();

        // Random Personality
        com.livingtools.mechanics.Personality[] personalities = com.livingtools.mechanics.Personality
                .values();
        com.livingtools.mechanics.Personality randomPersonality = personalities[new java.util.Random()
                .nextInt(personalities.length)];
        tool.getData().setPersonality(randomPersonality.name());

        tool.getData().setCustomName(ChatColor.GOLD + "Herramienta Despierta");
        tool.updateLore();

        // Effects
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1);
        player.spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 1);
        player.sendMessage(ChatColor.GOLD + "¡La herramienta ha despertado!");

        // Personality Dialogue
        PersonalityManager.sayLine(player, tool, PersonalityManager.EventType.AWAKENING);
    }
}
