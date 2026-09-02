package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ShrineManager implements Listener {

    private static final Random random = new Random();

    public enum ShrineType {
        COMBAT, MINING, FORTUNE
    }

    private static final long COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 Hours
    private static final java.util.Map<java.util.UUID, Long> cooldowns = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, java.util.Set<java.util.UUID>> activeChallenges = new java.util.HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.LODESTONE)
            return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ChatColor.GRAY + "Este antiguo altar parece reaccionar solo ante herramientas vivas...");
            return;
        }

        LivingTool tool = new LivingTool(item);
        if (tool.getData().getLevel() < 50) {
            player.sendMessage(ChatColor.RED
                    + "Tu herramienta es demasiado débil para despertar el santuario (Requiere Nivel 50).");
            return;
        }

        // Check Cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long lastUse = cooldowns.get(player.getUniqueId());
            long remaining = COOLDOWN_MS - (System.currentTimeMillis() - lastUse);
            if (remaining > 0) {
                long hours = remaining / (60 * 60 * 1000);
                long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
                player.sendMessage(ChatColor.RED + "El santuario descansa. Vuelve en " + hours + "h " + minutes + "m.");
                return;
            }
        }

        if (tool.getData().getXP() < 500) {
            player.sendMessage(ChatColor.RED + "El santuario exige una ofrenda de poder (500 XP).");
            return;
        }

        // Determine Shrine Type based on block below
        Block base = clicked.getRelative(0, -1, 0);
        ShrineType type = ShrineType.FORTUNE; // Default
        if (base.getType() == Material.RED_NETHER_BRICKS) {
            type = ShrineType.COMBAT;
        } else if (base.getType() == Material.GILDED_BLACKSTONE) {
            type = ShrineType.MINING;
        }

        // Perform Offering
        tool.getData().setXP(tool.getData().getXP() - 500);
        tool.updateLore();

        // Set Cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(ChatColor.GOLD + "✨ ¡El Santuario Ancestral (" + type.name() + ") acepta tu ofrenda! ✨");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 0.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM, clicked.getLocation().add(0.5, 1.5, 0.5), 30, 0.2,
                0.2, 0.2, 0.1);

        if (type == ShrineType.COMBAT) {
            startCombatChallenge(player, clicked.getLocation().add(0.5, 1, 0.5));
        } else {
            dropLoot(clicked.getLocation().add(0.5, 1.5, 0.5), type);
        }
    }

    private void startCombatChallenge(Player player, org.bukkit.Location loc) {
        player.sendMessage(ChatColor.RED + "¡El Santuario invoca guardianes para probar tu valía!");

        java.util.Set<java.util.UUID> guardians = new java.util.HashSet<>();

        for (int i = 0; i < 3; i++) {
            org.bukkit.entity.WitherSkeleton skeleton = (org.bukkit.entity.WitherSkeleton) loc.getWorld()
                    .spawnEntity(loc, org.bukkit.entity.EntityType.WITHER_SKELETON);
            skeleton.setCustomName(ChatColor.RED + "Guardián Ancestral");
            skeleton.setCustomNameVisible(true);
            skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
            skeleton.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            skeleton.setHealth(40);

            // Tag entity to identify it later if needed, but UUID map is safer
            guardians.add(skeleton.getUniqueId());
        }

        activeChallenges.put(player.getUniqueId(), guardians);
    }

    private void dropLoot(org.bukkit.Location loc, ShrineType type) {
        int roll = random.nextInt(100);
        ItemStack loot;

        if (type == ShrineType.MINING) {
            if (roll < 10)
                loot = new ItemStack(Material.NETHERITE_INGOT);
            else if (roll < 40)
                loot = new ItemStack(Material.DIAMOND, random.nextInt(3) + 1);
            else
                loot = new ItemStack(Material.RAW_GOLD, random.nextInt(10) + 5);
        } else if (type == ShrineType.COMBAT) {
            if (roll < 10)
                loot = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
            else if (roll < 40)
                loot = new ItemStack(Material.NETHER_STAR); // Rare!
            else
                loot = new ItemStack(Material.EXPERIENCE_BOTTLE, random.nextInt(20) + 10);
        } else { // FORTUNE
            if (roll < 5)
                loot = new ItemStack(Material.TOTEM_OF_UNDYING);
            else if (roll < 20)
                loot = new ItemStack(Material.EMERALD_BLOCK, random.nextInt(2) + 1);
            else
                loot = new ItemStack(Material.GOLDEN_APPLE, random.nextInt(3) + 1);
        }

        loc.getWorld().dropItemNaturally(loc, loot);
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}
