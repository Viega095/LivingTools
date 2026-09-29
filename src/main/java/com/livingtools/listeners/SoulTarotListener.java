package com.livingtools.listeners;

import com.livingtools.data.LivingTool;
import com.livingtools.gui.SoulTarotGUI;
import com.livingtools.manager.SoulTarotManager;
import com.livingtools.manager.SoulTarotManager.TarotCard;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SoulTarotListener — Maneja los clicks del GUI de Tarot y la aplicación en combate de las cartas del destino.
 */
public class SoulTarotListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(SoulTarotGUI.TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getRawSlot() == 13) { // Mazo de Tarot
            if (SoulTarotManager.canDrawDaily(player.getUniqueId())) {
                TarotCard card = SoulTarotManager.drawRandomCard();
                SoulTarotManager.applyCard(player, card);
                player.closeInventory();
            } else {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (!LivingTool.isLivingTool(held)) {
                    player.sendMessage(ChatColor.RED + "Para pagar la tirada de tarot debes sostener una Herramienta Viviente con al menos 500 XP.");
                    return;
                }
                LivingTool tool = new LivingTool(held);
                boolean success = SoulTarotManager.drawWithToolXP(player, tool, 500);
                if (success) {
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        // Jugador atacante
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            TarotCard card = SoulTarotManager.getActiveCard(attacker.getUniqueId());

            if (card == TarotCard.THE_TOWER) {
                event.setDamage(event.getDamage() * 1.6);
                attacker.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 10);
            } else if (card == TarotCard.THE_SUN) {
                event.setDamage(event.getDamage() * 1.25);
                event.getEntity().setFireTicks(100);
                attacker.getWorld().spawnParticle(Particle.FLAME, event.getEntity().getLocation().add(0, 1, 0), 8);
            }
        }

        // Jugador defensor
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            TarotCard card = SoulTarotManager.getActiveCard(victim.getUniqueId());

            if (card == TarotCard.THE_TOWER) {
                event.setDamage(event.getDamage() * 1.2);
            } else if (card == TarotCard.THE_JUSTICE && event.getDamager() instanceof LivingEntity) {
                LivingEntity damager = (LivingEntity) event.getDamager();
                double reflected = event.getDamage() * 0.25;
                damager.damage(reflected, victim);
                victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.5f);
                victim.getWorld().spawnParticle(Particle.CRIT_MAGIC, damager.getLocation().add(0, 1, 0), 10);
            } else if (card == TarotCard.THE_MOON) {
                if (Math.random() < 0.30) {
                    event.setCancelled(true);
                    victim.sendMessage(ChatColor.AQUA + "✨ ¡Evasión Lunar! Has esquivado el ataque.");
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.8f);
                    victim.getWorld().spawnParticle(Particle.PORTAL, victim.getLocation().add(0, 1, 0), 20);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnvironmentalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        TarotCard card = SoulTarotManager.getActiveCard(player.getUniqueId());
        if (card == TarotCard.THE_SUN) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE
                    || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                    || cause == EntityDamageEvent.DamageCause.LAVA
                    || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                event.setCancelled(true);
                player.setFireTicks(0);
            }
        }
    }
}
