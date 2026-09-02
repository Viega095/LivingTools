package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FeedingManager {

    private static final Random random = new Random();

    public static boolean tryFeed(Player player, LivingTool tool) {
        return tryFeed(player, tool, false);
    }

    public static boolean tryFeed(Player player, LivingTool tool, boolean silent) {
        if (tool.getData().getFullness() >= 10) {
            if (!silent)
                player.sendMessage(ChatColor.RED + "¡Tu herramienta ya está llena!");
            return false;
        }

        Material preferredFood = getPreferredFoodType(tool.getItem().getType());
        if (preferredFood == null) {
            if (!silent)
                player.sendMessage(ChatColor.RED + "Esta herramienta no parece tener hambre de nada conocido.");
            return false;
        }

        // Check for food in inventory
        ItemStack foodItem = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFoodMatch(item.getType(), preferredFood)) {
                foodItem = item;
                break;
            }
        }

        if (foodItem == null) {
            if (!silent)
                player.sendMessage(ChatColor.RED + "No tienes comida adecuada. Tu herramienta quiere: "
                        + getFoodDescription(preferredFood));
            return false;
        }

        // Consume food
        foodItem.setAmount(foodItem.getAmount() - 1);
        tool.getData().addFullness(1);
        if (!silent) {
            player.sendMessage(ChatColor.GREEN + "¡Has alimentado a tu herramienta! (Saciedad: "
                    + tool.getData().getFullness() + "/10)");
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
        }

        // Random dialogue after feeding (only if not silent to avoid spam)
        if (!silent && random.nextInt(100) < 30) {
            player.sendMessage(ChatColor.GRAY + tool.getItem().getItemMeta().getDisplayName() + ChatColor.RESET + ": "
                    + ChatColor.ITALIC + "Delicioso...");
        }
        return true;
    }

    public static void checkHunger(Player player, LivingTool tool) {
        // 5% chance to decrease fullness on use
        if (random.nextInt(100) < 5) {
            int currentFullness = tool.getData().getFullness();
            if (currentFullness > 0) {
                tool.getData().addFullness(-1);
            }

            // If hungry (<= 3), complain
            if (tool.getData().getFullness() <= 3) {
                // 20% chance to complain if hungry
                if (random.nextInt(100) < 20) {
                    Material food = getPreferredFoodType(tool.getItem().getType());
                    String foodName = getFoodDescription(food);
                    String toolName = tool.getItem().hasItemMeta() && tool.getItem().getItemMeta().hasDisplayName()
                            ? tool.getItem().getItemMeta().getDisplayName() : "tu herramienta";
                    String[] hungerLines = {
                        "Tengo hambre de " + foodName + "... aliméntame...",
                        "Me debilito... necesito " + foodName + "...",
                        "No puedo rendir bien sin comer... dame " + foodName + ".",
                        "¿Cuándo fue la última vez que me alimentaste? Dame " + foodName + ".",
                        "Me siento vacío... " + foodName + ", por favor..."
                    };
                    String line = hungerLines[random.nextInt(hungerLines.length)];
                    player.sendMessage(org.bukkit.ChatColor.RED + toolName + org.bukkit.ChatColor.RESET
                            + ": " + org.bukkit.ChatColor.ITALIC + line);
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_WHINE, 1, 1);
                }
            }
        }
    }

    /**
     * Devuelve el multiplicador de XP por saciedad.
     * Si saciedad > 8: +5% bonus XP. Si saciedad == 0: -10%.
     */
    public static double getSatietyXPMultiplier(LivingTool tool) {
        int fullness = tool.getData().getFullness();
        if (fullness >= 9) return 1.05;  // Bien alimentado: +5%
        if (fullness >= 7) return 1.00;  // Normal
        if (fullness >= 4) return 0.97;  // Ligero hambre: -3%
        if (fullness >= 1) return 0.93;  // Hambre: -7%
        return 0.90;                     // Muerto de hambre: -10%
    }

    private static Material getPreferredFoodType(Material toolType) {
        String name = toolType.name();
        if (name.contains("PICKAXE"))
            return Material.COBBLESTONE;
        if (name.contains("AXE"))
            return Material.OAK_LOG;
        if (name.contains("SHOVEL"))
            return Material.DIRT;
        if (name.contains("SWORD"))
            return Material.ROTTEN_FLESH;
        if (name.contains("HOE"))
            return Material.WHEAT_SEEDS;
        if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS")
                || name.contains("BOOTS"))
            return Material.IRON_INGOT;
        return null;
    }

    private static boolean isFoodMatch(Material itemType, Material preferredType) {
        // Simple matching for now, can be expanded to categories
        if (itemType == preferredType)
            return true;

        // Categories
        if (preferredType == Material.COBBLESTONE) {
            return itemType.name().contains("ORE") || itemType.name().contains("STONE")
                    || itemType.name().contains("COBBLESTONE");
        }
        if (preferredType == Material.OAK_LOG) {
            return itemType.name().contains("LOG") || itemType.name().contains("PLANKS");
        }
        if (preferredType == Material.DIRT) {
            return itemType == Material.DIRT || itemType == Material.SAND || itemType == Material.GRAVEL;
        }
        if (preferredType == Material.ROTTEN_FLESH) {
            return itemType == Material.ROTTEN_FLESH || itemType == Material.BONE || itemType.name().contains("MEAT")
                    || itemType.name().contains("BEEF") || itemType.name().contains("PORK");
        }
        if (preferredType == Material.WHEAT_SEEDS) {
            return itemType.name().contains("SEEDS") || itemType == Material.WHEAT || itemType == Material.CARROT
                    || itemType == Material.POTATO;
        }
        if (preferredType == Material.IRON_INGOT) {
            return itemType.name().contains("INGOT") || itemType == Material.LEATHER || itemType == Material.DIAMOND;
        }

        return false;
    }

    private static String getFoodDescription(Material material) {
        if (material == Material.COBBLESTONE)
            return "Piedra o Minerales";
        if (material == Material.OAK_LOG)
            return "Madera";
        if (material == Material.DIRT)
            return "Tierra o Arena";
        if (material == Material.ROTTEN_FLESH)
            return "Carne o Huesos";
        if (material == Material.WHEAT_SEEDS)
            return "Semillas o Cultivos";
        if (material == Material.IRON_INGOT)
            return "Lingotes o Minerales";
        return "Algo desconocido";
    }
}
