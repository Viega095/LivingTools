package com.livingtools.commands;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ChatColor.RED + "Debes sostener una Living Tool.");
            return true;
        }

        LivingTool tool = new LivingTool(item);
        com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
        return true;
    }
}
