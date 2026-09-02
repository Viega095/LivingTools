package com.livingtools.abilities;

import com.livingtools.data.LivingTool;
import com.livingtools.manager.BiomeManager;
import org.bukkit.entity.Player;

public abstract class InfusionAbility extends Ability {

    private final BiomeManager.BiomeCategory requiredBiome;
    private final int requiredBiomeXP;

    public InfusionAbility(String id, String name, String description, BiomeManager.BiomeCategory requiredBiome,
            int requiredBiomeXP) {
        super(id, name, description, 0); // Level req 0 because it depends on Biome XP
        this.requiredBiome = requiredBiome;
        this.requiredBiomeXP = requiredBiomeXP;
    }

    public boolean canUnlock(LivingTool tool) {
        return tool.getData().getBiomeXP(requiredBiome) >= requiredBiomeXP;
    }

    public abstract void onActiveTrigger(Player player);

    @Override
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event,
            com.livingtools.data.LivingTool tool) {
        onActiveTrigger(event.getPlayer());
    }
}
