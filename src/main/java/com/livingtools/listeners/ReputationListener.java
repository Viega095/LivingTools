package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.inventory.ItemStack;

public class ReputationListener implements Listener {

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) return;
        Player player = (Player) event.getBreeder();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;
        LivingTool tool = new LivingTool(item);
        tool.getData().setPeacefulActions(tool.getData().getPeacefulActions() + 1);
        checkReputation(player, tool);
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player)) return;
        Player player = (Player) event.getOwner();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!LivingTool.isLivingTool(item)) return;
        LivingTool tool = new LivingTool(item);
        tool.getData().setPeacefulActions(tool.getData().getPeacefulActions() + 1);
        checkReputation(player, tool);
    }

    /**
     * Evalúa y asigna títulos de reputación según los stats actuales de la herramienta.
     * Se puede llamar desde ExperienceListener tras matar mobs/jugadores.
     */
    public static void checkReputation(Player player, LivingTool tool) {
        ToolData data = tool.getData();
        int kills     = data.getMobKills();
        int pvp       = data.getPlayerKills();
        int peaceful  = data.getPeacefulActions();
        int prestige  = data.getPrestige();
        String current = data.getTitle();
        String newTitle = evaluateTitle(kills, pvp, peaceful, prestige);

        if (newTitle.isEmpty() || newTitle.equals(current)) return;

        data.setTitle(newTitle);
        tool.updateLore();

        // Announcement
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "⚔ ¡Tu herramienta ha ganado un nuevo título!");
        player.sendMessage(ChatColor.YELLOW + "  " + ChatColor.BOLD + newTitle);
        player.sendMessage("");

        // Visual effects
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);

        // Title on screen
        player.sendTitle(
                ChatColor.GOLD + "" + ChatColor.BOLD + "¡Nuevo Título!",
                ChatColor.YELLOW + newTitle,
                10, 80, 20);
    }

    // -----------------------------------------------------------------------
    // Title evaluation ladder — higher tiers override lower
    // -----------------------------------------------------------------------

    private static String evaluateTitle(int mobKills, int pvpKills, int peaceful, int prestige) {
        // Prestige-based (highest priority)
        if (prestige >= 3) return "The Undying";
        if (prestige >= 2) return "The Reborn";
        if (prestige >= 1) return "The Legend";

        // PvP-based
        if (pvpKills >= 50) return "The Warlord";
        if (pvpKills >= 20) return "The Warrior";
        if (pvpKills >= 10) return "The Duelist";
        if (pvpKills >= 5)  return "The Butcher";

        // Combat (mob kills) based
        if (mobKills >= 500) return "The Annihilator";
        if (mobKills >= 200) return "The Destroyer";
        if (mobKills >= 100) return "The Hunter";
        if (mobKills >= 50)  return "The Slayer";
        if (mobKills >= 10)  return "The Fighter";

        // Peaceful actions
        if (peaceful >= 50) return "The Guardian";
        if (peaceful >= 20) return "The Shepherd";
        if (peaceful >= 10) return "The Caretaker";

        return ""; // No title yet
    }
}
