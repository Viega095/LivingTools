package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.entities.GenesisAvatarBoss;
import com.livingtools.gui.*;
import com.livingtools.manager.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * AdminTestListener — Ejecuta las pruebas y verificaciones en 1-click desde AdminTestingGUI.
 */
public class AdminTestListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(AdminTestingGUI.TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        LivingTool tool = ensureLivingTool(player);

        switch (slot) {
            // Fila 2: GUIs Principales
            case 10:
                DashboardGUI.open(player, tool);
                break;
            case 11:
                TalentTreeGUI.open(player, tool);
                break;
            case 12:
                BestiaryGUI.open(player, 0, BestiaryGUI.BestiaryCategory.ALL);
                break;
            case 13:
                RecipeGUI.open(player);
                break;
            case 14:
                AscensionGUI.open(player, tool);
                break;
            case 15:
                SoulTarotGUI.open(player);
                break;
            case 16:
                GemSocketGUI.open(player, tool);
                break;

            // Fila 3: GUIs Especiales
            case 19:
                AbyssalRiftGUI.open(player);
                break;
            case 20:
                RhythmicForgeGUI.open(player, tool);
                break;
            case 21:
                BountyContractGUI.open(player);
                break;
            case 22:
                ToolReforgeGUI.open(player, tool);
                break;
            case 23:
                SoulFusionGUI.open(player);
                break;
            case 24:
                CosmeticTrailGUI.open(player, tool);
                break;
            case 25:
                player.closeInventory();
                player.performCommand("livingtool guild info");
                break;

            // Fila 4: Pruebas de Animación, Reino y Jefes
            case 28:
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "☄ ¡Lanzando animación de meteorito en tu posición!");
                SoulCompassManager.playMeteorFallCinematic(player.getLocation().add(8, 0, 8));
                break;
            case 29:
                player.closeInventory();
                LivingRealmManager.teleportToRealm(player, LivingRealmManager.getRiftArenaSpawn());
                break;
            case 30:
                player.closeInventory();
                LivingRealmManager.teleportToRealm(player, LivingRealmManager.getGenesisArenaSpawn());
                break;
            case 31:
                player.closeInventory();
                LivingRealmManager.teleportToRealm(player, LivingRealmManager.getForgeTempleSpawn());
                player.sendMessage(ChatColor.AQUA + "✦ Teletransportado al Templo de la Forja Rítmica.");
                break;
            case 32:
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "✦ ¡Invocando al Avatar del Génesis!");
                GenesisAvatarBoss.spawn(player.getLocation());
                break;
            case 33:
                player.closeInventory();
                player.performCommand("livingtool admin spawnboss LIVING");
                break;
            case 34:
                giveMaxTestKit(player);
                player.sendMessage(ChatColor.GREEN + "🎁 ¡Kit de pruebas completo entregado con éxito!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                break;

            // Fila 5: Nuevas Pruebas de Forja y Utilidades
            case 37:
                for (StructureCoreManager.StructureType st : StructureCoreManager.StructureType.values()) {
                    player.getInventory().addItem(StructureCoreManager.createCoreItem(st));
                }
                player.sendMessage(ChatColor.AQUA + "✦ Pack completo de 8 Núcleos Desplegables entregado al inventario.");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                break;
            case 38:
                // Dañar / romper herramienta en mano
                ItemStack heldItem = tool.getItem();
                int maxDur = heldItem.getType().getMaxDurability();
                if (heldItem.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable && maxDur > 0) {
                    org.bukkit.inventory.meta.Damageable d = (org.bukkit.inventory.meta.Damageable) heldItem.getItemMeta();
                    d.setDamage(maxDur - 5); // 5 de durabilidad restante
                    heldItem.setItemMeta((org.bukkit.inventory.meta.ItemMeta) d);
                }
                tool.setBroken(true);
                tool.updateLore();
                player.sendMessage(ChatColor.RED + "💥 ¡Herramienta en mano dañada y marcada como ROTA! Ve a la Forja Rítmica para probar su reparación.");
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);
                break;
            case 39:
                SoulElementalSealManager.applySeal(tool.getItem(), SoulElementalSealManager.ElementalSeal.VOLCANIC_FURY);
                player.sendMessage(ChatColor.RED + "🔥 ¡Sello de Furia Volcánica aplicado a tu herramienta!");
                player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1f, 1.2f);
                break;
            case 40:
                SoulElementalSealManager.applySeal(tool.getItem(), SoulElementalSealManager.ElementalSeal.GLACIAL_FROST);
                player.sendMessage(ChatColor.AQUA + "❄ ¡Sello de Escarcha Glacial aplicado a tu herramienta!");
                player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_BREAK, 1f, 1.2f);
                break;
            case 41:
                SoulElementalSealManager.applySeal(tool.getItem(), SoulElementalSealManager.ElementalSeal.CELESTIAL_THUNDER);
                player.sendMessage(ChatColor.YELLOW + "⚡ ¡Sello de Trueno Celestial aplicado a tu herramienta!");
                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1f, 1.2f);
                break;
            case 42:
                SoulElementalSealManager.applySeal(tool.getItem(), SoulElementalSealManager.ElementalSeal.VOID_VORTEX);
                player.sendMessage(ChatColor.DARK_PURPLE + "🌑 ¡Sello del Vórtice del Vacío aplicado a tu herramienta!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
                break;

            // Fila 6: Herramientas y Controles
            case 45:
                player.closeInventory();
                RecipeValidationManager.sendAuditReport(player);
                break;
            case 46:
                com.livingtools.gui.category.StructureCoresCategoryGUI.open(player);
                break;
            case 47:
                player.closeInventory();
                AutoUpdateManager updateManager = AutoUpdateManager.getInstance();
                if (updateManager != null) {
                    if (event.isShiftClick()) {
                        updateManager.downloadAndInstall(player, true);
                    } else if (event.isRightClick()) {
                        updateManager.performHotReload(player);
                    } else {
                        updateManager.checkForUpdates(player, true);
                    }
                }
                break;
            case 49:
                player.closeInventory();
                break;
            case 53:
                player.getInventory().addItem(SoulCompassManager.createSoulCompass());
                player.sendMessage(ChatColor.AQUA + "🧭 Brújula de Almas entregada.");
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f);
                break;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    private LivingTool ensureLivingTool(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (LivingTool.isLivingTool(held)) {
            return new LivingTool(held);
        }
        ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
        LivingTool newTool = new LivingTool(pick);
        newTool.getData().setLevel(100);
        newTool.getData().setXP(50000);
        newTool.getData().setPrestige(2);
        newTool.updateLore();
        player.getInventory().setItemInMainHand(pick);
        return newTool;
    }

    private void giveMaxTestKit(Player player) {
        // Herramienta Lv100
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        LivingTool swordTool = new LivingTool(sword);
        swordTool.getData().setLevel(100);
        swordTool.getData().setPrestige(2);
        swordTool.getData().setXP(100000);
        swordTool.updateLore();
        player.getInventory().addItem(sword);

        // Gemas y Brújula
        player.getInventory().addItem(SoulCompassManager.createSoulCompass());
        player.getInventory().addItem(GemSocketManager.createGemItem(GemSocketManager.GemType.PHOENIX_GEM));
        player.getInventory().addItem(GemSocketManager.createGemItem(GemSocketManager.GemType.CHRONO_GEM));
        player.getInventory().addItem(GemSocketManager.createGemItem(GemSocketManager.GemType.COSMIC_FORTUNE_GEM));
        player.getInventory().addItem(GemSocketManager.createGemItem(GemSocketManager.GemType.VOID_STAR_GEM));
        player.getInventory().addItem(GuideBookManager.getGuideBook());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle() == null) return;
        if (event.getView().getTitle().equals(AdminTestingGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
