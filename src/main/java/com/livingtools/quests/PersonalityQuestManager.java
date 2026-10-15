package com.livingtools.quests;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.manager.SoulEnergyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * PersonalityQuestManager — Misiones Dinámicas de Vínculo de Personalidad.
 * La herramienta interactúa en el chat con su dueño y le asigna pequeños desafíos
 * espontáneos temáticos según su carácter para fortalecer el vínculo y otorgar recompensas.
 */
public class PersonalityQuestManager implements Listener {

    public enum QuestGoalType {
        MINE_ORES, KILL_MONSTERS, MINE_HARD_BLOCKS, FIND_TREASURE, MINE_COMMON
    }

    public static class ActiveBondQuest {
        private final String personality;
        private final QuestGoalType goalType;
        private final String questTitle;
        private final String questDialogue;
        private final int targetCount;
        private int currentCount = 0;
        private final long expirationTime;

        public ActiveBondQuest(String personality, QuestGoalType goalType, String title, String dialogue, int target, long durationMs) {
            this.personality = personality;
            this.goalType = goalType;
            this.questTitle = title;
            this.questDialogue = dialogue;
            this.targetCount = target;
            this.expirationTime = System.currentTimeMillis() + durationMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    private static final Map<UUID, ActiveBondQuest> activeQuests = new HashMap<>();
    private static final Map<UUID, Long> lastQuestTime = new HashMap<>();
    private static final long QUEST_INTERVAL_MS = 25 * 60 * 1000L; // Cada 25 minutos
    private static final Random random = new Random();

    public static void startLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (!LivingTool.isLivingTool(hand)) continue;

                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();

                    if (!activeQuests.containsKey(uuid) && now - lastQuestTime.getOrDefault(uuid, 0L) > QUEST_INTERVAL_MS) {
                        triggerBondQuest(player, false);
                    } else if (activeQuests.containsKey(uuid)) {
                        ActiveBondQuest q = activeQuests.get(uuid);
                        if (q.isExpired()) {
                            activeQuests.remove(uuid);
                            player.sendMessage(ChatColor.GRAY + "[LivingTools] La misión de vínculo de tu herramienta ha expirado.");
                        }
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 600L, 1200L); // Chequear cada minuto
    }

    public static boolean triggerBondQuest(Player player, boolean force) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) {
            if (force) player.sendMessage(ChatColor.RED + "Debes sostener una Herramienta Viviente en tu mano principal.");
            return false;
        }

        LivingTool tool = new LivingTool(hand);
        ToolData data = tool.getData();
        String personality = data.getPersonality() != null ? data.getPersonality().toUpperCase() : "NORMAL";

        ActiveBondQuest quest = createQuestForPersonality(personality);
        activeQuests.put(player.getUniqueId(), quest);
        lastQuestTime.put(player.getUniqueId(), System.currentTimeMillis());

        // Diálogo en Chat
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡MISIÓN DE VÍNCULO DE TU HERRAMIENTA! " + ChatColor.GOLD + "✦");
        player.sendMessage(ChatColor.YELLOW + "  " + ChatColor.BOLD + quest.questTitle);
        player.sendMessage(ChatColor.AQUA + "  «" + quest.questDialogue + "»");
        player.sendMessage(ChatColor.GRAY + "  Progreso objetivo: " + ChatColor.WHITE + "0 / " + quest.targetCount);
        player.sendMessage(ChatColor.GRAY + "  Tiempo límite: 5 minutos.");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.2f);
        return true;
    }

    private static ActiveBondQuest createQuestForPersonality(String personality) {
        long duration = 5 * 60 * 1000L; // 5 minutos
        switch (personality) {
            case "GLUTTONOUS":
                return new ActiveBondQuest(personality, QuestGoalType.MINE_ORES, "Banquete de Metales",
                        "¡Tengo un hambre voraz! Rompe 15 menas minerales para alimentarme con su esencia.", 15, duration);
            case "AGGRESSIVE":
                return new ActiveBondQuest(personality, QuestGoalType.KILL_MONSTERS, "Sed de Combate",
                        "¡Siento sed de sangre y batalla! Extermina 8 criaturas hostiles ahora mismo.", 8, duration);
            case "HEROIC":
                return new ActiveBondQuest(personality, QuestGoalType.KILL_MONSTERS, "Prueba del Paladín",
                        "¡Un verdadero campeón forja su destino! Elimina 10 monstruos de la noche.", 10, duration);
            case "LAZY":
                return new ActiveBondQuest(personality, QuestGoalType.MINE_HARD_BLOCKS, "Trabajo Mínimo Esfuerzo",
                        "Uff, qué pereza... rompe sólo 5 bloques resistentes y tomémonos una siesta.", 5, duration);
            case "LUCKY":
                return new ActiveBondQuest(personality, QuestGoalType.FIND_TREASURE, "Golpe de Suerte",
                        "¡Siento que hoy la fortuna nos bendice! Extrae 3 minerales preciosos.", 3, duration);
            case "SARCASTIC":
                return new ActiveBondQuest(personality, QuestGoalType.KILL_MONSTERS, "A ver si puedes",
                        "A ver si hoy no fallas el golpe... elimina 6 monstruos sin caerte en lava.", 6, duration);
            case "SHY":
                return new ActiveBondQuest(personality, QuestGoalType.MINE_COMMON, "Buscando Refugio",
                        "Hay mucho ruido afuera... pica 20 bloques de piedra para cavar un escondite.", 20, duration);
            default:
                return new ActiveBondQuest(personality, QuestGoalType.MINE_COMMON, "Entrenamiento Rápido",
                        "¡Mantengamos el filo afilado! Pica 15 bloques de cualquier material.", 15, duration);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ActiveBondQuest quest = activeQuests.get(player.getUniqueId());
        if (quest == null || quest.isExpired()) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) return;

        Material mat = event.getBlock().getType();
        boolean matches = false;

        if (quest.goalType == QuestGoalType.MINE_ORES && (mat.name().contains("ORE") || mat == Material.ANCIENT_DEBRIS)) {
            matches = true;
        } else if (quest.goalType == QuestGoalType.MINE_HARD_BLOCKS && (mat == Material.OBSIDIAN || mat == Material.DEEPSLATE || mat == Material.CRYING_OBSIDIAN)) {
            matches = true;
        } else if (quest.goalType == QuestGoalType.FIND_TREASURE && (mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE || mat == Material.GOLD_ORE || mat == Material.DEEPSLATE_GOLD_ORE || mat == Material.ANCIENT_DEBRIS)) {
            matches = true;
        } else if (quest.goalType == QuestGoalType.MINE_COMMON) {
            matches = true;
        }

        if (matches) {
            progressQuest(player, quest);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ActiveBondQuest quest = activeQuests.get(killer.getUniqueId());
        if (quest == null || quest.isExpired()) return;

        ItemStack hand = killer.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(hand)) return;

        if (quest.goalType == QuestGoalType.KILL_MONSTERS && event.getEntity() instanceof Monster) {
            progressQuest(killer, quest);
        }
    }

    private static void progressQuest(Player player, ActiveBondQuest quest) {
        quest.currentCount++;

        if (quest.currentCount >= quest.targetCount) {
            completeQuest(player, quest);
        } else {
            // Notificar progreso suavemente cada pocos pasos
            if (quest.currentCount % Math.max(1, quest.targetCount / 3) == 0) {
                player.sendMessage(ChatColor.GRAY + "[Vínculo] " + ChatColor.AQUA + quest.questTitle + ": "
                        + ChatColor.YELLOW + quest.currentCount + " / " + quest.targetCount);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.4f);
            }
        }
    }

    private static void completeQuest(Player player, ActiveBondQuest quest) {
        activeQuests.remove(player.getUniqueId());

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(hand)) {
            LivingTool tool = new LivingTool(hand);
            tool.addXP(player, 350L);
            SoulEnergyManager.addEnergy(player, 30.0);
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "¡MISIÓN DE VÍNCULO COMPLETADA! " + ChatColor.GOLD + "✦");
        player.sendMessage(ChatColor.WHITE + "  " + quest.questTitle + ": " + ChatColor.GREEN + "¡Éxito absoluto!");
        player.sendMessage(ChatColor.YELLOW + "  +350 XP de Alma  |  +30% Energía de Alma  |  Afinidad Aumentada");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
    }

    public static ActiveBondQuest getActiveQuest(Player player) {
        return activeQuests.get(player.getUniqueId());
    }
}
