package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.mechanics.Personality;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bonificaciones de XP según el clima y la hora del día.
 * 
 * - Lluvia sin tormenta: +15% XP para herramientas agrícolas/picos
 * - Noche (tick 13000-23000): +20% XP para espadas/armas
 * - Tormenta: +25% XP global + reacción especial en personalidad AGGRESSIVE
 */
public class WeatherBonusManager implements Listener {

    private static final Map<UUID, Long> stormReactionCD = new HashMap<>();
    private static final long STORM_REACTION_CD_MS = 5 * 60 * 1000L; // 5 min cooldown

    // -----------------------------------------------------------------------
    // Calcular bonus de clima para aplicar en ExperienceListener
    // -----------------------------------------------------------------------

    /**
     * Devuelve el multiplicador de XP por clima (≥ 1.0).
     * Ejemplo: 1.25 = +25%.
     */
    public static double getWeatherMultiplier(Player player, Material toolMaterial) {
        World world = player.getWorld();
        String toolName = toolMaterial.name();

        // Tormenta — +25% para todo
        if (world.isThundering()) {
            return 1.25;
        }

        // Lluvia (sin tormenta)
        if (world.hasStorm()) {
            // Picos y azadas se benefician de la lluvia (simula tierra húmeda y filones expuestos)
            if (toolName.contains("PICKAXE") || toolName.contains("HOE") || toolName.contains("SHOVEL")) {
                return 1.15;
            }
        }

        // Noche — beneficia a espadas y arcos
        long time = world.getTime();
        boolean isNight = time >= 13000 && time <= 23000;
        if (isNight && (toolName.contains("SWORD") || toolName.contains("AXE"))) {
            return 1.20;
        }

        // Amanecer/atardecer (golden hour) — +5% global
        boolean isGoldenHour = (time >= 23000 && time <= 24000) || (time >= 0 && time <= 1000)
                || (time >= 12000 && time <= 13000);
        if (isGoldenHour) {
            return 1.05;
        }

        return 1.0; // Sin bonus
    }

    /**
     * Descripción legible del bonus activo, para mostrar en action bar (nullable si no hay bonus).
     */
    public static String getBonusDescription(Player player, Material toolMaterial) {
        World world = player.getWorld();
        String toolName = toolMaterial.name();
        long time = world.getTime();

        if (world.isThundering()) return ChatColor.AQUA + "⚡ Tormenta: +25% XP";
        if (world.hasStorm() && (toolName.contains("PICKAXE") || toolName.contains("HOE") || toolName.contains("SHOVEL")))
            return ChatColor.BLUE + "🌧 Lluvia: +15% XP";
        boolean isNight = time >= 13000 && time <= 23000;
        if (isNight && (toolName.contains("SWORD") || toolName.contains("AXE")))
            return ChatColor.DARK_PURPLE + "🌙 Noche: +20% XP";
        return null;
    }

    // -----------------------------------------------------------------------
    // Listener — reacción de herramienta AGGRESSIVE a tormentas
    // -----------------------------------------------------------------------

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!event.toWeatherState()) return; // Solo cuando EMPIEZA a llover/tronar

        // Notificar a jugadores con herramientas AGGRESSIVE durante tormenta
        org.bukkit.Bukkit.getScheduler().runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
            if (!event.getWorld().isThundering()) return;
            for (Player player : event.getWorld().getPlayers()) {
                org.bukkit.inventory.ItemStack held = player.getInventory().getItemInMainHand();
                if (!LivingTool.isLivingTool(held)) continue;
                LivingTool tool = new LivingTool(held);
                if (tool.getData().getPersonality() == null) continue;
                try {
                    Personality p = Personality.valueOf(tool.getData().getPersonality());
                    if (p == Personality.AGGRESSIVE) {
                        notifyStormReaction(player, tool);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }, 40L);
    }

    private static void notifyStormReaction(Player player, LivingTool tool) {
        UUID uid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (stormReactionCD.containsKey(uid) && now - stormReactionCD.get(uid) < STORM_REACTION_CD_MS) return;
        stormReactionCD.put(uid, now);

        String[] reactions = {
            "¡SÍ! ¡La tormenta me da poder! ¡VAMOS!",
            "¡El cielo ruge igual que yo! ¡ATACAMOS!",
            "¡Los rayos son mis aliados! ¡Nadie nos detiene!",
            "¡Siento la electricidad! ¡Estoy VIVO!",
            "¡Este es mi momento! ¡Aprovecha la tormenta!"
        };
        String reaction = reactions[(int)(Math.random() * reactions.length)];
        String toolName = tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName()
                ? tool.getItem().getItemMeta().getDisplayName() : ChatColor.GRAY + "tu herramienta";

        player.sendMessage(toolName + ChatColor.RESET + ": " + ChatColor.RED + ChatColor.ITALIC + reaction);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1.5f);
        MessageUtils.sendActionBar(player, ChatColor.RED + "⚡ +25% XP — ¡Modo Tormenta Activo!");
    }

    public static void cleanup(UUID uuid) {
        stormReactionCD.remove(uuid);
    }
}
