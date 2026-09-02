package com.livingtools.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class PlaneManager {

    private static final String PLANE_NAME = "plane_of_fire";
    private static World firePlane;

    public static void init() {
        if (Bukkit.getWorld(PLANE_NAME) == null) {
            WorldCreator creator = new WorldCreator(PLANE_NAME);
            creator.environment(World.Environment.NETHER);
            creator.generateStructures(false);
            firePlane = creator.createWorld();
            generatePlatform();
        } else {
            firePlane = Bukkit.getWorld(PLANE_NAME);
        }
    }

    private static void generatePlatform() {
        if (firePlane == null)
            return;
        int y = 100;
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                firePlane.getBlockAt(x, y, z).setType(Material.OBSIDIAN);
            }
        }
        // Center
        firePlane.getBlockAt(0, y + 1, 0).setType(Material.LODESTONE);
    }

    public static void teleportToPlane(Player player) {
        if (firePlane == null)
            init();
        Location loc = new Location(firePlane, 0.5, 101, 0.5);
        player.teleport(loc);
        player.sendMessage("§c§l¡Has entrado al Plano de Fuego!");
    }

    public static boolean isPlane(World world) {
        return world.getName().equals(PLANE_NAME);
    }
}
