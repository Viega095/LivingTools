package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrostNovaAbility extends ActiveAbility {

    public FrostNovaAbility() {
        super("frost_nova", "Nova de Escarcha", "Click Derecho: Congela enemigos cercanos (20s CD).", 20000);
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(), 50, 3, 1, 3, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.5f);

        for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10)); // 3s Freeze (Slowness high)
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 128)); // No Jump
                target.getWorld().spawnParticle(Particle.SNOW_SHOVEL, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5,
                        0.5, 0.05);
            }
        }

        player.sendMessage("§b¡Nova de Escarcha!");
    }

    @Override
    public boolean isCompatible(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }
}
