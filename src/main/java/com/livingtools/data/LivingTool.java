package com.livingtools.data;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LivingTool {

    private final ItemStack item;
    private final ToolData data;

    public LivingTool(ItemStack item) {
        this.item = item;
        this.data = new ToolData(item);
    }

    public static boolean isLivingTool(ItemStack item) {
        return new ToolData(item).isLivingTool();
    }

    public void addXP(Player player, long amount) {
        // Check Level Cap / Trial
        if (com.livingtools.manager.TrialManager
                .getCurrentTrial(this) != com.livingtools.manager.TrialManager.TrialType.NONE) {
            return; // XP Locked
        }

        // Rune Bonus (Sapientia)
        double multiplier = 1.0;
        for (com.livingtools.runes.RuneType rune : data.getRunes()) {
            if (rune == com.livingtools.runes.RuneType.SAPIENTIA) {
                multiplier += 0.20; // +20% XP
            }
        }

        // Prestige Bonus
        int prestige = data.getPrestige();
        if (prestige > 0) {
            multiplier += (prestige * 0.20); // +20% per prestige
        }

        // Legacy Bonus (from previous prestiges of same tool type)
        double legacyMult = com.livingtools.manager.LegacyManager.getLegacyMultiplier(player, item.getType());
        multiplier = multiplier * legacyMult;

        // Hive Mind Bonus (Phase 58)
        if (com.livingtools.manager.HiveMindManager.hasHiveMindBuff(player)) {
            multiplier += 0.10; // +10% XP
        }

        // Satiety Bonus/Penalty (well-fed = +5%, starving = -10%)
        multiplier *= com.livingtools.manager.FeedingManager.getSatietyXPMultiplier(this);

        long currentXP = data.getXP();
        long newXP = currentXP + (long) (amount * multiplier);
        data.setXP(newXP);

        checkLevelUp(player);
        updateLore();
    }

    private void checkLevelUp(Player player) {
        int currentLevel = data.getLevel();
        long requiredXP = getRequiredXP(currentLevel);

        while (data.getXP() >= requiredXP) {
            // Check for Trial Start (Block level up if trial active)
            com.livingtools.manager.TrialManager.checkTrialStart(player, this);
            if (com.livingtools.manager.TrialManager
                    .getCurrentTrial(this) != com.livingtools.manager.TrialManager.TrialType.NONE) {
                return; // Locked by new trial
            }

            // Level Up
            data.setXP(data.getXP() - requiredXP);
            currentLevel++;
            data.setLevel(currentLevel);

            String itemName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
                    : item.getType().name();

            com.livingtools.utils.MessageUtils.send(player,
                    "&#FFD700¡Tu " + itemName + " &#FFD700ha subido al Nivel &#00FFFF" + currentLevel + "&#FFD700!");
            com.livingtools.utils.MessageUtils.playLevelUpEffects(player);

            // Fire Event
            com.livingtools.api.events.LivingToolLevelUpEvent levelEvent = new com.livingtools.api.events.LivingToolLevelUpEvent(
                    player, this, currentLevel);
            org.bukkit.Bukkit.getPluginManager().callEvent(levelEvent);

            // Discord Webhook
            if (currentLevel % 10 == 0 || currentLevel == 50 || currentLevel == 100) {
                com.livingtools.manager.DiscordManager.sendLevelUp(player.getName(), itemName,
                        currentLevel);
            }

            // Personality Dialogue
            com.livingtools.manager.PersonalityManager.sayLine(player, this,
                    com.livingtools.manager.PersonalityManager.EventType.LEVEL_UP);

            // Check Evolution
            checkEvolution(player, currentLevel);

            // Update required XP for next loop
            requiredXP = getRequiredXP(currentLevel);
        }

        com.livingtools.manager.LeaderboardManager.updateEntry(player, this);

        // Check Infusions
        checkInfusions(player);
    }

    private void checkEvolution(Player player, int level) {
        org.bukkit.Material type = item.getType();
        if (level == 25) {
            if (type == org.bukkit.Material.IRON_PICKAXE) {
                evolve(player, org.bukkit.Material.DIAMOND_PICKAXE, "evolution_diamond_pickaxe");
            } else if (type == org.bukkit.Material.IRON_SWORD) {
                evolve(player, org.bukkit.Material.DIAMOND_SWORD, "evolution_diamond_sword");
            } else if (type == org.bukkit.Material.IRON_AXE) {
                evolve(player, org.bukkit.Material.DIAMOND_AXE, "evolution_diamond_axe");
            } else if (type == org.bukkit.Material.IRON_SHOVEL) {
                evolve(player, org.bukkit.Material.DIAMOND_SHOVEL, "evolution_diamond_shovel");
            }
        } else if (level == 50) {
            if (type == org.bukkit.Material.DIAMOND_PICKAXE) {
                evolve(player, org.bukkit.Material.NETHERITE_PICKAXE, "evolution_netherite_pickaxe");
            } else if (type == org.bukkit.Material.DIAMOND_SWORD) {
                evolve(player, org.bukkit.Material.NETHERITE_SWORD, "evolution_netherite_sword");
            } else if (type == org.bukkit.Material.DIAMOND_AXE) {
                evolve(player, org.bukkit.Material.NETHERITE_AXE, "evolution_netherite_axe");
            } else if (type == org.bukkit.Material.DIAMOND_SHOVEL) {
                evolve(player, org.bukkit.Material.NETHERITE_SHOVEL, "evolution_netherite_shovel");
            }
        }

        // Update Visuals via EvolutionManager
        com.livingtools.manager.EvolutionManager.updateAppearance(this);
    }

    private void evolve(Player player, org.bukkit.Material newType, String messageKey) {
        item.setType(newType);
        player.sendMessage(com.livingtools.manager.ConfigManager.getMessage(messageKey));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
        player.spawnParticle(org.bukkit.Particle.FLASH, player.getLocation(), 1);
    }

    public void spawnSwingParticles(Player player) {
        int level = data.getLevel();
        org.bukkit.Particle particle = org.bukkit.Particle.CRIT; // Default

        // Element/Theme based on Level/Prestige
        if (data.getPrestige() > 0) {
            particle = org.bukkit.Particle.SOUL_FIRE_FLAME;
        } else if (level >= 75) {
            particle = org.bukkit.Particle.FLAME;
        } else if (level >= 50) {
            particle = org.bukkit.Particle.DRAGON_BREATH;
        } else if (level >= 25) {
            particle = org.bukkit.Particle.ENCHANTMENT_TABLE;
        }

        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
    }

    private void checkInfusions(Player player) {
        for (com.livingtools.abilities.Ability ability : com.livingtools.abilities.AbilityRegistry
                .getAbilities().values()) {
            if (ability instanceof com.livingtools.abilities.InfusionAbility) {
                com.livingtools.abilities.InfusionAbility infusion = (com.livingtools.abilities.InfusionAbility) ability;
                if (!hasAbility(infusion.getId()) && infusion.canUnlock(this)) {
                    addAbility(infusion.getId());
                    player.sendMessage(com.livingtools.manager.ConfigManager.getMessage("infusion_unlocked")
                            .replace("%ability%", infusion.getName()));
                    player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
                }
            }
        }
    }

    private long getRequiredXP(int level) {
        // Simple exponential curve: Scaling * level^2
        long scaling = com.livingtools.manager.ConfigManager.getInt("xp.level-scaling");
        return scaling * level * level;
    }

    public void updateLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        String displayName = data.getCustomName();
        org.bukkit.ChatColor biomeColor = org.bukkit.ChatColor.valueOf(data.getBiomeColor());

        if (displayName.isEmpty()) {
            if (com.livingtools.data.LivingArmor.isLivingArmor(item)) {
                displayName = biomeColor + "Armadura Viviente";
            } else {
                displayName = biomeColor + "Herramienta Viviente";
            }
        } else {
            displayName = biomeColor + org.bukkit.ChatColor.translateAlternateColorCodes('&', displayName);
        }

        meta.setDisplayName(displayName + org.bukkit.ChatColor.GRAY + " ["
                + org.bukkit.ChatColor.YELLOW + "Lvl " + data.getLevel() + org.bukkit.ChatColor.GRAY + "]");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Nivel: " + ChatColor.YELLOW + data.getLevel());
        lore.add(ChatColor.GRAY + "XP: " + ChatColor.YELLOW + data.getXP() + ChatColor.GRAY + " / " + ChatColor.GOLD
                + getRequiredXP(data.getLevel()));

        String title = data.getTitle();
        if (!title.isEmpty()) {
            lore.add(ChatColor.GOLD + "Título: " + title);
        }

        String personalityName = data.getPersonality();
        if (personalityName != null && !personalityName.isEmpty()) {
            try {
                com.livingtools.mechanics.Personality p = com.livingtools.mechanics.Personality
                        .valueOf(personalityName);
                lore.add(ChatColor.GRAY + "Personalidad: " + ChatColor.YELLOW + p.name());
                lore.add(ChatColor.GRAY + "Beneficio: " + ChatColor.GREEN + getBenefitDescription(p));
            } catch (IllegalArgumentException ignored) {
            }
        }

        int prestige = data.getPrestige();
        if (prestige > 0) {
            lore.add(ChatColor.AQUA + "Prestigio: " + romanNumeral(prestige));
        }

        int affinity = data.getAffinity();
        String affinityStr = ChatColor.YELLOW + "Neutral";
        if (affinity > 50)
            affinityStr = ChatColor.GREEN + "Leal";
        else if (affinity > 20)
            affinityStr = ChatColor.GREEN + "Amistosa";
        else if (affinity < -50)
            affinityStr = ChatColor.DARK_RED + "Hostil";
        else if (affinity < -20)
            affinityStr = ChatColor.RED + "Molesta";

        lore.add(ChatColor.GRAY + "Afinidad: " + affinityStr + ChatColor.GRAY + " (" + affinity + ")");

        int corruption = data.getCorruption();
        if (corruption > 0) {
            lore.add(ChatColor.DARK_PURPLE + "Corrupción: " + corruption + "%");
        }

        com.livingtools.manager.TrialManager.TrialType trial = com.livingtools.manager.TrialManager
                .getCurrentTrial(this);
        if (trial != com.livingtools.manager.TrialManager.TrialType.NONE) {
            lore.add("");
            lore.add(ChatColor.RED + "DESAFÍO ACTIVO: "
                    + com.livingtools.manager.TrialManager.getTrialDescription(trial));
            lore.add(ChatColor.RED + "Progreso: " + data.getTrialProgress() + " / " + trial.getTargetAmount());
        }

        List<String> abilities = getAbilities();
        if (!abilities.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.GRAY + "Habilidades:");
            for (String abilityId : abilities) {
                com.livingtools.abilities.Ability ability = com.livingtools.abilities.AbilityRegistry
                        .getAbility(abilityId);
                if (ability != null) {
                    lore.add(ChatColor.GREEN + "- " + ability.getName());
                }
            }
        }

        // Check for Curses (PDC)
        org.bukkit.NamespacedKey keyFragility = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "curse_fragility");
        if (meta.getPersistentDataContainer().has(keyFragility, org.bukkit.persistence.PersistentDataType.BYTE)) {
            lore.add("");
            lore.add(ChatColor.RED + "Maldición: Fragilidad");
        }

        // Add Glint if not present (handled on creation, but good to ensure)
        if (!meta.hasEnchant(org.bukkit.enchantments.Enchantment.DURABILITY)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        }

        meta.setUnbreakable(false);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);

        // Check for Broken State
        if (isBroken()) {
            lore.add("");
            lore.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "¡ROTA / INSERVIBLE!");
            lore.add(ChatColor.GRAY + "Repárala en un yunque o aliméntala");
            lore.add(ChatColor.GRAY + "con materiales en tu inventario.");
        }

        meta.setLore(lore);

        // Check for Broken State
        if (isBroken()) {
            List<String> currentLore = meta.getLore();
            if (currentLore == null)
                currentLore = new ArrayList<>();
            currentLore.add("");
            currentLore.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "¡ROTA / INSERVIBLE!");
            currentLore.add(ChatColor.GRAY + "Repárala en un yunque o aliméntala");
            currentLore.add(ChatColor.GRAY + "con materiales en tu inventario.");
            meta.setLore(currentLore);
        }

        // Update Visual Model
        item.setItemMeta(meta); // Set meta first so item has lore
        com.livingtools.manager.ModelManager.updateModel(this);
    }

    public ToolData getData() {
        return data;
    }

    public ItemStack getItem() {
        return item;
    }

    public List<String> getAbilities() {
        String raw = data.getAbilitiesRaw();
        if (raw == null || raw.isEmpty())
            return new ArrayList<>();
        return new ArrayList<>(List.of(raw.split(",")));
    }

    public void addAbility(String abilityId) {
        List<String> abilities = getAbilities();
        if (!abilities.contains(abilityId)) {
            // Check Incompatibilities
            com.livingtools.abilities.Ability newAbility = com.livingtools.abilities.AbilityRegistry
                    .getAbility(abilityId);
            if (newAbility != null) {
                for (String existingId : abilities) {
                    com.livingtools.abilities.Ability existing = com.livingtools.abilities.AbilityRegistry
                            .getAbility(existingId);
                    if (existing != null) {
                        if (newAbility.getIncompatibleAbilities().contains(existingId)
                                || existing.getIncompatibleAbilities().contains(abilityId)) {
                            return; // Incompatible
                        }
                    }
                }
            }

            abilities.add(abilityId);
            data.setAbilitiesRaw(String.join(",", abilities));
            updateLore();
        }
    }

    public boolean hasAbility(String abilityId) {
        return getAbilities().contains(abilityId);
    }

    public void removeAbility(String abilityId) {
        List<String> abilities = getAbilities();
        if (abilities.remove(abilityId)) {
            data.setAbilitiesRaw(String.join(",", abilities));
            updateLore();
        }
    }

    public enum Mood {
        HAPPY,
        NEUTRAL,
        SAD,
        FURIOUS
    }

    public Mood getMood() {
        int affinity = data.getAffinity();
        int corruption = data.getCorruption();

        if (corruption > 75)
            return Mood.FURIOUS;
        if (affinity < -20)
            return Mood.SAD;
        if (affinity > 20)
            return Mood.HAPPY;
        return Mood.NEUTRAL;
    }

    private String getBenefitDescription(com.livingtools.mechanics.Personality p) {
        switch (p) {
            case LUCKY:
                return "Favor de la Fortuna (Drops Extra)";
            case GLUTTONOUS:
                return "Devorar (Restaura Hambre)";
            case AGGRESSIVE:
                return "Sed de Sangre (Fuerza al Matar)";
            case HEROIC:
                return "Corazón Valiente (Resistencia en Salud Baja)";
            case LAZY:
                return "Siesta Reparadora (Reparación Pasiva)";
            case SARCASTIC:
                return "Daño Emocional (Daño Verdadero)";
            case SHY:
                return "Inadvertido (Invisibilidad al Agacharse)";
            default:
                return "Ninguno";
        }
    }

    public void prestige(Player player) {
        if (data.getLevel() < 200) {
            player.sendMessage(ChatColor.RED + "Necesitas ser Nivel 200 para ascender.");
            return;
        }

        // Register Legacy before resetting stats
        com.livingtools.manager.LegacyManager.registerLegacy(player, item.getType());

        // Reset Stats
        data.setPrestige(data.getPrestige() + 1);
        data.setLevel(1);
        data.setXP(0);

        // Clear Abilities (Keep Awakening if we want, but usually full reset)
        data.setAbilitiesRaw(""); // Clear all abilities

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0.5f);
        player.spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, player.getLocation(), 1);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "¡ASCENSIÓN COMPLETADA!");
        player.sendMessage(ChatColor.YELLOW + "Tu herramienta ha renacido con mayor poder.");

        // Server-wide announcement
        com.livingtools.manager.AnnouncementManager.announcePrestige(player, this, data.getPrestige());

        updateLore();
    }

    public static String romanNumeral(int n) {
        if (n <= 0)
            return "";
        if (n == 1)
            return "I";
        if (n == 2)
            return "II";
        if (n == 3)
            return "III";
        if (n == 4)
            return "IV";
        if (n == 5)
            return "V";
        if (n == 6)
            return "VI";
        if (n == 7)
            return "VII";
        if (n == 8)
            return "VIII";
        if (n == 9)
            return "IX";
        if (n == 10)
            return "X";
        return String.valueOf(n);
    }

    public boolean isBroken() {
        return data.isBroken();
    }

    public void setBroken(boolean broken) {
        data.setBroken(broken);
        updateLore();
    }
}
