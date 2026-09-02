package com.livingtools.abilities.active;

import com.livingtools.abilities.ActiveAbility;
import com.livingtools.data.LivingTool;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LevitationAbility extends ActiveAbility {

    public LevitationAbility() {
        super("levitation", "Levitación", "Click Derecho: Levita por 2 segundos (15s CD).", 15000);
    }

    @Override
    public void onRightClick(Player player, LivingTool tool) {
        if (!checkCooldown(player))
            return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 2)); // 2s Levitation III

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1, 1);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 10, 0.5, 0, 0.5, 0.1);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_BOOTS");
    }
}
