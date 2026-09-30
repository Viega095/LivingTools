package com.livingtools.commands;

import com.livingtools.gui.category.StructureCoresCategoryGUI;
import com.livingtools.manager.StructureCoreManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StructureCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        if (args.length < 2 || args[1].equalsIgnoreCase("menu") || args[1].equalsIgnoreCase("gui")) {
            StructureCoresCategoryGUI.open(player);
            return true;
        }

        String action = args[1].toLowerCase();

        // 1. Comando: /lt structure give <type>
        if (action.equals("give") || action.equals("dar")) {
            if (!player.hasPermission("livingtools.admin")) {
                player.sendMessage(ChatColor.RED + "No tienes permisos para obtener núcleos de estructura directamente.");
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Uso: /lt structure give <assembly|pedestal|runeforge|rhythmicforge|cursedforge|fusion|bossforge|altar>");
                return true;
            }
            StructureCoreManager.StructureType type = resolveType(args[2]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "Tipo de estructura desconocido.");
                return true;
            }
            player.getInventory().addItem(StructureCoreManager.createCoreItem(type));
            player.sendMessage(ChatColor.GREEN + "✦ Has recibido el " + type.getFormattedName());
            return true;
        }

        // 2. Comando: /lt structure build <type>
        if (action.equals("build") || action.equals("construir")) {
            if (!player.hasPermission("livingtools.admin")) {
                player.sendMessage(ChatColor.RED + "No tienes permisos para construir estructuras directamente.");
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Uso: /lt structure build <assembly|pedestal|runeforge|rhythmicforge|cursedforge|fusion|bossforge|altar>");
                return true;
            }
            StructureCoreManager.StructureType type = resolveType(args[2]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "Tipo de estructura desconocido.");
                return true;
            }
            StructureCoreManager.deployStructure(player, player.getLocation(), type);
            return true;
        }

        // 3. Guías visuales holográficas clásicas
        switch (action) {
            case "altar":
                com.livingtools.visuals.RitualVisualizer.toggleGuide(player);
                return true;
            case "assembly":
                com.livingtools.visuals.AssemblyVisualizer.toggleGuide(player);
                return true;
            case "bossforge":
                com.livingtools.visuals.BossForgeVisualizer.toggleGuide(player);
                return true;
            case "forge":
            case "cursedforge":
                com.livingtools.visuals.CursedForgeVisualizer.toggleGuide(player);
                return true;
            case "runeforge":
                com.livingtools.visuals.RuneForgeVisualizer.toggleGuide(player);
                return true;
            case "rhythmicforge":
            case "forjaritmo":
                com.livingtools.visuals.RhythmicForgeVisualizer.toggleGuide(player);
                return true;
            case "help":
            case "list":
            default:
                StructureCoresCategoryGUI.open(player);
                return true;
        }
    }

    private static StructureCoreManager.StructureType resolveType(String key) {
        String lower = key.toLowerCase();
        if (lower.contains("assembly") || lower.contains("ensamblaje") || lower.contains("mesa"))
            return StructureCoreManager.StructureType.ASSEMBLY_TABLE;
        if (lower.contains("pedestal") || lower.contains("museum"))
            return StructureCoreManager.StructureType.SOUL_PEDESTAL;
        if (lower.contains("rune") || lower.contains("runica"))
            return StructureCoreManager.StructureType.RUNE_FORGE;
        if (lower.contains("rhythm") || lower.contains("ritmo") || lower.contains("ritmica"))
            return StructureCoreManager.StructureType.RHYTHMIC_FORGE;
        if (lower.contains("cursed") || lower.contains("maldita"))
            return StructureCoreManager.StructureType.CURSED_FORGE;
        if (lower.contains("fusion") || lower.contains("crucible") || lower.contains("crisol"))
            return StructureCoreManager.StructureType.SOUL_FUSION_CRUCIBLE;
        if (lower.contains("boss") || lower.contains("jefe") || lower.contains("cruz"))
            return StructureCoreManager.StructureType.BOSS_FORGE;
        if (lower.contains("altar") || lower.contains("ritual") || lower.contains("ascension") || lower.contains("celestial"))
            return StructureCoreManager.StructureType.RITUAL_ALTAR;
        return null;
    }
}
