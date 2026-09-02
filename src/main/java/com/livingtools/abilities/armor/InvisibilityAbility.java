package com.livingtools.abilities.armor;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvisibilityAbility extends Ability {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> sneakTime = new HashMap<>();

    public InvisibilityAbility() {
        super("invisibility", "Invisibilidad", "Agáchate 2s para volverte invisible.", 40, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        if (player.isSneaking()) {
            if (cooldowns.containsKey(player.getUniqueId())) {
                if (System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < 30000) { // 30s Cooldown
                    return;
                }
            }

            int ticks = sneakTime.getOrDefault(player.getUniqueId(), 0);
            ticks++;
            sneakTime.put(player.getUniqueId(), ticks);

            if (ticks >= 40) { // 2 Seconds (20 ticks/s * 2)
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false, true)); // 10s
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                sneakTime.put(player.getUniqueId(), 0);

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 1);
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation(), 10, 0.5, 1, 0.5, 0);
                player.sendMessage("§a¡Invisibilidad activada!");
            }
        } else {
            sneakTime.put(player.getUniqueId(), 0);
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_LEGGINGS");
    }
}
