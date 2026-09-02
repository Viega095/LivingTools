package com.livingtools.gui;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.manager.AbilityMasteryManager;
import com.livingtools.manager.AbilityStatisticsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AbilityMenuGUI {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "✦ Mis Habilidades ✦");

        int slot = 0;
        for (Ability ability : AbilityRegistry.getAbilities().values()) {
            if (slot >= 45)
                break; // Max 5 rows of abilities

            ItemStack item = createAbilityItem(player, ability);
            inv.setItem(slot, item);
            slot++;
        }

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "ℹ Información");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Click en una habilidad para");
        infoLore.add(ChatColor.GRAY + "ver estadísticas detalladas");
        infoLore.add("");
        infoLore.add(ChatColor.YELLOW + "Habilidades Desbloqueadas: " + ChatColor.WHITE + slot);
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(49, info);

        player.openInventory(inv);
    }

    private static ItemStack createAbilityItem(Player player, Ability ability) {
        Material material = getIconForAbility(ability.getId());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Title
        int level = AbilityMasteryManager.getLevel(player, ability.getId());
        String title = ChatColor.AQUA + "" + ChatColor.BOLD + ability.getName();
        if (level >= 10) {
            title += ChatColor.LIGHT_PURPLE + " ★AWAKENED★";
        }
        meta.setDisplayName(title);

        List<String> lore = new ArrayList<>();

        // Mastery
        int xp = AbilityMasteryManager.getXP(player, ability.getId());
        int nextLevel = AbilityMasteryManager.getXPForNextLevel(player, ability.getId());
        lore.add(ChatColor.GOLD + "Nivel de Maestría: " + ChatColor.YELLOW + level + "/10");
        lore.add(createProgressBar(xp, nextLevel, 20));
        lore.add(ChatColor.GRAY + "XP: " + xp + "/" + nextLevel);
        lore.add("");

        // Stats
        int uses = AbilityStatisticsManager.getTotalUses(player, ability.getId());
        double damage = AbilityStatisticsManager.getTotalDamage(player, ability.getId());
        int kills = AbilityStatisticsManager.getTotalKills(player, ability.getId());

        lore.add(ChatColor.YELLOW + "📊 Estadísticas:");
        lore.add(ChatColor.GRAY + "  • Usos: " + ChatColor.WHITE + uses);
        if (damage > 0) {
            lore.add(ChatColor.GRAY + "  • Daño Total: " + ChatColor.WHITE + String.format("%.1f", damage));
        }
        if (kills > 0) {
            lore.add(ChatColor.GRAY + "  • Enemigos: " + ChatColor.WHITE + kills);
        }
        lore.add("");

        // Bonuses
        if (level >= 3) {
            double cooldownReduc = AbilityMasteryManager.getCooldownReduction(player, ability.getId());
            double effectiveness = AbilityMasteryManager.getEffectivenessBonus(player, ability.getId());

            lore.add(ChatColor.GREEN + "⚡ Bonificaciones Activas:");
            if (cooldownReduc > 0) {
                lore.add(ChatColor.GRAY + "  • Cooldown: " + ChatColor.AQUA + "-" + (int) (cooldownReduc * 100) + "%");
            }
            if (effectiveness > 0) {
                lore.add(ChatColor.GRAY + "  • Efectividad: " + ChatColor.AQUA + "+" + (int) (effectiveness * 100)
                        + "%");
            }
            lore.add("");
        }

        // Description
        lore.add(ChatColor.DARK_GRAY + ability.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click para ver detalles");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static String createProgressBar(int current, int max, int length) {
        int filled = (int) ((double) current / max * length);
        StringBuilder bar = new StringBuilder(ChatColor.GREEN + "");

        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i == 0) {
                bar.append(ChatColor.GRAY);
                bar.append("█");
            } else {
                bar.append("█");
            }
        }

        return bar.toString();
    }

    private static Material getIconForAbility(String abilityId) {
        switch (abilityId.toLowerCase()) {
            case "blink":
            case "shadowstep":
            case "thunderstep":
                return Material.ENDER_PEARL;
            case "recall":
            case "timefreeze":
                return Material.CLOCK;
            case "swap":
            case "dimensionalrift":
                return Material.ENDER_EYE;
            case "blinkstrike":
                return Material.IRON_SWORD;
            case "vortex":
                return Material.CONDUIT;
            case "phaseshift":
                return Material.GHAST_TEAR;
            case "infernoleap":
                return Material.FIRE_CHARGE;
            case "bloodsacrifice":
                return Material.REDSTONE;
            case "soulharvest":
                return Material.SOUL_SAND;
            default:
                return Material.NETHER_STAR;
        }
    }
}
