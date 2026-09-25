package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bonus de primer uso diario.
 * La primera vez que usás una herramienta cada día, recibe XP bonus
 * y un mensaje especial. Si llevas días consecutivos, el bonus se multiplica.
 *
 * Streak:  1 día → +50 XP
 *          3 días → +150 XP
 *          7 días → +350 XP
 *         14 días → +700 XP (máximo)
 */
public class DailyBonusManager {

    // Lazy-initialized keys — avoids NPE if class loads before onEnable
    private static org.bukkit.NamespacedKey KEY_LAST_USE;
    private static org.bukkit.NamespacedKey KEY_STREAK;

    private static org.bukkit.NamespacedKey keyLastUse() {
        if (KEY_LAST_USE == null)
            KEY_LAST_USE = new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "daily_last_use");
        return KEY_LAST_USE;
    }
    private static org.bukkit.NamespacedKey keyStreak() {
        if (KEY_STREAK == null)
            KEY_STREAK = new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "daily_streak");
        return KEY_STREAK;
    }

    // En memoria: jugadores que ya recibieron el bonus hoy (evita revisar cada bloque)
    private static final Map<UUID, String> todayBonusTool = new HashMap<>();

    private static final long DAY_MS = 86_400_000L; // 24 horas en ms

    /**
     * Llamar al primer uso del día de la herramienta (en onBlockBreak o onEntityDeath).
     * Devuelve true si se otorgó el bonus.
     */
    public static boolean checkAndGrant(Player player, LivingTool tool) {
        String toolId = getToolId(tool);
        String cacheKey = player.getUniqueId() + ":" + toolId;

        // Ya se comprobó/dio el bonus hoy para esta herramienta (O(1) lookup)
        String cached = todayBonusTool.get(player.getUniqueId());
        if (cacheKey.equals(cached)) return false;

        if (!tool.getItem().hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();

        long now = System.currentTimeMillis();
        long lastUse = pdc.getOrDefault(keyLastUse(), PersistentDataType.LONG, 0L);
        int streak   = pdc.getOrDefault(keyStreak(),   PersistentDataType.INTEGER, 0);

        long daysSinceLast = (now - lastUse) / DAY_MS;

        // No aplica si se usó hace menos de 24h — cachear para no volver a leer PDC en cada bloque
        if (daysSinceLast < 1) {
            todayBonusTool.put(player.getUniqueId(), cacheKey);
            return false;
        }

        // Actualizar streak: si pasó más de 2 días, se rompe
        if (daysSinceLast > 2) {
            streak = 1; // reiniciar
        } else {
            streak = Math.min(streak + 1, 14);
        }

        // Guardar en PDC
        pdc.set(keyLastUse(), PersistentDataType.LONG, now);
        pdc.set(keyStreak(),   PersistentDataType.INTEGER, streak);
        tool.getItem().setItemMeta(meta);

        // Calcular bonus
        long bonus = calculateBonus(streak);

        // Cachear para no repetir
        todayBonusTool.put(player.getUniqueId(), cacheKey);

        // Dar XP
        tool.addXP(player, bonus);

        // Mensaje
        String toolName = meta.hasDisplayName() ? meta.getDisplayName() : "tu herramienta";
        String streakInfo = streak >= 7 ? ChatColor.GOLD + "🔥 ¡" + streak + " días de racha!"
                          : streak >= 3 ? ChatColor.YELLOW + "🔥 " + streak + " días seguidos"
                          : "";

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "☀ " + ChatColor.BOLD + "¡Bonus de Primer Uso!");
        player.sendMessage(ChatColor.GRAY + "  " + toolName + ChatColor.RESET + ChatColor.GRAY + " te saluda de vuelta.");
        player.sendMessage(ChatColor.GREEN + "  +" + bonus + " XP" + (streakInfo.isEmpty() ? "" : "  " + streakInfo));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        // Aniversario / Cumpleaños de la herramienta
        com.livingtools.manager.ToolBirthdayManager.checkBirthday(player, tool);

        // Mensaje de la herramienta si streak alto
        if (streak >= 7) {
            player.sendMessage(toolName + ChatColor.RESET + ": " + ChatColor.ITALIC + ChatColor.GREEN
                    + "¡" + streak + " días juntos! Nunca te rindas...");
        } else if (streak >= 3) {
            String[] lines = {"¡Ya era hora! Te extrañé.", "¡Volviste! Estaba aburrido.", "¡De vuelta al trabajo!"};
            player.sendMessage(toolName + ChatColor.RESET + ": " + ChatColor.ITALIC
                    + lines[(int)(Math.random() * lines.length)]);
        }

        return true;
    }

    private static long calculateBonus(int streak) {
        if (streak >= 14) return 700;
        if (streak >= 7)  return 350;
        if (streak >= 3)  return 150;
        return 50;
    }

    /** ID única para la herramienta basada en su creationDate (si existe) */
    private static String getToolId(LivingTool tool) {
        return String.valueOf(tool.getData().getCreationDate());
    }

    /** Limpiar cache al inicio del día (llamar desde un scheduler diario) */
    public static void clearDailyCache() {
        todayBonusTool.clear();
    }

    /** Scheduler: limpiar cache a medianoche */
    public static void startDailyReset() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                clearDailyCache();
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0L, 72000L); // 72000 ticks = 1 hora
        // Verificar cada hora si cambió el día
    }

    public static void cleanup(UUID uuid) {
        todayBonusTool.remove(uuid);
    }
}
