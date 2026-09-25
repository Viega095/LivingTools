package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.ToolReforgeGUI;
import com.livingtools.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Random;

/**
 * ToolReforgeListener — maneja interacciones con el Yunque/Mesa de Herrería
 * y las acciones dentro de ToolReforgeGUI.
 */
public class ToolReforgeListener implements Listener {

    private static final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material blockType = event.getClickedBlock().getType();
        if (blockType != Material.ANVIL && blockType != Material.CHIPPED_ANVIL
                && blockType != Material.DAMAGED_ANVIL && blockType != Material.SMITHING_TABLE) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) return;

        event.setCancelled(true);
        ToolReforgeGUI.open(player, new LivingTool(held));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(ToolReforgeGUI.TITLE)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Debes sostener tu herramienta viviente.");
            return;
        }

        LivingTool tool = new LivingTool(held);
        ToolData data = tool.getData();
        int slot = event.getSlot();

        // Slot 11: Reparar Durabilidad
        if (slot == 11) {
            if (!(held.getItemMeta() instanceof Damageable)) return;
            Damageable dam = (Damageable) held.getItemMeta();
            if (dam.getDamage() <= 0) {
                player.sendMessage(ChatColor.YELLOW + "Tu herramienta ya está completamente reparada.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                return;
            }

            if (data.getXP() < 300) {
                player.sendMessage(ChatColor.RED + "Tu herramienta necesita al menos 300 XP para autorrepararse (Tiene: " + data.getXP() + " XP).");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            data.setXP(data.getXP() - 300);
            dam.setDamage(0);
            held.setItemMeta((org.bukkit.inventory.meta.ItemMeta) dam);
            tool.updateLore();

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            player.sendMessage(ChatColor.GREEN + "🔨 ¡Herramienta completamente reparada con éxito! (-300 XP)");
            player.closeInventory();
            return;
        }

        // Slot 13: Pulido de Almas
        if (slot == 13) {
            if (data.getXP() < 500) {
                player.sendMessage(ChatColor.RED + "Se requieren 500 XP de herramienta para el pulido (Tiene: " + data.getXP() + " XP).");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            data.setXP(data.getXP() - 500);
            data.adjustMood(10); // Aumentar felicidad
            tool.updateLore();

            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.5f);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.2, 0), 35, 0.5, 0.5, 0.5, 0.1);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ ¡Herramienta pulida con éxito! (+10 Estado de Ánimo, -500 XP)");
            player.closeInventory();
            return;
        }

        // Slot 15: Re-alinear Personalidad
        if (slot == 15) {
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.NETHERITE_SCRAP), 1)) {
                player.sendMessage(ChatColor.RED + "Necesitas al menos 1 Netherite Scrap en tu inventario para la re-alineación.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            // Consumir 1 scrap
            player.getInventory().removeItem(new ItemStack(Material.NETHERITE_SCRAP, 1));

            // Seleccionar nueva personalidad distinta
            String[] personalities = {"AGGRESSIVE", "WISE", "LAZY", "CHEERFUL"};
            String current = data.getPersonality() != null ? data.getPersonality() : "WISE";
            String next;
            do {
                next = personalities[random.nextInt(personalities.length)];
            } while (next.equals(current) && personalities.length > 1);

            data.setPersonality(next);
            tool.updateLore();

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
            player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1.5, 0), 40, 0.6, 0.6, 0.6, 0.2);
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "🔮 ¡La conciencia de tu herramienta ha cambiado a: "
                    + ChatColor.YELLOW + next + ChatColor.GOLD + "!");
            player.sendMessage("");
            player.closeInventory();
            return;
        }

        // Slot 22: Cerrar
        if (slot == 22) {
            player.closeInventory();
        }
    }
}
