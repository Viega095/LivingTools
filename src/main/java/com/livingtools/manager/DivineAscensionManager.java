package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.data.LivingTool;
import com.livingtools.data.ToolData;
import com.livingtools.visuals.ParticleOptimizer;
import com.livingtools.visuals.SoundHarmonicsEngine;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * DivineAscensionManager — Motor de Ascensión Divina, Sendas Celestiales y Habilidades Trascendentales.
 */
public class DivineAscensionManager {

    private static NamespacedKey KEY_ASCENSION_PATH;
    private static BukkitTask auraTask = null;

    public static NamespacedKey getKeyAscensionPath() {
        if (KEY_ASCENSION_PATH == null) {
            KEY_ASCENSION_PATH = new NamespacedKey(LivingToolsPlugin.getInstance(), "lt_divine_ascension");
        }
        return KEY_ASCENSION_PATH;
    }

    public enum AscensionPath {
        CELESTIAL_SOVEREIGN("Soberano Celestial", ChatColor.GOLD + "👑 Soberano Celestial",
                "Consagrado a la Luz Suprema. Tus ataques curan a aliados cercanos (+20% del daño) e invoca un Pilar de Luz Solar.",
                Material.NETHER_STAR, Particle.TOTEM, Particle.END_ROD, Color.fromRGB(255, 220, 100)),

        ABYSSAL_LORD("Señor del Abismo", ChatColor.DARK_PURPLE + "🌑 Señor del Abismo",
                "Consagrado al Vacío Infinito. Absorbe daño para generar un Escudo de Almas y crea Agujeros Negros de Gravedad.",
                Material.WITHER_SKELETON_SKULL, Particle.PORTAL, Particle.SPELL_WITCH, Color.fromRGB(90, 0, 180)),

        PRIMORDIAL_TITAN("Titán Primordial", ChatColor.RED + "🌋 Titán Primordial",
                "Consagrado a la Furia de la Tierra. Inmunidad total al retroceso, daño en cono frontal y Martillazos Sísmicos.",
                Material.MAGMA_BLOCK, Particle.LAVA, Particle.FLAME, Color.fromRGB(255, 80, 0));

        private final String id;
        private final String displayName;
        private final String description;
        private final Material icon;
        private final Particle particle1;
        private final Particle particle2;
        private final Color auraColor;

        AscensionPath(String id, String displayName, String description, Material icon,
                      Particle particle1, Particle particle2, Color auraColor) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.particle1 = particle1;
            this.particle2 = particle2;
            this.auraColor = auraColor;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public Particle getParticle1() { return particle1; }
        public Particle getParticle2() { return particle2; }
        public Color getAuraColor() { return auraColor; }
    }

    public static void startAuraLoop() {
        if (auraTask != null) auraTask.cancel();

        auraTask = new BukkitRunnable() {
            float angle = 0f;

            @Override
            public void run() {
                angle = (angle + 0.15f) % ((float) (Math.PI * 2));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack held = player.getInventory().getItemInMainHand();
                    if (!LivingTool.isLivingTool(held)) continue;

                    LivingTool tool = new LivingTool(held);
                    AscensionPath path = getAscensionPath(tool);
                    if (path == null) continue;

                    // Renderizar corona celestial orbital sobre la cabeza del jugador
                    Location head = player.getEyeLocation().add(0, 0.4, 0);
                    World world = head.getWorld();
                    if (world == null) continue;

                    double r = 0.55;
                    double x1 = head.getX() + Math.cos(angle) * r;
                    double z1 = head.getZ() + Math.sin(angle) * r;
                    double x2 = head.getX() + Math.cos(angle + Math.PI) * r;
                    double z2 = head.getZ() + Math.sin(angle + Math.PI) * r;

                    world.spawnParticle(path.getParticle1(), new Location(world, x1, head.getY(), z1), 1, 0, 0, 0, 0);
                    world.spawnParticle(path.getParticle2(), new Location(world, x2, head.getY(), z2), 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 20L, 2L);
    }

    public static boolean canAscend(LivingTool tool) {
        ToolData data = tool.getData();
        return (data.getLevel() >= 100 || data.getPrestige() >= 2);
    }

    public static boolean ascendTool(Player player, LivingTool tool, AscensionPath path) {
        if (!canAscend(tool)) return false;

        tool.getItem().getItemMeta();
        org.bukkit.inventory.meta.ItemMeta meta = tool.getItem().getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getKeyAscensionPath(), PersistentDataType.STRING, path.name());
            tool.getItem().setItemMeta(meta);
        }
        tool.updateLore();

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            ParticleOptimizer.spawnHelix(loc.add(0, 0.5, 0), 2.0, 4.5, path.getParticle1(), null);
            SoundHarmonicsEngine.playDivineChime(loc);
            SoundHarmonicsEngine.playVictoryFanfare(player);
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "✦✦✦ " + ChatColor.WHITE + player.getName() + ChatColor.GOLD
                + " ha consagrado su herramienta viviente a la " + path.getDisplayName() + ChatColor.GOLD + "! ✦✦✦");
        Bukkit.broadcastMessage(ChatColor.GRAY + "  \"" + path.getDescription() + "\"");
        Bukkit.broadcastMessage("");

        return true;
    }

    public static AscensionPath getAscensionPath(LivingTool tool) {
        if (!tool.getItem().hasItemMeta()) return null;
        String raw = tool.getItem().getItemMeta().getPersistentDataContainer()
                .get(getKeyAscensionPath(), PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return AscensionPath.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Activa la habilidad divina trascendental al presionar Shift + Q (Drop).
     */
    public static void triggerDivineAbility(Player player, LivingTool tool) {
        AscensionPath path = getAscensionPath(tool);
        if (path == null) return;

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        switch (path) {
            case CELESTIAL_SOVEREIGN:
                // Pilar solar sanador
                ParticleOptimizer.spawnHelix(loc, 3.0, 6.0, Particle.TOTEM, null);
                world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.2f);
                for (Player ally : world.getPlayers()) {
                    if (ally.getLocation().distance(loc) <= 15.0) {
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 2));
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 1));
                        ally.sendMessage(ChatColor.GOLD + "✨ ¡Bendición del Soberano Celestial recibida!");
                    }
                }
                break;

            case ABYSSAL_LORD:
                // Agujero negro del vacío
                ParticleOptimizer.spawnCircle(loc, 5.0, Particle.PORTAL, 30, null);
                world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 0.6f);
                for (org.bukkit.entity.Entity e : world.getNearbyEntities(loc, 10, 5, 10)) {
                    if (e instanceof LivingEntity && !e.equals(player) && !(e instanceof org.bukkit.entity.ArmorStand)) {
                        Vector pull = loc.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.8);
                        e.setVelocity(pull);
                        ((LivingEntity) e).damage(14.0, player);
                    }
                }
                player.sendMessage(ChatColor.DARK_PURPLE + "🌑 ¡Agujero Negro del Abismo activado!");
                break;

            case PRIMORDIAL_TITAN:
                // Martillazo sísmico frontal
                ParticleOptimizer.spawnCircle(loc, 4.5, Particle.LAVA, 25, null);
                world.playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
                for (org.bukkit.entity.Entity e : world.getNearbyEntities(loc, 8, 4, 8)) {
                    if (e instanceof LivingEntity && !e.equals(player) && !(e instanceof org.bukkit.entity.ArmorStand)) {
                        e.setVelocity(new Vector(0, 0.9, 0));
                        ((LivingEntity) e).damage(18.0, player);
                    }
                }
                player.sendMessage(ChatColor.RED + "🌋 ¡Sismo Primordial desatado!");
                break;
        }
    }
}
