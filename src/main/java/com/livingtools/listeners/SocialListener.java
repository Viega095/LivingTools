package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class SocialListener implements Listener {

    @EventHandler
    public void onToolSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newStack = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldStack = player.getInventory().getItem(event.getPreviousSlot());

        if (LivingTool.isLivingTool(newStack) && LivingTool.isLivingTool(oldStack)) {
            // Switching between two living tools!
            if (Math.random() < 0.2) { // 20% chance
                LivingTool oldTool = new LivingTool(oldStack);
                String personality = oldTool.getData().getPersonality();

                if ("GLUTTONOUS".equals(personality)) {
                    player.sendMessage(ChatColor.GRAY + "[Herramienta Anterior]: ¡Oye! ¡Yo tenía hambre!");
                } else if ("LAZY".equals(personality)) {
                    player.sendMessage(ChatColor.GRAY + "[Herramienta Anterior]: Uff, por fin un descanso.");
                } else {
                    player.sendMessage(ChatColor.GRAY + "[Herramienta Anterior]: ¡Hey! ¿Por qué me guardas?");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player))
            return;

        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            String myPersonality = tool.getData().getPersonality();

            if ("GLUTTONOUS".equals(myPersonality)) {
                player.sendMessage(ChatColor.GRAY + "[Tu Herramienta]: ¿Ese jugador tiene comida?");
                target.sendMessage(ChatColor.GRAY + "[Herramienta de " + player.getName()
                        + "]: ¿Tienes comida? ¡Tengo hambre!");
            } else if ("LAZY".equals(myPersonality)) {
                player.sendMessage(ChatColor.GRAY + "[Tu Herramienta]: ¿Tú también trabajas? Qué pereza...");
                target.sendMessage(ChatColor.GRAY + "[Herramienta de " + player.getName()
                        + "]: ¿Tú también trabajas? Qué pereza...");
            } else {
                player.sendMessage(ChatColor.WHITE + "[Tu Herramienta]: ¡Hola!");
                target.sendMessage(ChatColor.WHITE + "[Herramienta de " + player.getName() + "]: ¡Hola!");
            }
        }
    }
}
