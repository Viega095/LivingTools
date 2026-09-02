package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class FactionManager {

    public enum Faction {
        VOID, LIGHT, NONE
    }

    public static final NamespacedKey KEY_FACTION = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "player_faction");

    public static void joinFaction(Player player, Faction faction) {
        if (getFaction(player) != Faction.NONE) {
            player.sendMessage(ChatColor.RED + "Ya perteneces a una facción.");
            return;
        }

        // Check Level Requirement (Level 50+)
        boolean hasLevel50Tool = false;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (LivingTool.isLivingTool(item)) {
                if (new LivingTool(item).getData().getLevel() >= 50) {
                    hasLevel50Tool = true;
                    break;
                }
            }
        }

        if (!hasLevel50Tool) {
            player.sendMessage(
                    ChatColor.RED + "Necesitas una Herramienta Viviente de Nivel 50+ para unirte a una facción.");
            return;
        }

        setFaction(player, faction);

        if (faction == Faction.VOID) {
            player.sendMessage(MessageUtils.color("&5&l¡TE HAS UNIDO AL VACÍO!"));
            Bukkit.broadcastMessage(MessageUtils.color("&5" + player.getName() + " ha sucumbido a la oscuridad."));
        } else if (faction == Faction.LIGHT) {
            player.sendMessage(MessageUtils.color("&e&l¡TE HAS UNIDO A LA LUZ!"));
            Bukkit.broadcastMessage(MessageUtils.color("&e" + player.getName() + " ha ascendido a la luz."));
        }
    }

    public static Faction getFaction(Player player) {
        String factionName = player.getPersistentDataContainer().get(KEY_FACTION, PersistentDataType.STRING);
        if (factionName == null)
            return Faction.NONE;
        try {
            return Faction.valueOf(factionName);
        } catch (IllegalArgumentException e) {
            return Faction.NONE;
        }
    }

    public static void setFaction(Player player, Faction faction) {
        player.getPersistentDataContainer().set(KEY_FACTION, PersistentDataType.STRING, faction.name());
    }
}
