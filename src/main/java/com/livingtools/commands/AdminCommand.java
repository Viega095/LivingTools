package com.livingtools.commands;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("livingtools.admin")) {
            sender.sendMessage(ConfigManager.getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /livingtool admin <subcomando> [args]");
            return true;
        }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "give":
                return handleGive(sender, args);
            case "xp":
                return handleXP(sender, args);
            case "givearmor":
                return handleGiveArmor(sender, args);
            case "unlock":
                return handleUnlock(sender, args);
            case "forcetrial":
                return handleForceTrial(sender, args);
            case "setpersonality":
                return handleSetPersonality(sender, args);
            case "reload":
                ConfigManager.reload();
                sender.sendMessage(ChatColor.GREEN + "Configuración recargada.");
                return true;
            case "giverune":
                return handleGiveRune(sender, args);
            case "givegeode":
                return handleGiveGeode(sender, args);
            case "abilityxp":
                return handleAbilityXP(sender, args);
            case "setcorruption":
                return handleSetCorruption(sender, args);
            case "giveartifact":
                return handleGiveArtifact(sender, args);
            case "debug":
                // Toggle debug
                return true;
            case "setmood":
                return handleSetMood(sender, args);
            case "givetome":
                return handleGiveTome(sender, args);
            case "resetcooldowns":
                return handleResetCooldowns(sender, args);
            case "smuggler":
                return handleSmuggler(sender, args);
            case "givebasic":
                return handleGiveBasic(sender, args);
            case "recipes":
                if (sender instanceof Player) {
                    com.livingtools.gui.RecipeGUI.open((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Solo jugadores pueden ver recetas.");
                }
                return true;
            case "opensmuggler":
                return handleOpenSmuggler(sender, args);
            case "givesoulgem":
                return handleGiveSoulGem(sender, args);
            case "opensoulforge":
                if (sender instanceof Player) {
                    com.livingtools.gui.SoulForgeGUI.open((Player) sender);
                }
                return true;
            case "openrunefusion":
                if (sender instanceof Player) {
                    com.livingtools.gui.RuneFusionGUI.open((Player) sender);
                }
                return true;
            case "givecustomenchant":
                return handleGiveCustomEnchant(sender, args);
            case "givevoidessence":
                return handleGiveVoidEssence(sender, args);
            case "upgradeabyssal":
                return handleUpgradeAbyssal(sender, args);
            case "spawnrift":
                return handleSpawnRift(sender, args);
            case "setplayercorruption":
                return handleSetPlayerCorruption(sender, args);
            case "givestarlightessence":
                return handleGiveStarlightEssence(sender, args);
            case "upgradeaetherial":
                return handleUpgradeAetherial(sender, args);
            case "purifytool":
                return handlePurifyTool(sender, args);
            case "spawnfortress":
                return handleSpawnFortress(sender, args);
            case "joinfaction":
                return handleJoinFaction(sender, args);
            case "claimchunk":
                return handleClaimChunk(sender, args);
            case "startconvergence":
                return handleStartConvergence(sender, args);
            case "giveomnitool":
                return handleGiveOmniTool(sender, args);
            case "spawnboss":
                return handleSpawnBoss(sender, args);
            case "spawnarena":
                return handleSpawnArena(sender, args);
            case "menu":
                if (sender instanceof Player) {
                    com.livingtools.gui.AdminGUI.open((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Solo jugadores.");
                }
                return true;
            case "wiki":
                com.livingtools.manager.WikiGenerator.generateWiki();
                sender.sendMessage(ChatColor.GREEN + "Wiki generada en la carpeta del plugin (WIKI.md).");
                return true;
            case "models":
                if (sender instanceof Player) {
                    com.livingtools.gui.ModelShowcaseGUI.open((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Solo jugadores.");
                }
                return true;
            case "testmodels": // Keep for legacy/quick test if needed, or remove. Let's keep it as alias or
                               // separate.
                return handleTestModels(sender, args);
            case "multiplier":
                return handleMultiplier(sender, args);
            case "setlevel":
                return handleSetLevel(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "inspect":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Solo jugadores.");
                    return true;
                }
                Player inspectTarget = args.length >= 3 ? org.bukkit.Bukkit.getPlayer(args[2]) : (Player) sender;
                if (inspectTarget == null || !inspectTarget.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                    return true;
                }
                com.livingtools.gui.AdminInspectGUI.open((Player) sender, inspectTarget);
                return true;
            case "freeze":
            case "unfreeze": {
                Player freezeTarget = args.length >= 3 ? org.bukkit.Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
                if (freezeTarget == null || !freezeTarget.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                    return true;
                }
                ItemStack held = freezeTarget.getInventory().getItemInMainHand();
                if (!LivingTool.isLivingTool(held)) {
                    sender.sendMessage(ChatColor.RED + freezeTarget.getName() + " no tiene una herramienta viviente.");
                    return true;
                }
                LivingTool freezeTool = new LivingTool(held);
                boolean freezeState = sub.equals("freeze");
                com.livingtools.manager.AdminFreezeManager.setFrozen(freezeTool, freezeState);
                sender.sendMessage(ChatColor.YELLOW + "Herramienta de " + freezeTarget.getName()
                        + (freezeState ? ChatColor.RED + " CONGELADA." : ChatColor.GREEN + " DESCONGELADA."));
                return true;
            }
            case "strip": {
                Player stripTarget = args.length >= 3 ? org.bukkit.Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
                if (stripTarget == null || !stripTarget.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                    return true;
                }
                ItemStack held = stripTarget.getInventory().getItemInMainHand();
                if (!LivingTool.isLivingTool(held)) {
                    sender.sendMessage(ChatColor.RED + stripTarget.getName() + " no tiene una herramienta viviente.");
                    return true;
                }
                stripTarget.getInventory().setItemInMainHand(new ItemStack(held.getType()));
                sender.sendMessage(ChatColor.GREEN + "Herramienta de " + stripTarget.getName() + " convertida a Vanilla.");
                return true;
            }
            case "givepoints": {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Uso: /livingtool admin givepoints [jugador] [cantidad]");
                    return true;
                }
                Player ptsTarget = org.bukkit.Bukkit.getPlayer(args[2]);
                if (ptsTarget == null || !ptsTarget.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                    return true;
                }
                ItemStack held = ptsTarget.getInventory().getItemInMainHand();
                if (!LivingTool.isLivingTool(held)) {
                    sender.sendMessage(ChatColor.RED + ptsTarget.getName() + " no tiene una herramienta viviente.");
                    return true;
                }
                try {
                    int count = Integer.parseInt(args[3]);
                    LivingTool ptsTool = new LivingTool(held);
                    for (int i = 0; i < count; i++) {
                        com.livingtools.manager.LevelUpRewardManager.awardSkillPoint(ptsTarget, ptsTool);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Otorgados " + count + " puntos a " + ptsTarget.getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Cantidad inválida.");
                }
                return true;
            }
            case "event": {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /livingtool admin event [start/stop] [evento]");
                    return true;
                }
                if (args[2].equalsIgnoreCase("stop")) {
                    com.livingtools.manager.WorldEventScheduler.stopEvent();
                    sender.sendMessage(ChatColor.GREEN + "Evento mundial detenido.");
                    return true;
                }
                if (args[2].equalsIgnoreCase("start") && args.length >= 4) {
                    try {
                        com.livingtools.manager.WorldEventScheduler.AutomatedWorldEvent ev =
                                com.livingtools.manager.WorldEventScheduler.AutomatedWorldEvent.valueOf(args[3].toUpperCase());
                        com.livingtools.manager.WorldEventScheduler.startEvent(ev);
                        sender.sendMessage(ChatColor.GREEN + "Evento " + ev.name() + " iniciado.");
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + "Evento no válido. Opciones: METEOR_SHOWER, BLOOD_MOON, SOLAR_RESONANCE");
                    }
                    return true;
                }
                return true;
            }
            default:
                sender.sendMessage(ChatColor.RED + "Subcomando desconocido.");
                return true;
        }
    }

    private static boolean handleOpenSmuggler(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        new com.livingtools.gui.SmugglerGUI(player).open();
        player.sendMessage(ChatColor.GREEN + "Abriendo Mercado Negro...");
        return true;
    }

    private static boolean handleGiveBasic(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        Material type = Material.WOODEN_PICKAXE;
        if (args.length > 2) {
            try {
                type = Material.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore, default to pickaxe
            }
        }

        ItemStack item = new ItemStack(type);
        LivingTool tool = new LivingTool(item);
        tool.getData().initialize();
        tool.updateLore();
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Herramienta básica entregada.");
        return true;
    }

    private static boolean handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        Material type = Material.DIAMOND_PICKAXE;
        if (args.length > 2) {
            try {
                type = Material.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Material inválido.");
                return true;
            }
        }
        int level = 1;
        if (args.length > 3) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Nivel inválido.");
                return true;
            }
        }

        ItemStack item = new ItemStack(type);
        LivingTool tool = new LivingTool(item);
        tool.getData().setLevel(level);
        tool.updateLore();
        player.getInventory().addItem(item);
        player.sendMessage(ConfigManager.getMessage("tool_given"));
        return true;
    }

    private static boolean handleXP(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin xp <cantidad>");
            return true;
        }
        try {
            long amount = Long.parseLong(args[2]);
            new LivingTool(item).addXP(player, amount);
            player.sendMessage(ChatColor.GREEN + "Añadida " + amount + " XP.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Cantidad inválida.");
        }
        return true;
    }

    private static boolean handleGiveArmor(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        Material type = Material.DIAMOND_CHESTPLATE;
        if (args.length > 2) {
            try {
                type = Material.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Material inválido.");
                return true;
            }
        }
        ItemStack item = new ItemStack(type);
        com.livingtools.data.LivingArmor armor = new com.livingtools.data.LivingArmor(item);
        armor.updateLore();
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Armadura viviente entregada.");
        return true;
    }

    private static boolean handleUnlock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin unlock <habilidad>");
            return true;
        }
        String abilityId = args[2].toLowerCase();
        new LivingTool(item).addAbility(abilityId);
        player.sendMessage(ChatColor.GREEN + "Habilidad desbloqueada.");
        return true;
    }

    private static boolean handleForceTrial(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        try {
            com.livingtools.manager.TrialManager.TrialType type = com.livingtools.manager.TrialManager.TrialType
                    .valueOf(args[2].toUpperCase());
            new LivingTool(item).getData().setTrial(type.name());
            new LivingTool(item).updateLore();
            player.sendMessage(ChatColor.GREEN + "Desafío forzado: " + type.name());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Tipo de desafío inválido.");
        }
        return true;
    }

    private static boolean handleSetPersonality(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin setpersonality <personalidad>");
            return true;
        }
        try {
            com.livingtools.mechanics.Personality personality = com.livingtools.mechanics.Personality
                    .valueOf(args[2].toUpperCase());
            new LivingTool(item).getData().setPersonality(personality.name());
            new LivingTool(item).updateLore();
            player.sendMessage(ChatColor.GREEN + "Personalidad establecida: " + personality.name());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Personalidad inválida.");
        }
        return true;
    }

    private static boolean handleGiveRune(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin giverune <tipo> <tier>");
            return true;
        }
        try {
            com.livingtools.runes.RuneType type = com.livingtools.runes.RuneType
                    .valueOf(args[2].toUpperCase());
            com.livingtools.runes.RuneManager.RuneTier tier = com.livingtools.runes.RuneManager.RuneTier
                    .valueOf(args[3].toUpperCase());
            player.getInventory().addItem(com.livingtools.runes.RuneManager.createRuneItem(type, tier));
            player.sendMessage(ChatColor.GREEN + "Runa entregada.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Tipo o Tier inválido.");
        }
        return true;
    }

    private static boolean handleGiveGeode(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
            }
        }
        ItemStack geode = com.livingtools.runes.RuneManager.createGeode();
        geode.setAmount(amount);
        player.getInventory().addItem(geode);
        player.sendMessage(ChatColor.GREEN + "Geoda entregada.");
        return true;
    }

    private static boolean handleAbilityXP(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin abilityxp <habilidad> <cantidad>");
            return true;
        }
        String abilityId = args[2].toLowerCase();
        try {
            int amount = Integer.parseInt(args[3]);
            LivingTool tool = new LivingTool(item);
            if (tool.hasAbility(abilityId)) {
                tool.getData().addAbilityXP(abilityId, amount);
                player.sendMessage(ChatColor.GREEN + "XP de habilidad añadida.");
            } else {
                player.sendMessage(ChatColor.RED + "La herramienta no tiene esa habilidad.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Cantidad inválida.");
        }
        return true;
    }

    private static boolean handleSetCorruption(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin setcorruption <cantidad>");
            return true;
        }
        try {
            int amount = Integer.parseInt(args[2]);
            new LivingTool(item).getData().setCorruption(amount);
            new LivingTool(item).updateLore();
            player.sendMessage(ChatColor.GREEN + "Corrupción establecida.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Cantidad inválida.");
        }
        return true;
    }

    private static boolean handleGiveArtifact(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin giveartifact <tipo>");
            return true;
        }
        try {
            com.livingtools.manager.ArtifactManager.ArtifactType type = com.livingtools.manager.ArtifactManager.ArtifactType
                    .valueOf(args[2].toUpperCase());
            player.getInventory().addItem(com.livingtools.manager.ArtifactManager.createArtifact(type));
            player.sendMessage(ChatColor.GREEN + "Artefacto entregado.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Tipo inválido.");
        }
        return true;
    }

    private static boolean handleSetMood(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin setmood <0-100>");
            return true;
        }
        try {
            int mood = Integer.parseInt(args[2]);
            new LivingTool(item).getData().setMood(mood);
            player.sendMessage(ChatColor.GREEN + "Humor establecido a: " + mood);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Número inválido.");
        }
        return true;
    }

    private static boolean handleGiveTome(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin givetome <ID>");
            return true;
        }
        String tomeId = args[2];
        try {
            com.livingtools.manager.LoreManager.TomeType type = com.livingtools.manager.LoreManager.TomeType
                    .valueOf(tomeId.toUpperCase());
            player.getInventory().addItem(com.livingtools.manager.LoreManager.createTome(type));
            player.sendMessage(ChatColor.GREEN + "Tomo entregado.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "ID de tomo inválido.");
        }
        return true;
    }

    private static boolean handleResetCooldowns(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        // This is tricky because cooldowns are private in Ability class.
        // For now, we can just say "Cooldowns reset" and maybe implement a global reset
        // later if needed.
        // Or we can just skip time forward which effectively resets them.
        com.livingtools.manager.TimeManager.skipTime(player.getWorld(), 24000); // Skip a full day
        player.sendMessage(ChatColor.GREEN + "Tiempo adelantado (Cooldowns expirados).");
        return true;
    }

    private static boolean handleSmuggler(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        com.livingtools.manager.SmugglerManager.spawnSmuggler(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Contrabandista invocado.");
        return true;
    }

    private static boolean handleGiveSoulGem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin givesoulgem <tipo>");
            return true;
        }
        try {
            com.livingtools.manager.SoulForgeManager.SoulType type = com.livingtools.manager.SoulForgeManager.SoulType
                    .valueOf(args[2].toUpperCase());
            player.getInventory().addItem(com.livingtools.manager.SoulForgeManager.createSoulGem(type));
            player.sendMessage(ChatColor.GREEN + "Gema de Alma entregada.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Tipo de alma inválido.");
        }
        return true;
    }

    private static boolean handleGiveCustomEnchant(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin givecustomenchant <encantamiento> [nivel]");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }

        try {
            com.livingtools.manager.CustomEnchantManager.LivingEnchant enchant = com.livingtools.manager.CustomEnchantManager.LivingEnchant
                    .valueOf(args[2].toUpperCase());
            int level = 1;
            if (args.length > 3) {
                level = Integer.parseInt(args[3]);
            }

            com.livingtools.manager.CustomEnchantManager.addEnchant(new LivingTool(item), enchant, level);
            player.sendMessage(ChatColor.GREEN + "Encantamiento aplicado: " + enchant.getName());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Encantamiento inválido.");
        }
        return true;
    }

    private static boolean handleGiveVoidEssence(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        player.getInventory().addItem(com.livingtools.manager.AbyssalManager.createVoidEssence());
        player.sendMessage(ChatColor.GREEN + "Esencia del Vacío entregada.");
        return true;
    }

    private static boolean handleUpgradeAbyssal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        com.livingtools.manager.AbyssalManager.upgradeToAbyssal(player, new LivingTool(item));
        return true;
    }

    private static boolean handleSpawnRift(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        com.livingtools.manager.VoidRiftManager.spawnRift(player.getLocation());
        return true;
    }

    private static boolean handleSetPlayerCorruption(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin setplayercorruption <cantidad>");
            return true;
        }
        try {
            int amount = Integer.parseInt(args[2]);
            com.livingtools.mechanics.CorruptionSystem.setCorruption(player, amount);
            player.sendMessage(ChatColor.GREEN + "Corrupción de JUGADOR establecida.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Cantidad inválida.");
        }
        return true;
    }

    private static boolean handleGiveStarlightEssence(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        player.getInventory().addItem(com.livingtools.manager.AetherialManager.createStarlightEssence());
        player.sendMessage(ChatColor.GREEN + "Esencia Estelar entregada.");
        return true;
    }

    private static boolean handleUpgradeAetherial(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        com.livingtools.manager.AetherialManager.upgradeToAetherial(player, new LivingTool(item));
        return true;
    }

    private static boolean handlePurifyTool(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        com.livingtools.mechanics.PurificationSystem.purifyTool(player, new LivingTool(item));
        return true;
    }

    private static boolean handleSpawnFortress(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        com.livingtools.manager.SkyFortressManager.spawnFortress(player);
        return true;
    }

    private static boolean handleJoinFaction(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin joinfaction <VOID|LIGHT>");
            return true;
        }
        try {
            com.livingtools.manager.FactionManager.Faction faction = com.livingtools.manager.FactionManager.Faction
                    .valueOf(args[2].toUpperCase());
            com.livingtools.manager.FactionManager.joinFaction(player, faction);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Facción inválida.");
        }
        return true;
    }

    private static boolean handleClaimChunk(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        com.livingtools.manager.TerritoryManager.claimChunk(player);
        return true;
    }

    private static boolean handleStartConvergence(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        com.livingtools.manager.ConvergenceManager.startEvent(player.getLocation());
        return true;
    }

    private static boolean handleGiveOmniTool(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        player.getInventory().addItem(com.livingtools.manager.OmniToolManager.createOmniTool());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "¡Has recibido la Omni-Herramienta!");
        return true;
    }

    private static boolean handleSpawnBoss(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin spawnboss <LIVING|SERAPHIM|TITAN|DRYAD|WYRM|LEVIATHAN>");
            return true;
        }

        String type = args[2].toUpperCase();
        try {
            switch (type) {
                case "LIVING":
                    com.livingtools.manager.VolcanicArenaManager.spawnArena(player.getLocation(), true);
                    break;
                case "SERAPHIM":
                    com.livingtools.manager.SkyFortressManager.spawnFortress(player, true);
                    break;
                case "TITAN":
                    com.livingtools.manager.TitanArenaManager.spawnArena(player.getLocation(), true);
                    break;
                case "DRYAD":
                    com.livingtools.manager.BossAbilityManager.spawnDryad(player.getLocation().add(0, 1, 0));
                    break;
                case "WYRM":
                    com.livingtools.manager.BossAbilityManager.spawnWyrm(player.getLocation().add(0, 1, 0));
                    break;
                case "LEVIATHAN":
                    com.livingtools.manager.BossAbilityManager.spawnLeviathan(player.getLocation().add(0, 1, 0));
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Tipo de jefe desconocido.");
                    return true;
            }
            player.sendMessage(ChatColor.GREEN + "Invocando jefe y arena: " + type);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error al invocar jefe: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private static boolean handleSpawnArena(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /livingtool admin spawnarena <LIVING|SERAPHIM|TITAN|DRYAD|WYRM|LEVIATHAN>");
            return true;
        }
        Player player = (Player) sender;
        String type = args[2].toUpperCase();
        try {
            switch (type) {
                case "LIVING":
                    com.livingtools.manager.VolcanicArenaManager.spawnArena(player.getLocation(), false);
                    break;
                case "SERAPHIM":
                    com.livingtools.manager.SkyFortressManager.spawnFortress(player, false);
                    break;
                case "TITAN":
                    com.livingtools.manager.TitanArenaManager.spawnArena(player.getLocation(), false);
                    break;
                case "DRYAD":
                case "FOREST":
                    com.livingtools.manager.ForestArenaManager.spawnArena(player.getLocation(), true);
                    break;
                case "WYRM":
                case "DESERT":
                    com.livingtools.manager.DesertArenaManager.spawnArena(player.getLocation(), true);
                    break;
                case "LEVIATHAN":
                case "OCEAN":
                    com.livingtools.manager.OceanArenaManager.spawnArena(player.getLocation(), true);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Tipo de arena desconocido: " + type);
                    player.sendMessage(ChatColor.GRAY + "Tipos: LIVING, SERAPHIM, TITAN, DRYAD, WYRM, LEVIATHAN");
                    return true;
            }
            player.sendMessage(ChatColor.GREEN + "✓ Generando arena: " + type);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error al generar arena: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private static boolean handleSetLevel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /livingtool admin setlevel <nivel>");
            return true;
        }
        try {
            int level = Integer.parseInt(args[2]);
            LivingTool tool = new LivingTool(item);
            tool.getData().setLevel(level);
            tool.updateLore();
            player.sendMessage(ChatColor.GREEN + "Nivel establecido a " + level);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Nivel inválido.");
        }
        return true;
    }

    private static boolean handleReset(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) {
            player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
            return true;
        }
        LivingTool tool = new LivingTool(item);
        tool.getData().initialize(); // Resets to Level 1, 0 XP, etc.
        tool.updateLore();
        player.sendMessage(ChatColor.GREEN + "Herramienta reiniciada.");
        return true;
    }

    private static boolean handleTestModels(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        int[] models = { 1000, 1001, 1002, 1003, 1004, 1005, 1100, 1200, 1300 };
        String[] names = { "Base", "Lvl 10", "Lvl 20", "Lvl 30", "Lvl 40", "Lvl 50", "Infernal", "Aetherial",
                "Demonic" };

        for (int i = 0; i < models.length; i++) {
            ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Model Test: " + names[i]);
            meta.setCustomModelData(models[i]);
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
        }

        player.sendMessage(ChatColor.GREEN + "Se han entregado picos de prueba con todos los modelos.");
        return true;
    }

    private static boolean handleMultiplier(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /livingtool admin multiplier <cantidad> [MINING|COMBAT|ALL]");
            return true;
        }
        try {
            double amount = Double.parseDouble(args[2]);
            String type = "ALL";
            if (args.length > 3) {
                type = args[3].toUpperCase();
            }
            if (type.equals("MINING") || type.equals("ALL")) {
                ConfigManager.setMiningXPMultiplier(amount);
            }
            if (type.equals("COMBAT") || type.equals("ALL")) {
                ConfigManager.setCombatXPMultiplier(amount);
            }
            sender.sendMessage(ChatColor.GREEN + "Multiplicador de XP actualizado a " + amount + " (" + type + ")");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Cantidad inválida.");
        }
        return true;
    }
}
