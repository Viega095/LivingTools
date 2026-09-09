package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * LevelUpRewardManager — al subir de nivel, activa efectos visuales y recompensas.
 *
 * Niveles especiales:
 *   10, 25, 50        → Partículas + mensaje
 *   75, 100           → Partículas grandes + sound + mensaje especial
 *   150, 200          → Fuegos artificiales + broadcast servidor
 *   Cada 50 niveles   → +1 punto de habilidad (en PDC de la herramienta)
 *
 * Se llama desde LivingTool.addXP() cuando el nivel sube.
 * Compatible con Prestige.
 */
public class LevelUpRewardManager {

    // Key PDC para puntos de habilidad pendientes
    private static NamespacedKey KEY_SKILL_POINTS;
    private static NamespacedKey keySkillPoints() {
        if (KEY_SKILL_POINTS == null)
            KEY_SKILL_POINTS = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "skill_points");
        return KEY_SKILL_POINTS;
    }

    // Niveles que dan puntos de habilidad extra
    private static final Set<Integer> SKILL_POINT_LEVELS = new HashSet<>(Arrays.asList(
            25, 50, 75, 100, 125, 150, 175, 200
    ));

    /**
     * Llama esto cuando el nivel de una herramienta sube.
     *
     * @param player  Jugador dueño
     * @param tool    La living tool que subió de nivel
     * @param oldLevel Nivel anterior
     * @param newLevel Nivel nuevo
     */
    public static void onLevelUp(Player player, LivingTool tool, int oldLevel, int newLevel) {
        // Iterar por todos los niveles subidos (pueden ser múltiples si hubo mucho XP)
        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
            processLevelUp(player, tool, lvl);
        }
    }

    private static void processLevelUp(Player player, LivingTool tool, int newLevel) {
        // Siempre: sonido y partícula básica
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.0f + (newLevel / 200f));
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0),
                20, 0.5, 0.5, 0.5, 0.05);

        // Mensaje base
        String toolName = getToolName(tool);
        String lvlColor = newLevel >= 150 ? "" + ChatColor.DARK_PURPLE
                : newLevel >= 100 ? "" + ChatColor.GOLD
                : newLevel >= 50 ? "" + ChatColor.AQUA
                : "" + ChatColor.GREEN;
        player.sendMessage(lvlColor + "▲ " + toolName + ChatColor.WHITE + " subió al nivel " + lvlColor + newLevel + "!");

        // Niveles especiales
        if (newLevel == 10) {
            sendSpecialMessage(player, toolName, ChatColor.GREEN,
                    "¡Empezamos a forjarnos! El viaje apenas comienza.");
            awardSkillPoint(player, tool);
        } else if (newLevel == 25) {
            sendSpecialMessage(player, toolName, ChatColor.YELLOW, "¡Nivel 25! Estamos ganando experiencia.");
            bigParticles(player);
            awardSkillPoint(player, tool);
        } else if (newLevel == 50) {
            sendSpecialMessage(player, toolName, ChatColor.GOLD,
                    "¡Nivel 50! Somos una fuerza a tener en cuenta.");
            bigParticles(player);
            firework(player, FireworkEffect.Type.BALL, Color.YELLOW, Color.ORANGE);
            awardSkillPoint(player, tool);
        } else if (newLevel == 75) {
            sendSpecialMessage(player, toolName, ChatColor.AQUA,
                    "¡Nivel 75! Pocos llegan tan lejos. Somos excepcionales.");
            bigParticles(player);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            awardSkillPoint(player, tool);
        } else if (newLevel == 100) {
            sendSpecialMessage(player, toolName, ChatColor.LIGHT_PURPLE,
                    "¡NIVEL 100! Hemos alcanzado la maestría absoluta. El mundo nos teme.");
            bigParticles(player);
            firework(player, FireworkEffect.Type.STAR, Color.PURPLE, Color.WHITE);
            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "✦ " + ChatColor.WHITE
                    + player.getName() + ChatColor.GOLD + " alcanzó el Nivel 100 con " + toolName
                    + ChatColor.GOLD + "! ✦");
            awardSkillPoint(player, tool);
        } else if (newLevel == 150) {
            sendSpecialMessage(player, toolName, ChatColor.DARK_PURPLE,
                    "¡NIVEL 150! Somos una leyenda viviente. Nuestro nombre perdurará.");
            bigParticles(player);
            firework(player, FireworkEffect.Type.BURST, Color.RED, Color.ORANGE);
            firework(player, FireworkEffect.Type.BALL_LARGE, Color.PURPLE, Color.AQUA);
            Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦✦ " + ChatColor.WHITE
                    + player.getName() + ChatColor.DARK_PURPLE + " ha forjado una leyenda con " + toolName
                    + ChatColor.DARK_PURPLE + "! Nivel 150. ✦✦");
            awardSkillPoint(player, tool);
        } else if (newLevel == 200) {
            sendSpecialMessage(player, toolName, ChatColor.DARK_RED,
                    "¡NIVEL 200! Hemos trascendido toda limitación mortal. Somos uno.");
            bigParticles(player);
            for (int i = 0; i < 3; i++) firework(player, FireworkEffect.Type.STAR, Color.RED, Color.YELLOW);
            Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "☠☠☠ " + ChatColor.WHITE
                    + player.getName() + ChatColor.DARK_RED + " ha ascendido al Nivel 200 con " + toolName
                    + ChatColor.DARK_RED + "! ¡El mayor logro del servidor! ☠☠☠");
            awardSkillPoint(player, tool);
        }

        // Hito cada 50 niveles (no duplicar los ya procesados)
        if (newLevel % 50 == 0 && newLevel > 200) {
            awardSkillPoint(player, tool);
        }
    }

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /** Retorna los puntos de habilidad disponibles para la herramienta */
    public static int getSkillPoints(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return 0;
        return tool.getItem().getItemMeta().getPersistentDataContainer()
                .getOrDefault(keySkillPoints(), PersistentDataType.INTEGER, 0);
    }

    /** Añade un punto de habilidad (máx 20) */
    public static void awardSkillPoint(Player player, LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return;
        ItemStack item = tool.getItem();
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        int current = meta.getPersistentDataContainer().getOrDefault(keySkillPoints(), PersistentDataType.INTEGER, 0);
        if (current >= 20) {
            player.sendMessage(ChatColor.GRAY + "  (Ya tienes el máximo de puntos de habilidad: 20)");
            return;
        }
        meta.getPersistentDataContainer().set(keySkillPoints(), PersistentDataType.INTEGER, current + 1);
        item.setItemMeta(meta);
        player.sendMessage(ChatColor.GREEN + "  +1 Punto de Habilidad disponible!"
                + ChatColor.GRAY + " (" + (current + 1) + "/20)");
    }

    // -----------------------------------------------------------------------
    // Visual helpers
    // -----------------------------------------------------------------------

    private static void sendSpecialMessage(Player p, String toolName, ChatColor color, String msg) {
        p.sendMessage("");
        p.sendMessage(color + "╔══════════════════════════════╗");
        p.sendMessage(color + "║  " + toolName + color + ": \"" + ChatColor.ITALIC + msg + color + "\"");
        p.sendMessage(color + "╚══════════════════════════════╝");
        p.sendMessage("");
    }

    private static void bigParticles(Player p) {
        Location loc = p.getLocation().add(0, 1, 0);
        p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 60, 1, 1, 1, 0.5);
        p.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 30, 0.8, 0.8, 0.8, 0.5);
    }

    private static void firework(Player p, FireworkEffect.Type type, Color primary, Color fade) {
        try {
            Location loc = p.getLocation().add(0, 1, 0);
            org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework)
                    p.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.FIREWORK);
            org.bukkit.inventory.meta.FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder()
                    .with(type)
                    .withColor(primary)
                    .withFade(fade)
                    .withFlicker()
                    .build());
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }

    private static String getToolName(LivingTool tool) {
        ItemStack item = tool.getItem();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return ChatColor.GOLD + "Tu herramienta";
    }
}
