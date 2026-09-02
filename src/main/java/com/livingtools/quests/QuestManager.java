package com.livingtools.quests;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class QuestManager {

    private static final Map<String, Quest> quests = new HashMap<>();
    private static final Map<UUID, Set<String>> activeQuests = new HashMap<>();
    private static final Map<UUID, Set<String>> completedQuests = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> questProgress = new HashMap<>();

    public static void registerQuest(Quest quest) {
        quests.put(quest.getId(), quest);
    }

    public static Quest getQuest(String id) {
        return quests.get(id);
    }

    public static Collection<Quest> getAllQuests() {
        return quests.values();
    }

    public static void startQuest(Player player, String questId) {
        Quest quest = quests.get(questId);
        if (quest == null)
            return;

        UUID uuid = player.getUniqueId();
        activeQuests.computeIfAbsent(uuid, k -> new HashSet<>()).add(questId);

        player.sendMessage("§a✓ Quest iniciada: §f" + quest.getName());
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    public static void completeQuest(Player player, String questId) {
        Quest quest = quests.get(questId);
        if (quest == null)
            return;

        UUID uuid = player.getUniqueId();

        if (!quest.isCompleted(player)) {
            player.sendMessage("§c✗ No has completado todos los objetivos.");
            return;
        }

        // Remover de activas y agregar a completadas
        activeQuests.getOrDefault(uuid, new HashSet<>()).remove(questId);
        completedQuests.computeIfAbsent(uuid, k -> new HashSet<>()).add(questId);

        // Dar recompensas
        quest.giveRewards(player);

        // Notificación
        player.sendTitle(
                "§6§l¡QUEST COMPLETADA!",
                "§e" + quest.getName(),
                10, 70, 20);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.sendMessage("§a§l✓ Quest completada: §f" + quest.getName());
    }

    public static boolean hasActiveQuest(Player player, String questId) {
        return activeQuests.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(questId);
    }

    public static boolean hasCompletedQuest(Player player, String questId) {
        return completedQuests.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(questId);
    }

    public static Set<String> getActiveQuests(Player player) {
        return new HashSet<>(activeQuests.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }

    public static Set<String> getCompletedQuests(Player player) {
        return new HashSet<>(completedQuests.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }

    public static void incrementProgress(Player player, String questId, int amount) {
        UUID uuid = player.getUniqueId();
        questProgress.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(questId, amount, Integer::sum);
    }

    public static int getProgress(Player player, String questId) {
        return questProgress.getOrDefault(player.getUniqueId(), new HashMap<>())
                .getOrDefault(questId, 0);
    }

    public static void resetDailyQuests() {
        // Resetear quests diarias
        for (UUID uuid : activeQuests.keySet()) {
            Set<String> active = activeQuests.get(uuid);
            active.removeIf(questId -> {
                Quest quest = quests.get(questId);
                return quest != null && quest.getType() == QuestType.DAILY;
            });
        }

        // Limpiar progreso de quests diarias
        for (UUID uuid : questProgress.keySet()) {
            Map<String, Integer> progress = questProgress.get(uuid);
            progress.keySet().removeIf(questId -> {
                Quest quest = quests.get(questId);
                return quest != null && quest.getType() == QuestType.DAILY;
            });
        }

        // Notificar jugadores online
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§6§l[Quests] §eNuevas misiones diarias disponibles!");
        }
    }

    public static void startDailyResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                resetDailyQuests();
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 24000L * 20L); // Cada 24 horas
    }
}
