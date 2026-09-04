package com.livingtools.listeners;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityRegistry;
import com.livingtools.data.LivingTool;
import com.livingtools.gui.SkillTreeGUI;
import com.livingtools.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String statsTitle = ConfigManager.getRawMessage("stats_title");
        String armorTitle = ConfigManager.getRawMessage("armor_title");
        String dashboardTitle = ConfigManager.getRawMessage("dashboard_title");
        String historyTitle = ConfigManager.getRawMessage("history_title");
        String helpTitle = ConfigManager.getRawMessage("help_title");

        // Check if the inventory is one of ours (basic check)
        boolean isOurGUI = title.equals(statsTitle) || title.equals(armorTitle) || title.equals(dashboardTitle)
                || title.equals(historyTitle) || title.equals(helpTitle)
                || ChatColor.stripColor(title).equals(ChatColor.stripColor(com.livingtools.gui.AdminGUI.TITLE))
                || ChatColor.stripColor(title).equals(ChatColor.stripColor(com.livingtools.gui.RitualGUI.TITLE))
                || title.equals(com.livingtools.gui.RitualGuideGUI.TITLE)
                || title.equals(com.livingtools.gui.CursedForgeGUI.TITLE)
                || title.equals(ChatColor.DARK_PURPLE + "Forja Rúnica")
                || title.startsWith(com.livingtools.gui.AbilityDetailGUI.TITLE_PREFIX)
                || title.equals(com.livingtools.gui.LibraryGUI.TITLE)
                || title.equals(ChatColor.DARK_RED + "Mercado Negro")
                || title.equals(com.livingtools.gui.SoulForgeGUI.TITLE)
                || title.equals(com.livingtools.gui.RuneFusionGUI.TITLE)
                || title.equals(com.livingtools.gui.AssemblyGUI.TITLE)
                || title.equals(com.livingtools.gui.BossForgeGUI.TITLE)
                || ChatColor.stripColor(title).equals(ChatColor.stripColor(com.livingtools.gui.BossForgeGUI.TITLE))
                || title.equals(com.livingtools.gui.HistoryGUI.TITLE)
                || isImprovedSkillTreeTitle(title)
                || title.startsWith(ChatColor.BLACK + "Comercio:")
                // Recipe category or recipe view titles
                || title.contains("Categoría:")
                || title.contains("Herramientas:")
                || title.startsWith("Crafteo:")
                || title.contains("Living Tools");

        if (!isOurGUI) {
            return;
        }

        // HistoryGUI — fully read-only, only close button active
        if (title.equals(com.livingtools.gui.HistoryGUI.TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null
                    && event.getCurrentItem().getType() == Material.BARRIER
                    && event.getWhoClicked() instanceof Player) {
                event.getWhoClicked().closeInventory();
            }
            return;
        }

        if (title.equals(com.livingtools.gui.SoulForgeGUI.TITLE)) {
            com.livingtools.gui.SoulForgeGUI.handleClick(event);
            return;
        }

        if (title.equals(com.livingtools.gui.RuneFusionGUI.TITLE)) {
            com.livingtools.gui.RuneFusionGUI.handleClick(event);
            return;
        }

        if (title.equals(com.livingtools.gui.ModelShowcaseGUI.TITLE)) {
            com.livingtools.gui.ModelShowcaseGUI.handleClick(event);
            return;
        }

        if (title.startsWith(ChatColor.BLACK + "Comercio:")) {
            com.livingtools.manager.TradeManager.getActiveTrade((Player) event.getWhoClicked())
                    .handleClick(event);
            return;
        }

        // Admin GUI Logic
        if (ChatColor.stripColor(title).equals(ChatColor.stripColor(ChatColor.DARK_RED + "Panel de Administración"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null)
                return;

            Player player = (Player) event.getWhoClicked();
            Material type = event.getCurrentItem().getType();

            if (type == Material.DIAMOND_PICKAXE) {
                // Give Tools Submenu (For now just give basic)
                player.performCommand("livingtool admin give DIAMOND_PICKAXE 1");
                player.closeInventory();
            } else if (type == Material.NETHER_STAR) {
                // Spawn Bosses Submenu
                player.performCommand("livingtool admin spawnboss LIVING"); // Default for now, or open submenu
                player.closeInventory();
            } else if (type == Material.REDSTONE_TORCH) {
                // Reload
                player.performCommand("livingtool admin reload");
                player.closeInventory();
            } else if (type == Material.TOTEM_OF_UNDYING) {
                // Give Artifacts
                player.performCommand("livingtool admin giveartifact RING_OF_POWER"); // Default example
                player.sendMessage(ChatColor.GREEN + "Ejemplo: Anillo de Poder entregado.");
                player.closeInventory();
            } else if (type == Material.LIME_DYE) {
                // Give Runes
                player.performCommand("livingtool admin giverune STRENGTH II"); // Default example
                player.sendMessage(ChatColor.GREEN + "Ejemplo: Runa de Fuerza II entregada.");
                player.closeInventory();
            } else if (type == Material.SOUL_SAND) {
                // Give Soul Gems
                player.performCommand("livingtool admin givesoulgem ZOMBIE"); // Default example
                player.sendMessage(ChatColor.GREEN + "Ejemplo: Alma de Zombie entregada.");
                player.closeInventory();
            } else if (type == Material.PLAYER_HEAD) {
                // Manage Players
                player.performCommand("livingtool top"); // Open leaderboard for now
                player.closeInventory();
            } else if (type == Material.COMPARATOR) {
                player.sendMessage(ChatColor.YELLOW + "Ajustes Globales: Próximamente.");
                player.closeInventory();
            }
            return;
        }

        // Ritual GUI Logic
        if (ChatColor.stripColor(title).equals(ChatColor.stripColor(com.livingtools.gui.RitualGUI.TITLE))) {
            // Security: Cancel everything by default unless explicitly allowed
            boolean allow = false;

            int rawSlot = event.getRawSlot();
            int inventorySize = event.getView().getTopInventory().getSize();
            Player player = (Player) event.getWhoClicked();

            // Check where the click happened
            if (rawSlot < inventorySize && rawSlot >= 0) {
                // Click in Top Inventory
                if (rawSlot == 13) {
                    ItemStack current = event.getCurrentItem();
                    ItemStack cursor = event.getCursor();

                    // Check if current is placeholder (Barrier)
                    boolean isPlaceholder = (current != null && current.getType() == Material.BARRIER);

                    if (isPlaceholder) {
                        if (LivingTool.isLivingTool(cursor)
                                || com.livingtools.manager.SleepingManager.isSleepingTool(cursor)) {
                            // Manual Place: Overwrite placeholder
                            event.getView().getTopInventory().setItem(13, cursor);
                            event.getWhoClicked().setItemOnCursor(null);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);

                            // Update GUI Button
                            com.livingtools.gui.RitualGUI.updateGUI(event.getView().getTopInventory());
                        }
                        // Always cancel if it was a placeholder (either we handled it or we blocked it)
                        allow = false;
                    } else {
                        // Not a placeholder (valid tool or empty) -> Allow interaction
                        allow = true;

                        // Schedule update for when item is removed/changed (next tick)
                        org.bukkit.Bukkit.getScheduler()
                                .runTask(com.livingtools.LivingToolsPlugin.getInstance(), () -> {
                                    com.livingtools.gui.RitualGUI
                                            .updateGUI(event.getView().getTopInventory());
                                });
                    }
                } else if (rawSlot == 22) {
                    ItemStack current = event.getCurrentItem();
                    ItemStack cursor = event.getCursor();

                    // Check if current is placeholder (Gray Dye)
                    boolean isPlaceholder = (current != null && current.getType() == Material.GRAY_DYE);

                    if (isPlaceholder) {
                        if (cursor != null && (cursor.getType() == Material.NETHER_STAR ||
                                cursor.getType() == Material.TOTEM_OF_UNDYING ||
                                cursor.getType() == Material.GHAST_TEAR)) {
                            // Manual Place: Overwrite placeholder
                            event.getView().getTopInventory().setItem(22, cursor);
                            event.getWhoClicked().setItemOnCursor(null);
                            player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1, 1);
                        }
                        // Always cancel if it was a placeholder
                        allow = false;
                    } else {
                        // Not a placeholder -> Allow interaction
                        allow = true;
                    }
                } else {
                    // Click on GUI elements (Glass, Buttons) -> Cancelled (default)
                    // Handle Button Logic
                    com.livingtools.gui.RitualGUI.handleClick(event);
                }
            } else {
                // Click in Player Inventory or Outside
                allow = true; // Allow moving items in own inventory

                // Exception: Shift-Clicking from Player Inventory
                if (event.isShiftClick()) {
                    allow = false; // Cancel default shift-click to prevent putting items in wrong slots

                    // Custom Shift-Click Logic
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedItem != null) {
                        Inventory top = event.getView().getTopInventory();
                        if (LivingTool.isLivingTool(clickedItem)
                                || com.livingtools.manager.SleepingManager.isSleepingTool(clickedItem)) {
                            ItemStack slot13 = top.getItem(13);
                            // Only put if slot 13 is empty OR is a placeholder
                            if (slot13 == null || slot13.getType() == Material.BARRIER) {
                                top.setItem(13, clickedItem);
                                event.setCurrentItem(null);
                                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);

                                // Update GUI Button
                                com.livingtools.gui.RitualGUI.updateGUI(top);
                            }
                        } else if (clickedItem.getType() == Material.NETHER_STAR ||
                                clickedItem.getType() == Material.TOTEM_OF_UNDYING ||
                                clickedItem.getType() == Material.GHAST_TEAR) {
                            ItemStack slot22 = top.getItem(22);
                            // Only put if slot 22 is empty OR is a placeholder
                            if (slot22 == null || slot22.getType() == Material.GRAY_DYE) {
                                top.setItem(22, clickedItem);
                                event.setCurrentItem(null);
                                player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1, 1);
                            }
                        }
                    }
                }
            }

            event.setCancelled(!allow);
            return;
        }

        // Ritual Guide GUI Logic
        if (title.equals(com.livingtools.gui.RitualGuideGUI.TITLE)) {
            com.livingtools.gui.RitualGuideGUI.handleClick(event);
            return;
        }

        // Cursed Forge GUI Logic
        if (title.equals(com.livingtools.gui.CursedForgeGUI.TITLE)) {
            com.livingtools.gui.CursedForgeGUI.handleClick(event);
            return;
        }

        // Rune Forge GUI Logic
        if (title.equals(ChatColor.DARK_PURPLE + "Forja Rúnica")) {
            com.livingtools.gui.RuneForgeGUI.handleEvent(event);
            return;
        }

        // Boss Forge GUI Logic
        if (title.equals(com.livingtools.gui.BossForgeGUI.TITLE)
                || ChatColor.stripColor(title).equals(ChatColor.stripColor(com.livingtools.gui.BossForgeGUI.TITLE))) {
            com.livingtools.gui.BossForgeGUI.handleClick(event);
            return;
        }

        // Ability Mastery GUI Logic
        if (title.equals(ChatColor.DARK_PURPLE + "Maestría de Habilidades")) {
            event.setCancelled(true); // Just view only
            return;
        }

        // Ability Detail GUI Logic
        if (title.startsWith(com.livingtools.gui.AbilityDetailGUI.TITLE_PREFIX)) {
            com.livingtools.gui.AbilityDetailGUI.handleClick(event);
            return;
        }

        // Assembly GUI Logic
        if (title.equals(com.livingtools.gui.AssemblyGUI.TITLE)) {
            handleAssemblyClick(event);
            return;
        }

        // Library GUI Logic
        if (title.equals(com.livingtools.gui.LibraryGUI.TITLE)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player))
                return;
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();

            if (clicked == null || clicked.getType() == Material.AIR)
                return;

            if (clicked.getType() == Material.WRITTEN_BOOK) {
                // Open Book
                player.closeInventory();
                player.openBook(clicked);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
            } else if (clicked.getType() == Material.BOOK) {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                player.sendMessage(ChatColor.RED + "Este tomo aún no ha sido descubierto.");
            }
            return;
        }

        // Smuggler GUI Logic
        if (title.equals(ChatColor.DARK_RED + "Mercado Negro")) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                com.livingtools.gui.SmugglerGUI.handleClick((Player) event.getWhoClicked(),
                        event.getRawSlot(), event.getCurrentItem(), event.getInventory());
            }
            return;
        }
        // Allow clicking in player inventory
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
            event.setCancelled(false);
        }

        // Prevent taking items from any of our other GUIs if not handled above
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // Handle clicks outside or on empty slots
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE)
            return;

        // Special handling for Armor GUI
        if (title.equals(armorTitle)) {
            event.setCancelled(true);
            if (clicked.getType() == Material.DIAMOND_PICKAXE && event.getSlot() == 22) {
                ItemStack toolItem = player.getInventory().getItemInMainHand();
                if (com.livingtools.data.LivingTool.isLivingTool(toolItem)) {
                    com.livingtools.gui.DashboardGUI.open(player,
                            new com.livingtools.data.LivingTool(toolItem));
                }
            }
            return;
        }

        // Dashboard Logic
        if (title.equals(dashboardTitle)) {
            event.setCancelled(true); // all clicks cancelled — pure navigation
            ItemStack toolItem = player.getInventory().getItemInMainHand();
            LivingTool tool = LivingTool.isLivingTool(toolItem) ? new LivingTool(toolItem) : null;
            if (clicked == null || clicked.getType() == Material.AIR) return;

            Material m = clicked.getType();

            // Close button
            if (m == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            // Skills tree (ENCHANTED_BOOK)
            if (m == Material.ENCHANTED_BOOK) {
                if (tool != null) {
                    com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
                    com.livingtools.manager.GuideManager.triggerStep(player, tool,
                            com.livingtools.manager.GuideManager.TutorialStep.GUI_OPENED);
                } else {
                    player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                    player.closeInventory();
                }
                return;
            }

            // History (BOOK)
            if (m == Material.BOOK) {
                if (tool != null) {
                    com.livingtools.gui.HistoryGUI.open(player, tool);
                } else {
                    player.closeInventory();
                    player.performCommand("livingtool history");
                }
                return;
            }

            // Armor (DIAMOND_CHESTPLATE)
            if (m == Material.DIAMOND_CHESTPLATE) {
                int armorCount = 0;
                ItemStack singlePiece = null;
                for (ItemStack it : player.getInventory().getArmorContents()) {
                    if (com.livingtools.data.LivingArmor.isLivingArmor(it)) {
                        armorCount++;
                        singlePiece = it;
                    }
                }
                if (armorCount > 1) {
                    com.livingtools.gui.ArmorSelectionGUI.open(player);
                } else if (armorCount == 1 && singlePiece != null) {
                    com.livingtools.gui.ArmorGUI.open(player, new com.livingtools.data.LivingArmor(singlePiece));
                } else {
                    player.sendMessage(ConfigManager.getMessage("no_armor_equipped"));
                    player.closeInventory();
                }
                return;
            }

            // Daily Challenges (RECOVERY_COMPASS)
            if (m == Material.RECOVERY_COMPASS) {
                player.closeInventory();
                if (tool != null) {
                    com.livingtools.manager.DailyChallengeManager.showChallenges(player, tool);
                } else {
                    player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                }
                return;
            }

            // Milestones / Help (NETHER_STAR / WRITABLE_BOOK)
            if (m == Material.NETHER_STAR || m == Material.WRITABLE_BOOK) {
                if (m == Material.NETHER_STAR && tool != null) {
                    // Open milestone list in chat
                    player.closeInventory();
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "═══ Hitos de Progresión ═══");
                    for (com.livingtools.manager.MilestoneManager.Milestone ms : com.livingtools.manager.MilestoneManager.Milestone.values()) {
                        boolean done = com.livingtools.manager.MilestoneManager.countCompleted(tool) > 0
                            && com.livingtools.manager.MilestoneManager.isCompleted(tool, ms);
                        String mark = done ? ChatColor.GREEN + "✔" : ChatColor.RED + "✘";
                        player.sendMessage(mark + " " + ChatColor.YELLOW + ms.getTitle()
                                + ChatColor.GRAY + " — " + ms.getRequirement()
                                + (done ? "" : ChatColor.GREEN + " (+" + ms.getXpBonus() + " XP)"));
                    }
                } else {
                    player.closeInventory();
                    player.performCommand("livingtool help");
                }
                return;
            }

            return;
        }


        // Skill Tree Logic (requires tool)
        if (title.equals(statsTitle) || isImprovedSkillTreeTitle(title)) {
            org.bukkit.event.inventory.InventoryAction action = event.getAction();
            if (action == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || action == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP
                    || action == org.bukkit.event.inventory.InventoryAction.HOTBAR_MOVE_AND_READD
                    || action == org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                return;
            }

            // Allow clicking in player inventory FIRST (before cancelling)
            if (event.getClickedInventory() != null
                    && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
                if (event.isShiftClick()) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(false);
                // Don't return - still need to handle cursor with rune
            } else {
                // Cancel clicks in GUI inventory by default
                event.setCancelled(true);
            }

            ItemStack toolItem = player.getInventory().getItemInMainHand();
            if (!LivingTool.isLivingTool(toolItem)) {
                player.closeInventory();
                player.sendMessage(ConfigManager.getMessage("must_hold_tool"));
                return;
            }
            LivingTool tool = new LivingTool(toolItem);

            // Handle Back Button Click in Skill Tree
            if (clicked != null && clicked.getType() == Material.ARROW && clicked.hasItemMeta()
                    && clicked.getItemMeta().hasDisplayName()
                    && ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).contains("Volver")) {
                com.livingtools.gui.DashboardGUI.open(player, tool);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                return;
            }

            // Handle Close Button Click in Skill Tree
            if (clicked != null && clicked.getType() == Material.BARRIER && clicked.hasItemMeta()
                    && clicked.getItemMeta().hasDisplayName()
                    && ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).contains("Cerrar")) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                return;
            }

            // Check for Rune Slot Click
            org.bukkit.NamespacedKey runeKey = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_rune_index");

            if (clicked.hasItemMeta() && clicked.getItemMeta().getPersistentDataContainer().has(runeKey,
                    org.bukkit.persistence.PersistentDataType.INTEGER)) {
                int runeIndex = clicked.getItemMeta().getPersistentDataContainer().get(runeKey,
                        org.bukkit.persistence.PersistentDataType.INTEGER);
                ItemStack cursor = event.getCursor();

                if (com.livingtools.runes.RuneManager.isRune(cursor)) {
                    // Equip Rune
                    com.livingtools.runes.RuneType type = com.livingtools.runes.RuneManager
                            .getRuneType(cursor);
                    if (type != null) {
                        if (clicked.getType() == Material.LIME_STAINED_GLASS_PANE) {
                            com.livingtools.runes.RuneManager.RuneTier tier = com.livingtools.runes.RuneManager
                                    .getRuneTier(cursor);
                            tool.getData().addRune(type, tier);
                            player.sendMessage(ConfigManager.getMessage("rune_equipped")
                                    .replace("%rune%", type.getName()));
                            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);

                            // Consume rune from cursor
                            cursor.setAmount(cursor.getAmount() - 1);
                            event.getWhoClicked().setItemOnCursor(cursor);

                            com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
                            return;
                        }
                    }
                } else if (clicked.getType() != Material.LIME_STAINED_GLASS_PANE
                        && clicked.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    // Remove Rune
                    tool.getData().removeRune(runeIndex);
                    player.sendMessage(ConfigManager.getMessage("rune_removed"));
                    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 1);

                    com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
                    return;
                }
            }

            // Check for Prestige Click
            org.bukkit.NamespacedKey actionKey = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_action");
            if (clicked.hasItemMeta() && clicked.getItemMeta().getPersistentDataContainer().has(actionKey,
                    org.bukkit.persistence.PersistentDataType.STRING)) {
                String prestigeAction = clicked.getItemMeta().getPersistentDataContainer().get(actionKey,
                        org.bukkit.persistence.PersistentDataType.STRING);

                if ("prestige".equals(prestigeAction)) {
                    if (tool.getData().getLevel() >= 100) {
                        // Perform Prestige
                        tool.prestige(player);

                        // Close and Re-open to refresh
                        com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
                    }
                    return;
                }
            }

            // Use PDC to get Ability ID
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                    com.livingtools.LivingToolsPlugin.getInstance(), "gui_ability_id");
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().getPersistentDataContainer().has(key,
                    org.bukkit.persistence.PersistentDataType.STRING)) {
                return;
            }

            String abilityId = clicked.getItemMeta().getPersistentDataContainer().get(key,
                    org.bukkit.persistence.PersistentDataType.STRING);

            if (abilityId != null) {
                if (tool.hasAbility(abilityId)) {
                    // Open Detail GUI
                    com.livingtools.gui.AbilityDetailGUI.open(player, tool, abilityId, 0);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    return;
                }

                Ability ability = AbilityRegistry.getAbility(abilityId);
                if (ability == null) {
                    player.sendMessage(ChatColor.RED + "Error: Habilidad no encontrada.");
                    return;
                }

                if (!ability.isCompatible(tool.getItem().getType())) {
                    player.sendMessage(ChatColor.RED + "Esta habilidad no es compatible con este tipo de herramienta.");
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    return;
                }

                if (tool.getData().getLevel() >= ability.getRequiredLevel()) {
                    tool.addAbility(abilityId);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                    player.sendMessage(ConfigManager.getMessage("ability_unlocked")
                            .replace("%ability%", ability.getName()));

                    // Refresh GUI
                    com.livingtools.gui.ImprovedSkillTreeGUI.open(player, tool);
                } else {
                    player.sendMessage(ConfigManager.getMessage("insufficient_level"));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        String statsTitle = ChatColor.stripColor(ConfigManager.getRawMessage("stats_title"));
        String armorTitle = ChatColor.stripColor(ConfigManager.getRawMessage("armor_title"));
        String dashboardTitle = ChatColor.stripColor(ConfigManager.getRawMessage("dashboard_title"));
        String historyTitle = ConfigManager.getRawMessage("history_title");
        String helpTitle = ConfigManager.getRawMessage("help_title");
        String ritualTitle = ChatColor.stripColor(com.livingtools.gui.RitualGUI.TITLE);
        String runeForgeTitle = ChatColor.stripColor(ChatColor.DARK_PURPLE + "Forja Rúnica");
        String assemblyTitle = ChatColor.stripColor(com.livingtools.gui.AssemblyGUI.TITLE);

        String bossForgeTitle = ChatColor.stripColor(com.livingtools.gui.BossForgeGUI.TITLE);
        String cursedForgeTitle = ChatColor.stripColor(com.livingtools.gui.CursedForgeGUI.TITLE);
        String soulForgeTitle = ChatColor.stripColor(com.livingtools.gui.SoulForgeGUI.TITLE);

        if (title.equals(statsTitle) || title.equals(armorTitle) || title.equals(dashboardTitle)
                || title.equals(historyTitle) || title.equals(helpTitle) || title.equals(ritualTitle)
                || title.equals(runeForgeTitle) || title.equals(bossForgeTitle)
                || title.equals(cursedForgeTitle) || title.equals(soulForgeTitle)
                || title.equals(ChatColor.stripColor(com.livingtools.gui.RuneFusionGUI.TITLE))
                || title.equals(ChatColor.stripColor(com.livingtools.gui.LibraryGUI.TITLE))
                || title.startsWith(com.livingtools.gui.AbilityDetailGUI.TITLE_PREFIX)
                || title.equals(assemblyTitle)
                || isImprovedSkillTreeTitle(title)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(com.livingtools.gui.RitualGUI.TITLE)) {
            ItemStack item = event.getInventory().getItem(13);
            if (item != null && item.getType() != Material.BARRIER) {
                event.getPlayer().getInventory().addItem(item);
            }
            ItemStack catalyst = event.getInventory().getItem(22);
            if (catalyst != null && catalyst.getType() != Material.GRAY_DYE) {
                event.getPlayer().getInventory().addItem(catalyst);
            }
        }

        if (event.getView().getTitle().equals(com.livingtools.gui.CursedForgeGUI.TITLE)) {
            ItemStack item = event.getInventory().getItem(11);
            if (item != null && item.getType() != Material.BARRIER) {
                event.getPlayer().getInventory().addItem(item);
            }
        }

        if (event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Forja Rúnica")) {
            // Return items from slots 11 (Geode), 13, 14, 15 (Runes), 22 (Catalyst)
            int[] slots = { 11, 13, 14, 15, 22 };
            for (int slot : slots) {
                ItemStack item = event.getInventory().getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    event.getPlayer().getInventory().addItem(item);
                }
            }
        }

        if (event.getView().getTitle().equals(com.livingtools.gui.BossForgeGUI.TITLE)) {
            // Return items from slots 12, 20, 21, 22, 30
            int[] slots = { 12, 20, 21, 22, 30 };
            for (int slot : slots) {
                ItemStack item = event.getInventory().getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    event.getPlayer().getInventory().addItem(item);
                }
            }
        }

        if (event.getView().getTitle().equals(com.livingtools.gui.AssemblyGUI.TITLE)) {
            // Return all remaining ingredients from the 3x3 grid
            int[] slots = com.livingtools.gui.AssemblyGUI.GRID_SLOTS;
            for (int slot : slots) {
                ItemStack item = event.getInventory().getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    event.getPlayer().getInventory().addItem(item);
                }
            }
        }

        if (event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Bolsa de Runas")) {
            ItemStack pouch = event.getPlayer().getInventory().getItemInMainHand();
            if (com.livingtools.manager.ArtifactManager.isArtifact(pouch) &&
                    com.livingtools.manager.ArtifactManager.getArtifactType(
                            pouch) == com.livingtools.manager.ArtifactManager.ArtifactType.RUNE_POUCH) {
                com.livingtools.gui.RunePouchGUI.save(pouch, event.getInventory());
                Player player = (Player) event.getPlayer();
                player.sendMessage(ChatColor.GREEN + "Bolsa de Runas guardada.");
                player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_INSERT, 1, 1);
            }
        }
        if (event.getView().getTitle().startsWith(ChatColor.BLACK + "Comercio:")) {
            com.livingtools.manager.TradeManager.getActiveTrade((Player) event.getPlayer())
                    .handleClose(event);
        }
    }

    private static boolean isImprovedSkillTreeTitle(String title) {
        String clean = ChatColor.stripColor(title);
        return clean != null && clean.contains("Habilidades:");
    }

    private static void handleAssemblyClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Inventory top = event.getView().getTopInventory();
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
            if (event.getClick() == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK) {
                return;
            }
            if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.getType() != Material.AIR) {
                    for (int gridSlot : com.livingtools.gui.AssemblyGUI.GRID_SLOTS) {
                        ItemStack slotItem = top.getItem(gridSlot);
                        if (slotItem == null || slotItem.getType() == Material.AIR) {
                            top.setItem(gridSlot, clicked.clone());
                            event.setCurrentItem(null);
                            org.bukkit.Bukkit.getScheduler().runTask(
                                    com.livingtools.LivingToolsPlugin.getInstance(),
                                    () -> com.livingtools.gui.AssemblyGUI.updateResult(top));
                            return;
                        }
                    }
                }
                return;
            }
            event.setCancelled(false);
            org.bukkit.Bukkit.getScheduler().runTask(com.livingtools.LivingToolsPlugin.getInstance(),
                    () -> com.livingtools.gui.AssemblyGUI.updateResult(top));
            return;
        }

        if (slot < 0 || slot >= top.getSize()) {
            return;
        }

        if (event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY
                || event.getClick() == org.bukkit.event.inventory.ClickType.SWAP_OFFHAND) {
            return;
        }

        if (com.livingtools.gui.AssemblyGUI.isGridSlot(slot)) {
            event.setCancelled(false);
            org.bukkit.Bukkit.getScheduler().runTask(com.livingtools.LivingToolsPlugin.getInstance(),
                    () -> com.livingtools.gui.AssemblyGUI.updateResult(top));
            return;
        }

        if (slot == com.livingtools.gui.AssemblyGUI.CONFIRM_SLOT) {
            com.livingtools.gui.AssemblyGUI.tryCraft(top, player);
            return;
        }

        if (com.livingtools.gui.AssemblyGUI.isProtectedSlot(slot)) {
            return;
        }
    }
}
