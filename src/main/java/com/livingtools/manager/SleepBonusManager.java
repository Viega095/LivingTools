package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

/**
 * SleepBonusManager — cuando el jugador duerme, su herramienta se "recupera".
 *
 * Al salir de la cama da:
 *  - +15% XP durante 20 minutos (400 bloques / kills)
 *  - Mensaje de la herramienta comentando sobre el descanso
 *  - Si la herramienta es LAZY, el bonus es +25% pero expira en 5 min
 *  - Si es AGGRESSIVE, comenta que quiere pelear ya
 *
 * El bonus se guarda en memoria (no en PDC) — se pierde al reconectarse.
 */
public class SleepBonusManager implements Listener {

    // UUID → tiempo hasta que expira el bonus (ms)
    private static final Map<UUID, Long> sleepBonusExpiry = new HashMap<>();
    // UUID → multiplicador activo
    private static final Map<UUID, Double> sleepBonusMult = new HashMap<>();

    private static final long NORMAL_DURATION_MS = 20 * 60 * 1000L; // 20 min
    private static final long LAZY_DURATION_MS   =  5 * 60 * 1000L; //  5 min

    @EventHandler
    public void onWakeUp(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;

        LivingTool tool = new LivingTool(item);
        ToolData data = tool.getData();
        String personality = data.getPersonality() != null ? data.getPersonality() : "WISE";

        double mult;
        long duration;
        String[] wakeMessages;

        switch (personality) {
            case "LAZY":
                mult = 1.25;
                duration = LAZY_DURATION_MS;
                wakeMessages = new String[]{
                    "Mmm... 5 minutos más...",
                    "Bueno, despierto... por ahora.",
                    "Ese sueño fue glorioso. Ya quiero volver.",
                    "Bien descansado, aunque no por mucho tiempo."
                };
                break;
            case "AGGRESSIVE":
                mult = 1.15;
                duration = NORMAL_DURATION_MS;
                wakeMessages = new String[]{
                    "¡Por fin! Llevaba horas queriendo destruir cosas.",
                    "¡Listo para la batalla! ¿A quién atacamos primero?",
                    "El descanso fue suficiente. Hora de actuar.",
                    "¡Energía restaurada! Dame un enemigo."
                };
                break;
            case "CHEERFUL":
                mult = 1.20;
                duration = NORMAL_DURATION_MS;
                wakeMessages = new String[]{
                    "¡Buenos días! ¡Hoy será un gran día!",
                    "¡Dormí de maravilla! ¡Vamos a por ello!",
                    "¡El amanecer es precioso! ¡Gracias por dejarme descansar!",
                    "Sueño reparador = energía extra. ¡A trabajar!"
                };
                break;
            default: // WISE
                mult = 1.15;
                duration = NORMAL_DURATION_MS;
                wakeMessages = new String[]{
                    "El descanso es tan importante como el trabajo.",
                    "Mente despejada, listo para el día.",
                    "Un buen sueño refuerza mis capacidades.",
                    "Descansé bien. El rendimiento mejorará hoy."
                };
        }

        UUID uuid = player.getUniqueId();
        sleepBonusExpiry.put(uuid, System.currentTimeMillis() + duration);
        sleepBonusMult.put(uuid, mult);

        // Mensaje de la herramienta
        String msg = wakeMessages[(int)(Math.random() * wakeMessages.length)];
        String toolName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName() : ChatColor.GOLD + "Tu herramienta";

        int durationMin = (int)(duration / 60000);
        player.sendMessage("");
        player.sendMessage(toolName + ChatColor.GRAY + ": \"" + ChatColor.ITALIC + msg + ChatColor.GRAY + "\"");
        player.sendMessage(ChatColor.GREEN + "✦ Bonus de Descanso: " + ChatColor.YELLOW
                + "+" + (int)((mult - 1) * 100) + "% XP "
                + ChatColor.GRAY + "durante " + durationMin + " min");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    /**
     * Devuelve el multiplicador de bonus de sueño activo (1.0 si no hay bonus).
     */
    public static double getSleepMultiplier(Player player) {
        UUID uuid = player.getUniqueId();
        Long expiry = sleepBonusExpiry.get(uuid);
        if (expiry == null || System.currentTimeMillis() > expiry) {
            sleepBonusExpiry.remove(uuid);
            sleepBonusMult.remove(uuid);
            return 1.0;
        }
        return sleepBonusMult.getOrDefault(uuid, 1.0);
    }

    /** True si hay bonus activo */
    public static boolean hasSleepBonus(Player player) {
        return getSleepMultiplier(player) > 1.0;
    }

    /** Tiempo restante en segundos (0 si no hay bonus) */
    public static long getRemainingSeconds(Player player) {
        Long expiry = sleepBonusExpiry.get(player.getUniqueId());
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
}
