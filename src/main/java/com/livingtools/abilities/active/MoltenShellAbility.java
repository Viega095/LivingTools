package com.livingtools.abilities.active;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class MoltenShellAbility extends Ability {

    private final HashMap<UUID, Long> activeShells = new HashMap<>();

    public MoltenShellAbility() {
        super("moltenshell", "Caparazón de Magma",
                "Click derecho para obtener resistencia al fuego y quemar atacantes.", 30);
    }

    @Override
    public void onInteract(PlayerInteractEvent event, LivingTool tool) {
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();

            if (isOnCooldown(player))
                return;

            int level = getLevel(tool);
            int duration = 200 + (level * 40); // Lvl 1: 12s, Lvl 5: 20s

            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
            player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY_LAVA, 1, 1);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 20, 0.5, 1, 0.5, 0.1);

            activeShells.put(player.getUniqueId(), System.currentTimeMillis() + (duration * 50)); // Store end time

            player.sendMessage(ChatColor.GOLD + "¡Caparazón de Magma activado!");
            addCooldown(player, 40000); // 40s cooldown

            addXP(player, tool, 20);
        }
    }

    @Override
    public void onDamageTakenByEntity(EntityDamageByEntityEvent event, Player victim) {
        if (activeShells.containsKey(victim.getUniqueId())) {
            if (System.currentTimeMillis() > activeShells.get(victim.getUniqueId())) {
                activeShells.remove(victim.getUniqueId());
                return;
            }

            // Shell is active
            if (event.getDamager() != null) {
                // Burn attacker
                event.getDamager().setFireTicks(60); // 3 seconds
                victim.getWorld().spawnParticle(Particle.LAVA, event.getDamager().getLocation(), 5);
            }
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_PICKAXE");
    }
}
