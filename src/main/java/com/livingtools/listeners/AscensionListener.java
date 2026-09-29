package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.AscensionGUI;
import com.livingtools.manager.DivineAscensionManager;
import com.livingtools.manager.DivineAscensionManager.AscensionPath;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AscensionListener — Maneja la selección en el GUI y la activación de habilidades divinas con Shift + Q.
 */
public class AscensionListener implements Listener {

    private static final Map<UUID, Long> divineCooldown = new HashMap<>();
    private static final long DIVINE_CD_MS = 35000L; // 35s

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(AscensionGUI.TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(held)) {
            player.closeInventory();
            return;
        }

        LivingTool tool = new LivingTool(held);
        int slot = event.getRawSlot();

        AscensionPath selected = null;
        if (slot == 11) selected = AscensionPath.CELESTIAL_SOVEREIGN;
        else if (slot == 13) selected = AscensionPath.ABYSSAL_LORD;
        else if (slot == 15) selected = AscensionPath.PRIMORDIAL_TITAN;

        if (selected != null) {
            if (!DivineAscensionManager.canAscend(tool)) {
                player.sendMessage(ChatColor.RED + "Tu herramienta necesita ser Nivel 100 o Prestigio 2+ para ascender.");
                return;
            }

            DivineAscensionManager.ascendTool(player, tool, selected);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(AscensionGUI.TITLE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShiftDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack dropped = event.getItemDrop().getItemStack();
        if (!LivingTool.isLivingTool(dropped)) return;

        LivingTool tool = new LivingTool(dropped);
        if (DivineAscensionManager.getAscensionPath(tool) == null) return;

        // Cancelar el drop para retener el arma
        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = divineCooldown.getOrDefault(uuid, 0L);

        if (now - last < DIVINE_CD_MS) {
            long rem = (DIVINE_CD_MS - (now - last)) / 1000;
            player.sendMessage(ChatColor.RED + "⏳ Habilidad Divina en enfriamiento: " + rem + "s");
            return;
        }

        divineCooldown.put(uuid, now);
        DivineAscensionManager.triggerDivineAbility(player, tool);
    }

    public static void cleanup(UUID uuid) {
        divineCooldown.remove(uuid);
    }
}
