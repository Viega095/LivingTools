package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

public class EnergyShieldAbility extends Ability {

    private static final NamespacedKey SHIELD_KEY = new NamespacedKey(
            com.livingtools.LivingToolsPlugin.getInstance(), "energy_shield");

    public EnergyShieldAbility() {
        super("energy_shield", "Escudo de Energía",
                "Absorbe daño en una barra de energía separada que se regenera",
                40, AbilityType.ARMOR);
    }

    @Override
    public void onDamageTaken(EntityDamageEvent event, Player player) {
        double shieldPoints = getShieldPoints(player);
        double damage = event.getDamage();

        if (shieldPoints > 0) {
            if (shieldPoints >= damage) {
                // Escudo absorbe todo el daño
                setShieldPoints(player, shieldPoints - damage);
                event.setDamage(0);
                player.sendMessage("§b⚡ Escudo: §3" + (int) (shieldPoints - damage) + "§7/§3100");
            } else {
                // Escudo absorbe parcialmente
                event.setDamage(damage - shieldPoints);
                setShieldPoints(player, 0);
                player.sendMessage("§c⚡ ¡Escudo agotado!");
            }
        }
    }

    @Override
    public void onHold(Player player) {
        // Regenerar escudo lentamente (1 punto cada 2 segundos)
        double current = getShieldPoints(player);
        if (current < 100) {
            if (Math.random() < 0.025) { // ~1 cada 40 ticks
                setShieldPoints(player, Math.min(100, current + 1));
            }
        }
    }

    private double getShieldPoints(Player player) {
        return player.getPersistentDataContainer()
                .getOrDefault(SHIELD_KEY, PersistentDataType.DOUBLE, 100.0);
    }

    private void setShieldPoints(Player player, double points) {
        player.getPersistentDataContainer()
                .set(SHIELD_KEY, PersistentDataType.DOUBLE, points);
    }

    @Override
    public boolean isCompatible(Material material) {
        return material.name().contains("CHESTPLATE");
    }
}
