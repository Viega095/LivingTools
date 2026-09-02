package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArtifactManager implements Listener {

    public enum ArtifactType {
        RING, AMULET, CHARM, SOUL_GEM, POUCH, GEODE, RUNE_POUCH, LIVING_CHARM
    }

    public static final org.bukkit.NamespacedKey KEY_ARTIFACT_TYPE = new org.bukkit.NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "artifact_type");

    public static ItemStack createArtifact(ArtifactType type) {
        switch (type) {
            case RING:
                return createRingOfTheForge();
            case AMULET:
                return createAmuletOfTime();
            case RUNE_POUCH:
                ItemStack pouch = new ItemStack(Material.BUNDLE);
                ItemMeta meta = pouch.getItemMeta();
                meta.setDisplayName(MessageUtils.color("&5Bolsa de Runas"));
                List<String> lore = new ArrayList<>();
                lore.add(MessageUtils.color("&7Guarda hasta 9 runas."));
                lore.add(MessageUtils.color("&7Clic derecho para abrir."));
                meta.setLore(lore);
                meta.getPersistentDataContainer().set(KEY_ARTIFACT_TYPE,
                        org.bukkit.persistence.PersistentDataType.STRING, type.name());
                pouch.setItemMeta(meta);
                return pouch;
            case LIVING_CHARM:
                ItemStack charm = new ItemStack(Material.EMERALD);
                ItemMeta cMeta = charm.getItemMeta();
                cMeta.setDisplayName(MessageUtils.color("&aEncanto Viviente"));
                List<String> charmLore = new ArrayList<>();
                charmLore.add(MessageUtils.color("&7Mientras esté en tu inventario:"));
                charmLore.add(MessageUtils.color("&a+10% XP &7de herramientas vivientes."));
                cMeta.setLore(charmLore);
                cMeta.getPersistentDataContainer().set(KEY_ARTIFACT_TYPE,
                        org.bukkit.persistence.PersistentDataType.STRING, type.name());
                charm.setItemMeta(cMeta);
                return charm;
            case SOUL_GEM:
                ItemStack gem = new ItemStack(Material.AMETHYST_SHARD);
                ItemMeta gMeta = gem.getItemMeta();
                gMeta.setDisplayName(MessageUtils.color("&5Gema del Alma"));
                List<String> gemLore = new ArrayList<>();
                gemLore.add(MessageUtils.color("&7Absorbe almas al matar mobs."));
                gemLore.add(MessageUtils.color("&7Clic derecho con 10 almas:"));
                gemLore.add(MessageUtils.color("&7repara una herramienta dañada."));
                gemLore.add(ChatColor.LIGHT_PURPLE + "Almas: 0/10");
                gMeta.setLore(gemLore);
                gMeta.getPersistentDataContainer().set(KEY_ARTIFACT_TYPE,
                        org.bukkit.persistence.PersistentDataType.STRING, type.name());
                gem.setItemMeta(gMeta);
                return gem;
            default:
                return null;
        }
    }

    public static ItemStack createRingOfTheForge() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET); // Placeholder material
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&#FFAA00Anillo de la Forja"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Un anillo antiguo que vibra con calor."));
        lore.add(MessageUtils.color("&6+10% Eficiencia de Reparación"));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(KEY_ARTIFACT_TYPE, org.bukkit.persistence.PersistentDataType.STRING,
                ArtifactType.RING.name());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createAmuletOfTime() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&#00AAFFAmuleto del Tiempo"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Las manecillas giran erráticamente."));
        lore.add(MessageUtils.color("&b-10% Cooldown de Habilidades"));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(KEY_ARTIFACT_TYPE, org.bukkit.persistence.PersistentDataType.STRING,
                ArtifactType.AMULET.name());
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isArtifact(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_ARTIFACT_TYPE,
                org.bukkit.persistence.PersistentDataType.STRING);
    }

    public static ArtifactType getArtifactType(ItemStack item) {
        if (!isArtifact(item))
            return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(KEY_ARTIFACT_TYPE,
                org.bukkit.persistence.PersistentDataType.STRING);
        try {
            return ArtifactType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean hasArtifact(Player player, ArtifactType type) {
        if (player == null || type == null) {
            return false;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && getArtifactType(item) == type) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack findArtifact(Player player, ArtifactType type) {
        if (player == null || type == null) {
            return null;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && getArtifactType(item) == type) {
                return item;
            }
        }
        return null;
    }

    @EventHandler
    public void onHold(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            if (item.getItemMeta().getDisplayName().contains("Anillo de la Forja")) {
                MessageUtils.sendActionBar(player, "&6[Anillo de la Forja Activo]");
            }
        }
    }
}
