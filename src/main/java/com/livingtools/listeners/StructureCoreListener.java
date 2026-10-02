package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.StructureCoreManager;
import com.livingtools.manager.StructureCoreManager.DeployedStructure;
import com.livingtools.manager.StructureCoreManager.StructureType;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * StructureCoreListener — Maneja el despliegue automático, el empaquetado rápido (Shift+Click),
 * el auto-desmantelamiento al romper 1 bloque con reembolso de Núcleo (anti-duplicación)
 * y la protección de estructuras contra explosiones y pistones.
 */
public class StructureCoreListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        ItemStack held = player.getInventory().getItemInMainHand();

        // 1. Detección de Empaquetado Rápido (Shift + Click Derecho en estructura)
        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null) {
            DeployedStructure struct = StructureCoreManager.getStructureAt(clicked.getLocation());
            if (struct != null) {
                // Verificar si tiene mano vacía o herramienta viviente
                boolean isEmptyHand = held.getType() == Material.AIR;
                boolean isLiving = LivingTool.isLivingTool(held);

                if (isEmptyHand || isLiving) {
                    event.setCancelled(true);

                    // Verificar permisos de propiedad o admin
                    if (!player.getUniqueId().equals(struct.getOwnerUUID()) && !player.hasPermission("livingtools.admin")) {
                        player.sendMessage(ChatColor.RED + "🔒 Esta estructura fue desplegada por " + struct.getOwnerName() + ".");
                        return;
                    }

                    // Empaquetar de inmediato
                    StructureCoreManager.dismantleStructure(struct, player, true, true);
                    return;
                }
            }
        }

        // 2. Detección de Despliegue de Núcleo
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        StructureType type = StructureCoreManager.getStructureType(held);
        if (type == null) return;

        event.setCancelled(true);
        if (clicked == null) return;

        Block placeBlock = clicked.getRelative(event.getBlockFace());
        Location targetLoc = placeBlock.getLocation();

        // Comprobación de espacio libre y sin solapamiento
        if (!StructureCoreManager.canDeployAt(targetLoc, type, player)) {
            return;
        }

        // Consumir 1 núcleo si está en modo supervivencia
        if (player.getGameMode() != GameMode.CREATIVE) {
            held.setAmount(held.getAmount() - 1);
        }

        // Desplegar estructura con animación cinemática y registro
        StructureCoreManager.deployStructure(player, targetLoc, type);
    }

    /**
     * Al romper CUALQUIER bloque de una estructura desplegada:
     * - Cancela la caída de bloques vanilla individuales (anti-duplicación de netherite, beacons, etc.)
     * - Desmantela toda la estructura dejándola limpia en AIR
     * - Devuelve el Núcleo completo al jugador
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        DeployedStructure struct = StructureCoreManager.getStructureAt(block.getLocation());
        if (struct == null) return;

        // Cancelar el evento de Minecraft para evitar drop individual
        event.setCancelled(true);

        Player player = event.getPlayer();

        // Comprobación de permisos
        if (!player.getUniqueId().equals(struct.getOwnerUUID()) && !player.hasPermission("livingtools.admin")) {
            player.sendMessage(ChatColor.RED + "🔒 No puedes desmantelar la estructura de " + struct.getOwnerName() + ".");
            return;
        }

        // Desmantelar en reversa y reembolsar Núcleo
        StructureCoreManager.dismantleStructure(struct, player, true, true);
    }

    /**
     * Protección contra explosiones (Creeper, TNT, Wither, End Crystal):
     * Protege los bloques de estructura de soltar ítems vanilla y desmantela dejando el núcleo en el suelo.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosionBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosionBlocks(event.blockList());
    }

    private void handleExplosionBlocks(java.util.List<Block> blockList) {
        if (blockList == null || blockList.isEmpty()) return;

        Set<DeployedStructure> affectedStructures = new HashSet<>();
        Iterator<Block> iterator = blockList.iterator();

        while (iterator.hasNext()) {
            Block b = iterator.next();
            DeployedStructure struct = StructureCoreManager.getStructureAt(b.getLocation());
            if (struct != null) {
                affectedStructures.add(struct);
                iterator.remove(); // Remover para evitar destrucción irregular vanilla
            }
        }

        for (DeployedStructure struct : affectedStructures) {
            StructureCoreManager.dismantleStructure(struct, null, true, true);
        }
    }

    /**
     * Protección contra pistones:
     * Evita que pistones muevan o desalineen bloques pertenecientes a estructuras vivientes.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (StructureCoreManager.isStructureBlock(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            if (StructureCoreManager.isStructureBlock(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
