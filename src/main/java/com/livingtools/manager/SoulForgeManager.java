package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SoulForgeManager {

    public static final NamespacedKey KEY_SOUL_TYPE = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "soul_type");

    public enum SoulType {
        CREEPER("Alma de Creeper", "Minería Explosiva", EntityType.CREEPER),
        ENDERMAN("Alma de Enderman", "Teletransporte al Golpear", EntityType.ENDERMAN),
        SKELETON("Alma de Esqueleto", "Proyectiles Perforantes", EntityType.SKELETON),
        ZOMBIE("Alma de Zombie", "Robo de Vida", EntityType.ZOMBIE),
        SPIDER("Alma de Araña", "Veneno", EntityType.SPIDER),
        BLAZE("Alma de Blaze", "Aspecto Ígneo II", EntityType.BLAZE);

        private final String name;
        private final String trait;
        private final EntityType entityType;

        SoulType(String name, String trait, EntityType entityType) {
            this.name = name;
            this.trait = trait;
            this.entityType = entityType;
        }

        public String getName() {
            return name;
        }

        public String getTrait() {
            return trait;
        }

        public EntityType getEntityType() {
            return entityType;
        }
    }

    public static ItemStack createSoulGem(SoulType type) {
        ItemStack gem = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = gem.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&d" + type.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Un fragmento de alma capturado."));
        lore.add("");
        lore.add(MessageUtils.color("&eRasgo: &f" + type.getTrait()));
        lore.add(MessageUtils.color("&7Úsalo en la Forja de Almas."));

        meta.setLore(lore);
        meta.getPersistentDataContainer().set(KEY_SOUL_TYPE, PersistentDataType.STRING, type.name());
        gem.setItemMeta(meta);

        return gem;
    }

    public static boolean isSoulGem(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_SOUL_TYPE, PersistentDataType.STRING);
    }

    public static SoulType getSoulType(ItemStack item) {
        if (!isSoulGem(item))
            return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(KEY_SOUL_TYPE, PersistentDataType.STRING);
        try {
            return SoulType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
