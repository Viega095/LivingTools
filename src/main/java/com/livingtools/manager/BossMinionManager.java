package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class BossMinionManager implements Listener {

    private final NamespacedKey MINION_STAGE_KEY = new NamespacedKey(LivingToolsPlugin.getInstance(), "minion_stage");
    private final Random random = new Random();

    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity boss = (LivingEntity) event.getEntity();

        if (isLivingBoss(boss)) {
            checkAndSpawnMinions(boss, EntityType.MAGMA_CUBE, "Living Boss Minion");
        } else if (isSeraphim(boss)) {
            checkAndSpawnMinions(boss, EntityType.VEX, "Cherub");
        } else if (isTitan(boss)) {
            checkAndSpawnMinions(boss, EntityType.ENDERMITE, "Void Parasite");
        }
    }

    private void checkAndSpawnMinions(LivingEntity boss, EntityType minionType, String minionName) {
        double maxHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = boss.getHealth();
        double percentage = currentHealth / maxHealth;

        int currentStage = boss.getPersistentDataContainer().getOrDefault(MINION_STAGE_KEY, PersistentDataType.INTEGER,
                0);
        int newStage = currentStage;

        if (percentage <= 0.75 && currentStage < 1) {
            spawnMinions(boss, minionType, minionName, 3);
            newStage = 1;
        } else if (percentage <= 0.50 && currentStage < 2) {
            spawnMinions(boss, minionType, minionName, 4);
            newStage = 2;
        } else if (percentage <= 0.25 && currentStage < 3) {
            spawnMinions(boss, minionType, minionName, 5);
            newStage = 3;
        }

        if (newStage != currentStage) {
            boss.getPersistentDataContainer().set(MINION_STAGE_KEY, PersistentDataType.INTEGER, newStage);
        }
    }

    private void spawnMinions(LivingEntity boss, EntityType type, String name, int count) {
        Location loc = boss.getLocation();
        for (int i = 0; i < count; i++) {
            Location spawnLoc = loc.clone().add(random.nextDouble() * 4 - 2, 0, random.nextDouble() * 4 - 2);
            Entity minion = boss.getWorld().spawnEntity(spawnLoc, type);
            minion.setCustomName(MessageUtils.color("&c" + name));
            minion.setCustomNameVisible(true);

            // Tag as minion for drops
            minion.getPersistentDataContainer().set(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_minion"),
                    PersistentDataType.BYTE, (byte) 1);

            if (minion instanceof MagmaCube) {
                ((MagmaCube) minion).setSize(2);
            }
        }
        boss.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1, 2);
    }

    // --- Identification ---
    private boolean isLivingBoss(LivingEntity entity) {
        return entity.getCustomName() != null && entity.getCustomName().contains("Living Boss") && !isMinion(entity);
    }

    private boolean isMinion(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_minion"),
                PersistentDataType.BYTE);
    }

    private boolean isSeraphim(LivingEntity entity) {
        return entity.getPersistentDataContainer()
                .has(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_seraphim"), PersistentDataType.BYTE);
    }

    private boolean isTitan(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(LivingToolsPlugin.getInstance(), "is_titan"),
                PersistentDataType.BYTE);
    }
}
