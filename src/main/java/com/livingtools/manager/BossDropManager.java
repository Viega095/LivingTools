package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossDropManager implements Listener {

    private final Random random = new Random();
    private static final List<org.bukkit.block.Block> activeTrophies = new ArrayList<>();

    private static NamespacedKey bossTypeKey(String id) {
        return new NamespacedKey(LivingToolsPlugin.getInstance(), id);
    }

    public static void init() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (activeTrophies.isEmpty()) {
                    return;
                }
                java.util.Iterator<org.bukkit.block.Block> it = activeTrophies.iterator();
                while (it.hasNext()) {
                    org.bukkit.block.Block b = it.next();
                    if (b.getType() != Material.PLAYER_HEAD && b.getType() != Material.PLAYER_WALL_HEAD) {
                        it.remove();
                        continue;
                    }
                    for (org.bukkit.entity.Player p : b.getWorld().getPlayers()) {
                        if (p.getLocation().distance(b.getLocation()) <= 10) {
                            applyTrophyEffect(b, p);
                        }
                    }
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 100L);
    }

    private static void applyTrophyEffect(org.bukkit.block.Block block, org.bukkit.entity.Player player) {
        if (!block.hasMetadata("living_trophy")) {
            return;
        }
        String type = block.getMetadata("living_trophy").get(0).asString();
        switch (type) {
            case "LIVING":
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 120, 0));
                break;
            case "SERAPHIM":
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.REGENERATION, 120, 0));
                break;
            case "TITAN":
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 120, 0));
                break;
            case "DRYAD":
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.REGENERATION, 120, 0));
                break;
            case "WYRM":
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 120, 0));
                break;
            case "LEVIATHAN":
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.WATER_BREATHING, 120, 0));
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getKiller() != null && isTrackedBoss(entity)) {
            LeaderboardManager.updateBossKill(entity.getKiller());
            DiscordManager.sendBossKill(entity.getKiller().getName(),
                    entity.getCustomName() != null ? entity.getCustomName() : entity.getName());
        }

        if (isLivingBoss(entity)) {
            dropLootBag(event.getEntity().getLocation(), "LIVING");
            event.getDrops().clear();
        } else if (isSeraphim(entity)) {
            dropLootBag(event.getEntity().getLocation(), "SERAPHIM");
            event.getDrops().clear();
        } else if (isTitan(entity)) {
            dropLootBag(event.getEntity().getLocation(), "TITAN");
            event.getDrops().clear();
        } else if (isDryad(entity)) {
            dropLootBag(event.getEntity().getLocation(), "DRYAD");
            event.getDrops().clear();
        } else if (isWyrm(entity)) {
            dropLootBag(event.getEntity().getLocation(), "WYRM");
            event.getDrops().clear();
        } else if (isLeviathan(entity)) {
            dropLootBag(event.getEntity().getLocation(), "LEVIATHAN");
            event.getDrops().clear();
        } else if (isShadowCreature(entity)) {
            handleShadowCreatureDrops(event);
        } else if (isMinion(entity)) {
            handleMinionDrops(event);
        }
    }

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }

        org.bukkit.entity.Player player = event.getPlayer();
        if (isCraftingGuiOpen(player)) {
            return;
        }

        ItemStack item = event.getItem();

        if (BossDropType.isCraftingOnlyDrop(item)) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.color("&cEste objeto es mágico y solo sirve para craftear."));
            return;
        }

        if (event.getAction().name().contains("RIGHT")) {
            if (item.getType() == Material.BUNDLE || item.getType() == Material.CHEST) {
                if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Bolsa de Botín")) {
                    event.setCancelled(true);
                    openLootBag(player, item);
                }
            }
        }
    }

    private static boolean isCraftingGuiOpen(org.bukkit.entity.Player player) {
        String title = player.getOpenInventory().getTitle();
        return title.equals(com.livingtools.gui.AssemblyGUI.TITLE)
                || title.equals(com.livingtools.gui.BossForgeGUI.TITLE)
                || org.bukkit.ChatColor.stripColor(title)
                        .equals(org.bukkit.ChatColor.stripColor(com.livingtools.gui.BossForgeGUI.TITLE));
    }

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) {
            return;
        }

        if (BossDropType.isCraftingOnlyDrop(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.color("&cEste objeto es mágico y no se puede colocar."));
            return;
        }

        if (item.getType() == Material.PLAYER_HEAD && item.getItemMeta().getDisplayName().contains("Trofeo")) {
            String type = resolveTrophyType(item.getItemMeta().getDisplayName());
            if (!type.isEmpty()) {
                event.getBlockPlaced().setMetadata("living_trophy",
                        new org.bukkit.metadata.FixedMetadataValue(LivingToolsPlugin.getInstance(), type));
                activeTrophies.add(event.getBlockPlaced());
                event.getPlayer()
                        .sendMessage(MessageUtils.color("&a¡Trofeo colocado! Otorgará beneficios en esta área."));
            }
        }
    }

    private String resolveTrophyType(String displayName) {
        if (displayName.contains("Living Boss")) {
            return "LIVING";
        }
        if (displayName.contains("Serafín") || displayName.contains("Seraphim")) {
            return "SERAPHIM";
        }
        if (displayName.contains("Titán") || displayName.contains("Titan")) {
            return "TITAN";
        }
        if (displayName.contains("Dríade") || displayName.contains("Dryad")) {
            return "DRYAD";
        }
        if (displayName.contains("Wyrm")) {
            return "WYRM";
        }
        if (displayName.contains("Leviatán") || displayName.contains("Leviathan")) {
            return "LEVIATHAN";
        }
        return "";
    }

    private boolean isTrackedBoss(LivingEntity entity) {
        return isLivingBoss(entity) || isSeraphim(entity) || isTitan(entity) || isShadowCreature(entity)
                || isDryad(entity) || isWyrm(entity) || isLeviathan(entity);
    }

    private boolean isLivingBoss(LivingEntity entity) {
        return entity.getCustomName() != null && entity.getCustomName().contains("Living Boss") && !isMinion(entity);
    }

    private boolean isSeraphim(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_seraphim"), PersistentDataType.BYTE);
    }

    private boolean isTitan(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_titan"), PersistentDataType.BYTE);
    }

    private boolean isDryad(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_dryad"), PersistentDataType.BYTE);
    }

    private boolean isWyrm(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_wyrm"), PersistentDataType.BYTE);
    }

    private boolean isLeviathan(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_leviathan"), PersistentDataType.BYTE);
    }

    private boolean isShadowCreature(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_shadow_creature"), PersistentDataType.BYTE);
    }

    private boolean isMinion(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(bossTypeKey("is_minion"), PersistentDataType.BYTE);
    }

    public static void tagDryad(LivingEntity entity) {
        entity.getPersistentDataContainer().set(bossTypeKey("is_dryad"), PersistentDataType.BYTE, (byte) 1);
    }

    public static void tagWyrm(LivingEntity entity) {
        entity.getPersistentDataContainer().set(bossTypeKey("is_wyrm"), PersistentDataType.BYTE, (byte) 1);
    }

    public static void tagLeviathan(LivingEntity entity) {
        entity.getPersistentDataContainer().set(bossTypeKey("is_leviathan"), PersistentDataType.BYTE, (byte) 1);
    }

    private void handleShadowCreatureDrops(EntityDeathEvent event) {
        if (random.nextDouble() < 0.3) {
            event.getDrops().add(BossDropType.SHADOW_ESSENCE.create());
        }
    }

    private void handleMinionDrops(EntityDeathEvent event) {
        event.getDrops().clear();
        LivingEntity entity = event.getEntity();
        String name = entity.getCustomName();

        if (name == null) {
            return;
        }
        if (name.contains("Living Boss Minion")) {
            event.getDrops().add(BossDropType.MAGMA_CORE.create());
        } else if (name.contains("Cherub")) {
            event.getDrops().add(BossDropType.ANGEL_DUST.create());
        } else if (name.contains("Void Parasite")) {
            event.getDrops().add(BossDropType.VOID_SCALE.create());
        }
    }

    private void dropLootBag(org.bukkit.Location loc, String type) {
        ItemStack bag = new ItemStack(Material.CHEST);
        ItemMeta meta = bag.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&6&lBolsa de Botín: " + formatBossName(type)));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Haz clic derecho para abrir."));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(bossTypeKey("loot_type"), PersistentDataType.STRING, type);
        bag.setItemMeta(meta);
        loc.getWorld().dropItemNaturally(loc, bag);
    }

    private String formatBossName(String type) {
        switch (type) {
            case "LIVING":
                return "Living Boss";
            case "SERAPHIM":
                return "Serafín";
            case "TITAN":
                return "Titán";
            case "DRYAD":
                return "Dríade";
            case "WYRM":
                return "Wyrm";
            case "LEVIATHAN":
                return "Leviatán";
            default:
                return type;
        }
    }

    private void openLootBag(org.bukkit.entity.Player player, ItemStack bag) {
        String type = bag.getItemMeta().getPersistentDataContainer().get(bossTypeKey("loot_type"),
                PersistentDataType.STRING);
        if (type == null) {
            return;
        }

        bag.setAmount(bag.getAmount() - 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1, 1);
        player.spawnParticle(org.bukkit.Particle.TOTEM, player.getLocation(), 20);

        switch (type) {
            case "LIVING":
                player.getInventory().addItem(BossDropType.MAGMA_CORE.create());
                if (random.nextDouble() < 0.05) {
                    player.getInventory().addItem(createHead("Trofeo Living Boss", "MHF_LavaSlime", "LIVING"));
                }
                break;
            case "SERAPHIM":
                player.getInventory().addItem(BossDropType.SERAPHIM_FEATHER.create());
                if (random.nextDouble() < 0.05) {
                    player.getInventory().addItem(createHead("Trofeo Serafín", "MHF_Blaze", "SERAPHIM"));
                }
                break;
            case "TITAN":
                player.getInventory().addItem(BossDropType.TITAN_SHARD.create());
                if (random.nextDouble() < 0.05) {
                    player.getInventory().addItem(createHead("Trofeo Titán", "MHF_Enderman", "TITAN"));
                }
                break;
            case "DRYAD":
                player.getInventory().addItem(BossDropType.DRYAD_HEARTWOOD.create());
                player.getInventory().addItem(new ItemStack(Material.EMERALD, 4));
                if (random.nextDouble() < 0.05) {
                    player.getInventory().addItem(createHead("Trofeo Dríade", "MHF_Oak", "DRYAD"));
                }
                break;
            case "WYRM":
                player.getInventory().addItem(BossDropType.WYRM_CORE.create());
                player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 8));
                if (random.nextDouble() < 0.05) {
                    player.getInventory().addItem(createHead("Trofeo Wyrm", "MHF_Question", "WYRM"));
                }
                break;
            case "LEVIATHAN":
                player.getInventory().addItem(BossDropType.LEVIATHAN_CORE.create());
                player.getInventory().addItem(new ItemStack(Material.PRISMARINE_SHARD, 8));
                if (random.nextDouble() < 0.03) {
                    player.getInventory().addItem(createAbilityToken("vortex"));
                }
                if (random.nextDouble() < 0.03) {
                    player.getInventory().addItem(createAbilityToken("timefreeze"));
                }
                if (random.nextDouble() < 0.05) {
                    player.getInventory().addItem(createHead("Trofeo Leviatán", "MHF_Squid", "LEVIATHAN"));
                }
                break;
            default:
                break;
        }

        player.sendMessage(MessageUtils.color("&a¡Has abierto la bolsa de botín!"));
    }

    private ItemStack createAbilityToken(String abilityId) {
        ItemStack token = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = token.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&d✦ Token de Habilidad: " + abilityId.toUpperCase()));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.color("&7Click derecho en una Living Tool"));
        lore.add(MessageUtils.color("&7para desbloquear esta habilidad"));
        lore.add("");
        lore.add(MessageUtils.color("&eHabilidad: &b" + abilityId));
        meta.setLore(lore);
        token.setItemMeta(meta);
        return token;
    }

    public static ItemStack createDryadHeartwood() {
        return BossDropType.DRYAD_HEARTWOOD.create();
    }

    public static ItemStack createWyrmCore() {
        return BossDropType.WYRM_CORE.create();
    }

    public static ItemStack createLeviathanCore() {
        return BossDropType.LEVIATHAN_CORE.create();
    }

    @SuppressWarnings("deprecation")
    private ItemStack createHead(String name, String playerName, String trophyType) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.color("&6" + name));
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            List<String> lores = new ArrayList<>();
            lores.add(MessageUtils.color("&7Colócalo para obtener beneficios."));
            lores.add(MessageUtils.color("&8Trofeo: " + trophyType));
            meta.setLore(lores);
            head.setItemMeta(meta);
        }
        return head;
    }
}
