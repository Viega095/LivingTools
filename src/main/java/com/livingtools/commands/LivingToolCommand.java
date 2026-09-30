package com.livingtools.commands;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LivingToolCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length > 0) {
                String sub = args[0].toLowerCase();
                if (sub.equals("reload") || sub.equals("recargar")) {
                    if (!sender.hasPermission("livingtools.admin")) {
                        sender.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.manager.AutoUpdateManager.getInstance().performHotReload(sender);
                    return true;
                }
                if (sub.equals("update") || sub.equals("actualizar")) {
                    if (!sender.hasPermission("livingtools.admin")) {
                        sender.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    return handleUpdateCommand(sender, args);
                }
                if (sub.equals("admin")) {
                    return AdminCommand.handle(sender, args);
                }
                if (sub.equals("checkrecipes") || sub.equals("auditrecipes")) {
                    com.livingtools.manager.RecipeValidationManager.sendAuditReport(sender);
                    return true;
                }
            }

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
                case "companion":
                case "espiritu":
                case "guardian": {
                    ItemStack compItem = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(compItem)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.data.LivingTool compTool = new com.livingtools.data.LivingTool(compItem);
                    if (compTool.getData().getLevel() < 50 && compTool.getData().getPrestige() < 1) {
                        player.sendMessage(ChatColor.RED + "Tu herramienta debe ser al menos Nivel 50 o Prestigio 1 para manifestar un Espíritu Guardián.");
                        return true;
                    }
                    boolean enabled = com.livingtools.manager.ToolGuardianCompanion.toggleCompanion(player);
                    if (enabled) {
                        player.sendMessage(ChatColor.AQUA + "✦ ¡Espíritu Guardián invocado y acompañándote! (Radar de diamantes y magnetismo activo)");
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.4f);
                    } else {
                        player.sendMessage(ChatColor.GRAY + "✧ Espíritu Guardián retirado a la herramienta.");
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 0.8f);
                    }
                    return true;
                }
                case "incursion":
                case "raid": {
                    if (!player.hasPermission("livingtools.command.incursion")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.manager.BossRaidDungeonManager.startIncursion(player);
                    return true;
                }
                case "fusion":
                case "fusionar": {
                    com.livingtools.gui.SoulFusionGUI.open(player);
                    return true;
                }
                case "rift":
                case "abismo": {
                    if (args.length > 1 && (args[1].equalsIgnoreCase("leave") || args[1].equalsIgnoreCase("salir"))) {
                        com.livingtools.manager.AbyssalRiftEngine.leaveRift(player);
                        return true;
                    }
                    if (args.length > 1 && args[1].equalsIgnoreCase("start")) {
                        com.livingtools.manager.AbyssalRiftEngine.startRift(player);
                    } else {
                        com.livingtools.gui.AbyssalRiftGUI.open(player);
                    }
                    return true;
                }
                case "realm":
                case "reino": {
                    if (args.length > 1 && (args[1].equalsIgnoreCase("leave") || args[1].equalsIgnoreCase("salir"))) {
                        com.livingtools.manager.LivingRealmManager.returnFromRealm(player);
                        return true;
                    }
                    if (args.length > 1 && args[1].equalsIgnoreCase("rift")) {
                        com.livingtools.manager.AbyssalRiftEngine.startRift(player);
                        return true;
                    }
                    if (args.length > 1 && args[1].equalsIgnoreCase("sanctuary")) {
                        com.livingtools.manager.LivingRealmManager.teleportToRealm(player, com.livingtools.manager.LivingRealmManager.getSanctuarySpawn());
                        return true;
                    }
                    if (args.length > 1 && args[1].equalsIgnoreCase("genesis")) {
                        if (!player.hasPermission("livingtools.admin")) {
                            player.sendMessage(ConfigManager.getMessage("no_permission"));
                            return true;
                        }
                        com.livingtools.manager.LivingRealmManager.teleportToRealm(player, com.livingtools.manager.LivingRealmManager.getGenesisArenaSpawn());
                        com.livingtools.entities.GenesisAvatarBoss.spawn(com.livingtools.manager.LivingRealmManager.getGenesisArenaSpawn());
                        return true;
                    }
                    if (args.length > 1 && (args[1].equalsIgnoreCase("forge") || args[1].equalsIgnoreCase("forja"))) {
                        com.livingtools.manager.LivingRealmManager.teleportToRealm(player, com.livingtools.manager.LivingRealmManager.getForgeTempleSpawn());
                        return true;
                    }
                    if (!player.hasPermission("livingtools.command.realm") && !player.hasPermission("livingtools.admin")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.manager.LivingRealmManager.teleportToRealm(player, com.livingtools.manager.LivingRealmManager.getRiftArenaSpawn());
                    return true;
                }
                case "sockets":
                case "gemas": {
                    ItemStack heldSocket = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(heldSocket)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.gui.GemSocketGUI.open(player, new com.livingtools.data.LivingTool(heldSocket));
                    return true;
                }
                case "bounties":
                case "contratos": {
                    com.livingtools.gui.BountyContractGUI.open(player);
                    return true;
                }
                case "pedestal":
                case "museum": {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GOLD + "🏛 " + ChatColor.BOLD + "Pedestal de Almas (Exhibición 3D):");
                    player.sendMessage(ChatColor.GRAY + "Coloca un bloque de " + ChatColor.YELLOW + "Lodestone" + ChatColor.GRAY + " o " + ChatColor.YELLOW + "Marco de Portal del End" + ChatColor.GRAY + " y haz");
                    player.sendMessage(ChatColor.AQUA + "Shift + Click Derecho" + ChatColor.GRAY + " sosteniendo tu herramienta para exponerla.");
                    player.sendMessage(ChatColor.GREEN + "Mientras repose en el pedestal, acumulará " + ChatColor.GOLD + "XP de Descanso" + ChatColor.GREEN + " continuamente.");
                    player.sendMessage("");
                    return true;
                }
                case "rhythmicforge":
                case "forjaritmo":
                case "ritmo": {
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("build")) {
                            if (!player.hasPermission("livingtools.admin")) {
                                player.sendMessage(ConfigManager.getMessage("no_permission"));
                                return true;
                            }
                            com.livingtools.manager.RhythmicForgeManager.buildStructure(player.getLocation());
                            player.sendMessage(ChatColor.GREEN + "✦ Estructura de la Forja Rítmica construida con éxito.");
                            return true;
                        }
                        if (args[1].equalsIgnoreCase("guide") || args[1].equalsIgnoreCase("guia")) {
                            com.livingtools.visuals.RhythmicForgeVisualizer.toggleGuide(player);
                            return true;
                        }
                        if (args[1].equalsIgnoreCase("core") || args[1].equalsIgnoreCase("givecore")) {
                            if (!player.hasPermission("livingtools.admin")) {
                                player.sendMessage(ConfigManager.getMessage("no_permission"));
                                return true;
                            }
                            player.getInventory().addItem(com.livingtools.manager.RhythmicForgeManager.createSoulForgeCore());
                            player.sendMessage(ChatColor.AQUA + "✦ Has recibido el Núcleo de la Forja Rítmica.");
                            return true;
                        }
                    }

                    ItemStack heldForge = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(heldForge)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.gui.RhythmicForgeGUI.open(player, new com.livingtools.data.LivingTool(heldForge));
                    return true;
                }
                case "compass":
                case "brujula": {
                    if (!player.hasPermission("livingtools.command.compass")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    player.getInventory().addItem(com.livingtools.manager.SoulCompassManager.createSoulCompass());
                    player.sendMessage(ChatColor.AQUA + "🧭 Has recibido una Brújula de Almas para rastrear meteoritos celestiales.");
                    return true;
                }
                case "ascend":
                case "ascension": {
                    ItemStack heldAscend = player.getInventory().getItemInMainHand();
                    if (!com.livingtools.data.LivingTool.isLivingTool(heldAscend)) {
                        player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                        return true;
                    }
                    com.livingtools.gui.AscensionGUI.open(player, new com.livingtools.data.LivingTool(heldAscend));
                    return true;
                }
                case "guild":
                case "clan": {
                    return handleGuildCommand(player, args);
                }
                case "tarot":
                case "cartas": {
                    com.livingtools.gui.SoulTarotGUI.open(player);
                    return true;
                }
                case "genesis": {
                    if (!player.hasPermission("livingtools.admin")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.entities.GenesisAvatarBoss.spawn(player.getLocation());
                    player.sendMessage(ChatColor.GOLD + "✦ ¡Invocando al Avatar del Génesis!");
                    return true;
                }
                case "test":
                case "testguide":
                case "pruebas": {
                    if (!player.hasPermission("livingtools.admin")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.gui.AdminTestingGUI.open(player);
                    return true;
                }
                case "checkrecipes":
                case "auditrecipes": {
                    com.livingtools.manager.RecipeValidationManager.sendAuditReport(player);
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
                case "bestiary":
                case "bestiario": {
                    com.livingtools.gui.BestiaryGUI.open(player, 0, com.livingtools.gui.BestiaryGUI.BestiaryCategory.ALL);
                    return true;
                }
                case "guide":
                case "guia":
                case "libro":
                    com.livingtools.manager.GuideBookManager.giveBook(player);
                    return true;
                case "library":
                    return handleLibraryCommand(player);
                case "recipes":
                case "recetas":
                    if (!player.hasPermission("livingtools.command.recipes")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.gui.category.MainCategoryGUI.open(player);
                    return true;
                case "forge":
                    com.livingtools.gui.BossForgeGUI.open(player);
                    return true;
                case "reload":
                case "recargar":
                    if (!player.hasPermission("livingtools.admin")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    com.livingtools.manager.AutoUpdateManager.getInstance().performHotReload(player);
                    return true;
                case "update":
                case "actualizar":
                    if (!player.hasPermission("livingtools.admin")) {
                        player.sendMessage(ConfigManager.getMessage("no_permission"));
                        return true;
                    }
                    return handleUpdateCommand(player, args);
                case "help":
                case "ayuda":
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
        player.sendMessage(ChatColor.YELLOW + "/livingtool recipes" + ChatColor.WHITE + " - Menú interactivo de recetas.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool bestiary" + ChatColor.WHITE + " - Bestiario de jefes y esbirros.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool forge" + ChatColor.WHITE
                + " - Abrir Forja de Jefes (crafteo en cruz).");
        player.sendMessage(ChatColor.YELLOW + "/livingtool ascend" + ChatColor.WHITE + " - Altar de Ascensión Divina.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool tarot" + ChatColor.WHITE + " - Tarot Arcano del Destino.");
        player.sendMessage(ChatColor.YELLOW + "/livingtool guild" + ChatColor.WHITE + " - Hermandades y Bóveda.");
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
            player.sendMessage(ChatColor.RED + "=== Admin & Sistema ===");
            player.sendMessage(ChatColor.YELLOW + "/livingtool update [check|install]" + ChatColor.WHITE + " - Auto-Update en vivo.");
            player.sendMessage(ChatColor.YELLOW + "/livingtool reload" + ChatColor.WHITE + " - Hot-Reload y recarga en vivo.");
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

    public static boolean handleUpdateCommand(CommandSender sender, String[] args) {
        com.livingtools.manager.AutoUpdateManager updateManager = com.livingtools.manager.AutoUpdateManager.getInstance();
        if (updateManager == null) {
            sender.sendMessage(ChatColor.RED + "Error: AutoUpdateManager no está inicializado.");
            return true;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("status") || args[1].equalsIgnoreCase("info")) {
            String current = updateManager.getCurrentVersion();
            String latest = updateManager.getLatestVersion();
            boolean hasUpdate = updateManager.isUpdateAvailable();
            boolean pending = updateManager.isUpdatePendingReload();

            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
            sender.sendMessage(ChatColor.YELLOW + "  🔄 " + ChatColor.BOLD + "GESTOR DE ACTUALIZACIONES LIVING TOOLS");
            sender.sendMessage(ChatColor.WHITE + "  Versión instalada: " + ChatColor.YELLOW + "v" + current);
            sender.sendMessage(ChatColor.WHITE + "  Última en GitHub:  " + (hasUpdate ? ChatColor.GREEN + "v" + latest + ChatColor.RED + " [¡NUEVA!]" : ChatColor.GREEN + "v" + latest + " (Al día)"));
            if (pending) {
                sender.sendMessage(ChatColor.AQUA + "  ✨ Estado: " + ChatColor.GREEN + "Paquete descargado listo para aplicar.");
                sender.sendMessage(ChatColor.YELLOW + "  ► Usa " + ChatColor.GOLD + "/livingtool reload" + ChatColor.YELLOW + " para activarla en vivo.");
            } else if (hasUpdate) {
                sender.sendMessage(ChatColor.AQUA + "  ► Usa " + ChatColor.YELLOW + "/livingtool update install" + ChatColor.AQUA + " para instalar en vivo sin reiniciar.");
            }
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "  Comandos disponibles:");
            sender.sendMessage(ChatColor.YELLOW + "  • /livingtool update check      " + ChatColor.GRAY + "➔ Comprobar si hay versión nueva.");
            sender.sendMessage(ChatColor.YELLOW + "  • /livingtool update install    " + ChatColor.GRAY + "➔ Descargar e instalar en vivo.");
            sender.sendMessage(ChatColor.YELLOW + "  • /livingtool update changelog  " + ChatColor.GRAY + "➔ Ver notas del último parche.");
            sender.sendMessage(ChatColor.YELLOW + "  • /livingtool reload            " + ChatColor.GRAY + "➔ Hot-reload completo y activar update.");
            sender.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
            sender.sendMessage("");
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "check":
            case "comprobar":
                updateManager.checkForUpdates(sender, true);
                return true;
            case "install":
            case "download":
            case "descargar":
            case "now":
            case "instalar":
            case "force":
            case "forzar":
                updateManager.downloadAndInstall(sender, true);
                return true;
            case "changelog":
            case "notes":
            case "notas": {
                String notes = updateManager.getReleaseNotes();
                if (notes == null || notes.trim().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "[LivingTools] No hay notas de parche descargadas. Comprobando...");
                    updateManager.checkForUpdates(sender, false);
                } else {
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GOLD + "=== NOTAS DE PARCHE (v" + updateManager.getLatestVersion() + ") ===");
                    for (String line : notes.split("\n")) {
                        sender.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + line);
                    }
                    sender.sendMessage("");
                }
                return true;
            }
            default:
                sender.sendMessage(ChatColor.RED + "Uso: /livingtool update [check|install|changelog|status]");
                return true;
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

    private boolean handleGuildCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_AQUA + "🏰 " + ChatColor.BOLD + "Comandos de Hermandad de Almas:");
            player.sendMessage(ChatColor.YELLOW + "/lt guild create <nombre>" + ChatColor.GRAY + " - Fundar una hermandad");
            player.sendMessage(ChatColor.YELLOW + "/lt guild invite <jugador>" + ChatColor.GRAY + " - Invitar a un miembro");
            player.sendMessage(ChatColor.YELLOW + "/lt guild accept" + ChatColor.GRAY + " - Aceptar invitación");
            player.sendMessage(ChatColor.YELLOW + "/lt guild vault" + ChatColor.GRAY + " - Abrir la Bóveda de Clan");
            player.sendMessage(ChatColor.YELLOW + "/lt guild info" + ChatColor.GRAY + " - Ver información y nivel");
            player.sendMessage(ChatColor.YELLOW + "/lt guild leave" + ChatColor.GRAY + " - Salir de la hermandad");
            player.sendMessage(ChatColor.YELLOW + "/lt guild disband" + ChatColor.GRAY + " - Disolver hermandad (Líder)");
            player.sendMessage("");
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "create": {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /lt guild create <nombre>");
                    return true;
                }
                com.livingtools.manager.SoulGuildManager.createGuild(player, args[2]);
                return true;
            }
            case "invite": {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /lt guild invite <jugador>");
                    return true;
                }
                Player target = org.bukkit.Bukkit.getPlayer(args[2]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Jugador no encontrado o desconectado.");
                    return true;
                }
                com.livingtools.manager.SoulGuildManager.inviteMember(player, target);
                return true;
            }
            case "accept": {
                com.livingtools.manager.SoulGuildManager.acceptInvite(player);
                return true;
            }
            case "vault":
            case "boveda": {
                com.livingtools.gui.SoulGuildVaultGUI.open(player);
                return true;
            }
            case "leave":
            case "salir": {
                com.livingtools.manager.SoulGuildManager.leaveGuild(player);
                return true;
            }
            case "disband":
            case "disolver": {
                com.livingtools.manager.SoulGuildManager.disbandGuild(player);
                return true;
            }
            case "info": {
                com.livingtools.manager.SoulGuildManager.SoulGuild guild = com.livingtools.manager.SoulGuildManager.getGuild(player.getUniqueId());
                if (guild == null) {
                    player.sendMessage(ChatColor.RED + "No perteneces a ninguna Hermandad de Almas.");
                    return true;
                }
                player.sendMessage("");
                player.sendMessage(ChatColor.DARK_AQUA + "🏰 " + ChatColor.BOLD + "Hermandad: " + ChatColor.WHITE + guild.getName());
                Player leaderP = org.bukkit.Bukkit.getPlayer(guild.getLeader());
                String leaderName = leaderP != null ? leaderP.getName() : guild.getLeader().toString().substring(0, 8);
                player.sendMessage(ChatColor.GRAY + "Líder: " + ChatColor.GOLD + leaderName);
                player.sendMessage(ChatColor.GRAY + "Nivel: " + ChatColor.YELLOW + guild.getLevel() + "/10" + ChatColor.GRAY + " (" + guild.getGuildXp() + " XP)");
                player.sendMessage(ChatColor.GRAY + "Miembros (" + guild.getMembers().size() + "/" + com.livingtools.manager.SoulGuildManager.MAX_MEMBERS + "):");
                for (java.util.UUID m : guild.getMembers()) {
                    Player mp = org.bukkit.Bukkit.getPlayer(m);
                    String mName = mp != null ? mp.getName() : m.toString().substring(0, 8);
                    boolean isOnline = mp != null && mp.isOnline();
                    player.sendMessage(ChatColor.GRAY + " - " + (isOnline ? ChatColor.GREEN + "● " : ChatColor.DARK_GRAY + "○ ") + ChatColor.WHITE + mName);
                }
                player.sendMessage("");
                return true;
            }
            default:
                player.sendMessage(ChatColor.RED + "Subcomando de hermandad desconocido. Usa /lt guild");
                return true;
        }
    }
}
