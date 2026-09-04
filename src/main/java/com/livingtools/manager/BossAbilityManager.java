package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import com.livingtools.entities.DryadBoss;
import com.livingtools.entities.LeviathanBoss;
import com.livingtools.entities.WyrmBoss;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class BossAbilityManager implements Listener {

    private static final Random random = new Random();
    private static final java.util.Map<java.util.UUID, java.util.List<Location>> bossArenas = new java.util.HashMap<>();

    public static void registerArena(java.util.UUID bossId, java.util.List<Location> blocks) {
        bossArenas.put(bossId, blocks);
    }

    public static void removeArena(java.util.UUID bossId) {
        if (bossArenas.containsKey(bossId)) {
            java.util.List<Location> blocks = bossArenas.get(bossId);
            for (Location loc : blocks) {
                loc.getBlock().setType(Material.AIR);
            }
            bossArenas.remove(bossId);
        }
    }

    @EventHandler
    public void onBossDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!bossArenas.containsKey(event.getEntity().getUniqueId())) return;
        removeArena(event.getEntity().getUniqueId());
        // Announce boss kill to server
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            String bossName = event.getEntity().getCustomName() != null
                    ? org.bukkit.ChatColor.stripColor(event.getEntity().getCustomName())
                    : event.getEntity().getType().name().replace("_", " ");
            org.bukkit.inventory.ItemStack held = killer.getInventory().getItemInMainHand();
            if (com.livingtools.data.LivingTool.isLivingTool(held)) {
                com.livingtools.data.LivingTool kt = new com.livingtools.data.LivingTool(held);
                AnnouncementManager.announceBossKill(killer, bossName, kt);
                DailyChallengeManager.onKill(killer, kt, event.getEntityType());
            } else {
                Bukkit.broadcastMessage(org.bukkit.ChatColor.RED + "☠ " + killer.getName()
                        + org.bukkit.ChatColor.GRAY + " derrotó a " + org.bukkit.ChatColor.RED + bossName + "!");
            }
        }
    }

    @EventHandler
    public void onBossAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity))
            return;
        LivingEntity boss = (LivingEntity) event.getDamager();

        if (isLivingBoss(boss)) {
            handleLivingBossAbilities(boss, event.getEntity());
        } else if (isSeraphim(boss)) {
            handleSeraphimAbilities(boss, event.getEntity());
        } else if (isTitan(boss)) {
            handleTitanAbilities(boss, event.getEntity());
        } else if (isShadowCreature(boss)) {
            handleShadowCreatureAbilities(boss, event.getEntity());
        }
    }

    public static LivingEntity spawnLivingBoss(Location location) {
        if (location.getWorld() == null)
            return null;
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location,
                org.bukkit.entity.EntityType.WITHER_SKELETON);
        entity.setCustomName(MessageUtils.color("&c&lLiving Boss"));
        entity.setCustomNameVisible(true);
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(300);
        entity.setHealth(300);

        // Equip with netherite armor
        entity.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.NETHERITE_HELMET));
        entity.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(Material.NETHERITE_CHESTPLATE));
        entity.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(Material.NETHERITE_LEGGINGS));
        entity.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(Material.NETHERITE_BOOTS));
        entity.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.NETHERITE_SWORD));

        Bukkit.broadcastMessage(MessageUtils.color("&c&l¡El Living Boss ha despertado!"));
        BossBarManager.addBoss(entity, "&c&lLiving Boss", org.bukkit.boss.BarColor.RED);
        return entity;
    }

    public static LivingEntity spawnTitan(Location location) {
        if (location.getWorld() == null)
            return null;
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location,
                org.bukkit.entity.EntityType.IRON_GOLEM);
        entity.setCustomName(MessageUtils.color("&5&lTitan del Vacío"));
        entity.setCustomNameVisible(true);
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(1000);
        entity.setHealth(1000);

        entity.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "is_titan"),
                PersistentDataType.BYTE, (byte) 1);

        Bukkit.broadcastMessage(MessageUtils.color("&5&l¡El Titán del Vacío ha despertado!"));
        BossBarManager.addBoss(entity, "&5&lTitan del Vacío", org.bukkit.boss.BarColor.PURPLE);
        return entity;
    }

    public static LivingEntity spawnDryad(Location location) {
        DryadBoss boss = new DryadBoss(location);
        BossBarManager.addBoss(boss.getBoss(), "&2&lDriada Corrupta", BarColor.GREEN);
        return boss.getBoss();
    }

    public static LivingEntity spawnWyrm(Location location) {
        WyrmBoss boss = new WyrmBoss(location);
        BossBarManager.addBoss(boss.getBoss(), "&6&lWyrm de las Arenas", BarColor.YELLOW);
        return boss.getBoss();
    }

    public static LivingEntity spawnLeviathan(Location location) {
        LeviathanBoss boss = new LeviathanBoss(location);
        BossBarManager.addBoss(boss.getBoss(), "&3&lLeviatán", BarColor.BLUE);
        return boss.getBoss();
    }

    // --- Living Boss ---
    private boolean isLivingBoss(LivingEntity entity) {
        return entity.getCustomName() != null && entity.getCustomName().contains("Living Boss") && !isMinion(entity);
    }

    private boolean isMinion(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "is_minion"),
                PersistentDataType.BYTE);
    }

    // --- Enrage Mechanic ---
    private boolean isEnraged(LivingEntity boss) {
        double maxHealth = boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        return boss.getHealth() <= (maxHealth * 0.5);
    }

    private void applyEnrageEffects(LivingEntity boss) {
        if (!boss.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            boss.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1)); // Strength II
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1)); // Speed II
            boss.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, boss.getLocation().add(0, 2, 0), 5);
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 2);
        }
    }

    private void handleLivingBossAbilities(LivingEntity boss, Entity target) {
        double chance = 0.2;
        if (isEnraged(boss)) {
            chance = 0.4; // Double chance
            applyEnrageEffects(boss);
        }

        if (random.nextDouble() < chance) {
            if (random.nextDouble() < chance) {
                int ability = random.nextInt(7);
                if (ability == 0) {
                    magmaEruption(boss.getLocation(), target.getLocation());
                } else if (ability == 1) {
                    anvilDrop(target.getLocation());
                } else if (ability == 2) {
                    meteorShower(target.getLocation());
                } else if (ability == 3) {
                    lavaWave(boss);
                } else if (ability == 4) {
                    summonMinions(boss);
                } else if (ability == 5) {
                    lavaFissure(boss, target);
                } else {
                    magmaBomb(boss, target);
                }
            }
        }
    }

    private void summonMinions(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&c&lLiving Boss: &c¡Mis esbirros, atacad y destruidlos!"));
        for (int i = 0; i < 3; i++) {
            Location spawnLoc = boss.getLocation().add(random.nextInt(6) - 3, 0, random.nextInt(6) - 3);
            LivingEntity minion = (LivingEntity) boss.getWorld().spawnEntity(spawnLoc,
                    org.bukkit.entity.EntityType.WITHER_SKELETON);
            minion.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.IRON_SWORD));
            minion.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.IRON_HELMET));
            minion.setCustomName(MessageUtils.color("&cEsbirro Infernal"));
            minion.setCustomNameVisible(true);
            minion.getPersistentDataContainer()
                    .set(new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                            "is_minion"),
                            PersistentDataType.BYTE, (byte) 1);
        }
    }

    private void lavaFissure(LivingEntity boss, Entity target) {
        Bukkit.broadcastMessage(MessageUtils.color("&c&lLiving Boss: &c¡La tierra arderá bajo vuestros pies!"));
        Location start = boss.getLocation();
        Vector dir = target.getLocation().toVector().subtract(start.toVector()).normalize();
        new BukkitRunnable() {
            int i = 0;
            Location current = start.clone();

            @Override
            public void run() {
                if (i >= 10) {
                    this.cancel();
                    return;
                }
                current.add(dir);
                current.getWorld().spawnParticle(Particle.LAVA, current, 10);
                current.getWorld().playSound(current, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);

                // Damage
                for (Entity e : current.getWorld().getNearbyEntities(current, 1, 1, 1)) {
                    if (e instanceof LivingEntity && e != boss) {
                        ((LivingEntity) e).damage(10);
                        ((LivingEntity) e).setFireTicks(100);
                    }
                }
                i++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 2);
    }

    private void magmaBomb(LivingEntity boss, Entity target) {
        Bukkit.broadcastMessage(MessageUtils.color("&c&lLiving Boss: &c¡Sentid el núcleo del infierno!"));
        Location spawnLoc = boss.getEyeLocation().add(boss.getLocation().getDirection().multiply(2));
        org.bukkit.entity.LargeFireball fireball = (org.bukkit.entity.LargeFireball) boss.getWorld()
                .spawnEntity(spawnLoc, org.bukkit.entity.EntityType.FIREBALL);
        fireball.setDirection(target.getLocation().toVector().subtract(boss.getEyeLocation().toVector()).normalize());
        fireball.setYield(4); // Big explosion
        fireball.setIsIncendiary(true);
    }

    // ... (existing methods)

    // In handleSeraphimAbilities
    private void handleSeraphimAbilities(LivingEntity boss, Entity target) {
        double chance = 0.15;
        if (isEnraged(boss)) {
            chance = 0.3;
            applyEnrageEffects(boss);
        }

        if (random.nextDouble() < chance) {
            if (random.nextDouble() < chance) {
                int ability = random.nextInt(7);
                if (ability == 0) {
                    holyRay(boss, target);
                } else if (ability == 1) {
                    ascension((LivingEntity) target);
                } else if (ability == 2) {
                    lightningStorm(boss.getLocation());
                } else if (ability == 3) {
                    healingNova(boss);
                } else if (ability == 4) {
                    divineJudgment(boss, target);
                } else if (ability == 5) {
                    holyNova(boss);
                } else {
                    angelGuard(boss);
                }
            }
        }
    }

    private void divineJudgment(LivingEntity boss, Entity target) {
        Bukkit.broadcastMessage(MessageUtils.color("&e&lSeraphim: &e¡Juicio Divino!"));
        Location targetLoc = target.getLocation();
        boss.getWorld().strikeLightningEffect(targetLoc);
        targetLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, targetLoc, 1);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        for (Entity e : targetLoc.getWorld().getNearbyEntities(targetLoc, 5, 5, 5)) {
            if (e instanceof LivingEntity && e != boss) {
                ((LivingEntity) e).damage(25);
            }
        }
    }

    private void holyNova(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&e&lSeraphim: &e¡Alejaos, impuros!"));
        boss.getWorld().spawnParticle(Particle.FLASH, boss.getLocation(), 10);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 2);

        // Heal Boss
        double maxHealth = boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        boss.setHealth(Math.min(maxHealth, boss.getHealth() + 20));

        // Push and Damage
        for (Entity e : boss.getNearbyEntities(8, 8, 8)) {
            if (e instanceof LivingEntity && e != boss) {
                ((LivingEntity) e).damage(15);
                Vector dir = e.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize().multiply(2);
                e.setVelocity(dir);
            }
        }
    }

    private void angelGuard(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&e&lSeraphim: &e¡Guardianes, protegedme!"));
        for (int i = 0; i < 2; i++) {
            Location spawnLoc = boss.getLocation().add(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
            org.bukkit.entity.IronGolem golem = (org.bukkit.entity.IronGolem) boss.getWorld().spawnEntity(spawnLoc,
                    org.bukkit.entity.EntityType.IRON_GOLEM);
            golem.setCustomName(MessageUtils.color("&eGuardián Celestial"));
            golem.setCustomNameVisible(true);
            golem.setPlayerCreated(false);

            // Remove after 20 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!golem.isDead()) {
                        golem.remove();
                        golem.getWorld().spawnParticle(Particle.CLOUD, golem.getLocation(), 10);
                    }
                }
            }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 400);
        }
    }

    // In handleTitanAbilities
    private void handleTitanAbilities(LivingEntity boss, Entity target) {
        double chance = 0.1;
        if (isEnraged(boss)) {
            chance = 0.2;
            applyEnrageEffects(boss);
        }

        if (random.nextDouble() < chance) {
            int ability = random.nextInt(7);
            if (ability == 0) {
                earthquake(boss);
            } else if (ability == 1) {
                voidScream(boss);
            } else if (ability == 2) {
                shockwave(boss);
            } else if (ability == 3) {
                gravityWell(boss, target);
            } else if (ability == 4) {
                voidCrush(boss);
            } else if (ability == 5) {
                obsidianCage(boss, target);
            } else {
                voidLaser(boss, target);
            }
        }
    }

    private void voidCrush(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&5&lTitan: &5¡Aplastamiento del Vacío!"));
        for (Entity e : boss.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity && e != boss && e instanceof Player) {
                e.teleport(e.getLocation().add(0, 10, 0));
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1)); // Brief
                                                                                                            // slow fall
                                                                                                            // then drop
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        e.setVelocity(new Vector(0, -2, 0)); // Slam down
                    }
                }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 20);
            }
        }
    }

    private void obsidianCage(LivingEntity boss, Entity target) {
        Bukkit.broadcastMessage(MessageUtils.color("&5&lTitan: &5¡No hay escape del vacío!"));
        Location loc = target.getLocation().getBlock().getLocation();
        java.util.List<Location> blocks = new java.util.ArrayList<>();

        // 3x3x3 hollow box
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location l = loc.clone().add(x, y, z);
                    if (l.getBlock().getType() == Material.AIR) {
                        if (x == -1 || x == 1 || z == -1 || z == 1 || y == 0 || y == 3) {
                            l.getBlock().setType(Material.OBSIDIAN);
                            blocks.add(l);
                        }
                    }
                }
            }
        }

        // Remove after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location l : blocks) {
                    l.getBlock().setType(Material.AIR);
                }
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
            }
        }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 100);
    }

    private void voidLaser(LivingEntity boss, Entity target) {
        Bukkit.broadcastMessage(MessageUtils.color("&5&lTitan: &5¡Desintegraos!"));
        Location start = boss.getEyeLocation();
        Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(start.toVector()).normalize();

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i >= 20) { // 1 second beam
                    this.cancel();
                    return;
                }

                // Raytrace
                Location current = start.clone();
                for (int k = 0; k < 20; k++) { // 20 blocks range
                    current.add(dir);
                    current.getWorld().spawnParticle(Particle.REDSTONE, current, 1,
                            new Particle.DustOptions(org.bukkit.Color.PURPLE, 1));
                    for (Entity e : current.getWorld().getNearbyEntities(current, 0.5, 0.5, 0.5)) {
                        if (e instanceof LivingEntity && e != boss) {
                            ((LivingEntity) e).damage(5); // Rapid damage
                        }
                    }
                }
                i++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 1);
    }

    private void magmaEruption(Location bossLoc, Location targetLoc) {
        targetLoc.getWorld().spawnParticle(Particle.LAVA, targetLoc, 20);
        targetLoc.getBlock().setType(Material.MAGMA_BLOCK);
        new BukkitRunnable() {
            @Override
            public void run() {
                targetLoc.getBlock().setType(Material.AIR);
            }
        }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 60); // 3 seconds
        Bukkit.broadcastMessage(MessageUtils.color("&c&l¡ERUPCIÓN DE MAGMA!"));
    }

    private void anvilDrop(Location targetLoc) {
        Location dropLoc = targetLoc.clone().add(0, 5, 0);
        FallingBlock anvil = dropLoc.getWorld().spawnFallingBlock(dropLoc, Material.ANVIL.createBlockData());
        anvil.setDropItem(false);
        anvil.setHurtEntities(true);
        Bukkit.broadcastMessage(MessageUtils.color("&8&l¡CUIDADO ARRIBA!"));
    }

    private void meteorShower(Location targetLoc) {
        Bukkit.broadcastMessage(MessageUtils.color("&6&l¡LLUVIA DE METEORITOS!"));
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 5) {
                    this.cancel();
                    return;
                }
                Location spawnLoc = targetLoc.clone().add(random.nextInt(10) - 5, 10, random.nextInt(10) - 5);
                org.bukkit.entity.Fireball fireball = (org.bukkit.entity.Fireball) spawnLoc.getWorld()
                        .spawnEntity(spawnLoc, org.bukkit.entity.EntityType.FIREBALL);
                fireball.setDirection(new Vector(0, -1, 0));
                fireball.setYield(2); // Explosion power
                count++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 10);
    }

    private void lavaWave(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&c&l¡OLA DE LAVA!"));
        new BukkitRunnable() {
            double radius = 1;

            @Override
            public void run() {
                if (radius > 10) {
                    this.cancel();
                    return;
                }
                for (double angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = radius * Math.cos(rad);
                    double z = radius * Math.sin(rad);
                    boss.getWorld().spawnParticle(Particle.DRIP_LAVA, boss.getLocation().add(x, 0.5, z), 5);
                }
                for (Entity e : boss.getNearbyEntities(radius, 2, radius)) {
                    if (e instanceof LivingEntity && e != boss
                            && e.getLocation().distance(boss.getLocation()) > radius - 1
                            && e.getLocation().distance(boss.getLocation()) < radius + 1) {
                        ((LivingEntity) e).damage(8);
                        ((LivingEntity) e).setFireTicks(60);
                    }
                }
                radius += 1;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 3);
    }

    // --- Seraphim ---
    private boolean isSeraphim(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(
                com.livingtools.LivingToolsPlugin.getInstance(), "is_seraphim"), PersistentDataType.BYTE);
    }

    private void holyRay(LivingEntity boss, Entity target) {
        Location start = boss.getEyeLocation();
        Location end = target.getLocation().add(0, 1, 0);
        Vector direction = end.toVector().subtract(start.toVector()).normalize();

        for (int i = 0; i < 10; i++) {
            start.add(direction);
            start.getWorld().spawnParticle(Particle.END_ROD, start, 5);
        }

        if (target instanceof LivingEntity) {
            ((LivingEntity) target).damage(10);
            ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
        }
        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 2);
        Bukkit.broadcastMessage(MessageUtils.color("&e&l¡RAYO SAGRADO!"));
    }

    private void ascension(LivingEntity target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 5)); // 3 seconds float
        Bukkit.broadcastMessage(MessageUtils.color("&e&l¡ASCENSIÓN!"));
    }

    private void lightningStorm(Location center) {
        Bukkit.broadcastMessage(MessageUtils.color("&e&l¡TORMENTA ELÉCTRICA!"));
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 10) {
                    this.cancel();
                    return;
                }
                Location strikeLoc = center.clone().add(random.nextInt(20) - 10, 0, random.nextInt(20) - 10);
                strikeLoc.setY(strikeLoc.getWorld().getHighestBlockYAt(strikeLoc));
                strikeLoc.getWorld().strikeLightning(strikeLoc);
                count++;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 5);
    }

    private void healingNova(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&a&l¡NOVA CURATIVA!"));
        boss.getWorld().spawnParticle(Particle.HEART, boss.getLocation().add(0, 2, 0), 20);
        boss.setHealth(Math.min(boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(),
                boss.getHealth() + 50));
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    // --- Titan ---
    private boolean isTitan(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(), "is_titan"),
                PersistentDataType.BYTE);
    }

    private void earthquake(LivingEntity boss) {
        for (Entity e : boss.getNearbyEntities(10, 5, 10)) {
            if (e instanceof LivingEntity && e != boss) {
                ((LivingEntity) e).damage(15);
                e.setVelocity(new Vector(0, 1, 0)); // Launch up
            }
        }
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.5f);
        boss.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, boss.getLocation(), 5);
        Bukkit.broadcastMessage(MessageUtils.color("&4&l¡TERREMOTO!"));
    }

    private void voidScream(LivingEntity boss) {
        for (Entity e : boss.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity && e != boss) {
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
            }
        }
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 0.5f);
        Bukkit.broadcastMessage(MessageUtils.color("&5&l¡GRITO DEL VACÍO!"));
    }

    private void shockwave(LivingEntity boss) {
        Bukkit.broadcastMessage(MessageUtils.color("&4&l¡ONDA DE CHOQUE! ¡SALTA!"));
        new BukkitRunnable() {
            double radius = 1;

            @Override
            public void run() {
                if (radius > 15) {
                    this.cancel();
                    return;
                }
                for (double angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = radius * Math.cos(rad);
                    double z = radius * Math.sin(rad);
                    boss.getWorld().spawnParticle(Particle.CRIT, boss.getLocation().add(x, 0.5, z), 1);
                }

                for (Entity e : boss.getNearbyEntities(radius, 2, radius)) {
                    if (e instanceof LivingEntity && e != boss
                            && e.getLocation().distance(boss.getLocation()) > radius - 1
                            && e.getLocation().distance(boss.getLocation()) < radius + 1) {
                        if (e.isOnGround()) {
                            ((LivingEntity) e).damage(20);
                            e.setVelocity(e.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize()
                                    .multiply(2).setY(0.5));
                        }
                    }
                }
                radius += 1;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 2);
    }

    private void gravityWell(LivingEntity boss, Entity target) {
        Bukkit.broadcastMessage(MessageUtils.color("&5&l¡POZO DE GRAVEDAD!"));
        Location center = boss.getLocation();
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    this.cancel();
                    return;
                }

                for (Entity e : boss.getNearbyEntities(15, 15, 15)) {
                    if (e instanceof LivingEntity && e != boss) {
                        Vector dir = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                        e.setVelocity(e.getVelocity().add(dir));
                    }
                }
                boss.getWorld().spawnParticle(Particle.PORTAL, center, 10);
                ticks += 5;
            }
        }.runTaskTimer(com.livingtools.LivingToolsPlugin.getInstance(), 0, 5);
    }

    // --- Shadow Creature ---
    private boolean isShadowCreature(LivingEntity entity) {
        return entity.getPersistentDataContainer()
                .has(new org.bukkit.NamespacedKey(com.livingtools.LivingToolsPlugin.getInstance(),
                        "is_shadow_creature"), PersistentDataType.BYTE);
    }

    private void handleShadowCreatureAbilities(LivingEntity boss, Entity target) {
        if (random.nextDouble() < 0.2) {
            shadowStep(boss, target);
        }
    }

    private void shadowStep(LivingEntity boss, Entity target) {
        Location behind = target.getLocation().add(target.getLocation().getDirection().multiply(-1));
        boss.teleport(behind);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation(), 20);
    }
}
