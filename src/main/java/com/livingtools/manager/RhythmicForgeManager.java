package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingArmor;
import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.visuals.DamageIndicatorManager;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * RhythmicForgeManager — Gestiona la estructura física en el mundo, el minijuego
 * de forja rítmica, la reparación integral de herramientas/armaduras y el temple de almas.
 */
public class RhythmicForgeManager {

    private static NamespacedKey KEY_MASTERWORK_EXPIRY;
    private static NamespacedKey KEY_SOUL_FORGE_CORE;

    public static NamespacedKey getKeyMasterworkExpiry() {
        if (KEY_MASTERWORK_EXPIRY == null) {
            KEY_MASTERWORK_EXPIRY = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_masterwork_expiry");
        }
        return KEY_MASTERWORK_EXPIRY;
    }

    public static NamespacedKey getKeySoulForgeCore() {
        if (KEY_SOUL_FORGE_CORE == null) {
            KEY_SOUL_FORGE_CORE = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_soul_forge_core");
        }
        return KEY_SOUL_FORGE_CORE;
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
        public LivingTool getTool() { return tool; }
    }

    private static final Map<UUID, ForgeGameSession> activeSessions = new HashMap<>();

    /**
     * Comprueba si el bloque es el centro de una Forja Rítmica de Almas válida.
     */
    public static boolean isRhythmicForge(Block block) {
        if (block == null) return false;
        Material mat = block.getType();
        if (mat != Material.ANVIL && mat != Material.CHIPPED_ANVIL && mat != Material.DAMAGED_ANVIL
                && mat != Material.SMITHING_TABLE && mat != Material.LODESTONE) {
            return false;
        }

        // Si estamos en el Templo del Reino de LivingTools
        if (block.getWorld().getName().equals(LivingRealmManager.REALM_WORLD_NAME)) {
            Location loc = block.getLocation();
            if (loc.getBlockX() == -60 && loc.getBlockZ() == 60) return true;
            if (loc.getBlockX() == -120 && loc.getBlockZ() == 6) return true;
        }

        // Comprobación de Base Mística (Bloque inferior)
        Block below = block.getRelative(0, -1, 0);
        Material belowMat = below.getType();
        boolean validBase = (belowMat == Material.SOUL_CAMPFIRE || belowMat == Material.SOUL_FIRE ||
                belowMat == Material.SOUL_SAND || belowMat == Material.SOUL_SOIL ||
                belowMat == Material.CRYING_OBSIDIAN || belowMat == Material.MAGMA_BLOCK ||
                belowMat == Material.POLISHED_BLACKSTONE_BRICKS || belowMat == Material.NETHERITE_BLOCK);

        if (!validBase) return false;

        // Comprobación de Esquinas Místicas (4 linternas o piedras rúnicas)
        int validCorners = 0;
        int[][] corners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] c : corners) {
            Material cornerAtLevel = block.getRelative(c[0], 0, c[1]).getType();
            Material cornerFloor = below.getRelative(c[0], 0, c[1]).getType();
            if (cornerAtLevel == Material.SOUL_LANTERN || cornerAtLevel == Material.SOUL_TORCH ||
                cornerAtLevel == Material.POLISHED_BLACKSTONE_WALL || cornerFloor == Material.CRYING_OBSIDIAN ||
                cornerFloor == Material.POLISHED_BLACKSTONE_BRICKS) {
                validCorners++;
            }
        }

        return validCorners >= 2;
    }

    /**
     * Construye la estructura física 3x3 de la Forja Rítmica en el mundo.
     */
    public static void buildStructure(Location centerLocation) {
        World world = centerLocation.getWorld();
        if (world == null) return;

        Location center = centerLocation.getBlock().getLocation();
        Location base = center.clone().add(0, -1, 0);

        // 1. Base 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = base.clone().add(x, 0, z).getBlock();
                boolean isCorner = Math.abs(x) == 1 && Math.abs(z) == 1;
                boolean isCenter = (x == 0 && z == 0);

                if (isCenter) {
                    b.setType(Material.SOUL_CAMPFIRE);
                } else if (isCorner) {
                    b.setType(Material.CRYING_OBSIDIAN);
                } else {
                    b.setType(Material.POLISHED_BLACKSTONE_BRICKS);
                }
            }
        }

        // 2. Nivel Superior: Yunque central y 4 Linternas de Almas en esquinas
        center.getBlock().setType(Material.ANVIL);

        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                Block b = center.clone().add(x, 0, z).getBlock();
                b.setType(Material.SOUL_LANTERN);
            }
        }

        // Efectos de invocación
        world.playSound(center, Sound.BLOCK_ANVIL_PLACE, 1.5f, 0.8f);
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.2f, 1.2f);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0.5, 1.0, 0.5), 50, 0.8, 0.8, 0.8, 0.08);
        world.spawnParticle(Particle.FLASH, center.clone().add(0.5, 1.0, 0.5), 2);
    }

    /**
     * Crea el ítem especial: Núcleo de la Forja de Almas.
     */
    public static ItemStack createSoulForgeCore() {
        ItemStack item = GUIBuilder.createGlowingItem(
                Material.LODESTONE,
                ChatColor.AQUA + "" + ChatColor.BOLD + "✦ Núcleo de la Forja Rítmica ✦",
                "",
                ChatColor.GRAY + "Un catalizador arcano que canaliza el fuego",
                ChatColor.GRAY + "de almas para restaurar y templar herramientas.",
                "",
                ChatColor.YELLOW + "✦ Click Derecho en el suelo:" + ChatColor.WHITE + " Construye la Forja Rítmica.",
                ChatColor.LIGHT_PURPLE + "✦ Estación:" + ChatColor.GRAY + " Forja de Jefes & Ensamblaje"
        );
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getKeySoulForgeCore(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isSoulForgeCore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(getKeySoulForgeCore(), PersistentDataType.BYTE);
    }

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
                        gui.setItem(slot, GUIBuilder.createGlowingItem(Material.ANVIL, ChatColor.YELLOW + "" + ChatColor.BOLD + "🔨 ¡MARTILLO AQUÍ!"));
                    } else if (slot == 13) {
                        gui.setItem(slot, GUIBuilder.createGlowingItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "★ ZONA PERFECTA (100% Reparación + Obra Maestra) ★"));
                    } else if (slot == 12 || slot == 14) {
                        gui.setItem(slot, GUIBuilder.createItem(Material.GOLD_BLOCK, ChatColor.GOLD + "★ Zona Buena (50% Reparación) ★"));
                    } else if (slot == 11 || slot == 15) {
                        gui.setItem(slot, GUIBuilder.createItem(Material.COPPER_BLOCK, ChatColor.YELLOW + "Zona Estándar (25% Reparación)"));
                    } else {
                        gui.setItem(slot, GUIBuilder.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Zona Exterior (10% Reparación)"));
                    }
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.4f, 1.0f + (session.getNeedlePos() - 10) * 0.12f);

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
        ItemStack item = tool.getItem();
        Location loc = player.getLocation();

        player.closeInventory();

        // 1. Manejo de Durabilidad y Reparación en Damageable
        ItemMeta meta = item.getItemMeta();
        int maxDurability = item.getType().getMaxDurability();

        if (hitSlot == 13) {
            // ★ GOLPE PERFECTO: 100% Reparación + Calidad Obra Maestra + 250 XP
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(0); // 100% REPARADO
            }
            long oneHour = System.currentTimeMillis() + (60 * 60 * 1000L);
            meta.getPersistentDataContainer().set(getKeyMasterworkExpiry(), PersistentDataType.LONG, oneHour);
            item.setItemMeta(meta);

            // Quitar estado de roto
            tool.setBroken(false);
            if (LivingArmor.isLivingArmor(item)) {
                new LivingArmor(item).setBroken(false);
            }

            tool.addXP(player, 250L);
            tool.getData().adjustAffinity(5);
            tool.updateLore();

            player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.2f);
            player.playSound(loc, Sound.BLOCK_ANVIL_USE, 1.3f, 1.4f);
            player.playSound(loc, Sound.ITEM_TOTEM_USE, 0.8f, 1.6f);
            player.spawnParticle(Particle.TOTEM, loc.clone().add(0, 1.2, 0), 40, 0.5, 0.8, 0.5, 0.2);
            player.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 1.0, 0), 30, 0.5, 0.5, 0.5, 0.1);
            DamageIndicatorManager.spawnIndicator(loc.clone().add(0, 1.8, 0), ChatColor.GOLD + "🌟 ¡100% REPARADO & OBRA MAESTRA!");

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
            player.sendMessage(ChatColor.YELLOW + "  🔨 ¡GOLPE DE FORJA PERFECTO! (TEMPLE MAESTRO)");
            player.sendMessage(ChatColor.GREEN + "  ✔ Tu herramienta ha sido reparada al " + ChatColor.AQUA + "100% de durabilidad.");
            player.sendMessage(ChatColor.GOLD + "  ✔ Calidad " + ChatColor.BOLD + "OBRA MAESTRA" + ChatColor.GOLD + " activa (+15% Daño y Minería por 1h).");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "  ✔ +250 XP de Alma y +5 Afinidad ganados.");
            player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
            player.sendMessage("");

        } else if (hitSlot == 12 || hitSlot == 14) {
            // ★ GOLPE BUENO: 50% Reparación + 150 XP
            if (meta instanceof Damageable && maxDurability > 0) {
                Damageable d = (Damageable) meta;
                int repair = maxDurability / 2;
                d.setDamage(Math.max(0, d.getDamage() - repair));
                item.setItemMeta((ItemMeta) d);
            }
            tool.setBroken(false);
            tool.addXP(player, 150L);
            tool.getData().adjustAffinity(2);
            tool.updateLore();

            player.playSound(loc, Sound.BLOCK_ANVIL_USE, 1.1f, 1.2f);
            player.spawnParticle(Particle.CRIT_MAGIC, loc.clone().add(0, 1.2, 0), 25, 0.4, 0.6, 0.4, 0.1);
            DamageIndicatorManager.spawnIndicator(loc.clone().add(0, 1.5, 0), ChatColor.GREEN + "🔨 ¡50% REPARADO & +150 XP!");
            player.sendMessage(ChatColor.GREEN + "🔨 ¡Buen golpe de forja! (+50% Durabilidad reparada, +150 XP de Alma).");

        } else if (hitSlot == 11 || hitSlot == 15) {
            // ★ GOLPE ESTÁNDAR: 25% Reparación + 75 XP
            if (meta instanceof Damageable && maxDurability > 0) {
                Damageable d = (Damageable) meta;
                int repair = maxDurability / 4;
                d.setDamage(Math.max(0, d.getDamage() - repair));
                item.setItemMeta((ItemMeta) d);
            }
            tool.addXP(player, 75L);
            tool.updateLore();

            player.playSound(loc, Sound.BLOCK_ANVIL_HIT, 1.0f, 1.0f);
            DamageIndicatorManager.spawnIndicator(loc.clone().add(0, 1.5, 0), ChatColor.YELLOW + "🔨 ¡25% Reparado (+75 XP)!");
            player.sendMessage(ChatColor.YELLOW + "🔨 Calibración de forja estándar (+25% Durabilidad, +75 XP).");

        } else {
            // ★ GOLPE EXTERIOR: 10% Reparación + 25 XP
            if (meta instanceof Damageable && maxDurability > 0) {
                Damageable d = (Damageable) meta;
                int repair = maxDurability / 10;
                d.setDamage(Math.max(0, d.getDamage() - repair));
                item.setItemMeta((ItemMeta) d);
            }
            tool.addXP(player, 25L);
            tool.updateLore();

            player.playSound(loc, Sound.BLOCK_ANVIL_HIT, 0.8f, 0.7f);
            player.sendMessage(ChatColor.GRAY + "🔨 Golpe desalineado (+10% Durabilidad, +25 XP). ¡Apunta al centro!");
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
