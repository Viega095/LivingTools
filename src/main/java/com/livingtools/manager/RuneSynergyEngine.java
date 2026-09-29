package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.runes.RuneType;
import com.livingtools.visuals.DamageIndicatorManager;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * RuneSynergyEngine — Reconoce combinaciones arcanas de 3 runas y desata super sinergias ocultas.
 */
public class RuneSynergyEngine {

    public enum SecretSynergy {
        HELIOS_LANCE("Lanza de Helios", ChatColor.GOLD + "☀️ Lanza de Helios",
                "Invoca un pilar solar descendente que incinera al enemigo (+80% daño ígneo)."),
        ARCANE_WIND("Viento Arcano", ChatColor.AQUA + "🌪 Viento Arcano",
                "Ráfaga sónica que desestabiliza y empuja a todos los enemigos cercanos."),
        MIDAS_BLESSING("Bendición de Midas", ChatColor.YELLOW + "👑 Bendición de Midas",
                "Transmuta el impacto en riqueza (+100% XP y lluvia de pepitas de oro).");

        private final String name;
        private final String displayName;
        private final String description;

        SecretSynergy(String name, String displayName, String description) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public static SecretSynergy detectSynergy(LivingTool tool) {
        List<RuneType> runes = tool.getData().getRunes();
        if (runes.size() < 2) return null;

        if (runes.contains(RuneType.IGNIS) && runes.contains(RuneType.VAMPIRISM)) {
            return SecretSynergy.HELIOS_LANCE;
        }
        if (runes.contains(RuneType.CELERITAS) && runes.contains(RuneType.ECHO)) {
            return SecretSynergy.ARCANE_WIND;
        }
        if (runes.contains(RuneType.FORTUNA) && runes.contains(RuneType.SAPIENTIA)) {
            return SecretSynergy.MIDAS_BLESSING;
        }

        return null;
    }

    public static void applySynergy(Player attacker, LivingEntity target, LivingTool tool, EntityDamageByEntityEvent event) {
        SecretSynergy synergy = detectSynergy(tool);
        if (synergy == null) return;

        // Probabilidad del 25% por golpe
        if (Math.random() > 0.25) return;

        Location targetLoc = target.getLocation();

        switch (synergy) {
            case HELIOS_LANCE:
                event.setDamage(event.getDamage() * 1.8);
                target.setFireTicks(120); // 6s

                // Pilar solar de partículas
                ParticleOptimizer.spawnHelix(targetLoc, 1.2, 5.0, Particle.FLAME, null);
                targetLoc.getWorld().spawnParticle(Particle.LAVA, targetLoc.add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
                targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.6f);

                DamageIndicatorManager.spawnIndicator(targetLoc.add(0, 1.5, 0), ChatColor.GOLD + "☀️ LANZA DE HELIOS (+80%)");
                attacker.sendMessage(ChatColor.GOLD + "⚡ ¡SINERGIA RÚNICA: " + synergy.getDisplayName() + ChatColor.GOLD + " ACTIVADA!");
                break;

            case ARCANE_WIND:
                // Ráfaga y empuje en área
                for (org.bukkit.entity.Entity near : targetLoc.getWorld().getNearbyEntities(targetLoc, 5.0, 5.0, 5.0)) {
                    if (near instanceof LivingEntity && !near.equals(attacker) && !(near instanceof org.bukkit.entity.ArmorStand)) {
                        Vector push = near.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(1.2).setY(0.4);
                        near.setVelocity(push);
                        ((LivingEntity) near).damage(6.0, attacker);
                    }
                }

                ParticleOptimizer.spawnCircle(targetLoc, 4.0, Particle.SWEEP_ATTACK, 16, null);
                targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f, 0.6f);

                DamageIndicatorManager.spawnIndicator(targetLoc.add(0, 1.5, 0), ChatColor.AQUA + "🌪 VIENTO ARCANO (AOE)");
                attacker.sendMessage(ChatColor.AQUA + "⚡ ¡SINERGIA RÚNICA: " + synergy.getDisplayName() + ChatColor.AQUA + " ACTIVADA!");
                break;

            case MIDAS_BLESSING:
                tool.addXP(attacker, 30L);
                targetLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, targetLoc.add(0, 1, 0), 25, 0.5, 0.5, 0.5, 0.1);
                targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2f, 1.8f);

                DamageIndicatorManager.spawnIndicator(targetLoc.add(0, 1.5, 0), ChatColor.YELLOW + "👑 BENDICIÓN DE MIDAS (+30 XP)");
                attacker.sendMessage(ChatColor.YELLOW + "⚡ ¡SINERGIA RÚNICA: " + synergy.getDisplayName() + ChatColor.YELLOW + " ACTIVADA!");
                break;
        }
    }
}
