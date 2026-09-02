package com.livingtools.commands;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import com.livingtools.manager.FeedingManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FeedCommand {

    public static void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigManager.getMessage("only_players"));
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
            LivingTool tool = new LivingTool(item);
            int fedCount = 0;

            // Loop until full or no food left
            while (true) {
                if (!FeedingManager.tryFeed(player, tool, true)) {
                    break;
                }
                fedCount++;
            }

            if (fedCount > 0) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "Alimentado " + fedCount + " veces.");
            } else {
                player.sendMessage(org.bukkit.ChatColor.RED + "No tienes comida o la herramienta está llena.");
            }
            return;
        }

        LivingTool tool = new LivingTool(item);
        FeedingManager.tryFeed(player, tool);
    }
}
