package com.livingtools.abilities.setbonus;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import com.livingtools.data.LivingArmor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FullSetBonusAbility extends Ability {

    public FullSetBonusAbility() {
        super("full_set_bonus", "Bonificación de Set Completo",
                "Bonos por usar las 4 piezas de armadura viviente",
                10, AbilityType.ARMOR);
    }

    @Override
    public void onHold(Player player) {
        // Verificar que tiene las 4 piezas
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        if (!hasFullSet(helmet, chestplate, leggings, boots)) {
            return;
        }

        // Calcular nivel promedio del set
        int avgLevel = getAverageLevel(helmet, chestplate, leggings, boots);

        // Aplicar bonos según nivel
        if (avgLevel >= 50) {
            applyTier3Bonuses(player);
        } else if (avgLevel >= 30) {
            applyTier2Bonuses(player);
        } else if (avgLevel >= 10) {
            applyTier1Bonuses(player);
        }
    }

    private boolean hasFullSet(ItemStack... pieces) {
        for (ItemStack piece : pieces) {
            if (piece == null || !LivingArmor.isLivingArmor(piece)) {
                return false;
            }
        }
        return true;
    }

    private int getAverageLevel(ItemStack... pieces) {
        int total = 0;
        for (ItemStack piece : pieces) {
            if (piece != null) {
                LivingArmor armor = new LivingArmor(piece);
                total += armor.getLevel();
            }
        }
        return total / pieces.length;
    }

    private void applyTier1Bonuses(Player player) {
        // +5% daño (simulado con Strength)
        if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0, false, false));
        }
    }

    private void applyTier2Bonuses(Player player) {
        // +10% daño + Regeneración I
        if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0, false, false));
        }
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, false, false));
        }
    }

    private void applyTier3Bonuses(Player player) {
        // +15% daño + Regeneración II + Resistencia I
        if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1, false, false));
        }
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false));
        }
        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0, false, false));
        }
    }

    @Override
    public boolean isCompatible(Material material) {
        return true;
    }
}
