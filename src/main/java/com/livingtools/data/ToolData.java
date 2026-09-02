package com.livingtools.data;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ToolData {

    private static final NamespacedKey KEY_XP = new NamespacedKey(LivingToolsPlugin.getInstance(), "livingtools_xp");
    private static final NamespacedKey KEY_LEVEL = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_level");
    private static final NamespacedKey KEY_PERSONALITY = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_personality");
    private static final NamespacedKey KEY_ABILITIES = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_abilities");
    private static final NamespacedKey KEY_PRESTIGE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_prestige");

    // Biome Tracking
    private static final NamespacedKey KEY_BIOME_NETHER = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_biome_nether");
    private static final NamespacedKey KEY_BIOME_OCEAN = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_biome_ocean");
    private static final NamespacedKey KEY_BIOME_SKY = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_biome_sky");

    private final ItemStack item;

    public ToolData(ItemStack item) {
        this.item = item;
    }

    public boolean isLivingTool() {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_LEVEL, PersistentDataType.INTEGER);
    }

    public void initialize() {
        setXP(0);
        setLevel(1);

        // Random personality
        com.livingtools.mechanics.Personality[] personalities = com.livingtools.mechanics.Personality
                .values();
        setPersonality(personalities[(int) (Math.random() * personalities.length)].name());
    }

    public long getXP() {
        return getPersistentData(KEY_XP, PersistentDataType.LONG, 0L);
    }

    public void setXP(long xp) {
        setPersistentData(KEY_XP, PersistentDataType.LONG, xp);
    }

    public int getLevel() {
        return getPersistentData(KEY_LEVEL, PersistentDataType.INTEGER, 1);
    }

    public void setLevel(int level) {
        setPersistentData(KEY_LEVEL, PersistentDataType.INTEGER, level);
    }

    public int getPrestige() {
        return getPersistentData(KEY_PRESTIGE, PersistentDataType.INTEGER, 0);
    }

    public void setPrestige(int prestige) {
        setPersistentData(KEY_PRESTIGE, PersistentDataType.INTEGER, prestige);
    }

    public String getPersonality() {
        return getPersistentData(KEY_PERSONALITY, PersistentDataType.STRING, null);
    }

    public void setPersonality(String personality) {
        setPersistentData(KEY_PERSONALITY, PersistentDataType.STRING, personality);
    }

    public String getAbilitiesRaw() {
        return getPersistentData(KEY_ABILITIES, PersistentDataType.STRING, "");
    }

    public void setAbilitiesRaw(String abilities) {
        setPersistentData(KEY_ABILITIES, PersistentDataType.STRING, abilities);
    }

    public List<String> getAbilities() {
        String raw = getAbilitiesRaw();
        if (raw == null || raw.isEmpty())
            return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(List.of(raw.split(",")));
    }

    public boolean hasAbility(String abilityId) {
        String raw = getAbilitiesRaw();
        for (String s : raw.split(",")) {
            if (s.equals(abilityId))
                return true;
        }
        return false;
    }

    public void addAbility(String abilityId) {
        if (hasAbility(abilityId))
            return;
        String raw = getAbilitiesRaw();
        if (raw.isEmpty()) {
            setAbilitiesRaw(abilityId);
        } else {
            setAbilitiesRaw(raw + "," + abilityId);
        }
    }

    public void addBiomeXP(com.livingtools.manager.BiomeManager.BiomeCategory category, int amount) {
        NamespacedKey key = getBiomeKey(category);
        if (key == null)
            return;

        int current = getPersistentData(key, PersistentDataType.INTEGER, 0);
        setPersistentData(key, PersistentDataType.INTEGER, current + amount);
    }

    public int getBiomeXP(com.livingtools.manager.BiomeManager.BiomeCategory category) {
        NamespacedKey key = getBiomeKey(category);
        if (key == null)
            return 0;
        return getPersistentData(key, PersistentDataType.INTEGER, 0);
    }

    private static final NamespacedKey KEY_TRIAL = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_trial");
    private static final NamespacedKey KEY_TRIAL_PROGRESS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_trial_progress");

    public String getTrial() {
        return getPersistentData(KEY_TRIAL, PersistentDataType.STRING, "NONE");
    }

    public void setTrial(String trial) {
        setPersistentData(KEY_TRIAL, PersistentDataType.STRING, trial);
    }

    public int getTrialProgress() {
        return getPersistentData(KEY_TRIAL_PROGRESS, PersistentDataType.INTEGER, 0);
    }

    public void setTrialProgress(int progress) {
        setPersistentData(KEY_TRIAL_PROGRESS, PersistentDataType.INTEGER, progress);
    }

    private static final NamespacedKey KEY_OWNER_NAME = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_owner_name");
    private static final NamespacedKey KEY_CREATION_DATE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_creation_date");
    private static final NamespacedKey KEY_MOB_KILLS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_mob_kills");
    private static final NamespacedKey KEY_PLAYER_KILLS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_player_kills");

    public String getOwnerName() {
        return getPersistentData(KEY_OWNER_NAME, PersistentDataType.STRING, "Desconocido");
    }

    public void setOwnerName(String name) {
        setPersistentData(KEY_OWNER_NAME, PersistentDataType.STRING, name);
    }

    public long getCreationDate() {
        return getPersistentData(KEY_CREATION_DATE, PersistentDataType.LONG, System.currentTimeMillis());
    }

    public void setCreationDate(long date) {
        setPersistentData(KEY_CREATION_DATE, PersistentDataType.LONG, date);
    }

    public int getMobKills() {
        return getPersistentData(KEY_MOB_KILLS, PersistentDataType.INTEGER, 0);
    }

    public void setMobKills(int kills) {
        setPersistentData(KEY_MOB_KILLS, PersistentDataType.INTEGER, kills);
    }

    public int getPlayerKills() {
        return getPersistentData(KEY_PLAYER_KILLS, PersistentDataType.INTEGER, 0);
    }

    public void setPlayerKills(int kills) {
        setPersistentData(KEY_PLAYER_KILLS, PersistentDataType.INTEGER, kills);
    }

    private static final NamespacedKey KEY_PEACEFUL_ACTIONS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_peaceful_actions");
    private static final NamespacedKey KEY_TITLE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_title");
    private static final NamespacedKey KEY_CUSTOM_NAME = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_custom_name");
    private static final NamespacedKey KEY_BLOCKS_MINED = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_blocks_mined");

    public int getBlocksMined() {
        return getPersistentData(KEY_BLOCKS_MINED, PersistentDataType.INTEGER, 0);
    }

    public void setBlocksMined(int blocks) {
        setPersistentData(KEY_BLOCKS_MINED, PersistentDataType.INTEGER, blocks);
    }

    public int getPeacefulActions() {
        return getPersistentData(KEY_PEACEFUL_ACTIONS, PersistentDataType.INTEGER, 0);
    }

    public void setPeacefulActions(int actions) {
        setPersistentData(KEY_PEACEFUL_ACTIONS, PersistentDataType.INTEGER, actions);
    }

    public String getTitle() {
        return getPersistentData(KEY_TITLE, PersistentDataType.STRING, "");
    }

    public void setTitle(String title) {
        setPersistentData(KEY_TITLE, PersistentDataType.STRING, title);
    }

    public String getCustomName() {
        return getPersistentData(KEY_CUSTOM_NAME, PersistentDataType.STRING, "");
    }

    public void setCustomName(String name) {
        setPersistentData(KEY_CUSTOM_NAME, PersistentDataType.STRING, name);
    }

    private static final NamespacedKey KEY_AFFINITY = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_affinity");

    public int getAffinity() {
        return getPersistentData(KEY_AFFINITY, PersistentDataType.INTEGER, 0);
    }

    public void setAffinity(int affinity) {
        // Clamp between -100 and 100
        int clamped = Math.max(-100, Math.min(100, affinity));
        setPersistentData(KEY_AFFINITY, PersistentDataType.INTEGER, clamped);
    }

    private static final NamespacedKey KEY_CORRUPTION = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_corruption");

    public int getCorruption() {
        return getPersistentData(KEY_CORRUPTION, PersistentDataType.INTEGER, 0);
    }

    public void setCorruption(int corruption) {
        // Clamp between 0 and 100
        int clamped = Math.max(0, Math.min(100, corruption));
        setPersistentData(KEY_CORRUPTION, PersistentDataType.INTEGER, clamped);
    }

    public void addCorruption(int amount) {
        setCorruption(getCorruption() + amount);
    }

    private static final NamespacedKey KEY_FULLNESS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_fullness");
    private static final NamespacedKey KEY_PREFERRED_FOOD = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_preferred_food");

    public int getFullness() {
        return getPersistentData(KEY_FULLNESS, PersistentDataType.INTEGER, 10); // Default 10 (Full)
    }

    public void setFullness(int fullness) {
        int clamped = Math.max(0, Math.min(10, fullness));
        setPersistentData(KEY_FULLNESS, PersistentDataType.INTEGER, clamped);
    }

    public void addFullness(int amount) {
        setFullness(getFullness() + amount);
    }

    public String getPreferredFood() {
        return getPersistentData(KEY_PREFERRED_FOOD, PersistentDataType.STRING, "");
    }

    public void setPreferredFood(String food) {
        setPersistentData(KEY_PREFERRED_FOOD, PersistentDataType.STRING, food);
    }

    private static final NamespacedKey KEY_RUNES = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_runes");
    private static final NamespacedKey KEY_RUNE_TIERS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_rune_tiers");

    public List<com.livingtools.runes.RuneType> getRunes() {
        String raw = getPersistentData(KEY_RUNES, PersistentDataType.STRING, "");
        List<com.livingtools.runes.RuneType> runes = new java.util.ArrayList<>();
        if (raw.isEmpty())
            return runes;

        for (String s : raw.split(",")) {
            try {
                runes.add(com.livingtools.runes.RuneType.valueOf(s));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return runes;
    }

    public List<com.livingtools.runes.RuneManager.RuneTier> getRuneTiers() {
        String raw = getPersistentData(KEY_RUNE_TIERS, PersistentDataType.STRING, "");
        List<com.livingtools.runes.RuneManager.RuneTier> tiers = new java.util.ArrayList<>();
        if (raw.isEmpty()) {
            // If empty but runes exist, fill with COMMON (migration)
            int runeCount = getRunes().size();
            for (int i = 0; i < runeCount; i++) {
                tiers.add(com.livingtools.runes.RuneManager.RuneTier.COMMON);
            }
            return tiers;
        }

        for (String s : raw.split(",")) {
            try {
                tiers.add(com.livingtools.runes.RuneManager.RuneTier.valueOf(s));
            } catch (IllegalArgumentException ignored) {
                tiers.add(com.livingtools.runes.RuneManager.RuneTier.COMMON);
            }
        }
        return tiers;
    }

    public void addRune(com.livingtools.runes.RuneType rune,
            com.livingtools.runes.RuneManager.RuneTier tier) {
        List<com.livingtools.runes.RuneType> runes = getRunes();
        List<com.livingtools.runes.RuneManager.RuneTier> tiers = getRuneTiers();

        if (runes.size() >= 3)
            return; // Max 3 runes

        runes.add(rune);
        tiers.add(tier);

        saveRunes(runes);
        saveRuneTiers(tiers);
    }

    // Deprecated: Default to COMMON
    public void addRune(com.livingtools.runes.RuneType rune) {
        addRune(rune, com.livingtools.runes.RuneManager.RuneTier.COMMON);
    }

    public void removeRune(int index) {
        List<com.livingtools.runes.RuneType> runes = getRunes();
        List<com.livingtools.runes.RuneManager.RuneTier> tiers = getRuneTiers();

        if (index >= 0 && index < runes.size()) {
            runes.remove(index);
            // Ensure tiers list is synced (handle legacy data)
            if (index < tiers.size()) {
                tiers.remove(index);
            }
            saveRunes(runes);
            saveRuneTiers(tiers);
        }
    }

    private void saveRunes(List<com.livingtools.runes.RuneType> runes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < runes.size(); i++) {
            sb.append(runes.get(i).name());
            if (i < runes.size() - 1)
                sb.append(",");
        }
        setPersistentData(KEY_RUNES, PersistentDataType.STRING, sb.toString());
    }

    private void saveRuneTiers(List<com.livingtools.runes.RuneManager.RuneTier> tiers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiers.size(); i++) {
            sb.append(tiers.get(i).name());
            if (i < tiers.size() - 1)
                sb.append(",");
        }
        setPersistentData(KEY_RUNE_TIERS, PersistentDataType.STRING, sb.toString());
    }

    private static final NamespacedKey KEY_BOND_PARTNER = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_bond_partner");

    public String getBondPartner() {
        return getPersistentData(KEY_BOND_PARTNER, PersistentDataType.STRING, "");
    }

    public void setBondPartner(String uuid) {
        setPersistentData(KEY_BOND_PARTNER, PersistentDataType.STRING, uuid);
    }

    private static final NamespacedKey KEY_ABILITY_XP = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_ability_xp");

    public int getAbilityXP(String abilityId) {
        String raw = getPersistentData(KEY_ABILITY_XP, PersistentDataType.STRING, "");
        if (raw.isEmpty())
            return 0;

        for (String entry : raw.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2 && parts[0].equals(abilityId)) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public void resetAbilityXP() {
        setPersistentData(KEY_ABILITY_XP, PersistentDataType.STRING, "");
    }

    public void addAbilityXP(String abilityId, int amount) {
        String raw = getPersistentData(KEY_ABILITY_XP, PersistentDataType.STRING, "");
        StringBuilder newRaw = new StringBuilder();
        boolean found = false;

        if (!raw.isEmpty()) {
            for (String entry : raw.split(",")) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    if (parts[0].equals(abilityId)) {
                        int current = Integer.parseInt(parts[1]);
                        newRaw.append(abilityId).append(":").append(current + amount).append(",");
                        found = true;
                    } else {
                        newRaw.append(entry).append(",");
                    }
                }
            }
        }

        if (!found) {
            newRaw.append(abilityId).append(":").append(amount).append(",");
        }

        // Remove trailing comma
        if (newRaw.length() > 0) {
            newRaw.setLength(newRaw.length() - 1);
        }

        setPersistentData(KEY_ABILITY_XP, PersistentDataType.STRING, newRaw.toString());
    }

    public int getAbilityLevel(String abilityId) {
        int xp = getAbilityXP(abilityId);
        // Formula: Level = sqrt(XP / 100) + 1. Max Level 5.
        // XP Req: Lvl 2 = 100, Lvl 3 = 400, Lvl 4 = 900, Lvl 5 = 1600
        int level = (int) Math.sqrt(xp / 100.0) + 1;
        return Math.min(5, level);
    }

    private NamespacedKey getBiomeKey(com.livingtools.manager.BiomeManager.BiomeCategory category) {
        switch (category) {
            case NETHER:
                return KEY_BIOME_NETHER;
            case OCEAN:
                return KEY_BIOME_OCEAN;
            case SKY:
                return KEY_BIOME_SKY;
            default:
                return null;
        }
    }

    private static final NamespacedKey KEY_ABILITY_ACTIVE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_ability_active");

    public boolean isAbilityActive(String abilityId) {
        String raw = getPersistentData(KEY_ABILITY_ACTIVE, PersistentDataType.STRING, "");
        if (raw.isEmpty())
            return true; // Default to active

        for (String entry : raw.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2 && parts[0].equals(abilityId)) {
                return Boolean.parseBoolean(parts[1]);
            }
        }
        return true; // Default to active if not found
    }

    public void setAbilityActive(String abilityId, boolean active) {
        String raw = getPersistentData(KEY_ABILITY_ACTIVE, PersistentDataType.STRING, "");
        StringBuilder newRaw = new StringBuilder();
        boolean found = false;

        if (!raw.isEmpty()) {
            for (String entry : raw.split(",")) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    if (parts[0].equals(abilityId)) {
                        newRaw.append(abilityId).append(":").append(active).append(",");
                        found = true;
                    } else {
                        newRaw.append(entry).append(",");
                    }
                }
            }
        }

        if (!found) {
            newRaw.append(abilityId).append(":").append(active).append(",");
        }

        // Remove trailing comma
        if (newRaw.length() > 0) {
            newRaw.setLength(newRaw.length() - 1);
        }

        setPersistentData(KEY_ABILITY_ACTIVE, PersistentDataType.STRING, newRaw.toString());
    }

    private static final NamespacedKey KEY_IS_DIVINE = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_is_divine");
    private static final NamespacedKey KEY_DIVINE_COOLDOWN = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_divine_cooldown");

    public boolean isDivine() {
        return getPersistentData(KEY_IS_DIVINE, PersistentDataType.INTEGER, 0) == 1;
    }

    public void setDivine(boolean divine) {
        setPersistentData(KEY_IS_DIVINE, PersistentDataType.INTEGER, divine ? 1 : 0);
    }

    public long getDivineCooldown() {
        return getPersistentData(KEY_DIVINE_COOLDOWN, PersistentDataType.LONG, 0L);
    }

    public void setDivineCooldown(long cooldown) {
        setPersistentData(KEY_DIVINE_COOLDOWN, PersistentDataType.LONG, cooldown);
    }

    private static final NamespacedKey KEY_TUTORIAL_STEP = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_tutorial_step");

    public com.livingtools.manager.GuideManager.TutorialStep getTutorialStep() {
        int id = getPersistentData(KEY_TUTORIAL_STEP, PersistentDataType.INTEGER, 0);
        return com.livingtools.manager.GuideManager.TutorialStep.fromId(id);
    }

    public void setTutorialStep(com.livingtools.manager.GuideManager.TutorialStep step) {
        setPersistentData(KEY_TUTORIAL_STEP, PersistentDataType.INTEGER, step.getId());
    }

    private static final NamespacedKey KEY_COLLECTED_TOMES = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_collected_tomes");

    public List<String> getCollectedTomes() {
        String raw = getPersistentData(KEY_COLLECTED_TOMES, PersistentDataType.STRING, "");
        List<String> tomes = new java.util.ArrayList<>();
        if (raw.isEmpty())
            return tomes;

        for (String s : raw.split(",")) {
            if (!s.isEmpty())
                tomes.add(s);
        }
        return tomes;
    }

    public void addCollectedTome(String tomeId) {
        List<String> tomes = getCollectedTomes();
        if (tomes.contains(tomeId))
            return;

        tomes.add(tomeId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tomes.size(); i++) {
            sb.append(tomes.get(i));
            if (i < tomes.size() - 1)
                sb.append(",");
        }
        setPersistentData(KEY_COLLECTED_TOMES, PersistentDataType.STRING, sb.toString());
    }

    private static final NamespacedKey KEY_MOOD = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_mood");

    public int getMood() {
        return getPersistentData(KEY_MOOD, PersistentDataType.INTEGER, 50); // Default 50 (Neutral)
    }

    public void setMood(int mood) {
        int clamped = Math.max(0, Math.min(100, mood));
        setPersistentData(KEY_MOOD, PersistentDataType.INTEGER, clamped);
    }

    public void adjustMood(int amount) {
        setMood(getMood() + amount);
    }

    private static final NamespacedKey KEY_BIOME_COLOR = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_biome_color");

    public String getBiomeColor() {
        return getPersistentData(KEY_BIOME_COLOR, PersistentDataType.STRING, "WHITE");
    }

    public void setBiomeColor(String colorName) {
        setPersistentData(KEY_BIOME_COLOR, PersistentDataType.STRING, colorName);
    }

    private static final NamespacedKey KEY_IS_BROKEN = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingtools_is_broken");

    public boolean isBroken() {
        return getPersistentData(KEY_IS_BROKEN, PersistentDataType.INTEGER, 0) == 1;
    }

    public void setBroken(boolean broken) {
        setPersistentData(KEY_IS_BROKEN, PersistentDataType.INTEGER, broken ? 1 : 0);
    }

    // Helper methods for PDC
    private <T, Z> Z getPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
        if (item == null || !item.hasItemMeta())
            return defaultValue;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.getOrDefault(key, type, defaultValue);
    }

    private <T, Z> void setPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (item == null)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        meta.getPersistentDataContainer().set(key, type, value);
        item.setItemMeta(meta);
    }
}
