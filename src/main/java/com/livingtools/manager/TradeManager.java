package com.livingtools.manager;

import com.livingtools.gui.TradeGUI;
import com.livingtools.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {

    private static final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private static final Map<UUID, TradeGUI> activeTrades = new HashMap<>();

    public static void sendRequest(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage(MessageUtils.color("&cNo puedes comerciar contigo mismo."));
            return;
        }

        if (activeTrades.containsKey(sender.getUniqueId()) || activeTrades.containsKey(target.getUniqueId())) {
            sender.sendMessage(MessageUtils.color("&cUno de los jugadores ya está en un intercambio."));
            return;
        }

        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        sender.sendMessage(MessageUtils.color("&aSolicitud enviada a " + target.getName()));
        target.sendMessage(MessageUtils.color("&a" + sender.getName() + " quiere comerciar contigo."));
        target.sendMessage(MessageUtils.color("&eUsa /livingtool trade accept para aceptar."));

        // Expire request after 30 seconds (simple implementation)
        // Ideally use a scheduler to remove it.
    }

    public static void acceptRequest(Player player) {
        UUID senderId = pendingRequests.remove(player.getUniqueId());
        if (senderId == null) {
            player.sendMessage(MessageUtils.color("&cNo tienes solicitudes pendientes."));
            return;
        }

        Player sender = org.bukkit.Bukkit.getPlayer(senderId);
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(MessageUtils.color("&cEl jugador ya no está en línea."));
            return;
        }

        startTrade(sender, player);
    }

    public static void startTrade(Player p1, Player p2) {
        TradeGUI trade = new TradeGUI(p1, p2);
        activeTrades.put(p1.getUniqueId(), trade);
        activeTrades.put(p2.getUniqueId(), trade);
        trade.open();
    }

    public static void endTrade(Player p1, Player p2) {
        activeTrades.remove(p1.getUniqueId());
        activeTrades.remove(p2.getUniqueId());
    }

    public static TradeGUI getActiveTrade(Player player) {
        return activeTrades.get(player.getUniqueId());
    }
}
