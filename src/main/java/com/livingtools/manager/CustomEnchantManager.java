package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomEnchantManager implements Listener {

    private static NamespacedKey KEY_ENCHANTS_INSTANCE;

    /** Lazy getter — seguro para llamar después de onEnable() */
    public static NamespacedKey getKeyEnchants() {
        if (KEY_ENCHANTS_INSTANCE == null)
            KEY_ENCHANTS_INSTANCE = new NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "custom_enchants");
        return KEY_ENCHANTS_INSTANCE;
    }

    private static final Random random = new Random();

    public enum LivingEnchant {
        SOUL_REAPER("Segador de Almas", "Aumenta la probabilidad de obtener Gemas de Alma.", 10),
        THUNDERLORD("Señor del Trueno", "Probabilidad de invocar un rayo al golpear.", 5),
        VEIN_BREAKER("Rompevenas", "Mina vetas enteras de mineral (hasta 16 bloques).", 3),

        // ── Nuevos encantamientos de minería ──────────────────────────────────
        TREASURE("Tesoro", "Chance muy baja de descubrir un cofre con loot al picar.", 5),
        ORE_ECHO("Eco de Minas", "Detecta ores cercanos y los marca en el chat brevemente.", 3),
        AUTO_SMELT("Fundición Viva", "Funde automáticamente los ores al romperlos.", 1),
        EXPLOSIVE_PICK("Pico Explosivo", "Probabilidad de romper un radio 3×3 de bloques.", 3),
        SOUL_HARVEST("Cosecha de Almas", "Mobs matados con el pico tienen +15% chance de drop raro.", 5);

        private final String name;
        private final String description;
        private final int maxLevel;

        LivingEnchant(String name, String description, int maxLevel) {
            this.name = name;
            this.description = description;
            this.maxLevel = maxLevel;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getMaxLevel() { return maxLevel; }
    }

    // --- Event Listeners ---

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity))
            return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            if (hasEnchant(tool, LivingEnchant.THUNDERLORD)) {
                int level = getEnchantLevel(tool, LivingEnchant.THUNDERLORD);
                // 5% chance per level
                if (random.nextDouble() < (level * 0.05)) {
                    event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
                    player.sendMessage(MessageUtils.color("&e¡TRUENO!"));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);
            if (hasEnchant(tool, LivingEnchant.SOUL_REAPER)) {
                int level = getEnchantLevel(tool, LivingEnchant.SOUL_REAPER);
                // Increase soul gem drop chance logic here
                // For now, just a debug message
                if (random.nextDouble() < (level * 0.01)) {
                    // Drop soul gem logic would go here
                    // SoulForgeManager.dropSoulGem(event.getEntity());
                }
            }
        }
    }

    // --- Helper Methods ---

    public static boolean hasEnchant(LivingTool tool, LivingEnchant enchant) {
        return getEnchantLevel(tool, enchant) > 0;
    }

    public static int getEnchantLevel(LivingTool tool, LivingEnchant enchant) {
        if (!tool.getItem().hasItemMeta()) return 0;
        String data = tool.getItem().getItemMeta().getPersistentDataContainer().get(getKeyEnchants(),
                PersistentDataType.STRING);
        if (data == null)
            return 0;

        for (String entry : data.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2 && parts[0].equals(enchant.name())) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static void addEnchant(LivingTool tool, LivingEnchant enchant, int level) {
        String data = tool.getItem().getItemMeta().getPersistentDataContainer().get(getKeyEnchants(),
                PersistentDataType.STRING);
        if (data == null)
            data = "";

        List<String> newData = new ArrayList<>();
        boolean found = false;
        for (String entry : data.split(",")) {
            if (entry.isEmpty())
                continue;
            String[] parts = entry.split(":");
            if (parts[0].equals(enchant.name())) {
                newData.add(enchant.name() + ":" + level); // Update level
                found = true;
            } else {
                newData.add(entry);
            }
        }
        if (!found) {
            newData.add(enchant.name() + ":" + level);
        }

        String finalString = String.join(",", newData);
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        meta.getPersistentDataContainer().set(getKeyEnchants(), PersistentDataType.STRING, finalString);

        // Update Lore
        List<String> lore = meta.getLore();
        if (lore == null)
            lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + enchant.getName() + " " + toRoman(level));
        meta.setLore(lore);

        tool.getItem().setItemMeta(meta);
    }

    private static String toRoman(int n) {
        return "I"; // Simplified
    }
}
