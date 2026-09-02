package com.livingtools.manager;

import com.livingtools.runes.RuneManager;
import com.livingtools.runes.RuneType;
import org.bukkit.inventory.ItemStack;

public class RuneFusionManager {

    public static ItemStack fuseRunes(ItemStack rune1, ItemStack rune2, ItemStack rune3) {
        if (!RuneManager.isRune(rune1) || !RuneManager.isRune(rune2) || !RuneManager.isRune(rune3)) {
            return null;
        }

        RuneType type1 = RuneManager.getRuneType(rune1);
        RuneType type2 = RuneManager.getRuneType(rune2);
        RuneType type3 = RuneManager.getRuneType(rune3);

        if (type1 != type2 || type1 != type3) {
            return null; // Must be same type
        }

        RuneManager.RuneTier tier1 = RuneManager.getRuneTier(rune1);
        RuneManager.RuneTier tier2 = RuneManager.getRuneTier(rune2);
        RuneManager.RuneTier tier3 = RuneManager.getRuneTier(rune3);

        if (tier1 != tier2 || tier1 != tier3) {
            return null; // Must be same tier
        }

        // Calculate next tier
        RuneManager.RuneTier nextTier = getNextTier(tier1);
        if (nextTier == null) {
            return null; // Already max tier
        }

        return RuneManager.createRuneItem(type1, nextTier);
    }

    private static RuneManager.RuneTier getNextTier(RuneManager.RuneTier current) {
        switch (current) {
            case COMMON:
                return RuneManager.RuneTier.RARE;
            case RARE:
                return RuneManager.RuneTier.MYTHIC;
            default:
                return null;
        }
    }
}
