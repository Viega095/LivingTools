package com.livingtools.commands;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingTool;
import com.livingtools.manager.AbilityMasteryManager;
import com.livingtools.manager.AbilityComboManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "give":
                return handleGive(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender, args);
            case "cooldown":
                return handleCooldown(sender, args);
            case "mastery":
                return handleMastery(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "combo":
                return handleCombo(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Subcomando desconocido. Usa /lt ability help");
                return true;
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /lt ability give <player> <ability_id>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        String abilityId = args[2];
        Ability ability = AbilityRegistry.getAbility(abilityId);

        if (ability == null) {
            sender.sendMessage(ChatColor.RED + "Habilidad no encontrada: " + abilityId);
            return true;
        }

        // Add ability to held item
        if (!LivingTool.isLivingTool(target.getInventory().getItemInMainHand())) {
            sender.sendMessage(ChatColor.RED + "El jugador debe sostener una Living Tool.");
            return true;
        }

        LivingTool tool = new LivingTool(target.getInventory().getItemInMainHand());
        if (!tool.hasAbility(abilityId)) {
            tool.addAbility(abilityId);
            sender.sendMessage(ChatColor.GREEN + "Habilidad " + abilityId + " añadida a " + target.getName());
            target.sendMessage(ChatColor.GREEN + "¡Nueva habilidad desbloqueada: " + ability.getName() + "!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "El jugador ya tiene esa habilidad.");
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /lt ability remove <player> <ability_id>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        String abilityId = args[2];

        if (!LivingTool.isLivingTool(target.getInventory().getItemInMainHand())) {
            sender.sendMessage(ChatColor.RED + "El jugador debe sostener una Living Tool.");
            return true;
        }

        LivingTool tool = new LivingTool(target.getInventory().getItemInMainHand());
        tool.removeAbility(abilityId);

        sender.sendMessage(ChatColor.GREEN + "Habilidad " + abilityId + " removida de " + target.getName());
        target.sendMessage(ChatColor.YELLOW + "Habilidad removida: " + abilityId);

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (args.length < 2 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Uso: /lt ability list [player]");
            return true;
        }

        Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : (Player) sender;

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Habilidades Registradas ===");
        for (Ability ability : AbilityRegistry.getAbilities().values()) {
            int level = AbilityMasteryManager.getLevel(target, ability.getId());
            sender.sendMessage(ChatColor.YELLOW + "- " + ability.getName() +
                    ChatColor.GRAY + " (" + ability.getId() + ") " +
                    ChatColor.AQUA + "Nivel: " + level);
        }

        return true;
    }

    private boolean handleCooldown(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /lt ability cooldown reset <player> [ability_id]");
            return true;
        }

        if (!args[1].equalsIgnoreCase("reset")) {
            sender.sendMessage(ChatColor.RED + "Acción desconocida. Usa 'reset'");
            return true;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        // Reset all cooldowns for target
        sender.sendMessage(ChatColor.GREEN + "Cooldowns reseteados para " + target.getName());
        target.sendMessage(ChatColor.GREEN + "¡Tus cooldowns han sido reseteados!");

        return true;
    }

    private boolean handleMastery(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /lt ability mastery <set/get> ...");
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("set")) {
            if (args.length < 5) {
                sender.sendMessage(ChatColor.RED + "Uso: /lt ability mastery set <player> <ability_id> <level>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                return true;
            }

            String abilityId = args[3];
            int level;
            try {
                level = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Nivel inválido.");
                return true;
            }

            AbilityMasteryManager.setLevel(target, abilityId, level);
            sender.sendMessage(ChatColor.GREEN + "Maestría establecida.");

        } else if (action.equals("get")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Uso: /lt ability mastery get <player> <ability_id>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                return true;
            }

            String abilityId = args[3];
            int level = AbilityMasteryManager.getLevel(target, abilityId);
            int xp = AbilityMasteryManager.getXP(target, abilityId);
            int nextLevel = AbilityMasteryManager.getXPForNextLevel(target, abilityId);

            sender.sendMessage(ChatColor.GOLD + "=== Maestría: " + abilityId + " ===");
            sender.sendMessage(ChatColor.YELLOW + "Nivel: " + level);
            sender.sendMessage(ChatColor.YELLOW + "XP: " + xp + " / " + nextLevel);
        }

        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (args.length < 2 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Uso: /lt ability stats <player> [ability_id]");
            return true;
        }

        Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : (Player) sender;

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Estadísticas de Habilidades: " + target.getName() + " ===");

        if (args.length >= 3) {
            String abilityId = args[2];
            showAbilityStats(sender, target, abilityId);
        } else {
            for (Ability ability : AbilityRegistry.getAbilities().values()) {
                int level = AbilityMasteryManager.getLevel(target, ability.getId());
                if (level > 1) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + ability.getName() + " (Nvl " + level + ")");
                }
            }
        }

        return true;
    }

    private void showAbilityStats(CommandSender sender, Player target, String abilityId) {
        int level = AbilityMasteryManager.getLevel(target, abilityId);
        int xp = AbilityMasteryManager.getXP(target, abilityId);
        double cooldownReduc = AbilityMasteryManager.getCooldownReduction(target, abilityId);
        double effectiveness = AbilityMasteryManager.getEffectivenessBonus(target, abilityId);
        boolean awakened = AbilityMasteryManager.isAwakened(target, abilityId);

        sender.sendMessage(ChatColor.GOLD + "=== " + abilityId + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Nivel: " + level + " | XP: " + xp);
        sender.sendMessage(ChatColor.GREEN + "Bonificaciones:");
        sender.sendMessage(ChatColor.AQUA + "  - Reducción CD: " + (int) (cooldownReduc * 100) + "%");
        sender.sendMessage(ChatColor.AQUA + "  - Efectividad: +" + (int) (effectiveness * 100) + "%");
        if (awakened) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "  ★ AWAKENED ★");
        }
    }

    private boolean handleCombo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo jugadores pueden usar esto.");
            return true;
        }

        Player player = (Player) sender;
        AbilityComboManager.ComboEffect effect = AbilityComboManager.getActiveEffect(player);

        if (effect == null) {
            sender.sendMessage(ChatColor.YELLOW + "No tienes combos activos.");
        } else {
            sender.sendMessage(ChatColor.GOLD + "Combo activo: " + ChatColor.YELLOW + effect.name());
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Comandos de Habilidades ===");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability give <player> <id> - Dar habilidad");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability remove <player> <id> - Quitar habilidad");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability list [player] - Listar habilidades");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability cooldown reset <player> - Resetear cooldowns");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability mastery set/get - Gestionar maestría");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability stats <player> - Ver estadísticas");
        sender.sendMessage(ChatColor.YELLOW + "/lt ability combo - Ver combo activo");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "remove", "list", "cooldown", "mastery", "stats", "combo"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove")
                || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("stats"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove"))) {
            completions.addAll(AbilityRegistry.getAbilities().keySet());
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
