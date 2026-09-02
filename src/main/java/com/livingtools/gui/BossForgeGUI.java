package com.livingtools.gui;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.gui.utils.GUIBuilder;
import com.livingtools.manager.BossCraftingManager;
import com.livingtools.manager.BossDropType;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Supplier;

public class BossForgeGUI {

    public static final String TITLE = MessageUtils.color("&4&lForja de Jefes");

    public static final int[] INPUT_SLOTS = { 12, 20, 21, 22, 30 };
    public static final int CONFIRM_SLOT = 23;
    public static final int OUTPUT_SLOT = 24;
    public static final int INFO_SLOT = 40;
    private static final String CONFIRM_LABEL = "Forjar";

    private enum ForgeRecipe {
        SOCKET_EXPANDER(BossCraftingManager::createSocketExpander),
        REPAIR_KIT(BossCraftingManager::createRepairKit),
        ANGEL_WINGS(BossCraftingManager::createAngelWings),
        SERAPHIM_HALO(BossCraftingManager::createSeraphimHalo),
        TITAN_RUNE(BossCraftingManager::createTitanRune);

        private final Supplier<ItemStack> factory;

        ForgeRecipe(Supplier<ItemStack> factory) {
            this.factory = factory;
        }

        ItemStack createResult() {
            return factory.get();
        }
    }

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE);

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(" ");
        border.setItemMeta(meta);

        for (int i = 0; i < 45; i++) {
            gui.setItem(i, border);
        }

        for (int slot : INPUT_SLOTS) {
            gui.setItem(slot, null);
        }
        setPreview(gui, null);

        ItemStack info = new ItemStack(Material.NETHER_STAR);
        org.bukkit.inventory.meta.ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.color("&eInformación"));
        infoMeta.setLore(Arrays.asList(
                MessageUtils.color("&7Coloca los materiales en forma de cruz."),
                MessageUtils.color("&7El resultado aparece a la derecha."),
                MessageUtils.color("&7Usa el botón &a" + CONFIRM_LABEL + " &7para craftear."),
                MessageUtils.color("&7Guía: /livingtool structure bossforge")));
        info.setItemMeta(infoMeta);
        gui.setItem(INFO_SLOT, info);

        player.openInventory(gui);
    }

    public static boolean isInputSlot(int slot) {
        for (int input : INPUT_SLOTS) {
            if (input == slot) {
                return true;
            }
        }
        return false;
    }

    public static boolean isProtectedSlot(int slot) {
        return slot == CONFIRM_SLOT || slot == OUTPUT_SLOT || slot == INFO_SLOT;
    }

    public static void updateResult(Inventory inv) {
        ForgeRecipe recipe = detectRecipe(inv);
        setPreview(inv, recipe != null ? recipe.createResult() : null);
    }

    private static void setPreview(Inventory inv, ItemStack result) {
        inv.setItem(OUTPUT_SLOT, result);
        updateConfirmButton(inv, result != null && result.getType() != Material.AIR);
    }

    private static void updateConfirmButton(Inventory inv, boolean enabled) {
        inv.setItem(CONFIRM_SLOT, createConfirmButton(enabled));
    }

    private static ItemStack createConfirmButton(boolean enabled) {
        if (enabled) {
            return GUIBuilder.createGlowingItem(
                    Material.LIME_DYE,
                    MessageUtils.color("&a✔ " + CONFIRM_LABEL),
                    MessageUtils.color("&7Clic para confirmar la forja."));
        }
        return GUIBuilder.createItem(
                Material.GRAY_DYE,
                MessageUtils.color("&8" + CONFIRM_LABEL),
                MessageUtils.color("&8Coloca los materiales correctos."));
    }

    public static boolean tryCraft(Inventory inv, Player player) {
        ForgeRecipe recipe = detectRecipe(inv);
        if (recipe == null) {
            return false;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(MessageUtils.color("&cInventario lleno."));
            return false;
        }

        consumeInputs(inv);
        player.getInventory().addItem(recipe.createResult());
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0.5f);
        player.sendMessage(MessageUtils.color("&a¡Objeto forjado con éxito!"));
        updateResult(inv);
        return true;
    }

    private static ForgeRecipe detectRecipe(Inventory inv) {
        ItemStack top = inv.getItem(12);
        ItemStack left = inv.getItem(20);
        ItemStack center = inv.getItem(21);
        ItemStack right = inv.getItem(22);
        ItemStack bottom = inv.getItem(30);

        if (isDrop(top, BossDropType.SERAPHIM_FEATHER) && isDrop(left, BossDropType.MAGMA_CORE)
                && isType(center, Material.DIAMOND) && isDrop(right, BossDropType.TITAN_SHARD)
                && isDrop(bottom, BossDropType.VOID_SCALE)) {
            return ForgeRecipe.SOCKET_EXPANDER;
        }
        if (isDrop(top, BossDropType.MAGMA_CORE) && isDrop(left, BossDropType.MAGMA_CORE)
                && isType(center, Material.DIAMOND_BLOCK) && isDrop(right, BossDropType.MAGMA_CORE)
                && isDrop(bottom, BossDropType.MAGMA_CORE)) {
            return ForgeRecipe.REPAIR_KIT;
        }
        if (isDrop(top, BossDropType.SERAPHIM_FEATHER) && isDrop(left, BossDropType.ANGEL_DUST)
                && isType(center, Material.ELYTRA) && isDrop(right, BossDropType.ANGEL_DUST)
                && isDrop(bottom, BossDropType.SERAPHIM_FEATHER)) {
            return ForgeRecipe.ANGEL_WINGS;
        }
        if (isDrop(top, BossDropType.SERAPHIM_FEATHER) && isType(left, Material.GOLDEN_HELMET)
                && isStarlightEssence(center) && isType(right, Material.GOLDEN_HELMET)
                && isDrop(bottom, BossDropType.SERAPHIM_FEATHER)) {
            return ForgeRecipe.SERAPHIM_HALO;
        }
        if (isDrop(top, BossDropType.VOID_SCALE) && isDrop(left, BossDropType.TITAN_SHARD)
                && isType(center, Material.PAPER) && isDrop(right, BossDropType.TITAN_SHARD)
                && isDrop(bottom, BossDropType.VOID_SCALE)) {
            return ForgeRecipe.TITAN_RUNE;
        }
        return null;
    }

    private static boolean isStarlightEssence(ItemStack item) {
        return BossDropType.STARLIGHT_ESSENCE.matches(item)
                || com.livingtools.manager.AetherialManager.isStarlightEssence(item);
    }

    private static boolean isDrop(ItemStack item, BossDropType type) {
        return BossDropType.matchesAmount(item, type, 1);
    }

    private static boolean isType(ItemStack item, Material mat) {
        return item != null && item.getType() == mat && item.getAmount() >= 1;
    }

    public static void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Inventory top = event.getView().getTopInventory();
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                return;
            }
            if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.getType() != Material.AIR) {
                    for (int inputSlot : INPUT_SLOTS) {
                        ItemStack slotItem = top.getItem(inputSlot);
                        if (slotItem == null || slotItem.getType() == Material.AIR) {
                            top.setItem(inputSlot, clicked.clone());
                            event.setCurrentItem(null);
                            Bukkit.getScheduler().runTask(LivingToolsPlugin.getInstance(), () -> updateResult(top));
                            return;
                        }
                    }
                }
                return;
            }
            event.setCancelled(false);
            Bukkit.getScheduler().runTask(LivingToolsPlugin.getInstance(), () -> updateResult(top));
            return;
        }

        if (slot < 0 || slot >= top.getSize()) {
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SWAP_OFFHAND) {
            return;
        }

        if (isInputSlot(slot)) {
            event.setCancelled(false);
            Bukkit.getScheduler().runTask(LivingToolsPlugin.getInstance(), () -> updateResult(top));
            return;
        }

        if (slot == CONFIRM_SLOT) {
            tryCraft(top, player);
            return;
        }

        if (isProtectedSlot(slot)) {
            return;
        }
    }

    private static void consumeInputs(Inventory inv) {
        for (int inputSlot : INPUT_SLOTS) {
            ItemStack item = inv.getItem(inputSlot);
            if (item == null) {
                continue;
            }
            int remaining = item.getAmount() - 1;
            if (remaining <= 0) {
                inv.setItem(inputSlot, null);
            } else {
                item.setAmount(remaining);
                inv.setItem(inputSlot, item);
            }
        }
    }
}
