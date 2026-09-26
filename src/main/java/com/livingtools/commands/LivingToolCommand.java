package com.livingtools.commands;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LivingToolCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ConfigManager.getMessage("only_players"));
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                sendHelp(player);
                return true;
            }

            String subCommand = args[0].toLowerCase();

            // Check if subcommand is a player name (Shortcut for stats/admin view)
            if (org.bukkit.Bukkit.getPlayer(subCommand) != null) {
                Player target = org.bukkit.Bukkit.getPlayer(subCommand);
                if (player.hasPermission("livingtools.admin") || player.getName().equalsIgnoreCase(subCommand)) {
                    // Open stats for target
                    ItemStack item = target.getInventory().getItemInMainHand();
                    if (LivingTool.isLivingTool(item)) {
                        com.livingtools.gui.ImprovedSkillTreeGUI.open(player, new LivingTool(item));
                        player.sendMessage(ChatColor.GREEN + "Viendo estadísticas de: " + target.getName());
                    } else {
                        player.sendMessage(
                                ChatColor.RED + target.getName() + " no tiene una Herramienta Viviente en la mano.");
                    }
                } else {
                    player.sendMessage(ConfigManager.getMessage("no_permission"));
                }
                return true;
            }

            switch (subCommand) {
                case "admin":
                    return AdminCommand.handle(player, args);
                case "structure":
                    return StructureCommand.handle(player, args);
                case "menu":
                    return handleMenuCommand(player);
                case "stats":
                    return handleStatsCommand(player);
                case "bond":
                    return handleBondCommand(player, args);
                case "accept":
                    return handleAcceptCommand(player);
                case "duel":
                    return handleDuelCommand(player, args);
                case "trade":
                    return handleTradeCommand(player, args);
                case "top":
                    if (!player.hasPermission("livingtools.command.top")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.gui.LeaderboardGUI.open(player);
                    return true;
                case "feed":
                    FeedCommand.execute(player, args);
                    return true;
                case "inspect":
                case "ver":
                    Player inspectTarget = (args.length >= 2)
                            ? org.bukkit.Bukkit.getPlayer(args[1]) : player;
                    if (inspectTarget == null) {
                        player.sendMessage(ChatColor.RED + "Jugador no encontrado: " + args[1]);
                        return true;
                    }
                    com.livingtools.manager.ToolInspectManager.inspect(player, inspectTarget);
                    return true;
                case "challenges":
                case "retos":
                    ItemStack heldChallenges = player.getInventory().getItemInMainHand();
                    if (com.livingtools.data.LivingTool.isLivingTool(heldChallenges)) {
                        com.livingtools.manager.DailyChallengeManager.showChallenges(
                                player, new com.livingtools.data.LivingTool(heldChallenges));
                    } else {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                    }
                    return true;
                case "relic":
                case "reliquia": {
                    ItemStack relicItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(relicItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.data.LivingTool relicTool = new com.livingtools.data.LivingTool(relicItem);
                    long remaining = com.livingtools.manager.RelicFragmentSystem.getRelicRemainingMinutes(relicTool);
                    if (remaining > 0) {
                        player.sendMessage(ChatColor.DARK_PURPLE + "✦ Reliquia Ancestral activa: "
                                + ChatColor.GOLD + remaining + " min restantes.");
                        return true;
                    }
                    com.livingtools.manager.RelicFragmentSystem.tryFuseRelic(player, relicTool);
                    return true;
                }
                case "logros":
                case "achievements": {
                    ItemStack achItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(achItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.data.LivingTool achTool = new com.livingtools.data.LivingTool(achItem);
                    java.util.List<com.livingtools.manager.SecretAchievementManager.SecretAchievement> achs =
                            com.livingtools.manager.SecretAchievementManager.getAchievements(achTool);
                    if (achs.isEmpty()) {
                        player.sendMessage(ChatColor.GRAY + "Aún no has desbloqueado logros secretos.");
                    } else {
                        player.sendMessage(ChatColor.DARK_PURPLE + "✦ Logros secretos (" + achs.size() + "/"
                                + com.livingtools.manager.SecretAchievementManager.SecretAchievement.values().length + "):");
                        for (com.livingtools.manager.SecretAchievementManager.SecretAchievement a : achs) {
                            player.sendMessage(ChatColor.GOLD + "  ✓ " + a.getTitle() + ChatColor.GRAY + " — " + a.getDescription());
                        }
                    }
                    return true;
                }
                case "titulo": {
                    ItemStack titleItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(titleItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.data.LivingTool titleTool = new com.livingtools.data.LivingTool(titleItem);
                    com.livingtools.manager.ToolTitleSystem.ToolTitle t =
                            com.livingtools.manager.ToolTitleSystem.getHighestTitle(titleTool);
                    if (t == null) {
                        player.sendMessage(ChatColor.GRAY + "Aún no tienes ningún título. ¡Sube de nivel!");
                    } else {
                        player.sendMessage(ChatColor.GOLD + "Título actual: " + t.formatted());
                        com.livingtools.manager.ToolTitleSystem.updateTitle(player, titleTool);
                    }
                    return true;
                }
                case "awaken":
                case "despertar": {
                    ItemStack awakenItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(awakenItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.data.LivingTool awakenTool = new com.livingtools.data.LivingTool(awakenItem);
                    if (awakenTool.getData().getLevel() < 100 && awakenTool.getData().getPrestige() < 1) {
                        player.sendMessage(ChatColor.RED + "Tu herramienta debe ser al menos Nivel 100 o Prestigio 1 para despertar.");
                        return true;
                    }
                    long cd = com.livingtools.manager.ToolAwakeningManager.getCooldownRemainingSeconds(player);
                    if (cd > 0) {
                        player.sendMessage(ChatColor.RED + "⏳ Despertar en cooldown: " + ChatColor.YELLOW + cd + "s restantes.");
                        return true;
                    }
                    player.sendMessage(ChatColor.GOLD + "✦ ¡Usa Shift + Click Derecho para activar el Despertar!");
                    return true;
                }
                case "reforge":
                case "reparar": {
                    ItemStack reforgeItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(reforgeItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.gui.ToolReforgeGUI.open(player, new com.livingtools.data.LivingTool(reforgeItem));
                    return true;
                }
                case "talents":
                case "talentos": {
                    ItemStack talentItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(talentItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.gui.TalentTreeGUI.open(player, new com.livingtools.data.LivingTool(talentItem));
                    return true;
                }
                case "trails":
                case "auras": {
                    ItemStack trailItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(trailItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.gui.CosmeticTrailGUI.open(player, new com.livingtools.data.LivingTool(trailItem));
                    return true;
                }
                case "history":
                    return handleHistoryCommand(player);
                case "armor":
                    return handleArmorCommand(player);
                case "prestige":
                    return handlePrestigeCommand(player);
                case "bind":
                    return handleBindCommand(player);
                case "rename":
                    return handleRenameCommand(player, args);
                case "guide":
                    player.getInventory().addItem(com.livingtools.manager.GuideBookManager.getGuideBook());
                    player.sendMessage(ChatColor.GREEN + "Has recibido la guía.");
                    return true;
                case "library":
                    return handleLibraryCommand(player);
                case "recipes":
                    if (!player.hasPermission("livingtools.command.recipes")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.gui.category.MainCategoryGUI.open(player);
                    return true;
                case "forge":
                    com.livingtools.gui.BossForgeGUI.open(player);
                    return true;
                case "help":
                default:
                    sendHelp(player);
                    return true;
            }
        } catch (Throwable e) {
            sender.sendMessage(
                    ChatColor.RED + "CRITICAL ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private boolean handleMenuCommand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            com.livingtools.gui.DashboardGUI.open(player, new LivingTool(item));
        } else {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
        }
        return true;
    }

    private boolean handleStatsCommand(Player player) {
        if (!player.hasPermission("livingtools.command.stats")) {
            player.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            com.livingtools.gui.ImprovedSkillTreeGUI.open(player, new LivingTool(item));
        } else {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
        }
        return true;
    }

    private boolean handleBondCommand(Player player, String[] args) {
        if (!player.hasPermission("livingtools.command.social")) {
            player.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool bond <jugador>");
            return true;
        }
        Player target = org.bukkit.Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ConfigManager.getMessage("player_not_found"));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "No puedes formar un pacto contigo mismo.");
            return true;
        }
        com.livingtools.manager.BondManager.bond(player, target);
        return true;
    }

    private boolean handleAcceptCommand(Player player) {
        com.livingtools.manager.DuelManager.acceptRequest(player);
        return true;
    }

    private boolean handleDuelCommand(Player player, String[] args) {
        if (!player.hasPermission("livingtools.command.social")) {
            player.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("accept")) {
            com.livingtools.manager.DuelManager.acceptRequest(player);
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool duel <jugador> <apuesta_xp>");
            return true;
        }

        Player target = org.bukkit.Bukkit.getPlayer(args[1]);
        long wager;
        try {
            wager = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Apuesta inválida.");
            return true;
        }

        if (target == null) {
            player.sendMessage(ConfigManager.getMessage("player_not_found"));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "No puedes pelear contigo mismo.");
            return true;
        }
        if (wager <= 0) {
            player.sendMessage(ChatColor.RED + "La apuesta debe ser mayor a 0.");
            return true;
        }

        com.livingtools.manager.DuelManager.sendRequest(player, target, wager);
        return true;
    }

    private boolean handleHistoryCommand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("not_living_tool"));
            return true;
        }
        LivingTool tool = new LivingTool(item);
        com.livingtools.gui.HistoryGUI.open(player, tool);
        return true;
    }

    private boolean handleArmorCommand(Player player) {
        boolean found = false;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (com.livingtools.data.LivingArmor.isLivingArmor(item)) {
                com.livingtools.gui.ArmorGUI.open(player,
                        new com.livingtools.data.LivingArmor(item));
                found = true;
                break;
            }
        }
        if (!found) {
            player.sendMessage(ChatColor.RED + "No tienes ninguna Armadura Viviente equipada.");
        }
        return true;
    }

    private boolean handlePrestigeCommand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("not_living_tool"));
            return true;
        }
        LivingTool tool = new LivingTool(item);
        if (tool.getData().getLevel() < 100) {
            player.sendMessage(ConfigManager.getMessage("insufficient_level"));
            return true;
        }
        tool.getData().setLevel(1);
        tool.getData().setXP(0);
        tool.getData().setPrestige(tool.getData().getPrestige() + 1);
        tool.updateLore();
        player.sendMessage(ChatColor.AQUA + "¡PRESTIGIO ALCANZADO! Tu herramienta ha renacido más fuerte.");
        com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
        return true;
    }

    private boolean handleBindCommand(Player player) {
        if (!player.hasPermission("livingtools.bind")) {
            player.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Debes sostener una herramienta.");
            return true;
        }
        if (LivingTool.isLivingTool(item)) {
            player.sendMessage(ChatColor.RED + "Esta herramienta ya tiene vida.");
            return true;
        }
        String type = item.getType().toString();
        if (!type.endsWith("_PICKAXE") && !type.endsWith("_SWORD") && !type.endsWith("_AXE")
                && !type.endsWith("_SHOVEL") && !type.endsWith("_HOE")
                && !type.endsWith("_HELMET") && !type.endsWith("_CHESTPLATE")
                && !type.endsWith("_LEGGINGS") && !type.endsWith("_BOOTS")) {
            player.sendMessage(ChatColor.RED + "Este ítem no puede cobrar vida.");
            return true;
        }
        LivingTool tool = new LivingTool(item);
        tool.getData().setOwnerName(player.getName());
        tool.getData().setCreationDate(System.currentTimeMillis());
        tool.getData().initialize();
        tool.updateLore();
        player.sendMessage(ChatColor.GREEN + "¡Tu herramienta ha cobrado vida!");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 15, 0.5,
                0.5, 0.5);
        com.livingtools.manager.GuideManager.triggerStep(player, tool,
                com.livingtools.manager.GuideManager.TutorialStep.INTRO);
        return true;
    }

    private boolean handleRenameCommand(Player player, String[] args) {
        if (!player.hasPermission("livingtools.rename")) {
            player.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool rename <nombre>");
            return true;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            nameBuilder.append(args[i]).append(" ");
        }
        String newName = nameBuilder.toString().trim();

        if (LivingTool.isLivingTool(handItem)) {
            LivingTool toolToRename = new LivingTool(handItem);
            toolToRename.getData().setCustomName(newName);
            toolToRename.updateLore();
            player.sendMessage(ChatColor.GREEN + "¡Herramienta renombrada a: "
                    + ChatColor.translateAlternateColorCodes('&', newName) + "!");
        } else if (com.livingtools.data.LivingArmor.isLivingArmor(handItem)) {
            org.bukkit.inventory.meta.ItemMeta meta = handItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', newName));
            handItem.setItemMeta(meta);
            new com.livingtools.data.LivingArmor(handItem).updateLore();
            player.sendMessage(ChatColor.GREEN + "¡Armadura renombrada!");
        } else {
            player.sendMessage(ConfigManager.getMessage("not_living_tool"));
        }
        return true;
    }

    private boolean handleLibraryCommand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(item)) {
            com.livingtools.gui.LibraryGUI.open(player, new LivingTool(item));
        } else {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ConfigManager.getMessage("command_help"));
        player.sendMessage(ChatColor.GOLD + "=== Comandos de Living Tools ===");

        player.sendMessage(ChatColor.AQUA + "-- General --");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool menu" + ChatColor.WHITE + " - Abrir el Dashboard principal.");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool stats [jugador]" + ChatColor.WHITE + " - Ver árbol de talentos.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool top" + ChatColor.WHITE + " - Ver el ranking.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool guide" + ChatColor.WHITE + " - Obtener libro guía.");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool history" + ChatColor.WHITE + " - Ver historia de la herramienta.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool armor" + ChatColor.WHITE + " - Ver armadura viviente.");
        player.sendMessage(ChatColor.YELLOW + "/living recipes" + ChatColor.WHITE + " - Menú unificado de recetas.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool recipes" + ChatColor.WHITE + " - Menú unificado de recetas.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool forge" + ChatColor.WHITE
                + " - Abrir Forja de Jefes (crafteo en cruz).");
        player.sendMessage(ChatColor.YELLOW + "/livingtool structure assembly" + ChatColor.WHITE
                + " - Guía Mesa de Ensamblaje (3×3).");
        player.sendMessage(ChatColor.YELLOW + "/livingtool structure bossforge" + ChatColor.WHITE
                + " - Guía Forja de Jefes (cruz).");
        player.sendMessage(ChatColor.YELLOW + "/livingtool structure list" + ChatColor.WHITE
                + " - Ver materiales de todas las estructuras.");

        player.sendMessage(ChatColor.AQUA + "-- Gestión --");
        player.sendMessage(ChatColor.YELLOW + "/livingtool bind" + ChatColor.WHITE + " - Dar vida al ítem en mano.");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool rename <nombre>" + ChatColor.WHITE + " - Renombrar herramienta.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool feed" + ChatColor.WHITE
                + " - Alimentar herramienta (sacrificar ítems).");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool prestige" + ChatColor.WHITE + " - Renacer herramienta (Nivel 100+).");

        player.sendMessage(ChatColor.AQUA + "-- Social & Estructuras --");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool bond <jugador>" + ChatColor.WHITE + " - Formar Pacto de Sangre.");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool duel <jugador> <xp>" + ChatColor.WHITE + " - Desafiar a duelo.");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool structure <altar|cursedforge|runeforge|bossforge|assembly>" + ChatColor.WHITE
                        + " - Ver guías de estructuras.");
        player.sendMessage(
                ChatColor.YELLOW + "/livingtool trade <jugador>" + ChatColor.WHITE + " - Comerciar ítems.");

        if (player.hasPermission("livingtools.admin")) {
            player.sendMessage(ChatColor.RED + "=== Admin ===");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin give <material> [lvl]");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin xp <amount>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin multiplier <amount>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin bloodmoon <start|stop>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin unlock <ability>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin forcetrial <type>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin setpersonality <type>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin giverune <type> <tier>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin givegeode [amount]");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin giveartifact <type>");
            player.sendMessage(ChatColor.GRAY + "/livingtool admin reload");
        }
    }

    private boolean handleTradeCommand(Player player, String[] args) {
        if (!player.hasPermission("livingtools.command.social")) {
            player.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("accept")) {
            com.livingtools.manager.TradeManager.acceptRequest(player);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool trade <jugador> o /livingtool trade accept");
            return true;
        }

        Player target = org.bukkit.Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ConfigManager.getMessage("player_not_found"));
            return true;
        }

        com.livingtools.manager.TradeManager.sendRequest(player, target);
        return true;
    }
}
