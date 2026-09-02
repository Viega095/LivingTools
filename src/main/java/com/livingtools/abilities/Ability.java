package com.livingtools.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class Ability {

    private final String id;
    private final String name;
    private final String description;
    private final int requiredLevel;
    private final AbilityType type;
    private final int maxXP;

    public Ability(String id, String name, String description, int requiredLevel) {
        this(id, name, description, requiredLevel, AbilityType.PASSIVE);
    }

    public Ability(String id, String name, String description, int requiredLevel, int maxXP) {
        this(id, name, description, requiredLevel, AbilityType.PASSIVE, maxXP);
    }

    public Ability(String id, String name, String description, int requiredLevel, AbilityType type) {
        this(id, name, description, requiredLevel, type, 1000); // Default maxXP
    }

    public Ability(String id, String name, String description, int requiredLevel, AbilityType type, int maxXP) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.type = type;
        this.maxXP = maxXP;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public AbilityType getType() {
        return type;
    }

    public int getMaxXP() {
        return maxXP;
    }

    public int getCooldown() {
        return 0; // Default to 0 (Passive/Dynamic), override in subclasses for fixed cooldowns
    }

    // Hook for passive effects (e.g. potion effects while holding)
    public void onHold(Player player) {
    }

    // Hook for active triggers (e.g. on block break)
    // Deprecated: Use specific methods below
    public void onTrigger(Player player, Event event) {
    }

    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event,
            com.livingtools.data.LivingTool tool) {
        if (!tool.getData().isAbilityActive(this.id))
            return;
    }

    public void onEntityDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event,
            com.livingtools.data.LivingTool tool) {
        if (!tool.getData().isAbilityActive(this.id))
            return;
    }

    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event,
            com.livingtools.data.LivingTool tool) {
        if (!tool.getData().isAbilityActive(this.id))
            return;
    }

    // Hook for Armor Damage Taken
    public void onDamageTaken(org.bukkit.event.entity.EntityDamageEvent event, Player victim) {
        // Armor abilities need to check active state too, but we need the tool
        // instance.
        // Usually handled by listener iterating armor.
    }

    // Hook for Armor Damage Taken (By Entity) - Helper
    public void onDamageTakenByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event, Player victim) {
    }

    public boolean isCompatible(org.bukkit.Material type) {
        return true; // Default to all
    }

    public java.util.List<String> getIncompatibleAbilities() {
        return new java.util.ArrayList<>();
    }

    // Mastery System
    public int getLevel(com.livingtools.data.LivingTool tool) {
        return tool.getData().getAbilityLevel(this.id);
    }

    protected void addXP(Player player, com.livingtools.data.LivingTool tool, int amount) {
        tool.getData().addAbilityXP(this.id, amount);

        // Notify user occasionally (e.g. every 10 XP or on level up)
        int currentXP = tool.getData().getAbilityXP(this.id);
        if (currentXP % 10 == 0) {
            com.livingtools.utils.MessageUtils.sendActionBar(player,
                    org.bukkit.ChatColor.AQUA + this.name + " XP: " + currentXP);
        }
    }

    // Helper to check for Echo Rune (Cooldown Skip)
    protected boolean checkEchoRune(com.livingtools.data.LivingTool tool, Player player) {
        java.util.List<com.livingtools.runes.RuneType> runes = tool.getData().getRunes();
        java.util.List<com.livingtools.runes.RuneManager.RuneTier> tiers = tool.getData().getRuneTiers();

        for (int i = 0; i < runes.size(); i++) {
            if (runes.get(i) == com.livingtools.runes.RuneType.ECHO) {
                com.livingtools.runes.RuneManager.RuneTier tier = (i < tiers.size()) ? tiers.get(i)
                        : com.livingtools.runes.RuneManager.RuneTier.COMMON;

                double chance = 0.10; // Common
                if (tier == com.livingtools.runes.RuneManager.RuneTier.RARE)
                    chance = 0.20;
                if (tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC)
                    chance = 0.30;

                if (Math.random() < chance) {
                    player.sendMessage(org.bukkit.ChatColor.DARK_AQUA + "🌀 ¡Eco Rúnico! Habilidad instantánea.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 2);
                    return true; // Skip cooldown
                }
            }
        }
        return false;
    }

    public void onAttack(org.bukkit.event.entity.EntityDamageByEntityEvent event,
            com.livingtools.data.LivingTool tool) {
        // Default implementation delegates to onEntityDamage for backward compatibility
        onEntityDamage(event, tool);
    }

    public void onKill(org.bukkit.event.entity.EntityDeathEvent event,
            com.livingtools.data.LivingTool tool) {
    }

    // Cooldown Management
    private final java.util.Map<java.util.UUID, Long> cooldowns = new java.util.HashMap<>();

    protected void addCooldown(Player player, long millis) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + millis);
    }

    protected boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId()))
            return false;
        long expiry = cooldowns.get(player.getUniqueId());
        if (System.currentTimeMillis() < expiry) {
            long remaining = (expiry - System.currentTimeMillis()) / 1000;
            player.sendMessage(org.bukkit.ChatColor.RED + "Habilidad en enfriamiento (" + remaining + "s).");
            return true;
        }
        cooldowns.remove(player.getUniqueId());
        return false;
    }

    public void resetCooldowns() {
        cooldowns.clear();
    }
}
