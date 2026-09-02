package com.livingtools.abilities;

import java.util.HashMap;
import java.util.Map;

public class AbilityRegistry {

    private static final Map<String, Ability> abilities = new HashMap<>();

    public static void register(Ability ability) {
        abilities.put(ability.getId(), ability);
    }

    public static Ability getAbility(String id) {
        return abilities.get(id);
    }

    public static Map<String, Ability> getAbilities() {
        return abilities;
    }
}
