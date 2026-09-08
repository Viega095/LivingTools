package com.livingtools.listeners;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.abilities.active.RecallAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityListener implements Listener {

    // Track players with active Recall auto-save
    private static final Map<UUID, Integer> recallTasks = new HashMap<>();

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);

            // Active Abilities (Right Click)
            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                // Elemental Abilities (Shift + Right Click) -> CHANGED: Now opens Dashboard
                if (player.isSneaking()) {
                    // Open Dashboard
                    com.livingtools.gui.DashboardGUI.open(player, tool);

                    /*
                     * Elemental Ability Activation moved to specific keybind or another interaction
                     * if needed?
                     * For now, user requested Shift+Click for Menu.
                     * If we want to keep Elemental Abilities, we might need another trigger.
                     * However, the user explicitly asked:
                     * "cuando tengas una herramienta viva que con shift y click se pueda abrir el menu"
                     * .
                     * So I will prioritize the menu.
                     */
                } else {
                    // Standard Active Abilities (Right Click)
                    for (String abilityId : tool.getAbilities()) {
                        Ability ability = AbilityRegistry.getAbility(abilityId);
                        if (ability instanceof com.livingtools.abilities.ActiveAbility) {
                            ((com.livingtools.abilities.ActiveAbility) ability).onRightClick(player, tool);

                            // Corruption Check (Phase 37)
                            if (tool.getData().getCorruption() > 50 && Math.random() < 0.05) { // 5% chance
                                com.livingtools.manager.CorruptionManager
                                        .spawnShadowMob(player.getLocation(), player);
                                player.sendMessage(
                                        org.bukkit.ChatColor.DARK_RED + "¡La corrupción manifiesta una sombra!");
                            }
                        }
                        // Legacy Interact Hook
                        if (ability != null) {
                            ability.onInteract(event, tool);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            for (String abilityId : tool.getAbilities()) {
                Ability ability = AbilityRegistry.getAbility(abilityId);
                if (ability != null) {
                    ability.onTrigger(player, event); // Keep for backward compatibility
                    ability.onBlockBreak(event, tool);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // 1. Player Attacking (Offense)
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);

                // Rune Bonus (Ignis)
                double damageMultiplier = 1.0;
                for (com.livingtools.runes.RuneType rune : tool.getData().getRunes()) {
                    if (rune == com.livingtools.runes.RuneType.IGNIS) {
                        damageMultiplier += 0.10; // +10% Damage
                    }
                }
                if (damageMultiplier > 1.0) {
                    event.setDamage(event.getDamage() * damageMultiplier);
                }

                // Phase 38: Miner's Rage (Consume)
                if (com.livingtools.manager.SynergyManager.consumeMinerRage(player)) {
                    event.setDamage(event.getDamage() * 1.5); // +50% Damage
                    player.sendMessage(org.bukkit.ChatColor.RED + "¡GOLPE DE FURIA MINERA! (+50% Daño)");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1, 2.0f);
                    player.spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, event.getEntity().getLocation(), 1);
                }

                // Corruption Check (Phase 37)
                if (tool.getData().getCorruption() > 75 && Math.random() < 0.03) { // 3% chance on hit
                    com.livingtools.manager.CorruptionManager.spawnShadowMob(player.getLocation(), player);
                    player.sendMessage(org.bukkit.ChatColor.DARK_RED + "¡Tu furia corrupta atrae sombras!");
                }

                for (String abilityId : tool.getAbilities()) {
                    Ability ability = AbilityRegistry.getAbility(abilityId);
                    if (ability != null) {
                        ability.onTrigger(player, event); // Keep for backward compatibility
                        ability.onEntityDamage(event, tool);
                    }
                }
            }
        }

        // 2. Player Defending (Defense - Thorns)
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (com.livingtools.data.LivingArmor.isLivingArmor(armor)) {
                    com.livingtools.data.LivingArmor livingArmor = new com.livingtools.data.LivingArmor(
                            armor);
                    if (livingArmor.hasAbility("thorns")) {
                        Ability ability = AbilityRegistry.getAbility("thorns");
                        if (ability != null) {
                            ability.onDamageTakenByEntity(event, player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamageTaken(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (com.livingtools.data.LivingArmor.isLivingArmor(armor)) {
                    com.livingtools.data.LivingArmor livingArmor = new com.livingtools.data.LivingArmor(
                            armor);
                    if (livingArmor.hasAbility("adrenaline")) {
                        Ability ability = AbilityRegistry.getAbility("adrenaline");
                        if (ability != null) {
                            ability.onDamageTaken(event, player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);

            // Check for Recall ability and start auto-save task
            if (tool.hasAbility("recall") && !recallTasks.containsKey(player.getUniqueId())) {
                int taskId = RecallAbility.startAutoSave(player);
                recallTasks.put(player.getUniqueId(), taskId);
            }

            for (String abilityId : tool.getAbilities()) {
                Ability ability = AbilityRegistry.getAbility(abilityId);
                if (ability != null) {
                    ability.onHold(player);
                }
            }
        } else {
            // Stop Recall auto-save if no longer holding tool with recall
            if (recallTasks.containsKey(player.getUniqueId())) {
                int taskId = recallTasks.remove(player.getUniqueId());
                org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }

    @EventHandler
    public void onFlightToggle(org.bukkit.event.player.PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Check Armor for Double Jump
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (com.livingtools.data.LivingArmor.isLivingArmor(armor)) {
                com.livingtools.data.LivingArmor livingArmor = new com.livingtools.data.LivingArmor(
                        armor);
                if (livingArmor.hasAbility("double_jump")) {
                    Ability ability = AbilityRegistry.getAbility("double_jump");
                    if (ability instanceof com.livingtools.abilities.armor.DoubleJumpAbility) {
                        ((com.livingtools.abilities.armor.DoubleJumpAbility) ability).onFlightToggle(event);
                    }
                }
            }
        }
    }
}
