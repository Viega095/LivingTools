package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.manager.MilestoneManager;
import com.livingtools.manager.SleepBonusManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.ArrayList;

/**
 * ToolInspectManager — muestra un resumen detallado de la herramienta
 * en el chat con formato rico (pseudo-tooltip).
 *
 * Uso: /lt inspect [jugador]
 * Sin argumento = inspecciona la herramienta propia.
 *
 * Muestra:
 *  - Nombre, tipo, nivel/prestige
 *  - Personalidad, humor, hambre
 *  - Título de reputación
 *  - Estadísticas (mobs/bloques/kills)
 *  - Hitos completados
 *  - Habilidades equipadas
 *  - Bonus activos (sueño, clima, evento de mundo)
 *  - Corrupción (si > 0)
 */
public class ToolInspectManager {

    public static void inspect(Player viewer, Player target) {
        ItemStack item = target.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            viewer.sendMessage(ChatColor.RED + target.getName()
                    + " no tiene una Herramienta Viviente en la mano.");
            return;
        }

        LivingTool tool = new LivingTool(item);
        ToolData data = tool.getData();

        String toolName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                ? item.getItemMeta().getDisplayName()
                : ChatColor.GOLD + item.getType().name().replace("_", " ").toLowerCase();

        String personality = data.getPersonality() != null ? data.getPersonality() : "???";
        String personalityColor = colorForPersonality(personality);

        // Construir el inspector
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(ChatColor.GOLD + "╔═══════════════════════════════╗");
        lines.add(ChatColor.GOLD + "║ " + toolName);
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + item.getType().name().replace("_", " ").toLowerCase()
                + " de " + ChatColor.WHITE + target.getName());
        lines.add(ChatColor.GOLD + "╠═══════════════════════════════╣");

        // Nivel y prestige
        String levelStr = ChatColor.GREEN + "Lv." + data.getLevel();
        if (data.getPrestige() > 0) levelStr += ChatColor.GOLD + " [Prestige " + data.getPrestige() + "]";
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "Nivel: " + levelStr);

        // XP
        long xpForNext = com.livingtools.data.LivingTool.getRequiredXP(data.getLevel() + 1);
        long currentXP = data.getXP();
        int percent = xpForNext > 0 ? (int)((currentXP * 100) / xpForNext) : 100;
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "XP: " + ChatColor.WHITE
                + formatNum(currentXP) + "/" + formatNum(xpForNext)
                + ChatColor.DARK_GRAY + " (" + percent + "%)");

        lines.add(ChatColor.GOLD + "║");

        // Alma
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "Personalidad: " + personalityColor + personality);
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "Humor: " + moodBar(data.getMood()));
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "Hambre: " + hungerBar(data.getFullness()));

        // Título
        String title = data.getTitle();
        if (title != null && !title.isEmpty()) {
            lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "Título: " + ChatColor.AQUA + title);
        }

        // Corrupción
        int corruption = data.getCorruption();
        if (corruption > 0) {
            String corruptColor = corruption > 75 ? "" + ChatColor.DARK_RED
                    : corruption > 40 ? "" + ChatColor.RED : "" + ChatColor.YELLOW;
            lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "Corrupción: "
                    + corruptColor + corruption + "% " + corruptBar(corruption));
        }

        lines.add(ChatColor.GOLD + "║");

        // Estadísticas
        lines.add(ChatColor.GOLD + "║ " + ChatColor.DARK_GRAY + "― ESTADÍSTICAS ―");
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "⚔ Mobs: " + ChatColor.WHITE + formatNum(data.getMobKills())
                + ChatColor.GRAY + "  ☠ PvP: " + ChatColor.WHITE + formatNum(data.getPlayerKills()));
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "⛏ Bloques: " + ChatColor.WHITE + formatNum(data.getBlocksMined())
                + ChatColor.GRAY + "  ♡ Pac: " + ChatColor.WHITE + data.getPeacefulActions());

        // Hitos
        int milestonesCompleted = MilestoneManager.countCompleted(tool);
        int milestonesTotal = MilestoneManager.Milestone.values().length;
        lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "✦ Hitos: " + ChatColor.YELLOW
                + milestonesCompleted + "/" + milestonesTotal);

        // Habilidades
        List<String> abilities = data.getAbilities();
        if (!abilities.isEmpty()) {
            lines.add(ChatColor.GOLD + "║");
            lines.add(ChatColor.GOLD + "║ " + ChatColor.DARK_GRAY + "― HABILIDADES ―");
            for (int i = 0; i < Math.min(4, abilities.size()); i++) {
                lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "• " + ChatColor.WHITE
                        + abilities.get(i).replace("_", " ").toLowerCase());
            }
            if (abilities.size() > 4) {
                lines.add(ChatColor.GOLD + "║ " + ChatColor.GRAY + "  ...y " + (abilities.size() - 4) + " más");
            }
        }

        // Bonus activos
        List<String> bonuses = new ArrayList<>();
        if (SleepBonusManager.hasSleepBonus(target)) {
            long secs = SleepBonusManager.getRemainingSeconds(target);
            bonuses.add(ChatColor.GREEN + "☽ Bonus de Sueño (" + (secs / 60) + "m restantes)");
        }
        String weatherDesc = WeatherBonusManager.getBonusDescription(target, item.getType());
        if (weatherDesc != null) bonuses.add(ChatColor.AQUA + "☁ " + weatherDesc);
        ServerEventManager.WorldEvent worldEvent = ServerEventManager.getActiveEvent(target.getWorld());
        if (worldEvent != ServerEventManager.WorldEvent.NONE) {
            String evtName = worldEvent == ServerEventManager.WorldEvent.CORRUPTED_NIGHT
                    ? ChatColor.DARK_PURPLE + "☠ Noche Corrupta" : ChatColor.YELLOW + "⚡ Tormenta de Runas";
            bonuses.add(evtName);
        }
        if (!bonuses.isEmpty()) {
            lines.add(ChatColor.GOLD + "║");
            lines.add(ChatColor.GOLD + "║ " + ChatColor.DARK_GRAY + "― BONUS ACTIVOS ―");
            for (String b : bonuses) lines.add(ChatColor.GOLD + "║ " + b);
        }

        lines.add(ChatColor.GOLD + "╚═══════════════════════════════╝");
        lines.add("");

        for (String line : lines) viewer.sendMessage(line);
    }

    // -----------------------------------------------------------------------
    // Visual helpers
    // -----------------------------------------------------------------------

    private static String moodBar(int mood) {
        // mood 0-100
        int filled = Math.max(0, Math.min(10, mood / 10));
        String color = mood > 70 ? "" + ChatColor.GREEN : mood > 40 ? "" + ChatColor.YELLOW : "" + ChatColor.RED;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? color + "█" : ChatColor.DARK_GRAY + "░");
        }
        bar.append(ChatColor.WHITE + "]");
        return bar.toString();
    }

    private static String hungerBar(int fullness) {
        // fullness 0-10
        int filled = Math.max(0, Math.min(10, fullness));
        String color = fullness > 6 ? "" + ChatColor.GREEN : fullness > 3 ? "" + ChatColor.YELLOW : "" + ChatColor.RED;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? color + "♥" : ChatColor.DARK_GRAY + "♡");
        }
        bar.append(ChatColor.WHITE + "]");
        return bar.toString();
    }

    private static String corruptBar(int corruption) {
        int filled = Math.max(0, Math.min(10, corruption / 10));
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? ChatColor.DARK_PURPLE + "▓" : ChatColor.DARK_GRAY + "░");
        }
        bar.append(ChatColor.WHITE + "]");
        return bar.toString();
    }

    private static String colorForPersonality(String p) {
        switch (p) {
            case "AGGRESSIVE": return "" + ChatColor.RED;
            case "WISE":       return "" + ChatColor.AQUA;
            case "LAZY":       return "" + ChatColor.BLUE;
            default:           return "" + ChatColor.YELLOW;
        }
    }

    private static String formatNum(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }
}
