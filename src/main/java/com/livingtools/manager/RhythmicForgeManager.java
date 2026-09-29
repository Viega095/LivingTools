package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.visuals.DamageIndicatorManager;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RhythmicForgeManager — Minijuego de Forja Rítmica de Calibración de Almas con Calidad Obra Maestra.
 */
public class RhythmicForgeManager {

    private static NamespacedKey KEY_MASTERWORK_EXPIRY;

    public static NamespacedKey getKeyMasterworkExpiry() {
        if (KEY_MASTERWORK_EXPIRY == null) {
            KEY_MASTERWORK_EXPIRY = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_masterwork_expiry");
        }
        return KEY_MASTERWORK_EXPIRY;
    }

    public static class ForgeGameSession {
        private final UUID playerId;
        private final Inventory gui;
        private final LivingTool tool;
        private int needlePos = 10; // Slots 10 a 16
        private int direction = 1;
        private BukkitTask ticker;
        private boolean finished = false;

        public ForgeGameSession(UUID playerId, Inventory gui, LivingTool tool) {
            this.playerId = playerId;
            this.gui = gui;
            this.tool = tool;
        }

        public int getNeedlePos() { return needlePos; }
        public void setNeedlePos(int p) { this.needlePos = p; }
        public int getDirection() { return direction; }
        public void setDirection(int d) { this.direction = d; }
        public boolean isFinished() { return finished; }
        public void setFinished(boolean f) { this.finished = f; }
    }

    private static final Map<UUID, ForgeGameSession> activeSessions = new HashMap<>();

    public static void startGame(Player player, LivingTool tool, Inventory gui) {
        ForgeGameSession session = new ForgeGameSession(player.getUniqueId(), gui, tool);
        activeSessions.put(player.getUniqueId(), session);

        session.ticker = new BukkitRunnable() {
            @Override
            public void run() {
                if (session.isFinished() || !player.isOnline() || !player.getOpenInventory().getTitle().contains("Forja Rítmica")) {
                    cancel();
                    activeSessions.remove(player.getUniqueId());
                    return;
                }

                // Renderizar pista de ritmo (slots 10 a 16)
                for (int slot = 10; slot <= 16; slot++) {
                    if (slot == session.getNeedlePos()) {
                        gui.setItem(slot, GUIBuilder.createGlowingItem(Material.ANVIL, ChatColor.YELLOW + "🔨 ¡GOLPE AQUÍ!"));
                    } else if (slot == 13) {
                        gui.setItem(slot, GUIBuilder.createGlowingItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "★ ZONA PERFECTA ★"));
                    } else if (slot == 12 || slot == 14) {
                        gui.setItem(slot, GUIBuilder.createItem(Material.GOLD_BLOCK, ChatColor.YELLOW + "Zona Buena"));
                    } else {
                        gui.setItem(slot, GUIBuilder.createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
                    }
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f + (session.getNeedlePos() - 10) * 0.1f);

                // Mover aguja
                int next = session.getNeedlePos() + session.getDirection();
                if (next > 16) {
                    session.setNeedlePos(15);
                    session.setDirection(-1);
                } else if (next < 10) {
                    session.setNeedlePos(11);
                    session.setDirection(1);
                } else {
                    session.setNeedlePos(next);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 2L, 2L);
    }

    public static void handleStrike(Player player) {
        ForgeGameSession session = activeSessions.remove(player.getUniqueId());
        if (session == null || session.isFinished()) return;

        session.setFinished(true);
        if (session.ticker != null) session.ticker.cancel();

        int hitSlot = session.getNeedlePos();
        LivingTool tool = session.tool;
        Location loc = player.getLocation();

        player.closeInventory();

        if (hitSlot == 13) {
            // ¡GOLPE PERFECTO! -> OBRA MAESTRA
            long oneHour = System.currentTimeMillis() + (60 * 60 * 1000L);
            tool.getItem().getItemMeta();
            org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(getKeyMasterworkExpiry(), PersistentDataType.LONG, oneHour);
                tool.getItem().setItemMeta(meta);
            }
            tool.updateLore();

            player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.2f);
            player.playSound(loc, Sound.BLOCK_ANVIL_USE, 1.2f, 1.6f);
            ParticleOptimizer.spawnHelix(loc.add(0, 1, 0), 1.5, 3.0, Particle.TOTEM, null);
            DamageIndicatorManager.spawnIndicator(loc.add(0, 1.5, 0), ChatColor.GOLD + "🌟 ¡CALIDAD: OBRA MAESTRA! (+15% Stats)");

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "╔══════════════════════════════════════════════╗");
            player.sendMessage(ChatColor.YELLOW + "  🔨 ¡GOLPE DE FORJA PERFECTO! (BULLSEYE)");
            player.sendMessage(ChatColor.GREEN + "  Tu arma ha sido imbuida con calidad " + ChatColor.GOLD + "OBRA MAESTRA");
            player.sendMessage(ChatColor.GRAY + "  +15% Daño y Velocidad de Minería durante 1 hora.");
            player.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════════════╝");
            player.sendMessage("");
        } else if (hitSlot == 12 || hitSlot == 14) {
            // Golpe Bueno
            tool.addXP(player, 150L);
            player.playSound(loc, Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
            player.sendMessage(ChatColor.GREEN + "🔨 ¡Buen golpe de forja! (+150 XP de bonificación)");
        } else {
            // Golpe Normal
            tool.addXP(player, 50L);
            player.playSound(loc, Sound.BLOCK_ANVIL_HIT, 1.0f, 0.8f);
            player.sendMessage(ChatColor.YELLOW + "🔨 Calibración de forja estándar completada (+50 XP).");
        }
    }

    public static boolean isMasterwork(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return false;
        Long expiry = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(getKeyMasterworkExpiry(), PersistentDataType.LONG);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    public static void cleanup(UUID uuid) {
        ForgeGameSession s = activeSessions.remove(uuid);
        if (s != null && s.ticker != null) s.ticker.cancel();
    }
}
