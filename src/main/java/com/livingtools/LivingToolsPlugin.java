package com.livingtools;

import org.bukkit.plugin.java.JavaPlugin;

public class LivingToolsPlugin extends JavaPlugin {

        private static LivingToolsPlugin instance;

        @Override
        public void onEnable() {
                instance = this;
                getLogger().info("Living Tools has been awakened!");

                // Load Config
                com.livingtools.manager.ConfigManager.load();
                com.livingtools.manager.LeaderboardManager.init();
                // com.livingtools.manager.BondManager.init(); // Not needed
                com.livingtools.manager.RecipeManager.registerRecipes();

                // Registration
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.ExperienceListener(), this);
                getServer().getPluginManager().registerEvents(new com.livingtools.listeners.GUIListener(),
                                this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.PersonalityListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.AbilityListener(), this);
                getServer().getPluginManager().registerEvents(new com.livingtools.listeners.ArmorListener(),
                                this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.SleepingListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.ReputationListener(), this);
                getServer().getPluginManager().registerEvents(new com.livingtools.listeners.RuneListener(),
                                this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.RitualListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.CorruptionListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.SoulboundListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.DuelManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.ShrineManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.SmugglerManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.GuideListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.CurseListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.TomeListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.abilities.SelfRepairAbility(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.abilities.SoulVampirismAbility(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.CustomEnchantManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.VoidRiftManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.SkyFortressManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.TerritoryManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.ConvergenceManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.BossAbilityManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.BossBarManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.OmniToolManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.BossDropManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.BossMinionManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.AssemblyTableManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.manager.SoulBindingManager(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.BossForgeListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.CategoryGUIListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.EnhancedGUIListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.BlinkBoostListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.BossWeaponListener(), this);
                getServer().getPluginManager()
                                .registerEvents(new com.livingtools.listeners.AbilityWorldInteractionListener(), this);

                com.livingtools.manager.BiomeManager.startBiomeTask();
                com.livingtools.manager.BloodMoonManager.startCycle();
                com.livingtools.manager.ElementalStormManager.startTask();
                com.livingtools.mechanics.CorruptionSystem.startTask();
                com.livingtools.manager.BondManager.startBondTask();
                com.livingtools.manager.HiveMindManager.startTask();
                com.livingtools.manager.BossDropManager.init();
                com.livingtools.manager.SetBonusManager.startTask();

                // RelicEffectManager — passive relic effects for boss relics
                com.livingtools.manager.RelicEffectManager relicManager = new com.livingtools.manager.RelicEffectManager();
                getServer().getPluginManager().registerEvents(relicManager, this);
                com.livingtools.manager.RelicEffectManager.startTicker();

                // WeatherBonusManager — climate and time-of-day XP bonuses
                getServer().getPluginManager().registerEvents(new com.livingtools.manager.WeatherBonusManager(), this);

                // TrailManager — particle trails for high-level tools
                getServer().getPluginManager().registerEvents(new com.livingtools.manager.TrailManager(), this);

                // ServerEventManager — Corrupted Night + Rune Storm world events
                getServer().getPluginManager().registerEvents(new com.livingtools.manager.ServerEventManager(), this);
                com.livingtools.manager.ServerEventManager.startEventScheduler();

                // DailyBonusManager — first-use-of-day XP streak bonus
                com.livingtools.manager.DailyBonusManager.startDailyReset();

                // NamingCeremonyManager — chat listener for tool naming
                getServer().getPluginManager().registerEvents(
                    new com.livingtools.manager.NamingCeremonyManager(), this);

                // SleepBonusManager — XP boost after sleeping
                getServer().getPluginManager().registerEvents(
                    new com.livingtools.manager.SleepBonusManager(), this);

                // LootBonusManager — personality-driven bonus drops on kill
                getServer().getPluginManager().registerEvents(
                    new com.livingtools.manager.LootBonusManager(), this);

                // KillStreakManager — consecutive kill tracking and XP multiplier
                getServer().getPluginManager().registerEvents(
                    new com.livingtools.manager.KillStreakManager(), this);

                // TrapKillManager — attribute kills from traps, dispensers, TNT, lava
                getServer().getPluginManager().registerEvents(
                    new com.livingtools.manager.TrapKillManager(), this);

                // Metrics
                new com.livingtools.metrics.Metrics(this, 24321); // Example ID

                // Update Checker
                if (com.livingtools.manager.ConfigManager.getBoolean("update-checker.enabled")) {
                        String repo = com.livingtools.manager.ConfigManager
                                        .getString("update-checker.repository");
                        new com.livingtools.manager.UpdateChecker(this, repo).getVersion(version -> {
                                this.latestVersion = version;
                                getLogger().info("Found new version: " + version);
                        });
                        getServer().getPluginManager().registerEvents(
                                        new com.livingtools.listeners.UpdateListener(this), this);
                }

                // Register Commands
                getCommand("living").setExecutor(new com.livingtools.commands.LivingToolCommand());
                getCommand("living")
                                .setTabCompleter(new com.livingtools.commands.LivingToolTabCompleter());
                getCommand("livingtool").setExecutor(new com.livingtools.commands.LivingToolCommand());
                getCommand("livingtool")
                                .setTabCompleter(new com.livingtools.commands.LivingToolTabCompleter());
                getCommand("ltmenu").setExecutor(new com.livingtools.commands.MenuCommand());

                // Register Ability Command
                getCommand("ltability").setExecutor(new com.livingtools.commands.AbilityCommand());
                getCommand("ltability").setTabCompleter(new com.livingtools.commands.AbilityCommand());

                // Register Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mining.VeinMinerAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.utility.MagnetAbility());

                // Register Mythic Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mythic.WorldBreakerAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mythic.GodSlayerAbility());

                // Register Combat Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.LightningStrikeAbility());

                // Register Infusions
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.infusion.InfernalInfusion());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.infusion.FireInfusion());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mining.TreeAssistAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.farming.AreaPlowAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.defense.TankAbility());

                // Register New Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mining.AutoSmeltAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.utility.TelepathyAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.passive.NightVisionAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.passive.JumpBoostAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.passive.WaterBreathingAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.passive.FireResistanceAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.passive.HasteAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.utility.SoulboundAbility());

                // Register Elemental Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.FireNovaAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.IceFreezeAbility());

                // Register Armor Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.ReflectAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.RegenerationAbility());

                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.FeatherWeightAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.ObsidianSkinAbility());

                // Register New Combat Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.VampirismAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.WitheringAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.ExecuteAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.CleaveAbility());

                // Register New Mining Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mining.TunnelingAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mining.TimberAbility());

                // Register New Armor Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.DolphinsGraceAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.InvisibilityAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.ThornsAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.AdrenalineAbility());

                // Phase 1: New Armor Expansion Abilities
                // Helmet Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.ThermalVisionAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.EternalBreathAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.MentalClarityAbility());

                // Chestplate Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.EnergyShieldAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.SpectralWingsAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.TitanHeartAbility());

                // Leggings Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.PhantomStepAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.QuantumLeapAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.IronRootsAbility());

                // Boots Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.LavaWalkerAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.SonicSpeedAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.SeismicStompAbility());

                // Set Bonuses
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.FullSetBonusAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.FireSetBonusAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.IceSetBonusAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.AirSetBonusAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.EarthSetBonusAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.VoidSetBonusAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.setbonus.LightSetBonusAbility());

                // Phase 1: Elemental Expansion
                // Fire
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.SearingSmiteAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.MagmaWalkerAbility());
                // Ice
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.FrostNovaAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.IcePathAbility());
                // Air
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.DoubleJumpAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.WindDashAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.ArrowDeflectAbility());
                // Earth
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.BedrockSkinAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.EarthSpikesAbility());

                // Phase 2: Arcane & Blood
                // Arcane
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.BlinkAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.armor.ArcaneShieldAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.utility.XPMagnetAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.LevitationAbility());

                // New Advanced Active Abilities
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.ShadowStepAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.RecallAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.SwapAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.BlinkStrikeAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.VortexAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.ThunderStepAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.PhaseShiftAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.InfernoLeapAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.TimeFreezeAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.DimensionalRiftAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.BloodSacrificeAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.SoulHarvestAbility());

                // Blood
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.BloodRageAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.combat.BleedAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.farming.GreenThumbAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.active.SandstormAbility());

                // Register New Abilities (Phase 47)
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mythic.MeteorAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mythic.BlackHoleAbility());

                // Register Void Abilities (Phase 47)
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.void_abilities.VoidShiftAbility());

                // Register Mythic Slayer (Phase 50)
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mythic.MythicSlayerAbility());

                // Register God Mode (Phase 54)
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.mythic.TerraformAbility());

                // Register Chronomancy (Phase 56)
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.chronomancy.TimeSkipAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.chronomancy.StasisAbility());

                // Register Custom Enchantments (Phase 62)
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.enchant.SoulReaperAbility());
                com.livingtools.abilities.AbilityRegistry
                                .register(new com.livingtools.abilities.enchant.ThunderlordAbility());

                // Register Durability & Balance Abilities
                com.livingtools.tasks.PlayerUpdateTask.start();

                // Start new managers
                com.livingtools.manager.ParticlePreviewManager.start();
                com.livingtools.manager.PerformanceOptimizer.startMonitoring();

        }

        public static LivingToolsPlugin getInstance() {
                return instance;
        }

        private String latestVersion;

        public String getLatestVersion() {
                return latestVersion;
        }

        public void setLatestVersion(String latestVersion) {
                this.latestVersion = latestVersion;
        }

        public static com.livingtools.api.LivingToolsAPI getAPI() {
                return com.livingtools.api.LivingToolsAPI.getInstance();
        }
}
