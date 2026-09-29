package com.livingtools.listeners;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.gui.GemSocketGUI;
import com.livingtools.manager.GemSocketManager;
import com.livingtools.manager.GemSocketManager.GemType;
import com.livingtools.visuals.DamageIndicatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GemSocketListener — Maneja el engarce de gemas en el GUI y los efectos activos como la Gema del Fénix.
 */
public class GemSocketListener implements Listener {

    private static final Map<UUID, Long> phoenixCooldown = new HashMap<>();
    private static final long PHOENIX_CD_MS = 10 * 60 * 1000L; // 10 min

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(GemSocketGUI.TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(held)) {
            player.closeInventory();
            return;
        }

        LivingTool tool = new LivingTool(held);
        int slot = event.getRawSlot();

        int[] socketSlots = {10, 12, 14, 16};
        int clickedIndex = -1;
        for (int i = 0; i < socketSlots.length; i++) {
            if (socketSlots[i] == slot) {
                clickedIndex = i;
                break;
            }
        }

        if (clickedIndex != -1) {
            List<GemType> currentGems = GemSocketManager.getSocketedGems(tool);
            int max = GemSocketManager.getMaxUnlockedSockets(tool);

            if (clickedIndex < currentGems.size()) {
                // Desengarzar gema existente
                GemType removed = GemSocketManager.unsocketGem(tool, clickedIndex);
                if (removed != null) {
                    player.getInventory().addItem(GemSocketManager.createGemItem(removed));
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.2f, 1.2f);
                    player.sendMessage(ChatColor.YELLOW + "✧ Gema desengarzada y devuelta a tu inventario.");
                    GemSocketGUI.open(player, tool);
                }
            } else if (clickedIndex < max) {
                // Intentar engarzar una gema del inventario
                GemType toSocket = findGemInInventory(player);
                if (toSocket == null) {
                    player.sendMessage(ChatColor.RED + "No tienes ninguna Gema Celestial en tu inventario para engarzar.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return;
                }

                if (tool.getData().getLevel() < toSocket.getRequiredLevel() && tool.getData().getPrestige() < 1) {
                    player.sendMessage(ChatColor.RED + "Tu herramienta necesita ser Nivel " + toSocket.getRequiredLevel() + " para esta gema.");
                    return;
                }

                removeGemFromInventory(player, toSocket);
                GemSocketManager.socketGem(tool, toSocket);

                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.5f);
                player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 25, 0.4, 0.4, 0.4, 0.1);
                player.sendMessage(ChatColor.GREEN + "✦ ¡Gema " + toSocket.getDisplayName() + ChatColor.GREEN + " engarzada con éxito!");

                GemSocketGUI.open(player, tool);
            }
        }
    }

    private GemType findGemInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            String raw = item.getItemMeta().getPersistentDataContainer()
                    .get(new org.bukkit.NamespacedKey(LivingToolsPlugin.getInstance(), "gem_item_type"), PersistentDataType.STRING);
            if (raw != null) {
                try {
                    return GemType.valueOf(raw);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    private void removeGemFromInventory(Player player, GemType gem) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            String raw = item.getItemMeta().getPersistentDataContainer()
                    .get(new org.bukkit.NamespacedKey(LivingToolsPlugin.getInstance(), "gem_item_type"), PersistentDataType.STRING);
            if (raw != null && raw.equals(gem.name())) {
                item.setAmount(item.getAmount() - 1);
                return;
            }
        }
    }

    /**
     * Gema del Fénix — Previene muerte fatal si la herramienta tiene engarzada la gema.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getHealth() - event.getFinalDamage() > 0) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            held = player.getInventory().getItemInOffHand();
        }
        if (!LivingTool.isLivingTool(held)) return;

        LivingTool tool = new LivingTool(held);
        if (!GemSocketManager.hasGem(tool, GemType.PHOENIX_GEM)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = phoenixCooldown.getOrDefault(uuid, 0L);

        if (now - last < PHOENIX_CD_MS) return;

        // Salvar al jugador
        event.setCancelled(true);
        phoenixCooldown.put(uuid, now);
        player.setHealth(Math.min(player.getMaxHealth(), 12.0)); // 6 corazones

        // Efectos del Fénix
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 80, 0.8, 1.2, 0.8, 0.2);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.4f);

        DamageIndicatorManager.spawnIndicator(player.getLocation().add(0, 1.5, 0), ChatColor.GOLD + "🔥 ¡RENACER DEL FÉNIX! 🔥");
        player.sendMessage(ChatColor.GOLD + "🔥 ¡Tu Gema del Fénix se ha activado salvándote de la muerte!");
    }

    public static void cleanup(UUID uuid) {
        phoenixCooldown.remove(uuid);
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(GemSocketGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
