package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TerritoryManager implements Listener {

    public static final NamespacedKey KEY_CHUNK_FACTION = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "chunk_faction");

    public static void claimChunk(Player player) {
        FactionManager.Faction faction = FactionManager.getFaction(player);
        if (faction == FactionManager.Faction.NONE) {
            player.sendMessage(MessageUtils.color("&cDebes unirte a una facción para reclamar territorio."));
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        String currentOwner = chunk.getPersistentDataContainer().get(KEY_CHUNK_FACTION, PersistentDataType.STRING);

        if (currentOwner != null && currentOwner.equals(faction.name())) {
            player.sendMessage(MessageUtils.color("&cEste territorio ya pertenece a tu facción."));
            return;
        }

        // Claim logic (simplified: instant claim for now)
        chunk.getPersistentDataContainer().set(KEY_CHUNK_FACTION, PersistentDataType.STRING, faction.name());
        player.sendMessage(MessageUtils.color("&a¡Territorio reclamado para " + faction.name() + "!"));

        // Visuals
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, player.getLocation(), 50);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk()))
            return;

        Player player = event.getPlayer();
        Chunk chunk = event.getTo().getChunk();
        String ownerName = chunk.getPersistentDataContainer().get(KEY_CHUNK_FACTION, PersistentDataType.STRING);

        if (ownerName != null) {
            try {
                FactionManager.Faction owner = FactionManager.Faction.valueOf(ownerName);
                FactionManager.Faction playerFaction = FactionManager.getFaction(player);

                if (playerFaction == owner) {
                    player.sendTitle("", MessageUtils.color("&aTerritorio Aliado"), 10, 40, 10);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0));
                } else if (playerFaction != FactionManager.Faction.NONE) {
                    player.sendTitle("", MessageUtils.color("&cTerritorio Enemigo"), 10, 40, 10);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
