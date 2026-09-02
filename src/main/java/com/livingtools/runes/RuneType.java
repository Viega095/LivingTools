package com.livingtools.runes;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum RuneType {
    IGNIS("Ignis", "Aumenta el daño en 10%", Material.MAGMA_CREAM, ChatColor.RED),
    CELERITAS("Celeritas", "Otorga Velocidad II al sostener", Material.SUGAR, ChatColor.WHITE),
    SAPIENTIA("Sapientia", "Aumenta la ganancia de XP en 20%", Material.LAPIS_LAZULI, ChatColor.BLUE),
    FORTUNA("Fortuna", "Probabilidad de duplicar drops de bloques y mobs", Material.EMERALD, ChatColor.GREEN),
    VAMPIRISM("Vampirismo", "Cura 1 corazón al atacar", Material.REDSTONE, ChatColor.DARK_RED),
    LUNAR("Lunar", "Daño extra durante la noche", Material.QUARTZ, ChatColor.AQUA),
    GREED("Codicia", "Probabilidad de duplicar drops de mobs", Material.GOLD_NUGGET, ChatColor.GOLD),
    ECHO("Eco", "Probabilidad de no activar cooldown de habilidad", Material.ECHO_SHARD, ChatColor.DARK_AQUA);

    private final String name;
    private final String description;
    private final Material material;
    private final ChatColor color;

    RuneType(String name, String description, Material material, ChatColor color) {
        this.name = name;
        this.description = description;
        this.material = material;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getMaterial() {
        return material;
    }

    public ChatColor getColor() {
        return color;
    }
}
