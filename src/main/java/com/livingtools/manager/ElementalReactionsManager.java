package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import com.livingtools.visuals.ParticleOptimizer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ElementalReactionsManager — Sistema de Combate con Reacciones Elementales.
 *
 * Elementos: FUEGO, HIELO, TRUENO, SOMBRA, LUZ.
 * Reacciones:
 *  - FUEGO + HIELO   → Evaporación Explosiva (+200% daño, vapor abrasador).
 *  - TRUENO + HIELO → Superconductividad (Congela al enemigo, +30% daño físico).
 *  - FUEGO + TRUENO → Sobrecarga Rúnica (Explosión ascendente de chispas).
 *  - SOMBRA + LUZ   → Vórtice del Vacío (Succión en área + Robo de Vida).
 */
public class ElementalReactionsManager implements Listener {

    public enum Element {
        FUEGO   (ChatColor.RED + "🔥 Fuego",       Color.fromRGB(255, 60, 0),   Particle.FLAME),
        HIELO   (ChatColor.AQUA + "❄ Hielo",      Color.fromRGB(100, 200, 255),Particle.SNOWFLAKE),
        TRUENO  (ChatColor.YELLOW + "⚡ Trueno",   Color.fromRGB(255, 230, 0),  Particle.ELECTRIC_SPARK),
        SOMBRA  (ChatColor.DARK_PURPLE + "🌑 Sombra", Color.fromRGB(80, 0, 150),  Particle.PORTAL),
        LUZ     (ChatColor.GOLD + "✨ Luz",        Color.fromRGB(255, 255, 200),Particle.TOTEM);

        private final String displayName;
        private final Color color;
        private final Particle particle;

        Element(String displayName, Color color, Particle particle) {
            this.displayName = displayName;
            this.color = color;
            this.particle = particle;
        }

        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
        public Particle getParticle() { return particle; }
    }

    private static final Map<UUID, Element> activeElements = new HashMap<>();
    private static final Map<UUID, Long> elementExpiry = new HashMap<>();
    private static final long ELEMENT_DURATION_MS = 6000L; // 6 segundos de aura

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) return;

        Player attacker = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        ItemStack held = attacker.getInventory().getItemInMainHand();

        if (!LivingTool.isLivingTool(held)) return;
        LivingTool tool = new LivingTool(held);

        // Determinar elemento del arma basado en personalidad o encantamientos
        Element weaponElement = getToolElement(tool);
        if (weaponElement == null) return;

        applyElementAndTriggerReactions(target, weaponElement, attacker, event);
    }

    public static Element getToolElement(LivingTool tool) {
        String personality = tool.getData().getPersonality();
        if (personality == null) personality = "WISE";

        switch (personality.toUpperCase()) {
            case "AGGRESSIVE": return Element.FUEGO;
            case "WISE":       return Element.LUZ;
            case "LAZY":       return Element.HIELO;
            case "CHEERFUL":   return Element.TRUENO;
            default:           return Element.SOMBRA;
        }
    }

    public static void applyElementAndTriggerReactions(LivingEntity target, Element newElement, Player attacker, EntityDamageByEntityEvent event) {
        UUID targetId = target.getUniqueId();
        long now = System.currentTimeMillis();

        Element existingElement = activeElements.get(targetId);
        Long expiry = elementExpiry.get(targetId);

        // Si ya expiró el elemento anterior
        if (expiry == null || now > expiry) {
            existingElement = null;
        }

        Location loc = target.getLocation().add(0, 1, 0);
        World world = target.getWorld();

        if (existingElement == null || existingElement == newElement) {
            // Aplicar nuevo elemento
            activeElements.put(targetId, newElement);
            elementExpiry.put(targetId, now + ELEMENT_DURATION_MS);
            world.spawnParticle(newElement.getParticle(), loc, 10, 0.4, 0.5, 0.4, 0.05);
            return;
        }

        // --- REACCIONES ELEMENTALES ---
        activeElements.remove(targetId);
        elementExpiry.remove(targetId);

        // 1. FUEGO + HIELO → Evaporación Explosiva
        if ((existingElement == Element.FUEGO && newElement == Element.HIELO)
                || (existingElement == Element.HIELO && newElement == Element.FUEGO)) {
            event.setDamage(event.getDamage() * 2.0); // +100% de daño
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 25, 0.5, 0.5, 0.5, 0.05);
            world.spawnParticle(Particle.LAVA, loc, 15, 0.5, 0.5, 0.5, 0.1);
            world.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 1.2f, 1.2f);
            attacker.sendMessage(ChatColor.GOLD + "💥 ¡REACCIÓN: EVAPORACIÓN EXPLOSIVA! (+100% Daño)");
            try {
                com.livingtools.visuals.DamageIndicatorManager.spawnIndicator(loc.add(0, 0.5, 0), ChatColor.GOLD + "💥 EVAPORACIÓN (+100%)");
            } catch (Throwable ignored) {}
            return;
        }

        // 2. TRUENO + HIELO → Superconductividad
        if ((existingElement == Element.TRUENO && newElement == Element.HIELO)
                || (existingElement == Element.HIELO && newElement == Element.TRUENO)) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            world.spawnParticle(Particle.CRIT_MAGIC, loc, 30, 0.6, 0.6, 0.6, 0.1);
            world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.2f, 1.5f);
            attacker.sendMessage(ChatColor.AQUA + "❄⚡ ¡REACCIÓN: SUPERCONDUCTIVIDAD! (Enemigo congelado y debilitado)");
            try {
                com.livingtools.visuals.DamageIndicatorManager.spawnIndicator(loc.add(0, 0.5, 0), ChatColor.AQUA + "❄⚡ SUPERCONDUCTIVIDAD");
            } catch (Throwable ignored) {}
            return;
        }

        // 3. FUEGO + TRUENO → Sobrecarga Rúnica
        if ((existingElement == Element.FUEGO && newElement == Element.TRUENO)
                || (existingElement == Element.TRUENO && newElement == Element.FUEGO)) {
            target.setVelocity(new Vector(0, 0.8, 0));
            world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 2, 0, 0, 0, 0);
            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.4f);
            attacker.sendMessage(ChatColor.RED + "⚡🔥 ¡REACCIÓN: SOBRECARGA RÚNICA! (Enemigo catapultado)");
            try {
                com.livingtools.visuals.DamageIndicatorManager.spawnIndicator(loc.add(0, 0.5, 0), ChatColor.RED + "⚡🔥 SOBRECARGA RÚNICA");
            } catch (Throwable ignored) {}
            return;
        }

        // 4. SOMBRA + LUZ → Vórtice del Vacío (Succión + Robo de Vida)
        if ((existingElement == Element.SOMBRA && newElement == Element.LUZ)
                || (existingElement == Element.LUZ && newElement == Element.SOMBRA)) {
            double maxHealth = 20.0;
            if (attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            }
            double heal = Math.min(maxHealth - attacker.getHealth(), 4.0);
            if (heal > 0) attacker.setHealth(attacker.getHealth() + heal);

            world.spawnParticle(Particle.PORTAL, loc, 40, 0.8, 0.8, 0.8, 0.2);
            world.spawnParticle(Particle.HEART, attacker.getLocation().add(0, 1.5, 0), 4, 0.3, 0.3, 0.3, 0.1);
            world.playSound(loc, Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.2f);
            attacker.sendMessage(ChatColor.DARK_PURPLE + "🌑✨ ¡REACCIÓN: VÓRTICE DEL VACÍO! (Drenaje de vida vital)");
            try {
                com.livingtools.visuals.DamageIndicatorManager.spawnIndicator(loc.add(0, 0.5, 0), ChatColor.DARK_PURPLE + "🌑✨ VÓRTICE DEL VACÍO (+4❤)");
            } catch (Throwable ignored) {}
        }
    }

    public static void cleanup(UUID uuid) {
        activeElements.remove(uuid);
        elementExpiry.remove(uuid);
    }
}
