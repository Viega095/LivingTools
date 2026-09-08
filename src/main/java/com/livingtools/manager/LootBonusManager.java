package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * LootBonusManager — al matar un mob con una living tool, hay chance de drops
 * especiales basados en la PERSONALIDAD y NIVEL de la herramienta.
 *
 * AGGRESSIVE → Exp bottles extra, flechas, huesos
 * WISE       → libros encantados, pergaminos, papel
 * LAZY       → comida (pan, carne), monedas (gold nuggets)
 * CHEERFUL   → flores, semillas, items decorativos
 *
 * El nivel escala la probabilidad base:
 *   Nivel 1-49:  2% base
 *   Nivel 50-99: 5% base
 *   Nivel 100+:  8% base
 *   Prestige I+: +3% adicional por prestige (máx +9%)
 */
public class LootBonusManager implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        ItemStack item = killer.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;

        LivingTool tool = new LivingTool(item);
        ToolData data = tool.getData();

        // Calcular probabilidad base
        int level = data.getLevel();
        int prestige = data.getPrestige();
        double chance = 0.02; // 2% base
        if (level >= 100) chance = 0.08;
        else if (level >= 50) chance = 0.05;
        chance += Math.min(prestige, 3) * 0.03; // +3% por prestige, máx +9%

        if (random.nextDouble() > chance) return;

        String personality = data.getPersonality() != null ? data.getPersonality() : "WISE";
        Location loc = event.getEntity().getLocation();

        // Seleccionar drops según personalidad
        ItemStack drop = selectDrop(personality, level, event.getEntityType());
        if (drop == null) return;

        loc.getWorld().dropItemNaturally(loc, drop);

        // Notificación discreta en action bar
        String personalityEmoji = personality.equals("AGGRESSIVE") ? "⚔" :
                personality.equals("WISE") ? "📖" :
                personality.equals("LAZY") ? "💰" : "✿";
        com.livingtools.utils.MessageUtils.sendActionBar(killer,
                ChatColor.GOLD + personalityEmoji + " ¡Loot especial!");
    }

    private static ItemStack selectDrop(String personality, int level, EntityType entityType) {
        switch (personality) {
            case "AGGRESSIVE":
                return selectAggressiveDrop(level, entityType);
            case "WISE":
                return selectWiseDrop(level);
            case "LAZY":
                return selectLazyDrop(level);
            default: // CHEERFUL
                return selectCheerfulDrop(level);
        }
    }

    private static ItemStack selectAggressiveDrop(int level, EntityType entityType) {
        int roll = random.nextInt(4);
        switch (roll) {
            case 0:
                // Botella de experiencia — más exp si nivel alto
                int expAmount = level >= 100 ? 7 : level >= 50 ? 4 : 1;
                return new ItemStack(Material.EXPERIENCE_BOTTLE, expAmount);
            case 1:
                return new ItemStack(Material.ARROW, 4 + random.nextInt(8));
            case 2:
                return new ItemStack(Material.BONE, 2 + random.nextInt(4));
            case 3:
                // Tropheo de combate — cabeza del mob si posible
                if (entityType == EntityType.SKELETON) return new ItemStack(Material.SKELETON_SKULL);
                if (entityType == EntityType.ZOMBIE)   return new ItemStack(Material.ZOMBIE_HEAD);
                if (entityType == EntityType.WITHER_SKELETON) return new ItemStack(Material.WITHER_SKELETON_SKULL);
                return new ItemStack(Material.GUNPOWDER, 2 + random.nextInt(4));
        }
        return null;
    }

    private static ItemStack selectWiseDrop(int level) {
        int roll = random.nextInt(3);
        switch (roll) {
            case 0:
                // Libro encantado aleatorio (simple — solo UNBREAKING para no complicar)
                return createNamedItem(Material.BOOK, ChatColor.AQUA + "Conocimiento Arcano",
                        ChatColor.GRAY + "La sabiduría de la herramienta impregna este tomo.",
                        ChatColor.YELLOW + "+50 XP al leerlo");
            case 1:
                return new ItemStack(Material.PAPER, 3 + random.nextInt(5));
            case 2:
                if (level >= 100) {
                    return createNamedItem(Material.ENCHANTED_BOOK, ChatColor.LIGHT_PURPLE + "Fragmento de Saber",
                            ChatColor.GRAY + "Contiene sabiduría antigua.",
                            ChatColor.YELLOW + "Lleva al RuneForge para investigar");
                }
                return new ItemStack(Material.INK_SAC, 2 + random.nextInt(4));
        }
        return null;
    }

    private static ItemStack selectLazyDrop(int level) {
        int roll = random.nextInt(3);
        switch (roll) {
            case 0:
                return new ItemStack(Material.BREAD, 2 + random.nextInt(3));
            case 1:
                return new ItemStack(Material.GOLD_NUGGET, 3 + random.nextInt(6));
            case 2:
                if (level >= 50) return new ItemStack(Material.COOKED_BEEF, 1 + random.nextInt(3));
                return new ItemStack(Material.COOKED_CHICKEN, 1 + random.nextInt(3));
        }
        return null;
    }

    private static ItemStack selectCheerfulDrop(int level) {
        int roll = random.nextInt(4);
        switch (roll) {
            case 0:
                Material[] flowers = {Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
                        Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
                        Material.OXEYE_DAISY, Material.CORNFLOWER};
                return new ItemStack(flowers[random.nextInt(flowers.length)], 1 + random.nextInt(3));
            case 1:
                return new ItemStack(Material.WHEAT_SEEDS, 3 + random.nextInt(6));
            case 2:
                if (level >= 50) {
                    return createNamedItem(Material.FEATHER, ChatColor.WHITE + "Pluma Bendita",
                            ChatColor.GRAY + "Trae suerte y alegría.",
                            ChatColor.GREEN + "+25 XP al recogerla");
                }
                return new ItemStack(Material.FEATHER, 2 + random.nextInt(4));
            case 3:
                return new ItemStack(Material.SUGAR, 2 + random.nextInt(4));
        }
        return null;
    }

    private static ItemStack createNamedItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>(Arrays.asList(loreLines));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
