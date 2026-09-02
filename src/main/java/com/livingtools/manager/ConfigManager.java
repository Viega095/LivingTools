package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private static FileConfiguration config;
    private static FileConfiguration messages;
    private static File messagesFile;

    public static void load() {
        LivingToolsPlugin plugin = LivingToolsPlugin.getInstance();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        mergeMissingMessageDefaults(plugin);
    }

    public static void reload() {
        LivingToolsPlugin plugin = LivingToolsPlugin.getInstance();
        plugin.reloadConfig();
        config = plugin.getConfig();

        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        mergeMissingMessageDefaults(plugin);
    }

    public static String getMessage(String key) {
        if (messages == null)
            return "";
        String prefix = messages.getString("prefix", "&8[&6LivingTools&8] &r");
        String msg = messages.getString(key, "&cMensaje no encontrado: " + key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public static String getRawMessage(String key) {
        if (messages == null)
            return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', messages.getString(key, key));
    }

    public static List<String> getMessageList(String key) {
        if (messages == null)
            return new ArrayList<>();
        List<String> list = messages.getStringList(key);
        List<String> colored = new ArrayList<>();
        for (String s : list) {
            colored.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', s));
        }
        return colored;
    }

    private static void mergeMissingMessageDefaults(LivingToolsPlugin plugin) {
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in == null || messages == null) {
                return;
            }

            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(in));
            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (defaults.isConfigurationSection(key)) {
                    continue;
                }
                if (!messages.contains(key)) {
                    messages.set(key, defaults.get(key));
                    changed = true;
                }
            }

            if (changed) {
                try {
                    messages.save(messagesFile);
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not save merged messages.yml: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not merge message defaults: " + e.getMessage());
        }
    }

    // --- Getters for Config ---
    public static int getInt(String path) {
        return config.getInt(path);
    }

    public static double getDouble(String path) {
        return config.getDouble(path);
    }

    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    // --- Specific Getters (Delegating to generic ones for cleaner code later) ---
    public static double getMiningXPMultiplier() {
        return getDouble("xp.mining-multiplier");
    }

    public static double getCombatXPMultiplier() {
        return getDouble("xp.combat-multiplier");
    }

    public static int getVeinMinerMaxBlocks() {
        return getInt("abilities.veinminer.max-blocks");
    }

    public static double getMagnetRadius() {
        return getDouble("abilities.magnet.radius");
    }

    public static int getLightningChance() {
        return getInt("abilities.lightning.chance");
    }

    public static void setMiningXPMultiplier(double value) {
        config.set("xp.mining-multiplier", value);
        LivingToolsPlugin.getInstance().saveConfig();
    }

    public static void setCombatXPMultiplier(double value) {
        config.set("xp.combat-multiplier", value);
        LivingToolsPlugin.getInstance().saveConfig();
    }

    private static boolean debug = false;

    public static void setDebug(boolean value) {
        debug = value;
    }

    public static boolean isDebug() {
        return debug;
    }
}
