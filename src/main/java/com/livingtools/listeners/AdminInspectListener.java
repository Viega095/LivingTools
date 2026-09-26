package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.AdminInspectGUI;
import com.livingtools.manager.AdminAlertManager;
import com.livingtools.manager.AdminFreezeManager;
import com.livingtools.manager.LevelUpRewardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * AdminInspectListener — ejecuta las acciones de moderación seleccionadas en AdminInspectGUI.
 */
public class AdminInspectListener implements Listener {

    @EventHandler
    public void onAdminClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().startsWith(AdminInspectGUI.TITLE_PREFIX)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        Player admin = (Player) event.getWhoClicked();
        String targetName = event.getView().getTitle().replace(AdminInspectGUI.TITLE_PREFIX, "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            admin.sendMessage(ChatColor.RED + "El jugador objetivo ya no está en línea.");
            admin.closeInventory();
            return;
        }

        ItemStack held = target.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            admin.sendMessage(ChatColor.RED + target.getName() + " ya no tiene la herramienta viviente en mano.");
            admin.closeInventory();
            return;
        }

        LivingTool tool = new LivingTool(held);
        ToolData data = tool.getData();
        int slot = event.getSlot();

        // Slot 19: Congelar / Descongelar
        if (slot == 19) {
            boolean current = AdminFreezeManager.isFrozen(tool);
            AdminFreezeManager.setFrozen(tool, !current);
            admin.sendMessage(ChatColor.YELLOW + "[Moderación] Herramienta de " + target.getName()
                    + (current ? ChatColor.GREEN + " DESCONGELADA." : ChatColor.RED + " CONGELADA."));
            AdminAlertManager.sendStaffAlert(admin.getName() + (current ? " descongeló" : " congeló")
                    + " la herramienta de " + target.getName());
            AdminInspectGUI.open(admin, target);
            return;
        }

        // Slot 21: Purificar Corrupción
        if (slot == 21) {
            data.setCorruption(0);
            tool.updateLore();
            admin.sendMessage(ChatColor.GREEN + "[Moderación] Corrupción de " + target.getName() + " purificada a 0%.");
            admin.playSound(admin.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
            AdminInspectGUI.open(admin, target);
            return;
        }

        // Slot 23: Re-Vincular
        if (slot == 23) {
            data.setOwnerName(admin.getName());
            tool.updateLore();
            admin.sendMessage(ChatColor.GREEN + "[Moderación] Vínculo de dueño reasignado a ti (" + admin.getName() + ").");
            AdminInspectGUI.open(admin, target);
            return;
        }

        // Slot 25: Strip Living Data
        if (slot == 25) {
            ItemStack vanilla = new ItemStack(held.getType());
            target.getInventory().setItemInMainHand(vanilla);
            admin.sendMessage(ChatColor.RED + "[Moderación] Herramienta de " + target.getName() + " des-vivida (convertida a Vanilla).");
            AdminAlertManager.sendStaffAlert(admin.getName() + " transformó la herramienta de " + target.getName() + " a ítem vanilla.");
            admin.closeInventory();
            return;
        }

        // Ajustes de Nivel (+1, +5, +25, -1, -5, -25)
        int levelChange = 0;
        if (slot == 29) levelChange = 1;
        if (slot == 30) levelChange = 5;
        if (slot == 31) levelChange = 25;
        if (slot == 38) levelChange = -1;
        if (slot == 39) levelChange = -5;
        if (slot == 40) levelChange = -25;

        if (levelChange != 0) {
            int newLvl = Math.max(1, data.getLevel() + levelChange);
            data.setLevel(newLvl);
            tool.updateLore();
            admin.sendMessage(ChatColor.YELLOW + "[Moderación] Nivel de " + target.getName() + " cambiado a: " + newLvl);
            AdminInspectGUI.open(admin, target);
            return;
        }

        // Slot 33: +5 Puntos de Talento
        if (slot == 33) {
            for (int i = 0; i < 5; i++) {
                LevelUpRewardManager.awardSkillPoint(target, tool);
            }
            admin.sendMessage(ChatColor.GREEN + "[Moderación] Otorgados +5 Puntos de Habilidad a " + target.getName());
            AdminInspectGUI.open(admin, target);
            return;
        }

        // Slot 49: Cerrar
        if (slot == 49) {
            admin.closeInventory();
        }
    }
}
