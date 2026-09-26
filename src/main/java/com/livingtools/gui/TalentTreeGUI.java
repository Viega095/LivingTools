package com.livingtools.gui;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.LevelUpRewardManager;
import com.livingtools.manager.TalentTreeManager;
import com.livingtools.manager.TalentTreeManager.Talent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * TalentTreeGUI — Interfaz gráfica para desbloquear y asignar Puntos de Habilidad.
 */
public class TalentTreeGUI {

    public static final String TITLE = ChatColor.DARK_GREEN + "✦ Árbol de Talentos Vivientes ✦";

    public static void open(Player player, LivingTool tool) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE);

        ItemStack border = GUIBuilder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            gui.setItem(i, border);
        }

        int points = LevelUpRewardManager.getSkillPoints(tool);

        // Slot 4: Info de Puntos
        ItemStack info = GUIBuilder.createGlowingItem(Material.NETHER_STAR,
                ChatColor.GOLD + "" + ChatColor.BOLD + "Puntos de Habilidad Disponibles: " + ChatColor.GREEN + points,
                ChatColor.GRAY + "Gana más puntos al subir a niveles clave",
                ChatColor.GRAY + "(Niveles 25, 50, 75, 100, 125, 150, 175, 200).",
                "",
                ChatColor.YELLOW + "Haz click en un talento para mejorarlo.");
        gui.setItem(4, info);

        // Rama Fortuna & Minería (Columna 2: 10, 19, 28)
        gui.setItem(10, createTalentItem(tool, Talent.MINING_INSTINCT, Material.RAW_GOLD, points));
        gui.setItem(19, createTalentItem(tool, Talent.DEEP_ECHO, Material.ECHO_SHARD, points));
        gui.setItem(28, createTalentItem(tool, Talent.TREASURE_MASTER, Material.CHEST, points));

        // Rama Combate Marcial (Columna 5: 13, 22, 31)
        gui.setItem(13, createTalentItem(tool, Talent.SPIRIT_STRIKE, Material.IRON_SWORD, points));
        gui.setItem(22, createTalentItem(tool, Talent.QUICK_AWAKENING, Material.BLAZE_POWDER, points));
        gui.setItem(31, createTalentItem(tool, Talent.IGNITE_BLADE, Material.FIRE_CHARGE, points));

        // Rama Simbiosis & Alma (Columna 8: 16, 25, 34)
        gui.setItem(16, createTalentItem(tool, Talent.ANCESTRAL_FLOW, Material.EXPERIENCE_BOTTLE, points));
        gui.setItem(25, createTalentItem(tool, Talent.CALM_MIND, Material.HEART_OF_THE_SEA, points));
        gui.setItem(34, createTalentItem(tool, Talent.IMMORTAL_BOND, Material.TOTEM_OF_UNDYING, points));

        // Slot 40: Reiniciar Talentos
        int spent = TalentTreeManager.countTotalSpentPoints(tool);
        ItemStack resetBtn = GUIBuilder.createItem(Material.ANVIL,
                ChatColor.RED + "🔄 Reiniciar Todos los Talentos",
                ChatColor.GRAY + "Puntos invertidos actualmente: " + ChatColor.YELLOW + spent,
                "",
                ChatColor.YELLOW + "► Click para recuperar todos tus puntos");
        gui.setItem(40, resetBtn);

        // Slot 44: Cerrar
        gui.setItem(44, GUIBuilder.createItem(Material.BARRIER, ChatColor.RED + "Cerrar"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
    }

    private static ItemStack createTalentItem(LivingTool tool, Talent talent, Material mat, int points) {
        int level = TalentTreeManager.getTalentLevel(tool, talent);
        boolean maxed = level >= talent.getMaxLevel();

        String status = maxed ? ChatColor.GOLD + "[MAX]" : (level > 0 ? ChatColor.GREEN + "[" + level + "/" + talent.getMaxLevel() + "]" : ChatColor.GRAY + "[0/" + talent.getMaxLevel() + "]");
        String clickHint = maxed ? ChatColor.GOLD + "Talento maximizado" : (points > 0 ? ChatColor.GREEN + "► Click para mejorar (1 Punto)" : ChatColor.RED + "Necesitas puntos de habilidad");

        return GUIBuilder.createGlowingItem(mat,
                ChatColor.YELLOW + talent.getName() + " " + status,
                ChatColor.GRAY + talent.getDescription(),
                "",
                ChatColor.AQUA + "Nivel actual: " + ChatColor.WHITE + level + "/" + talent.getMaxLevel(),
                "",
                clickHint);
    }
}
