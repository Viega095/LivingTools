package com.livingtools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.Material;
import com.livingtools.mechanics.Personality;
import com.livingtools.runes.RuneType;
import com.livingtools.runes.RuneManager.RuneTier;
import com.livingtools.manager.ArtifactManager.ArtifactType;
import com.livingtools.manager.LoreManager.TomeType;
import com.livingtools.manager.SoulForgeManager.SoulType;
import com.livingtools.manager.CustomEnchantManager.LivingEnchant;
import com.livingtools.manager.FactionManager.Faction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LivingToolTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList(
                    "menu", "stats", "top", "guide", "history", "armor", "recipes", "forge",
                    "bind", "rename", "feed", "prestige", "bond", "duel", "structure", "trade",
                    "inspect", "ver", "challenges", "retos", "relic", "reliquia", "logros",
                    "achievements", "titulo", "awaken", "despertar", "library"));
            if (sender.hasPermission("livingtools.admin")) {
                subcommands.add("admin");
            }
            return filter(subcommands, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("admin") && sender.hasPermission("livingtools.admin")) {
                return filter(Arrays.asList(
                        "give", "xp", "givearmor", "unlock", "forcetrial", "setpersonality",
                        "reload", "giverune", "givegeode", "abilityxp", "setcorruption",
                        "giveartifact", "setmood", "givetome", "resetcooldowns", "smuggler",
                        "givebasic", "givesoulgem", "opensoulforge", "openrunefusion",
                        "givecustomenchant", "givevoidessence", "upgradeabyssal", "spawnrift",
                        "setplayercorruption", "givestarlightessence", "upgradeaetherial",
                        "purifytool", "spawnfortress", "joinfaction", "claimchunk",
                        "startconvergence", "giveomnitool", "spawnboss", "spawnarena",
                        "menu", "wiki", "models", "multiplier", "setlevel", "reset"), args[1]);
            }
            if (sub.equals("bond") || sub.equals("duel") || sub.equals("trade") || sub.equals("stats")
                    || sub.equals("inspect") || sub.equals("ver")) {
                return null; // Player names
            }
            if (sub.equals("structure")) {
                return filter(Arrays.asList("altar", "assembly", "bossforge", "cursedforge", "forge", "runeforge", "list"),
                        args[1]);
            }
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("livingtools.admin")) {
            String adminSub = args[1].toLowerCase();
            if (args.length == 3) {
                switch (adminSub) {
                    case "give":
                    case "givebasic":
                    case "givearmor":
                        return filter(Arrays.stream(Material.values())
                                .map(Enum::name)
                                .filter(n -> n.contains("PICKAXE") || n.contains("SWORD") || n.contains("AXE")
                                        || n.contains("SHOVEL") || n.contains("HOE") || n.contains("HELMET")
                                        || n.contains("CHESTPLATE") || n.contains("LEGGINGS") || n.contains("BOOTS"))
                                .collect(Collectors.toList()), args[2]);
                    case "setpersonality":
                        return filter(Arrays.stream(Personality.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "giverune":
                        return filter(Arrays.stream(RuneType.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "giveartifact":
                        return filter(Arrays.stream(ArtifactType.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "givetome":
                        return filter(Arrays.stream(TomeType.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "givesoulgem":
                        return filter(Arrays.stream(SoulType.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "givecustomenchant":
                        return filter(
                                Arrays.stream(LivingEnchant.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "joinfaction":
                        return filter(Arrays.stream(Faction.values()).map(Enum::name).collect(Collectors.toList()),
                                args[2]);
                    case "spawnboss":
                    case "spawnarena":
                        return filter(Arrays.asList("LIVING", "SERAPHIM", "TITAN", "DRYAD", "WYRM", "LEVIATHAN"),
                                args[2]);
                    case "multiplier":
                        return filter(Arrays.asList("1.5", "2.0", "3.0"), args[2]);
                }
            }
            if (args.length == 4) {
                if (adminSub.equals("giverune")) {
                    return filter(Arrays.stream(RuneTier.values()).map(Enum::name).collect(Collectors.toList()),
                            args[3]);
                }
                if (adminSub.equals("multiplier")) {
                    return filter(Arrays.asList("MINING", "COMBAT", "ALL"), args[3]);
                }
            }
        }

        return completions;
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
