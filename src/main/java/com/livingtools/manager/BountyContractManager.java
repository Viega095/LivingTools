package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * BountyContractManager — Tablón de Contratos de Caza Dinámicos y Recompensas Semanales.
 */
public class BountyContractManager {

    public enum ContractType {
        ELITE_HUNT("Cacería de Élite", "Elimina monstruos hostiles con tu arma viviente.", 20, 800L, Material.DIAMOND_SWORD),
        DEEP_MINING("Minería Profunda", "Mina minerales de Diamante o Ancient Debris.", 30, 600L, Material.NETHERITE_PICKAXE),
        CORRUPTION_PURGE("Purificación Sagrada", "Purifica criaturas oscuras en la noche.", 25, 750L, Material.SOUL_LANTERN);

        private final String title;
        private final String description;
        private final int targetCount;
        private final long rewardXP;
        private final Material icon;

        ContractType(String title, String description, int targetCount, long rewardXP, Material icon) {
            this.title = title;
            this.description = description;
            this.targetCount = targetCount;
            this.rewardXP = rewardXP;
            this.icon = icon;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getTargetCount() { return targetCount; }
        public long getRewardXP() { return rewardXP; }
        public Material getIcon() { return icon; }
    }

    public static class PlayerContract {
        private final ContractType type;
        private int currentProgress = 0;
        private boolean completed = false;
        private boolean claimed = false;

        public PlayerContract(ContractType type) {
            this.type = type;
        }

        public ContractType getType() { return type; }
        public int getCurrentProgress() { return currentProgress; }
        public void setProgress(int p) { this.currentProgress = p; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean c) { this.completed = c; }
        public boolean isClaimed() { return claimed; }
        public void setClaimed(boolean cl) { this.claimed = cl; }

        public void increment(int amount) {
            if (completed) return;
            currentProgress += amount;
            if (currentProgress >= type.getTargetCount()) {
                currentProgress = type.getTargetCount();
                completed = true;
            }
        }
    }

    private static final Map<UUID, List<PlayerContract>> playerContracts = new HashMap<>();

    public static List<PlayerContract> getContracts(Player player) {
        return playerContracts.computeIfAbsent(player.getUniqueId(), k -> generateDailyContracts());
    }

    private static List<PlayerContract> generateDailyContracts() {
        List<PlayerContract> list = new ArrayList<>();
        for (ContractType type : ContractType.values()) {
            list.add(new PlayerContract(type));
        }
        return list;
    }

    public static void onMobKill(Player player) {
        List<PlayerContract> list = getContracts(player);
        for (PlayerContract c : list) {
            if (c.getType() == ContractType.ELITE_HUNT || c.getType() == ContractType.CORRUPTION_PURGE) {
                c.increment(1);
            }
        }
    }

    public static void onOreMine(Player player) {
        List<PlayerContract> list = getContracts(player);
        for (PlayerContract c : list) {
            if (c.getType() == ContractType.DEEP_MINING) {
                c.increment(1);
            }
        }
    }

    public static boolean claimContract(Player player, int index) {
        List<PlayerContract> list = getContracts(player);
        if (index < 0 || index >= list.size()) return false;

        PlayerContract contract = list.get(index);
        if (!contract.isCompleted() || contract.isClaimed()) return false;

        contract.setClaimed(true);

        // Otorgar XP a la herramienta
        ItemStack held = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(held)) {
            LivingTool tool = new LivingTool(held);
            tool.addXP(player, contract.getType().getRewardXP());
        }

        // Otorgar ítems de recompensa
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
        player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 5));

        return true;
    }

    public static void cleanup(UUID uuid) {
        playerContracts.remove(uuid);
    }
}
