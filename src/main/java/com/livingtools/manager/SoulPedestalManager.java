package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.*;

/**
 * SoulPedestalManager — Vitrinas y Pedestales de Almas con Animación 3D y Generación de Rested XP.
 */
public class SoulPedestalManager {

    public static class ActivePedestal {
        private final UUID ownerId;
        private final Location location;
        private final ItemStack toolItem;
        private final ArmorStand displayStand;
        private final ArmorStand hologramStand1;
        private final ArmorStand hologramStand2;
        private long restedXP = 0;
        private final long placedTime;

        public ActivePedestal(UUID ownerId, Location location, ItemStack toolItem,
                              ArmorStand displayStand, ArmorStand hologramStand1, ArmorStand hologramStand2) {
            this.ownerId = ownerId;
            this.location = location;
            this.toolItem = toolItem;
            this.displayStand = displayStand;
            this.hologramStand1 = hologramStand1;
            this.hologramStand2 = hologramStand2;
            this.placedTime = System.currentTimeMillis();
        }

        public UUID getOwnerId() { return ownerId; }
        public Location getLocation() { return location; }
        public ItemStack getToolItem() { return toolItem; }
        public ArmorStand getDisplayStand() { return displayStand; }
        public ArmorStand getHologramStand1() { return hologramStand1; }
        public ArmorStand getHologramStand2() { return hologramStand2; }
        public long getRestedXP() { return restedXP; }
        public void addRestedXP(long amount) { this.restedXP = Math.min(5000L, this.restedXP + amount); }
    }

    private static final Map<Location, ActivePedestal> activePedestals = new HashMap<>();
    private static final Map<UUID, Location> playerPedestals = new HashMap<>();
    private static BukkitTask rotationTask = null;

    public static void startLoop() {
        if (rotationTask != null) rotationTask.cancel();

        rotationTask = new BukkitRunnable() {
            float yaw = 0f;
            int tickCount = 0;

            @Override
            public void run() {
                yaw = (yaw + 3.0f) % 360f;
                tickCount++;

                for (ActivePedestal ped : activePedestals.values()) {
                    ArmorStand display = ped.getDisplayStand();
                    if (display != null && display.isValid()) {
                        // Rotación suave continua
                        display.setRotation(yaw, 0f);

                        // Partículas orbitales suaves según la personalidad
                        if (tickCount % 5 == 0) {
                            LivingTool tool = new LivingTool(ped.getToolItem());
                            String pers = tool.getData().getPersonality();
                            Particle p = Particle.TOTEM;
                            if (pers != null) {
                                if (pers.contains("AGGRESSIVE") || pers.contains("FURY")) p = Particle.FLAME;
                                else if (pers.contains("LAZY") || pers.contains("HARMONY")) p = Particle.SNOWFLAKE;
                                else if (pers.contains("CHEERFUL") || pers.contains("STORM")) p = Particle.ELECTRIC_SPARK;
                                else if (pers.contains("VOID")) p = Particle.PORTAL;
                            }
                            ParticleOptimizer.spawnCircle(display.getLocation().add(0, 0.8, 0), 0.6, p, 6, null);
                        }
                    }

                    // Acumular Rested XP cada 60 segundos (1200 ticks / 20 = 60 ticks en bucle de 1 tick)
                    if (tickCount % 1200 == 0) {
                        ped.addRestedXP(20L);
                        updateHolograms(ped);
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 1L, 1L);
    }

    public static boolean placeToolOnPedestal(Player player, Location blockLoc) {
        if (playerPedestals.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ya tienes una herramienta expuesta en un Pedestal de Almas.");
            return false;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(held)) {
            player.sendMessage(ChatColor.RED + "Debes sostener una Herramienta Viviente para exponerla.");
            return false;
        }

        Location spawnLoc = blockLoc.clone().add(0.5, 0.5, 0.5);
        World world = spawnLoc.getWorld();
        if (world == null) return false;

        // Clonar y remover del inventario
        ItemStack toolCopy = held.clone();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        LivingTool tool = new LivingTool(toolCopy);
        ToolData data = tool.getData();

        // 1. ArmorStand principal de visualización del ítem
        ArmorStand displayStand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        displayStand.setVisible(false);
        displayStand.setGravity(false);
        displayStand.setSmall(true);
        displayStand.setMarker(true);
        displayStand.getEquipment().setItemInMainHand(toolCopy);
        displayStand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(45), 0));

        // 2. Holograma Línea 1 (Nombre & Nivel)
        Location holoLoc1 = spawnLoc.clone().add(0, 1.4, 0);
        ArmorStand holo1 = (ArmorStand) world.spawnEntity(holoLoc1, EntityType.ARMOR_STAND);
        holo1.setVisible(false);
        holo1.setGravity(false);
        holo1.setMarker(true);
        holo1.setCustomNameVisible(true);
        holo1.setCustomName(ChatColor.GOLD + "✦ " + getDisplayName(tool) + ChatColor.YELLOW + " [Lv." + data.getLevel() + "]");

        // 3. Holograma Línea 2 (Dueño & Rested XP)
        Location holoLoc2 = spawnLoc.clone().add(0, 1.15, 0);
        ArmorStand holo2 = (ArmorStand) world.spawnEntity(holoLoc2, EntityType.ARMOR_STAND);
        holo2.setVisible(false);
        holo2.setGravity(false);
        holo2.setMarker(true);
        holo2.setCustomNameVisible(true);
        holo2.setCustomName(ChatColor.GRAY + "Dueño: " + ChatColor.WHITE + player.getName() + ChatColor.AQUA + " (0 XP de Descanso)");

        ActivePedestal pedestal = new ActivePedestal(player.getUniqueId(), blockLoc, toolCopy, displayStand, holo1, holo2);
        activePedestals.put(blockLoc, pedestal);
        playerPedestals.put(player.getUniqueId(), blockLoc);

        // Efectos de colocación
        world.spawnParticle(Particle.TOTEM, spawnLoc.clone().add(0, 1, 0), 40, 0.4, 0.5, 0.4, 0.1);
        world.playSound(spawnLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.2f, 1.5f);

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "🏛 ¡Herramienta expuesta en el Pedestal de Almas!");
        player.sendMessage(ChatColor.GRAY + "Generará " + ChatColor.YELLOW + "XP de Descanso" + ChatColor.GRAY + " mientras repose aquí.");
        player.sendMessage("");

        return true;
    }

    public static boolean retrieveToolFromPedestal(Player player, Location blockLoc) {
        ActivePedestal ped = activePedestals.get(blockLoc);
        if (ped == null) return false;

        if (!player.getUniqueId().equals(ped.getOwnerId()) && !player.hasPermission("livingtools.admin")) {
            player.sendMessage(ChatColor.RED + "Este pedestal pertenece a " + Bukkit.getOfflinePlayer(ped.getOwnerId()).getName());
            return false;
        }

        activePedestals.remove(blockLoc);
        playerPedestals.remove(ped.getOwnerId());

        // Limpiar entidades visuales
        if (ped.getDisplayStand() != null && ped.getDisplayStand().isValid()) ped.getDisplayStand().remove();
        if (ped.getHologramStand1() != null && ped.getHologramStand1().isValid()) ped.getHologramStand1().remove();
        if (ped.getHologramStand2() != null && ped.getHologramStand2().isValid()) ped.getHologramStand2().remove();

        ItemStack item = ped.getToolItem();
        LivingTool tool = new LivingTool(item);

        // Otorgar Rested XP acumulada
        if (ped.getRestedXP() > 0) {
            tool.addXP(player, ped.getRestedXP());
            player.sendMessage(ChatColor.GOLD + "⚡ ¡Has absorbido +" + ped.getRestedXP() + " XP de Descanso acumulada!");
        }

        // Devolver ítem al inventario
        Map<Integer, ItemStack> left = player.getInventory().addItem(item);
        for (ItemStack l : left.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), l);
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 1.4f);
        player.sendMessage(ChatColor.GREEN + "✔ Herramienta retirada del Pedestal.");
        return true;
    }

    private static void updateHolograms(ActivePedestal ped) {
        if (ped.getHologramStand2() != null && ped.getHologramStand2().isValid()) {
            String ownerName = Bukkit.getOfflinePlayer(ped.getOwnerId()).getName();
            ped.getHologramStand2().setCustomName(ChatColor.GRAY + "Dueño: " + ChatColor.WHITE + ownerName
                    + ChatColor.GOLD + " (+" + ped.getRestedXP() + " Rested XP)");
        }
    }

    private static String getDisplayName(LivingTool tool) {
        ItemStack item = tool.getItem();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }

    public static boolean isPedestal(Location loc) {
        return activePedestals.containsKey(loc);
    }

    public static void cleanup(UUID uuid) {
        Location loc = playerPedestals.remove(uuid);
        if (loc != null) {
            ActivePedestal ped = activePedestals.remove(loc);
            if (ped != null) {
                if (ped.getDisplayStand() != null) ped.getDisplayStand().remove();
                if (ped.getHologramStand1() != null) ped.getHologramStand1().remove();
                if (ped.getHologramStand2() != null) ped.getHologramStand2().remove();
            }
        }
    }
}
