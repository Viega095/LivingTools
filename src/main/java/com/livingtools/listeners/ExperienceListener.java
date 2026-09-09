package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class ExperienceListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block block = event.getBlock();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);

            // Swords should NOT gain XP from mining - they level up via combat only
            if (item.getType().name().endsWith("_SWORD")) {
                return;
            }

            // Naming Ceremony — first use of tool
            com.livingtools.manager.NamingCeremonyManager.tryStartCeremony(player, tool);

            // Daily Challenge progress — block mined
            com.livingtools.manager.DailyChallengeManager.onBlockMined(player, tool, block.getType());

            // Phase 53: Sentience (Refusal)
            if (com.livingtools.manager.SentienceManager.shouldRefuseWork(player, tool)) {
                event.setCancelled(true);
                return;
            }

            // Phase 53: Sentience (Mood Decrease & Effects)
            if (Math.random() < 0.01) { // 1% chance to get tired/bored
                tool.getData().adjustMood(-1);
            }
            com.livingtools.manager.SentienceManager.checkEffects(player, tool);

            // Calculate XP based on block type
            int baseXP = 1;
            switch (block.getType()) {
                case COAL_ORE:
                case DEEPSLATE_COAL_ORE:
                case IRON_ORE:
                case DEEPSLATE_IRON_ORE:
                case COPPER_ORE:
                case DEEPSLATE_COPPER_ORE:
                    baseXP = 3;
                    break;
                case GOLD_ORE:
                case DEEPSLATE_GOLD_ORE:
                case LAPIS_ORE:
                case DEEPSLATE_LAPIS_ORE:
                case REDSTONE_ORE:
                case DEEPSLATE_REDSTONE_ORE:
                    baseXP = 5;
                    break;
                case DIAMOND_ORE:
                case DEEPSLATE_DIAMOND_ORE:
                case EMERALD_ORE:
                case DEEPSLATE_EMERALD_ORE:
                    baseXP = 10;
                    break;
                case NETHER_QUARTZ_ORE:
                case NETHER_GOLD_ORE:
                    baseXP = 4;
                    break;
                case ANCIENT_DEBRIS:
                    baseXP = 20;
                    break;
                default:
                    baseXP = 1;
                    break;
            }

            double multiplier = com.livingtools.manager.ConfigManager.getMiningXPMultiplier();

            // Hotbar Synergy (Phase 38)
            if (com.livingtools.manager.SynergyManager.checkHotbarSynergy(player)) {
                multiplier += 0.10; // +10% XP
            }

            // Artifact: Living Charm (+10% XP si está en el inventario)
            if (com.livingtools.manager.ArtifactManager.hasArtifact(player,
                    com.livingtools.manager.ArtifactManager.ArtifactType.LIVING_CHARM)) {
                multiplier += 0.10;
            }

            // Weather Bonus
            double weatherMult = com.livingtools.manager.WeatherBonusManager.getWeatherMultiplier(player, item.getType());
            multiplier = multiplier * weatherMult;

            // Sleep Bonus — extra XP if player slept recently
            double sleepMult = com.livingtools.manager.SleepBonusManager.getSleepMultiplier(player);
            multiplier = multiplier * sleepMult;

            long finalXP = (long) (baseXP * multiplier);

            // Daily first-use bonus (runs once per tool per day)
            com.livingtools.manager.DailyBonusManager.checkAndGrant(player, tool);

            if (finalXP > 0) {
                tool.addXP(player, finalXP);

                // Track blocks mined for history
                tool.getData().setBlocksMined(tool.getData().getBlocksMined() + 1);

                // Tool Memory — track biome visited
                com.livingtools.manager.ToolMemoryManager.onBlockMined(
                        player, tool, event.getBlock().getLocation().getBlock().getBiome());

                // Weather bonus action bar (only if bonus active)
                String weatherDesc = com.livingtools.manager.WeatherBonusManager.getBonusDescription(player, item.getType());
                if (weatherDesc != null) {
                    com.livingtools.utils.MessageUtils.sendActionBar(player,
                            org.bukkit.ChatColor.GREEN + "+" + finalXP + " XP  " + weatherDesc);
                }

                // Milestone check (deferred to next tick to avoid concurrent modification)
                final LivingTool toolRef = tool;
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    com.livingtools.LivingToolsPlugin.getInstance(),
                    () -> com.livingtools.manager.MilestoneManager.checkAll(player, toolRef), 1L);

                // Bond Shared XP (Phase 28)
                java.util.UUID partnerId = com.livingtools.manager.BondManager.getBondPartner(tool);
                if (partnerId != null) {
                    Player partner = org.bukkit.Bukkit.getPlayer(partnerId);
                    if (partner != null && partner.isOnline() && partner.getWorld().equals(player.getWorld())
                            && partner.getLocation().distanceSquared(player.getLocation()) < 225) { // 15 blocks

                        ItemStack partnerItem = partner.getInventory().getItemInMainHand();
                        if (LivingTool.isLivingTool(partnerItem)) {
                            LivingTool partnerTool = new LivingTool(partnerItem);
                            long sharedXP = (long) (finalXP * 0.10);
                            if (sharedXP > 0) {
                                partnerTool.addXP(partner, sharedXP);
                                com.livingtools.utils.MessageUtils.sendActionBar(partner,
                                        org.bukkit.ChatColor.LIGHT_PURPLE + "+" + sharedXP + " XP (Vínculo)");
                            }
                        }
                    }
                }

                com.livingtools.utils.MessageUtils.sendActionBar(player,
                        org.bukkit.ChatColor.GREEN + "+" + finalXP + " XP");
                com.livingtools.utils.MessageUtils.playXPGainSound(player);

                // Guide AI Triggers
                if (tool.getData().getXP() > 0) {
                    com.livingtools.manager.GuideManager.triggerStep(player, tool,
                            com.livingtools.manager.GuideManager.TutorialStep.FIRST_XP);
                }
                if (tool.getData().getLevel() >= 5) {
                    com.livingtools.manager.GuideManager.triggerStep(player, tool,
                            com.livingtools.manager.GuideManager.TutorialStep.LEVEL_5);
                }
                if (tool.getData().getLevel() >= 25) {
                    com.livingtools.manager.GuideManager.triggerStep(player, tool,
                            com.livingtools.manager.GuideManager.TutorialStep.LEVEL_25);
                }
                if (tool.getData().getLevel() >= 50) {
                    com.livingtools.manager.GuideManager.triggerStep(player, tool,
                            com.livingtools.manager.GuideManager.TutorialStep.LEVEL_50);
                }
            }

            // Trial Progress
            com.livingtools.manager.TrialManager.onMine(player, tool, block.getType());

            // Biome Tracking
            com.livingtools.manager.BiomeManager.BiomeCategory category = com.livingtools.manager.BiomeManager
                    .getCategory(block.getBiome());
            tool.getData().addBiomeXP(category, finalXP > 0 ? (int) finalXP : 1);

            // Hunger Check
            com.livingtools.manager.FeedingManager.checkHunger(player, tool);

            // Phase 34: Rune Geode Drop (chance increased by Rune Storm event)
            if (block.getType() == org.bukkit.Material.DEEPSLATE || block.getType() == org.bukkit.Material.STONE
                    || block.getType() == org.bukkit.Material.DEEPSLATE_DIAMOND_ORE
                    || block.getType() == org.bukkit.Material.DIAMOND_ORE) {
                double geodaMult = com.livingtools.manager.ServerEventManager.getGeodaDropMultiplier(player.getWorld());
                double geodaChance = 0.005 * geodaMult; // 0.5% base, up to 2% during Rune Storm
                if (Math.random() < geodaChance) {
                    ItemStack geode = com.livingtools.runes.RuneManager.createGeode();
                    player.getWorld().dropItemNaturally(block.getLocation(), geode);
                    String eventMsg = geodaMult > 1 ? org.bukkit.ChatColor.LIGHT_PURPLE + "⚡ [Tormenta de Runas] " : org.bukkit.ChatColor.LIGHT_PURPLE + "";
                    player.sendMessage(eventMsg + "¡Has encontrado una Geoda Rúnica!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
                }
            }

            // Corrupted Night — Shard of Darkness drop (2% chance)
            if (com.livingtools.manager.ServerEventManager.getActiveEvent(player.getWorld())
                    == com.livingtools.manager.ServerEventManager.WorldEvent.CORRUPTED_NIGHT) {
                if (Math.random() < 0.02) {
                    // Drop from mobs, not blocks — handled in EntityDeath. Skip here.
                }
            }

            // Phase 38: Miner's Rage
            com.livingtools.manager.SynergyManager.addMinerRageStack(player);

            // Phase 38: Warrior's Focus (Consume)
            if (com.livingtools.manager.SynergyManager.consumeWarriorFocus(player)) {
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, 1, 2.0f);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;
        Player killer = event.getEntity().getKiller();
        ItemStack item = killer.getInventory().getItemInMainHand();

        if (LivingTool.isLivingTool(item)) {
            LivingTool tool = new LivingTool(item);

            // Naming Ceremony — first use of tool
            com.livingtools.manager.NamingCeremonyManager.tryStartCeremony(killer, tool);

            // Daily Challenge progress
            if (event.getEntity() instanceof Player) {
                com.livingtools.manager.DailyChallengeManager.onPlayerKill(killer, tool);
            } else {
                com.livingtools.manager.DailyChallengeManager.onKill(killer, tool, event.getEntityType());
            }

            // Corruption Logic (Phase 21)
            if (event.getEntity() instanceof org.bukkit.entity.Villager) {
                tool.getData().addCorruption(5);
                killer.sendMessage(org.bukkit.ChatColor.DARK_PURPLE
                        + "Tu herramienta se corrompe al probar sangre inocente... (+5 Corrupción)");
                killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_VEX_CHARGE, 1, 0.5f);
                com.livingtools.manager.CorruptionManager.checkCorruption(killer, tool);
            } else if (event.getEntity() instanceof Player) {
                tool.getData().addCorruption(10);
                killer.sendMessage(org.bukkit.ChatColor.DARK_PURPLE
                        + "La esencia de otro jugador alimenta la oscuridad... (+10 Corrupción)");
                killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 1, 0.5f);
                com.livingtools.manager.CorruptionManager.checkCorruption(killer, tool);
            }

            // Combat XP — base 5, scaled by weather, config, world event, sleep, and kill streak
            double combatMult = com.livingtools.manager.ConfigManager.getCombatXPMultiplier()
                    * com.livingtools.manager.WeatherBonusManager.getWeatherMultiplier(killer, item.getType())
                    * com.livingtools.manager.ServerEventManager.getCombatXPMultiplier(killer.getWorld())
                    * com.livingtools.manager.SleepBonusManager.getSleepMultiplier(killer)
                    * com.livingtools.manager.KillStreakManager.getStreakMultiplier(killer);
            long killXP = Math.max(1L, (long)(5 * combatMult));
            tool.addXP(killer, killXP);

            // Tool Memory — track mob type
            com.livingtools.manager.ToolMemoryManager.onKill(killer, tool, event.getEntityType());

            // Daily first-use bonus
            com.livingtools.manager.DailyBonusManager.checkAndGrant(killer, tool);

            // Corrupted Night: Shard of Darkness drop (3% chance per mob)
            if (!(event.getEntity() instanceof Player)
                    && com.livingtools.manager.ServerEventManager.getActiveEvent(killer.getWorld())
                       == com.livingtools.manager.ServerEventManager.WorldEvent.CORRUPTED_NIGHT
                    && Math.random() < 0.03) {
                org.bukkit.inventory.ItemStack shard = com.livingtools.manager.ServerEventManager.createShardOfDarkness();
                killer.getWorld().dropItemNaturally(event.getEntity().getLocation(), shard);
                com.livingtools.utils.MessageUtils.sendActionBar(killer,
                        org.bukkit.ChatColor.DARK_PURPLE + "☠ ¡Shard of Darkness!");
            }

            // Kill Tracking
            if (event.getEntity() instanceof Player) {
                tool.getData().setPlayerKills(tool.getData().getPlayerKills() + 1);
                com.livingtools.listeners.ReputationListener.checkReputation(killer, tool);
            } else {
                tool.getData().setMobKills(tool.getData().getMobKills() + 1);
                com.livingtools.listeners.ReputationListener.checkReputation(killer, tool);
            }

            // Milestone check
            final LivingTool toolRef = tool;
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                com.livingtools.LivingToolsPlugin.getInstance(),
                () -> com.livingtools.manager.MilestoneManager.checkAll(killer, toolRef), 1L);

            // Trial Progress
            com.livingtools.manager.TrialManager.onKill(killer, tool, event.getEntityType());

            // Biome Tracking
            com.livingtools.manager.BiomeManager.BiomeCategory category = com.livingtools.manager.BiomeManager
                    .getCategory(killer.getLocation().getBlock().getBiome());
            tool.getData().addBiomeXP(category, (int) killXP);

            // Action bar — show weather bonus if active
            String weatherDesc = com.livingtools.manager.WeatherBonusManager.getBonusDescription(killer, item.getType());
            String xpMsg = org.bukkit.ChatColor.GREEN + "+" + killXP + " XP";
            if (weatherDesc != null) xpMsg += "  " + weatherDesc;
            com.livingtools.utils.MessageUtils.sendActionBar(killer, xpMsg);
            com.livingtools.utils.MessageUtils.playXPGainSound(killer);

            // Hunger Check
            com.livingtools.manager.FeedingManager.checkHunger(killer, tool);

            // Artifact: Soul Gem (Phase 39)
            for (ItemStack invItem : killer.getInventory().getContents()) {
                if (com.livingtools.manager.ArtifactManager.isArtifact(invItem)) {
                    if (com.livingtools.manager.ArtifactManager.getArtifactType(
                            invItem) == com.livingtools.manager.ArtifactManager.ArtifactType.SOUL_GEM) {
                        org.bukkit.inventory.meta.ItemMeta meta = invItem.getItemMeta();
                        org.bukkit.NamespacedKey keySouls = new org.bukkit.NamespacedKey(
                                com.livingtools.LivingToolsPlugin.getInstance(), "soul_gem_souls");
                        int souls = meta.getPersistentDataContainer().getOrDefault(keySouls,
                                org.bukkit.persistence.PersistentDataType.INTEGER, 0);

                        if (souls < 10) {
                            souls++;
                            meta.getPersistentDataContainer().set(keySouls,
                                    org.bukkit.persistence.PersistentDataType.INTEGER, souls);
                            com.livingtools.listeners.ArtifactListener.updateSoulGemLore(meta, souls);
                            invItem.setItemMeta(meta);
                            killer.playSound(killer.getLocation(), org.bukkit.Sound.ITEM_BOTTLE_FILL, 1, 1.5f);
                            break; // Only fill one gem per kill
                        }
                    }
                }
            }

            // Phase 26: Soul Echoes (Visuals)
            com.livingtools.visuals.SoulEchoVisualizer.playEffect(killer, event.getEntity().getLocation());

            // Phase 38: Warrior's Focus
            com.livingtools.manager.SynergyManager.addWarriorFocusStack(killer);

            // Phase 51: Ancient Library (Tome Drop)
            com.livingtools.manager.LoreManager.tryDropTome(event.getEntity().getLocation());

            // Phase 53: Sentience (Kill)
            com.livingtools.manager.SentienceManager.onKill(killer, tool);
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        double damage = event.getFinalDamage();

        if (damage <= 0)
            return;

        // Debug Log
        // com.livingtools.LivingToolsPlugin.getInstance().getLogger().info("Damage
        // Event: " + player.getName() + " took " + damage + " damage.");

        // XP Calculation: 5 XP per 1 damage taken, minimum 1 XP
        long xpGained = (long) Math.ceil(damage * 5);
        if (xpGained < 1)
            xpGained = 1;

        boolean gainedXP = false;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().isAir())
                continue;

            boolean isArmor = com.livingtools.data.LivingArmor.isLivingArmor(armor);
            boolean isTool = com.livingtools.data.LivingTool.isLivingTool(armor);

            // com.livingtools.LivingToolsPlugin.getInstance().getLogger().info("Checking
            // Item: " + armor.getType() + " | IsArmor: " + isArmor + " | IsTool: " +
            // isTool);

            if (isArmor) {
                com.livingtools.data.LivingArmor livingArmor = new com.livingtools.data.LivingArmor(
                        armor);
                livingArmor.addXP(player, xpGained);

                // Trial Progress
                com.livingtools.manager.TrialManager.onDamageTaken(player, livingArmor, damage);

                gainedXP = true;
            } else if (isTool) {
                // Fallback for armor created as LivingTool
                com.livingtools.data.LivingTool livingTool = new com.livingtools.data.LivingTool(
                        armor);
                livingTool.addXP(player, xpGained);
                gainedXP = true;
                // com.livingtools.LivingToolsPlugin.getInstance().getLogger().info("Awarded
                // XP to LivingTool Armor");
            }
        }

        if (gainedXP) {
            com.livingtools.utils.MessageUtils.sendActionBar(player,
                    org.bukkit.ChatColor.GREEN + "+" + xpGained + " Armor XP");
        }
    }
}
