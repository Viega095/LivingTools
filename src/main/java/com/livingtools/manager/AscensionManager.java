package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AscensionManager {

    public static void ascendTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            MessageUtils.send(player, "&cDebes sostener una Herramienta Viviente.");
            return;
        }

        LivingTool tool = new LivingTool(item);
        if (tool.getData().getLevel() < 200) {
            MessageUtils.send(player, "&cTu herramienta debe ser Nivel 200 para ascender.");
            return;
        }

        // Perform Ascension
        tool.getData().setLevel(1);
        tool.getData().setXP(0);
        // In a real system, we would store "Prestige Level" in PDC
        // For now, we'll just add a lore line or prefix

        MessageUtils.send(player, "&b&l¡ASCENSIÓN COMPLETADA!");
        MessageUtils.send(player, "&7Tu herramienta ha renacido con mayor poder.");

        tool.updateLore();
        player.getWorld().strikeLightningEffect(player.getLocation());
    }
}
