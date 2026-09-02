package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager implements Listener {

    private static final Map<UUID, UUID> requests = new HashMap<>();
    private static final Map<UUID, Long> wagers = new HashMap<>();
    private static final Map<UUID, UUID> activeDuels = new HashMap<>(); // Player -> Opponent
    private static final Map<UUID, Location> preDuelLocations = new HashMap<>();

    public static void sendRequest(Player sender, Player target, long wager) {
        if (activeDuels.containsKey(sender.getUniqueId()) || activeDuels.containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Uno de los jugadores ya está en duelo.");
            return;
        }

        ItemStack item = sender.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            sender.sendMessage(ChatColor.RED + "Debes sostener tu Herramienta Viviente.");
            return;
        }
        LivingTool tool = new LivingTool(item);
        if (tool.getData().getXP() < wager) {
            sender.sendMessage(ChatColor.RED + "No tienes suficiente XP para apostar.");
            return;
        }

        requests.put(target.getUniqueId(), sender.getUniqueId());
        wagers.put(target.getUniqueId(), wager);

        sender.sendMessage(ChatColor.YELLOW + "Has desafiado a " + target.getName() + " por " + wager + " XP.");
        target.sendMessage(ChatColor.YELLOW + sender.getName() + " te ha desafiado a un duelo por " + wager + " XP.");
        target.sendMessage(ChatColor.GOLD + "Escribe /livingtool duel accept para aceptar.");

        // Expire request after 60 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (requests.containsKey(target.getUniqueId())
                        && requests.get(target.getUniqueId()).equals(sender.getUniqueId())) {
                    requests.remove(target.getUniqueId());
                    wagers.remove(target.getUniqueId());
                    if (sender.isOnline())
                        sender.sendMessage(ChatColor.RED + "El desafío a " + target.getName() + " ha expirado.");
                    if (target.isOnline())
                        target.sendMessage(ChatColor.RED + "El desafío de " + sender.getName() + " ha expirado.");
                }
            }
        }.runTaskLater(LivingToolsPlugin.getInstance(), 1200L);
    }

    public static void acceptRequest(Player accepter) {
        if (!requests.containsKey(accepter.getUniqueId())) {
            accepter.sendMessage(ChatColor.RED + "No tienes desafíos pendientes.");
            return;
        }

        UUID challengerUUID = requests.remove(accepter.getUniqueId());
        long wager = wagers.remove(accepter.getUniqueId());
        Player challenger = Bukkit.getPlayer(challengerUUID);

        if (challenger == null || !challenger.isOnline()) {
            accepter.sendMessage(ChatColor.RED + "El desafiante ya no está en línea.");
            return;
        }

        ItemStack item = accepter.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            accepter.sendMessage(ChatColor.RED + "Debes sostener tu Herramienta Viviente para aceptar.");
            return;
        }
        LivingTool tool = new LivingTool(item);
        if (tool.getData().getXP() < wager) {
            accepter.sendMessage(ChatColor.RED + "No tienes suficiente XP para cubrir la apuesta.");
            return;
        }

        startDuel(challenger, accepter, wager);
    }

    private static void startDuel(Player p1, Player p2, long wager) {
        activeDuels.put(p1.getUniqueId(), p2.getUniqueId());
        activeDuels.put(p2.getUniqueId(), p1.getUniqueId());

        // Store wager on p1's entry (shared for the pair)
        wagers.put(p1.getUniqueId(), wager);

        preDuelLocations.put(p1.getUniqueId(), p1.getLocation());
        preDuelLocations.put(p2.getUniqueId(), p2.getLocation());

        // Teleport to Arena (For now, just a simple offset or keep them there but lock
        // interaction)
        // Ideally, we'd have a set arena location. For simplicity, we'll fight where
        // they stand but announce it.

        p1.sendMessage(ChatColor.RED + "¡DUELO INICIADO! Apuesta: " + wager + " XP");
        p2.sendMessage(ChatColor.RED + "¡DUELO INICIADO! Apuesta: " + wager + " XP");

        p1.playSound(p1.getLocation(), Sound.EVENT_RAID_HORN, 1, 1);
        p2.playSound(p2.getLocation(), Sound.EVENT_RAID_HORN, 1, 1);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        if (activeDuels.containsKey(loser.getUniqueId())) {
            UUID winnerUUID = activeDuels.get(loser.getUniqueId());
            Player winner = Bukkit.getPlayer(winnerUUID);

            if (winner != null) {
                endDuel(winner, loser);
            }

            // Prevent drops if we want, but for now let's keep it vanilla + XP transfer
        }
    }

    private void endDuel(Player winner, Player loser) {
        activeDuels.remove(winner.getUniqueId());
        activeDuels.remove(loser.getUniqueId());

        // Retrieve wager (stored on one of the keys, check both)
        Long wager = wagers.remove(winner.getUniqueId());
        if (wager == null)
            wager = wagers.remove(loser.getUniqueId());
        if (wager == null)
            wager = 0L;

        // Transfer XP
        ItemStack winnerItem = winner.getInventory().getItemInMainHand();
        ItemStack loserItem = loser.getInventory().getItemInMainHand(); // Might be dropped, need to handle this
                                                                        // carefully.

        // If loser died, item might be in drops. But Living Tools have Soulbound (Phase
        // 7).
        // So it should be in inventory (or restored soon).
        // For simplicity, we assume it's in inventory or we deduct from PDC directly if
        // we could find it.
        // Since Soulbound restores it, we can try to deduct from the item in inventory
        // (if kept) or wait.

        if (LivingTool.isLivingTool(winnerItem)) {
            LivingTool winTool = new LivingTool(winnerItem);
            winTool.addXP(winner, wager);
            winner.sendMessage(ChatColor.GOLD + "¡Has ganado el duelo! +" + wager + " XP");
        }

        // Deduct from loser (if possible immediately, otherwise they just lost the
        // wagered opportunity)
        // Ideally we deduct at start, but that's complex with cancellations.
        // Let's try to deduct now.
        if (LivingTool.isLivingTool(loserItem)) {
            LivingTool loseTool = new LivingTool(loserItem);
            long current = loseTool.getData().getXP();
            loseTool.getData().setXP(Math.max(0, current - wager));
            loseTool.updateLore();
            loser.sendMessage(ChatColor.RED + "Has perdido el duelo. -" + wager + " XP");
        }

        // Teleport back
        if (preDuelLocations.containsKey(winner.getUniqueId()))
            winner.teleport(preDuelLocations.remove(winner.getUniqueId()));
        if (preDuelLocations.containsKey(loser.getUniqueId()))
            loser.teleport(preDuelLocations.remove(loser.getUniqueId()));

        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
    }

    // Prevent interference?
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            boolean victimInDuel = activeDuels.containsKey(victim.getUniqueId());
            boolean attackerInDuel = activeDuels.containsKey(attacker.getUniqueId());

            if (victimInDuel && attackerInDuel) {
                if (!activeDuels.get(victim.getUniqueId()).equals(attacker.getUniqueId())) {
                    event.setCancelled(true); // Fighting wrong person
                    attacker.sendMessage(ChatColor.RED + "¡No es tu oponente!");
                }
            } else if (victimInDuel || attackerInDuel) {
                event.setCancelled(true); // One is in duel, one is not
            }
        }
    }
}
