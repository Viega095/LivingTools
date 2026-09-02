package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class ThunderlordAbility extends Ability {

    private final Random random = new Random();

    public ThunderlordAbility() {
        super("thunderlord", "Señor del Trueno", "Probabilidad de invocar rayos y cadenas de rayos.", 45);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        Player attacker = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();

        int level = getLevel(tool);
        double chance = 0.05 + (level * 0.02); // Lvl 1: 7%, Lvl 5: 15%

        if (random.nextDouble() < chance) {
            victim.getWorld().strikeLightning(victim.getLocation());

            // Chain Lightning
            int chainCount = 0;
            int maxChain = level; // Lvl 1: 1 extra target, Lvl 5: 5 extra targets

            for (Entity e : victim.getNearbyEntities(5, 5, 5)) {
                if (e instanceof LivingEntity && e != attacker && e != victim) {
                    if (chainCount >= maxChain)
                        break;

                    e.getWorld().strikeLightning(e.getLocation());
                    chainCount++;
                }
            }

            attacker.sendMessage(ChatColor.YELLOW + "¡El trueno responde a tu llamado!");
            addXP(attacker, tool, 20 + (chainCount * 5));
        }
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().endsWith("_AXE") || type.name().endsWith("_SWORD");
    }
}
