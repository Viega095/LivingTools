package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Ceremonia de nombramiento.
 * La primera vez que una herramienta viviente se usa, se le da la opción
 * de ponerle un nombre personal (máx 30 caracteres).
 *
 * Si el jugador no responde en 30s, se asigna un nombre por defecto según
 * el tipo de herramienta y personalidad.
 */
public class NamingCeremonyManager implements Listener {

    // Lazy-initialized — avoids NPE before onEnable
    private static NamespacedKey KEY_NAMED;
    private static NamespacedKey keyNamed() {
        if (KEY_NAMED == null)
            KEY_NAMED = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "ceremony_named");
        return KEY_NAMED;
    }

    // UUID del jugador → herramienta esperando nombre
    private static final Map<UUID, LivingTool> waitingForName = new HashMap<>();

    // -----------------------------------------------------------------------
    // Trigger — llamar al primer uso de la herramienta
    // -----------------------------------------------------------------------

    public static void tryStartCeremony(Player player, LivingTool tool) {
        if (isNamed(tool)) return;
        if (waitingForName.containsKey(player.getUniqueId())) return;

        // Mark as named immediately to prevent re-triggering
        setNamed(tool);

        waitingForName.put(player.getUniqueId(), tool);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡Tu herramienta ha despertado!");
        player.sendMessage(ChatColor.GRAY + "Esta herramienta ha cobrado conciencia en tus manos.");
        player.sendMessage(ChatColor.YELLOW + "¿Cómo quieres llamarla? " + ChatColor.GRAY + "(escribe en el chat, máx 30 caracteres)");
        player.sendMessage(ChatColor.GRAY + "Escribe " + ChatColor.RED + "cancel" + ChatColor.GRAY + " para usar el nombre por defecto.");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1.5, 0), 30, 0.5, 0.5, 0.5, 1.0);

        // Timeout en 30s — asignar nombre por defecto
        org.bukkit.Bukkit.getScheduler().runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
            if (waitingForName.containsKey(player.getUniqueId())) {
                LivingTool pendingTool = waitingForName.remove(player.getUniqueId());
                if (pendingTool != null) {
                    assignDefaultName(player, pendingTool);
                }
            }
        }, 600L); // 30 segundos = 600 ticks
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!waitingForName.containsKey(player.getUniqueId())) return;

        event.setCancelled(true); // no mostrar en chat global
        LivingTool tool = waitingForName.remove(player.getUniqueId());
        if (tool == null) return;

        String input = event.getMessage().trim();

        // Correr en el hilo principal para modificar items
        org.bukkit.Bukkit.getScheduler().runTask(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
            if (input.equalsIgnoreCase("cancel") || input.isEmpty()) {
                assignDefaultName(player, tool);
                return;
            }

            String name = ChatColor.GOLD + input.substring(0, Math.min(input.length(), 30));
            applyName(player, tool, name, input);
        });
    }

    private static void assignDefaultName(Player player, LivingTool tool) {
        String personality = tool.getData().getPersonality();
        if (personality == null) personality = "WISE";
        String materialName = tool.getItem().getType().name().replace("_", " ").toLowerCase();
        String defaultName = getDefaultName(personality, materialName);
        applyName(player, tool, ChatColor.GOLD + defaultName, defaultName);
    }

    private static void applyName(Player player, LivingTool tool, String coloredName, String rawName) {
        if (!tool.getItem().hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();

        // Conservar el display name original o usar el nuevo
        String currentName = meta.hasDisplayName() ? meta.getDisplayName() : "";
        // Si ya tiene un nombre de crafteo previo, lo reemplazamos
        meta.setDisplayName(coloredName);

        // Guardar "Nombre dado por:" en lore
        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        // Buscar si ya existe la línea de nombre dado
        lore.removeIf(l -> l.contains("Nombre dado por:") || l.contains("Bautizado como:"));
        lore.add(ChatColor.DARK_GRAY + "Bautizado por " + ChatColor.GRAY + player.getName());
        meta.setLore(lore);

        tool.getItem().setItemMeta(meta);

        // También guardar en ToolData custom name
        tool.getData().setCustomName(rawName);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.WHITE + "¡" + coloredName + ChatColor.WHITE + " ha recibido su nombre!");
        player.sendMessage(ChatColor.GRAY + "Ahora tienes un vínculo especial con esta herramienta.");
        player.sendMessage(ChatColor.GREEN + "+100 XP de bonus de vínculo");
        player.sendMessage("");

        tool.addXP(player, 100);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 25, 0.5, 0.8, 0.5, 0.1);
    }

    private static String getDefaultName(String personality, String material) {
        Random r = new Random();
        String[][] names = {
            {"Filo de la Ira", "Rompe-Piedras", "El Destructor", "Sombra de Acero"},  // AGGRESSIVE
            {"Piedra Sabia", "El Pensativo", "Espejo del Destino", "Sombra Tranquila"},// WISE
            {"Dormilón", "Mínimo Esfuerzo", "El Cansado", "Brillo Apagado"},          // LAZY
            {"Destello", "Rayo de Sol", "El Alegre", "Brillante"}                      // CHEERFUL
        };
        int row = personality.equals("AGGRESSIVE") ? 0 : personality.equals("WISE") ? 1
                : personality.equals("LAZY") ? 2 : 3;
        return names[row][r.nextInt(names[row].length)];
    }

    public static boolean isNamed(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return false;
        return tool.getItem().getItemMeta().getPersistentDataContainer()
                .has(keyNamed(), PersistentDataType.BYTE);
    }

    private static void setNamed(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        meta.getPersistentDataContainer().set(keyNamed(), PersistentDataType.BYTE, (byte) 1);
        tool.getItem().setItemMeta(meta);
    }
}
