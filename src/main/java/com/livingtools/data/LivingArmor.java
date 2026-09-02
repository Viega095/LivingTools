package com.livingtools.data;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class LivingArmor {

    private final ItemStack item;
    private static final NamespacedKey KEY_ARMOR_XP = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_xp");
    private static final NamespacedKey KEY_ARMOR_LEVEL = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_level");
    private static final NamespacedKey KEY_ARMOR_PERSONALITY = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_personality");

    public LivingArmor(ItemStack item) {
        this.item = item;
    }

    public static boolean isLivingArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_ARMOR_LEVEL, PersistentDataType.INTEGER);
    }

    public void initialize() {
        setLevel(1);
        setXP(0);

        // Random Personality
        com.livingtools.mechanics.ArmorPersonality[] personalities = com.livingtools.mechanics.ArmorPersonality
                .values();
        setPersonality(personalities[(int) (Math.random() * personalities.length)].name());

        updateLore();
    }

    public void addXP(Player player, long amount) {
        long currentXP = getXP();
        setXP(currentXP + amount);
        checkLevelUp(player);
        updateLore();
    }

    private void checkLevelUp(Player player) {
        int currentLevel = getLevel();
        long requiredXP = currentLevel * 150L; // Harder to level up armor

        if (getXP() >= requiredXP) {
            setLevel(currentLevel + 1);
            String itemName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
                    : item.getType().name();
            player.sendMessage(ChatColor.AQUA + "¡Tu " + itemName + ChatColor.AQUA + " ha subido al nivel "
                    + (currentLevel + 1) + "!");
            com.livingtools.utils.MessageUtils.playLevelUpEffects(player);
        }
    }

    public void updateLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        // Display Name
        String displayName = ChatColor.AQUA + "Armadura Viviente" + ChatColor.GRAY + " [" + ChatColor.YELLOW + "Lvl "
                + getLevel() + ChatColor.GRAY + "]";
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Nivel: " + ChatColor.YELLOW + getLevel());
        lore.add(ChatColor.GRAY + "XP: " + ChatColor.YELLOW + getXP() + ChatColor.GRAY + " / " + ChatColor.GOLD
                + (getLevel() * 150L));

        String personalityName = getPersonality();
        if (personalityName != null) {
            try {
                com.livingtools.mechanics.ArmorPersonality p = com.livingtools.mechanics.ArmorPersonality
                        .valueOf(personalityName);
                lore.add("");
                lore.add(ChatColor.GRAY + "Personalidad: " + ChatColor.LIGHT_PURPLE + p.getDisplayName());
                lore.add(ChatColor.GRAY + "Efecto: " + ChatColor.GREEN + p.getDescription());
            } catch (IllegalArgumentException ignored) {
            }
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

        // Unbreakable & Flags
        // Unbreakable & Flags
        meta.setUnbreakable(false);
        meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

        // Glint
        if (!meta.hasEnchant(org.bukkit.enchantments.Enchantment.DURABILITY)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
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
        item.setItemMeta(meta);
    }

    public String getPersonality() {
        return getPersistentData(KEY_ARMOR_PERSONALITY, PersistentDataType.STRING, null);
    }

    public void setPersonality(String personality) {
        setPersistentData(KEY_ARMOR_PERSONALITY, PersistentDataType.STRING, personality);
    }

    // Getters & Setters
    public long getXP() {
        return getPersistentData(KEY_ARMOR_XP, PersistentDataType.LONG, 0L);
    }

    public void setXP(long xp) {
        setPersistentData(KEY_ARMOR_XP, PersistentDataType.LONG, xp);
    }

    private static final NamespacedKey KEY_ARMOR_TRIAL = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_trial");
    private static final NamespacedKey KEY_ARMOR_TRIAL_PROGRESS = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_trial_progress");

    public String getTrial() {
        return getPersistentData(KEY_ARMOR_TRIAL, PersistentDataType.STRING, "NONE");
    }

    public void setTrial(String trial) {
        setPersistentData(KEY_ARMOR_TRIAL, PersistentDataType.STRING, trial);
    }

    public int getTrialProgress() {
        return getPersistentData(KEY_ARMOR_TRIAL_PROGRESS, PersistentDataType.INTEGER, 0);
    }

    public void setTrialProgress(int progress) {
        setPersistentData(KEY_ARMOR_TRIAL_PROGRESS, PersistentDataType.INTEGER, progress);
    }

    public int getLevel() {
        return getPersistentData(KEY_ARMOR_LEVEL, PersistentDataType.INTEGER, 1);
    }

    public void setLevel(int level) {
        setPersistentData(KEY_ARMOR_LEVEL, PersistentDataType.INTEGER, level);
    }

    private static final NamespacedKey KEY_ARMOR_ABILITIES = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_abilities");

    public List<String> getAbilities() {
        String data = getPersistentData(KEY_ARMOR_ABILITIES, PersistentDataType.STRING, "");
        List<String> abilities = new ArrayList<>();
        if (data.isEmpty())
            return abilities;

        for (String s : data.split(",")) {
            if (!s.isEmpty())
                abilities.add(s);
        }
        return abilities;
    }

    public void addAbility(String abilityId) {
        List<String> abilities = getAbilities();
        if (!abilities.contains(abilityId)) {
            abilities.add(abilityId);
            setPersistentData(KEY_ARMOR_ABILITIES, PersistentDataType.STRING, String.join(",", abilities));
            updateLore();
        }
    }

    public boolean hasAbility(String abilityId) {
        return getAbilities().contains(abilityId);
    }

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

    private static final NamespacedKey KEY_ARMOR_BROKEN = new NamespacedKey(LivingToolsPlugin.getInstance(),
            "livingarmor_is_broken");

    public boolean isBroken() {
        return getPersistentData(KEY_ARMOR_BROKEN, PersistentDataType.INTEGER, 0) == 1;
    }

    public void setBroken(boolean broken) {
        setPersistentData(KEY_ARMOR_BROKEN, PersistentDataType.INTEGER, broken ? 1 : 0);
        updateLore();
    }

}
