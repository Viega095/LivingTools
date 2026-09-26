package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.TalentTreeGUI;
import com.livingtools.manager.TalentTreeManager;
import com.livingtools.manager.TalentTreeManager.Talent;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * TalentTreeListener — procesa las mejoras y reinicios de talentos en TalentTreeGUI.
 */
public class TalentTreeListener implements Listener {

    @EventHandler
    public void onTalentClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(TalentTreeGUI.TITLE)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            player.sendMessage(ChatColor.RED + "Debes sostener tu herramienta viviente.");
            player.closeInventory();
            return;
        }

        LivingTool tool = new LivingTool(held);
        int slot = event.getSlot();

        Talent selectedTalent = null;
        if (slot == 10) selectedTalent = Talent.MINING_INSTINCT;
        if (slot == 19) selectedTalent = Talent.DEEP_ECHO;
        if (slot == 28) selectedTalent = Talent.TREASURE_MASTER;
        if (slot == 13) selectedTalent = Talent.SPIRIT_STRIKE;
        if (slot == 22) selectedTalent = Talent.QUICK_AWAKENING;
        if (slot == 31) selectedTalent = Talent.IGNITE_BLADE;
        if (slot == 16) selectedTalent = Talent.ANCESTRAL_FLOW;
        if (slot == 25) selectedTalent = Talent.CALM_MIND;
        if (slot == 34) selectedTalent = Talent.IMMORTAL_BOND;

        if (selectedTalent != null) {
            boolean success = TalentTreeManager.upgradeTalent(tool, selectedTalent);
            if (success) {
                int newLvl = TalentTreeManager.getTalentLevel(tool, selectedTalent);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1.2, 0), 20, 0.5, 0.5, 0.5, 0.1);
                player.sendMessage(ChatColor.GREEN + "✦ ¡Talento mejorado! " + ChatColor.YELLOW + selectedTalent.getName()
                        + ChatColor.GREEN + " (Nivel " + newLvl + "/" + selectedTalent.getMaxLevel() + ")");
                TalentTreeGUI.open(player, tool);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
            }
            return;
        }

        // Slot 40: Resetear talentos
        if (slot == 40) {
            TalentTreeManager.resetTalents(tool);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.0f);
            player.sendMessage(ChatColor.GOLD + "🔄 Todos los talentos han sido reiniciados. Puntos recuperados.");
            TalentTreeGUI.open(player, tool);
            return;
        }

        // Slot 44: Cerrar
        if (slot == 44) {
            player.closeInventory();
        }
    }
}
