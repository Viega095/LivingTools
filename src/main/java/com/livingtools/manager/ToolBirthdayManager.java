package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * ToolBirthdayManager — celebra los hitos de longevidad de la herramienta.
 *
 * Hitos de cumpleaños:
 *  - 7 días   → +500 XP + "Pastel Forjado"
 *  - 30 días  → +2,000 XP + Fuegos artificiales + Anuncio
 *  - 90 días  → +5,000 XP + Gema de Aniversario
 *  - 365 días → +20,000 XP + Título "el Eterno" + Broadcast global
 */
public class ToolBirthdayManager {

    private static NamespacedKey KEY_CELEBRATED_DAYS;
    private static NamespacedKey keyCelebratedDays() {
        if (KEY_CELEBRATED_DAYS == null)
            KEY_CELEBRATED_DAYS = new NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "celebrated_anniversaries");
        return KEY_CELEBRATED_DAYS;
    }

    private static final int[] MILESTONE_DAYS = {7, 30, 90, 365};

    /**
     * Comprueba si la herramienta cumple un aniversario pendiente de celebrar.
     */
    public static void checkBirthday(Player player, LivingTool tool) {
        int ageDays = ToolMemoryManager.getAgeInDays(tool);
        if (ageDays < 7) return;

        ItemStack item = tool.getItem();
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        String celebratedStr = meta.getPersistentDataContainer()
                .getOrDefault(keyCelebratedDays(), PersistentDataType.STRING, "");
        Set<Integer> celebrated = new HashSet<>();
        for (String s : celebratedStr.split(",")) {
            if (!s.isEmpty()) {
                try { celebrated.add(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
            }
        }

        for (int milestone : MILESTONE_DAYS) {
            if (ageDays >= milestone && !celebrated.contains(milestone)) {
                celebrated.add(milestone);
                celebrateAnniversary(player, tool, milestone);
            }
        }

        // Guardar hitos celebrados
        StringBuilder sb = new StringBuilder();
        for (int d : celebrated) {
            if (sb.length() > 0) sb.append(",");
            sb.append(d);
        }
        meta.getPersistentDataContainer().set(keyCelebratedDays(), PersistentDataType.STRING, sb.toString());
        item.setItemMeta(meta);
    }

    private static void celebrateAnniversary(Player player, LivingTool tool, int days) {
        String toolName = (tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName())
                ? tool.getItem().getItemMeta().getDisplayName() : "Tu herramienta";

        long xpReward = days == 7 ? 500 : days == 30 ? 2000 : days == 90 ? 5000 : 20000;
        tool.addXP(player, xpReward);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "🎂 " + ChatColor.BOLD + "¡ANIVERSARIO DE HERRAMIENTA! (" + days + " DÍAS) 🎂");
        player.sendMessage(ChatColor.GRAY + "  " + toolName + ChatColor.RESET + ChatColor.GRAY + " lleva "
                + ChatColor.YELLOW + days + " días" + ChatColor.GRAY + " acompañándote en tus aventuras.");
        player.sendMessage(ChatColor.GREEN + "  +" + xpReward + " XP de bonificación");
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1.5, 0), 50, 0.8, 0.8, 0.8, 0.1);

        // Recompensa especial en items
        ItemStack gift = createAnniversaryGift(days);
        if (gift != null) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(gift);
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
        }

        // Broadcast a partir de 30 días
        if (days >= 30) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + player.getName()
                    + ChatColor.GRAY + " y su " + toolName + ChatColor.RESET + ChatColor.GRAY
                    + " celebran " + ChatColor.GOLD + days + " días" + ChatColor.GRAY + " de vínculo juntos.");
        }
    }

    private static ItemStack createAnniversaryGift(int days) {
        if (days == 7) {
            ItemStack cake = new ItemStack(Material.CAKE);
            ItemMeta m = cake.getItemMeta();
            if (m != null) {
                m.setDisplayName(ChatColor.GOLD + "🎂 Pastel Forjado");
                m.setLore(Arrays.asList(
                        ChatColor.GRAY + "Un pastel horneado en el fuego de la forja.",
                        ChatColor.YELLOW + "Celebración del 7° día de vida."
                ));
                cake.setItemMeta(m);
            }
            return cake;
        } else if (days == 30 || days == 90) {
            ItemStack gem = new ItemStack(Material.AMETHYST_SHARD);
            ItemMeta m = gem.getItemMeta();
            if (m != null) {
                m.setDisplayName(ChatColor.AQUA + "✦ Gema de " + days + " Días");
                m.setLore(Arrays.asList(
                        ChatColor.GRAY + "Cristalizada por la lealtad y el tiempo.",
                        ChatColor.YELLOW + "Coleccionable especial de LivingTools."
                ));
                gem.setItemMeta(m);
            }
            return gem;
        } else if (days == 365) {
            ItemStack star = new ItemStack(Material.NETHER_STAR);
            ItemMeta m = star.getItemMeta();
            if (m != null) {
                m.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "★ Corazón Eterno (1 Año)");
                m.setLore(Arrays.asList(
                        ChatColor.GRAY + "Un año entero junto a tu herramienta.",
                        ChatColor.GOLD + "Simboliza un lazo indestructible."
                ));
                star.setItemMeta(m);
            }
            return star;
        }
        return null;
    }
}
