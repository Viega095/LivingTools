package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SleepingManager {

    private static final NamespacedKey KEY_SLEEPING = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_sleeping");
    private static final NamespacedKey KEY_TITAN_CORE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_titan_core");

    public static ItemStack createSleepingTool(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Herramienta Dormida");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Esta reliquia contiene un alma ancestral latente.");
            lore.add(ChatColor.YELLOW + "✦ Click Derecho teniendo 30 niveles de XP");
            lore.add(ChatColor.GRAY + "o interactúa con una " + ChatColor.AQUA + "Forja Mística" + ChatColor.GRAY + " para despertarla.");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(KEY_SLEEPING, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isSleepingTool(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_SLEEPING, PersistentDataType.BYTE);
    }

    /**
     * Crea un Núcleo de Esencia Titánica (drop de jefes épicos).
     */
    public static ItemStack createTitanSoulCore() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Núcleo de Esencia Titánica ✦");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Condensación de energía cósmica de un titán caído.");
            lore.add("");
            lore.add(ChatColor.YELLOW + "► Uso: " + ChatColor.WHITE + "Arrastra sobre una Herramienta Viviente en tu inventario");
            lore.add(ChatColor.GRAY + "o haz Click Derecho sosteniéndolo:");
            lore.add(ChatColor.GREEN + "  +5 Niveles Inmediatos de Alma");
            lore.add(ChatColor.AQUA + "  +100% Carga de Energía de Alma (Listo para Overdrive)");
            lore.add(ChatColor.LIGHT_PURPLE + "  ★ Rasgo Cazador de Titanes (+15% daño a jefes)");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(KEY_TITAN_CORE, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isTitanSoulCore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_TITAN_CORE, PersistentDataType.BYTE);
    }

    /**
     * Aplica los efectos del Núcleo de Esencia Titánica a una herramienta viviente.
     */
    public static boolean applyTitanSoulCore(Player player, ItemStack targetTool) {
        if (!LivingTool.isLivingTool(targetTool)) return false;

        LivingTool tool = new LivingTool(targetTool);
        ToolData data = tool.getData();

        int currentLvl = data.getLevel();
        int newLvl = currentLvl + 5;
        data.setLevel(newLvl);
        tool.updateLore();

        // Cargar energía de alma al 100%
        SoulEnergyManager.setEnergy(player, 100.0);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);
        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5, 0.3);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡ESENCIA TITÁNICA ABSORBIDA! " + ChatColor.GOLD + "✦");
        player.sendMessage(ChatColor.WHITE + "  Tu herramienta ha ascendido a " + ChatColor.YELLOW + "Nivel " + newLvl + ChatColor.WHITE + ".");
        player.sendMessage(ChatColor.AQUA + "  ¡Energía de Alma cargada al 100%! Pulsa " + ChatColor.YELLOW + "Shift + F" + ChatColor.AQUA + " para Overdrive.");
        player.sendMessage("");

        return true;
    }

    /**
     * Arma Mítica de Jefe: Cuchilla del Vacío Primordial (Ender Dragon).
     */
    public static ItemStack createDragonVoidBlade() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        LivingTool tool = new LivingTool(item);
        tool.getData().initialize();
        tool.getData().setLevel(20);
        tool.getData().setPersonality("HEROIC");
        tool.getData().setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Cuchilla del Vacío Primordial ✦");
        tool.updateLore();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.LIGHT_PURPLE + "★ Reliquia Titánica: Ender Dragon");
            lore.add(ChatColor.GRAY + "Hendedura Cósmica: Teletransporte táctico al impactar críticos.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Arma Mítica de Jefe: Guadaña de la Muerte Marchita (Wither).
     */
    public static ItemStack createWitherScythe() {
        ItemStack item = new ItemStack(Material.NETHERITE_HOE);
        LivingTool tool = new LivingTool(item);
        tool.getData().initialize();
        tool.getData().setLevel(20);
        tool.getData().setPersonality("AGGRESSIVE");
        tool.getData().setCustomName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "✦ Guadaña de la Muerte Marchita ✦");
        tool.updateLore();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.RED + "★ Reliquia Titánica: The Wither");
            lore.add(ChatColor.GRAY + "Aura Marchita: Inflige Wither II a enemigos y drena 25% de vida.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Arma Mítica de Jefe: Martillo Sísmico del Abismo (Warden).
     */
    public static ItemStack createWardenHammer() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        LivingTool tool = new LivingTool(item);
        tool.getData().initialize();
        tool.getData().setLevel(25);
        tool.getData().setPersonality("WISE");
        tool.getData().setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "✦ Martillo Sísmico del Abismo ✦");
        tool.updateLore();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.DARK_AQUA + "★ Reliquia Titánica: The Warden");
            lore.add(ChatColor.GRAY + "Onda Sónica Sísmica: Demolición cúbica y rechazo sónico.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Despierta una herramienta dormida consumiendo XP o en estructura.
     */
    public static void tryAwaken(Player player, ItemStack item) {
        if (!isSleepingTool(item))
            return;

        if (player.getLevel() < 30) {
            player.sendMessage(ChatColor.RED + "Necesitas 30 niveles de experiencia para despertar esta reliquia, o colocarla en una Forja Mística.");
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
            return;
        }

        // Consume XP
        player.setLevel(player.getLevel() - 30);

        // Transform Item
        LivingTool tool = new LivingTool(item);

        // Remove Sleeping Tag
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().remove(KEY_SLEEPING);
            item.setItemMeta(meta);
        }

        // Initialize Living Tool a nivel 10
        tool.getData().initialize();
        tool.getData().setLevel(10);

        // Random Personality
        com.livingtools.mechanics.Personality[] personalities = com.livingtools.mechanics.Personality.values();
        com.livingtools.mechanics.Personality randomPersonality = personalities[new java.util.Random().nextInt(personalities.length)];
        tool.getData().setPersonality(randomPersonality.name());

        tool.getData().setCustomName(ChatColor.GOLD + "✦ Herramienta Despierta ✦");
        tool.updateLore();

        // Effects
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1);
        player.spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 1);
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡LA RELIQUIA HA DESPERTADO! " + ChatColor.GOLD + "✦");
        player.sendMessage(ChatColor.WHITE + "Ha cobrado vida en " + ChatColor.YELLOW + "Nivel 10" + ChatColor.WHITE + " con temperamento " + ChatColor.AQUA + randomPersonality.getDisplayName() + ChatColor.WHITE + ".");
        player.sendMessage("");

        // Personality Dialogue
        PersonalityManager.sayLine(player, tool, PersonalityManager.EventType.AWAKENING);
    }
}
