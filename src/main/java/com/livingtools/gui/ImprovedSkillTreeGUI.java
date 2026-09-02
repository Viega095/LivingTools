package com.livingtools.gui;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingTool;
import com.livingtools.gui.utils.GUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Skill Tree GUI mejorado con filtrado dinámico por tipo de herramienta
 */
public class ImprovedSkillTreeGUI {

    public static void open(Player player, LivingTool tool) {
        Material toolType = tool.getItem().getType();
        String toolName = getToolTypeName(toolType);

        Inventory gui = Bukkit.createInventory(null, 54,
                GUIBuilder.createTitle("Habilidades: " + toolName));

        // Borde decorativo
        GUIBuilder.setBorder54(gui, Material.PURPLE_STAINED_GLASS_PANE);

        // Obtener habilidades filtradas
        List<Ability> compatibleAbilities = getCompatibleAbilities(toolType);
        List<Ability> generalAbilities = getGeneralAbilities();

        // Título de sección: Habilidades Específicas
        ItemStack specificTitle = GUIBuilder.createGlowingItem(
                Material.ENCHANTED_BOOK,
                GUIBuilder.PRIMARY + "⚡ Habilidades de " + toolName,
                "",
                GUIBuilder.INFO + "" + compatibleAbilities.size() + " habilidades disponibles");
        gui.setItem(4, specificTitle);

        // Mostrar habilidades específicas (slots 10-25)
        int[] specificSlots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
        displayAbilities(gui, compatibleAbilities, specificSlots, tool);

        // Título de sección: Habilidades Generales
        ItemStack generalTitle = GUIBuilder.createGlowingItem(
                Material.NETHER_STAR,
                GUIBuilder.ACCENT + "✨ Habilidades Generales",
                "",
                GUIBuilder.INFO + "" + generalAbilities.size() + " habilidades universales");
        gui.setItem(31, generalTitle);

        // Mostrar habilidades generales (slots 37-43)
        int[] generalSlots = { 37, 38, 39, 40, 41, 42, 43 };
        displayAbilities(gui, generalAbilities, generalSlots, tool);

        // Slots de runas (abajo)
        placeRuneSlots(gui, tool);

        // Botón de volver
        gui.setItem(45, GUIBuilder.createBackButton());

        // Botón de cerrar
        gui.setItem(49, GUIBuilder.createCloseButton());

        player.openInventory(gui);
    }

    private static List<Ability> getCompatibleAbilities(Material toolType) {
        return AbilityRegistry.getAbilities().values().stream()
                .filter(ability -> ability.isCompatible(toolType))
                .filter(ability -> !isGeneralAbility(ability))
                .sorted((a, b) -> Integer.compare(a.getRequiredLevel(), b.getRequiredLevel()))
                .collect(Collectors.toList());
    }

    private static List<Ability> getGeneralAbilities() {
        return AbilityRegistry.getAbilities().values().stream()
                .filter(ImprovedSkillTreeGUI::isGeneralAbility)
                .sorted((a, b) -> Integer.compare(a.getRequiredLevel(), b.getRequiredLevel()))
                .collect(Collectors.toList());
    }

    private static boolean isGeneralAbility(Ability ability) {
        // Habilidades que son compatibles con TODOS los tipos
        // Verificar si es compatible con varios tipos diferentes
        boolean compatibleWithPickaxe = ability.isCompatible(Material.DIAMOND_PICKAXE);
        boolean compatibleWithSword = ability.isCompatible(Material.DIAMOND_SWORD);
        boolean compatibleWithAxe = ability.isCompatible(Material.DIAMOND_AXE);
        boolean compatibleWithShovel = ability.isCompatible(Material.DIAMOND_SHOVEL);
        boolean compatibleWithHoe = ability.isCompatible(Material.DIAMOND_HOE);

        // Si es compatible con 4+ tipos diferentes, es general
        int compatibleCount = 0;
        if (compatibleWithPickaxe)
            compatibleCount++;
        if (compatibleWithSword)
            compatibleCount++;
        if (compatibleWithAxe)
            compatibleCount++;
        if (compatibleWithShovel)
            compatibleCount++;
        if (compatibleWithHoe)
            compatibleCount++;

        return compatibleCount >= 4;
    }

    private static void displayAbilities(Inventory gui, List<Ability> abilities, int[] slots, LivingTool tool) {
        for (int i = 0; i < Math.min(abilities.size(), slots.length); i++) {
            Ability ability = abilities.get(i);
            int slot = slots[i];

            boolean unlocked = tool.hasAbility(ability.getId());
            boolean canUnlock = tool.getData().getLevel() >= ability.getRequiredLevel();

            ItemStack item = createAbilityItem(ability, tool, unlocked, canUnlock);
            gui.setItem(slot, item);
        }
    }

    private static ItemStack createAbilityItem(Ability ability, LivingTool tool, boolean unlocked, boolean canUnlock) {
        Material icon = getAbilityIcon(ability);

        String levelSuffix = "";
        if (unlocked) {
            int level = tool.getData().getAbilityLevel(ability.getId());
            levelSuffix = ChatColor.GOLD + " [Nv. " + level + "]";
        }

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        ChatColor nameColor = unlocked ? ChatColor.GREEN : (canUnlock ? ChatColor.YELLOW : ChatColor.RED);
        meta.setDisplayName(nameColor + ability.getName() + levelSuffix);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + ability.getDescription());
        lore.add("");

        if (unlocked) {
            int xp = tool.getData().getAbilityXP(ability.getId());
            int maxXP = ability.getMaxXP();
            lore.add(ChatColor.AQUA + "XP: " + ChatColor.WHITE + xp + "/" + maxXP);
            lore.add("");
            lore.addAll(GUIBuilder.createProgressBar(xp, maxXP, 10));
            lore.add("");
            lore.add(ChatColor.GREEN + "✔ Desbloqueada");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click para ver detalles");
        } else if (canUnlock) {
            lore.add(ChatColor.YELLOW + "Nivel requerido: " + ChatColor.WHITE + ability.getRequiredLevel());
            lore.add("");
            lore.add(ChatColor.GREEN + "Click para desbloquear");
        } else {
            lore.add(ChatColor.RED + "Nivel requerido: " + ChatColor.WHITE + ability.getRequiredLevel());
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Bloqueada");
        }

        meta.setLore(lore);

        // Add Ability ID to PDC
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "gui_ability_id");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, ability.getId());

        // Add glow if unlocked
        if (unlocked) {
            GUIBuilder.addGlow(item);
        }

        item.setItemMeta(meta);
        return item;
    }

    private static Material getAbilityIcon(Ability ability) {
        // Iconos basados en el tipo de habilidad
        String id = ability.getId();

        // Mining
        if (id.contains("vein"))
            return Material.TNT;
        if (id.contains("haste"))
            return Material.GOLDEN_PICKAXE;
        if (id.contains("smelt"))
            return Material.FURNACE;

        // Combat
        if (id.contains("lightning"))
            return Material.END_ROD;
        if (id.contains("execute"))
            return Material.DIAMOND_SWORD;
        if (id.contains("vampir"))
            return Material.REDSTONE;
        if (id.contains("bleed"))
            return Material.NETHER_WART;

        // Utility
        if (id.contains("magnet"))
            return Material.IRON_INGOT;
        if (id.contains("xp"))
            return Material.EXPERIENCE_BOTTLE;
        if (id.contains("soulbound"))
            return Material.TOTEM_OF_UNDYING;

        // Armor
        if (id.contains("thermal"))
            return Material.BLAZE_POWDER;
        if (id.contains("breath"))
            return Material.TURTLE_HELMET;
        if (id.contains("shield"))
            return Material.SHIELD;
        if (id.contains("wings"))
            return Material.ELYTRA;
        if (id.contains("speed"))
            return Material.SUGAR;
        if (id.contains("stomp"))
            return Material.ANVIL;

        // Default
        return Material.ENCHANTED_BOOK;
    }

    private static void placeRuneSlots(Inventory gui, LivingTool tool) {
        List<com.livingtools.runes.RuneType> runes = tool.getData().getRunes();
        List<com.livingtools.runes.RuneManager.RuneTier> tiers = tool.getData().getRuneTiers();

        placeRuneSlot(gui, 46, tool.getData().getLevel() >= 25,
                runes.size() > 0 ? runes.get(0) : null,
                tiers.size() > 0 ? tiers.get(0) : com.livingtools.runes.RuneManager.RuneTier.COMMON, 0);

        placeRuneSlot(gui, 52, tool.getData().getLevel() >= 50,
                runes.size() > 1 ? runes.get(1) : null,
                tiers.size() > 1 ? tiers.get(1) : com.livingtools.runes.RuneManager.RuneTier.COMMON, 1);

        placeRuneSlot(gui, 53, tool.getData().getLevel() >= 75,
                runes.size() > 2 ? runes.get(2) : null,
                tiers.size() > 2 ? tiers.get(2) : com.livingtools.runes.RuneManager.RuneTier.COMMON, 2);
    }

    private static void placeRuneSlot(Inventory gui, int slot, boolean unlocked,
            com.livingtools.runes.RuneType rune,
            com.livingtools.runes.RuneManager.RuneTier tier, int index) {
        ItemStack item;
        if (!unlocked) {
            item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_GRAY + "Slot de Runa Bloqueado");
            meta.setLore(List.of(ChatColor.GRAY + "Se desbloquea en nivel " + ((index + 1) * 25)));
            item.setItemMeta(meta);
        } else if (rune != null) {
            item = new ItemStack(rune.getMaterial());
            ItemMeta meta = item.getItemMeta();

            String tierSymbol = tier == com.livingtools.runes.RuneManager.RuneTier.RARE ? "★★"
                    : tier == com.livingtools.runes.RuneManager.RuneTier.MYTHIC ? "★★★" : "★";

            meta.setDisplayName(tier.getColor() + "Runa " + rune.getName() + " " + tierSymbol);
            meta.setLore(List.of(
                    ChatColor.GRAY + rune.getDescription(),
                    ChatColor.DARK_GRAY + "Tier: " + tier.name(),
                    "",
                    ChatColor.YELLOW + "Click para remover"));

            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_rune_index");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, index);

            item.setItemMeta(meta);
        } else {
            item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Slot de Runa Vacío");
            meta.setLore(List.of(ChatColor.GRAY + "Arrastra una runa aquí para equipar"));

            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_rune_index");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, index);

            item.setItemMeta(meta);
        }
        gui.setItem(slot, item);
    }

    private static String getToolTypeName(Material type) {
        String name = type.name();

        if (name.endsWith("_PICKAXE"))
            return "Pico";
        if (name.endsWith("_AXE"))
            return "Hacha";
        if (name.endsWith("_SHOVEL"))
            return "Pala";
        if (name.endsWith("_HOE"))
            return "Azada";
        if (name.endsWith("_SWORD"))
            return "Espada";
        if (name.endsWith("_HELMET"))
            return "Casco";
        if (name.endsWith("_CHESTPLATE"))
            return "Pechera";
        if (name.endsWith("_LEGGINGS"))
            return "Pantalones";
        if (name.endsWith("_BOOTS"))
            return "Botas";

        return "Herramienta";
    }
}
