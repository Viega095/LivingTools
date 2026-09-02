package com.livingtools.abilities.mythic;

import com.livingtools.abilities.Ability;
import com.livingtools.abilities.AbilityType;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.Set;

public class MythicSlayerAbility extends Ability {

    private static final Set<EntityType> MYTHIC_MOBS = EnumSet.of(
            EntityType.WITHER,
            EntityType.ENDER_DRAGON,
            EntityType.WARDEN,
            EntityType.ELDER_GUARDIAN);

    public MythicSlayerAbility() {
        super("mythic_slayer", "Asesino Mítico", "Inflige +50% de daño a Jefes Míticos.", 0, AbilityType.PASSIVE);
    }

    public static boolean isMythic(EntityType type) {
        return MYTHIC_MOBS.contains(type);
    }
}
