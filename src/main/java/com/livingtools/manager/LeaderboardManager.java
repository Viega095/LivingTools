package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private static File file;
    private static FileConfiguration config;
    private static final Map<String, LeaderboardEntry> entries = new HashMap<>();
    private static final Map<String, Integer> bossKills = new HashMap<>();
    private static final Map<String, String> playerNames = new HashMap<>();

    public static void init() {
        file = new File(LivingToolsPlugin.getInstance().getDataFolder(), "leaderboard.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadEntries();
    }

    private static void loadEntries() {
        entries.clear();
        if (config.contains("entries")) {
            for (String key : config.getConfigurationSection("entries").getKeys(false)) {
                String path = "entries." + key;
                String playerName = config.getString(path + ".player");
                int level = config.getInt(path + ".level");
                int prestige = config.getInt(path + ".prestige", 0); // Default 0
                long xp = config.getLong(path + ".xp", 0);
                String toolName = config.getString(path + ".toolName");
                entries.put(key, new LeaderboardEntry(playerName, level, prestige, xp, toolName));
                playerNames.put(key, playerName);
            }
        }

        bossKills.clear();
        if (config.contains("boss_kills")) {
            for (String key : config.getConfigurationSection("boss_kills").getKeys(false)) {
                bossKills.put(key, config.getInt("boss_kills." + key + ".kills"));
                playerNames.put(key, config.getString("boss_kills." + key + ".name"));
            }
        }
    }

    public static void updateEntry(Player player, LivingTool tool) {
        String id = player.getUniqueId().toString();
        String toolName = tool.getData().getCustomName();
        if (toolName.isEmpty())
            toolName = tool.getItem().getType().name();

        entries.put(id,
                new LeaderboardEntry(player.getName(), tool.getData().getLevel(), tool.getData().getPrestige(),
                        tool.getData().getXP(),
                        toolName));
        playerNames.put(id, player.getName());
        saveEntries();
    }

    public static void updateBossKill(Player player) {
        String id = player.getUniqueId().toString();
        bossKills.put(id, bossKills.getOrDefault(id, 0) + 1);
        playerNames.put(id, player.getName());
        saveEntries();
    }

    private static void saveEntries() {
        for (Map.Entry<String, LeaderboardEntry> entry : entries.entrySet()) {
            String path = "entries." + entry.getKey();
            config.set(path + ".player", entry.getValue().playerName);
            config.set(path + ".level", entry.getValue().level);
            config.set(path + ".prestige", entry.getValue().prestige);
            config.set(path + ".xp", entry.getValue().xp);
            config.set(path + ".toolName", entry.getValue().toolName);
        }

        for (Map.Entry<String, Integer> entry : bossKills.entrySet()) {
            String path = "boss_kills." + entry.getKey();
            config.set(path + ".name", playerNames.get(entry.getKey()));
            config.set(path + ".kills", entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<LeaderboardEntry> getTop(int limit) {
        return entries.values().stream()
                .sorted((e1, e2) -> {
                    if (e1.prestige != e2.prestige) {
                        return Integer.compare(e2.prestige, e1.prestige); // Higher prestige first
                    }
                    if (e1.level != e2.level) {
                        return Integer.compare(e2.level, e1.level); // Then higher level
                    }
                    return Long.compare(e2.xp, e1.xp); // Then higher XP
                })
                .limit(limit)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static void showTopLevels(Player player) {
        player.sendMessage(MessageUtils.color("&6&l--- Top Herramientas Vivientes ---"));
        List<LeaderboardEntry> top = getTop(10);
        for (int i = 0; i < top.size(); i++) {
            LeaderboardEntry e = top.get(i);
            player.sendMessage(MessageUtils.color("&e" + (i + 1) + ". " + e.playerName + ": &fNivel " + e.level + " (P"
                    + e.prestige + ") - " + e.toolName));
        }
    }

    public static void showTopKills(Player player) {
        player.sendMessage(MessageUtils.color("&c&l--- Top Asesinos de Jefes ---"));
        bossKills.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String name = playerNames.getOrDefault(entry.getKey(), "Unknown");
                    player.sendMessage(MessageUtils.color("&e" + name + ": &f" + entry.getValue() + " Kills"));
                });
    }

    public static class LeaderboardEntry {
        public final String playerName;
        public final int level;
        public final int prestige;
        public final long xp;
        public final String toolName;

        public LeaderboardEntry(String playerName, int level, int prestige, long xp, String toolName) {
            this.playerName = playerName;
            this.level = level;
            this.prestige = prestige;
            this.xp = xp;
            this.toolName = toolName;
        }
    }
}
