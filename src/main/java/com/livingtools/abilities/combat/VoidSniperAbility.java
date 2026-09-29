package com.livingtools.abilities.combat;

import com.livingtools.abilities.Ability;
import com.livingtools.data.LivingTool;
import com.livingtools.visuals.DamageIndicatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * VoidSniperAbility — Disparos a distancia que infligen daño de penetración, ceguera y marchitamiento.
 */
public class VoidSniperAbility extends Ability {

    public VoidSniperAbility() {
        super("void_sniper", "Francotirador del Vacío", "Los disparos a más de 20 bloques aplican Ceguera y Marchitamiento.", 40);
    }

    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, LivingTool tool) {
        if (!(event.getDamager() instanceof Projectile)) return;
        Projectile projectile = (Projectile) event.getDamager();
        if (!(projectile.getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player shooter = (Player) projectile.getShooter();
        LivingEntity target = (LivingEntity) event.getEntity();

        double distance = shooter.getLocation().distance(target.getLocation());
        if (distance < 20.0) return;

        // Efectos del Vacío
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
        event.setDamage(event.getDamage() + 6.0); // Daño penetrante extra

        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_HURT, 0.8f, 1.6f);

        DamageIndicatorManager.spawnIndicator(target.getLocation().add(0, 1.2, 0), ChatColor.DARK_PURPLE + "🌑 IMPACTO DEL VACÍO");
    }

    @Override
    public boolean isCompatible(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }
}
