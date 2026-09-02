package com.livingtools.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StructureCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED
                    + "Uso: /livingtool structure <altar|assembly|bossforge|cursedforge|runeforge>");
            player.sendMessage(ChatColor.GRAY + "Activa la guía visual y muestra los materiales necesarios.");
            player.sendMessage(ChatColor.YELLOW + "Tip: 'forge' es alias de 'cursedforge' (Forja Maldita).");
            sendAllLegends(player);
            return true;
        }

        String type = args[1].toLowerCase();

        switch (type) {
            case "altar":
                com.livingtools.visuals.RitualVisualizer.toggleGuide(player);
                return true;
            case "assembly":
                com.livingtools.visuals.AssemblyVisualizer.toggleGuide(player);
                return true;
            case "bossforge":
                com.livingtools.visuals.BossForgeVisualizer.toggleGuide(player);
                return true;
            case "forge":
            case "cursedforge":
                com.livingtools.visuals.CursedForgeVisualizer.toggleGuide(player);
                if (type.equals("forge")) {
                    player.sendMessage(ChatColor.YELLOW + "Alias 'forge' → Forja Maldita. "
                            + "Para jefes usa: /livingtool structure bossforge");
                }
                return true;
            case "runeforge":
                com.livingtools.visuals.RuneForgeVisualizer.toggleGuide(player);
                return true;
            case "help":
            case "list":
                sendAllLegends(player);
                return true;
            default:
                player.sendMessage(ChatColor.RED
                        + "Estructura desconocida. Usa: altar, assembly, bossforge, cursedforge, runeforge");
                return true;
        }
    }

    private static void sendAllLegends(Player player) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "--- Resumen de estructuras ---");
        com.livingtools.visuals.RitualVisualizer.sendMaterialLegend(player);
        player.sendMessage("");
        com.livingtools.visuals.AssemblyVisualizer.sendMaterialLegend(player);
        player.sendMessage("");
        com.livingtools.visuals.BossForgeVisualizer.sendMaterialLegend(player);
        player.sendMessage("");
        com.livingtools.visuals.CursedForgeVisualizer.sendMaterialLegend(player);
        player.sendMessage("");
        com.livingtools.visuals.RuneForgeVisualizer.sendMaterialLegend(player);
    }
}
