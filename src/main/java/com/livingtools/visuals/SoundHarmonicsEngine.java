package com.livingtools.visuals;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * SoundHarmonicsEngine — Motor de Audio Musical Pentatónico y Armonías 3D Inmersivas.
 */
public class SoundHarmonicsEngine {

    // Frecuencias pentatónicas de pitch en Minecraft (C=0.707, D=0.794, E=0.891, G=1.059, A=1.189, C5=1.414)
    public static final float PITCH_C = 0.707f;
    public static final float PITCH_D = 0.794f;
    public static final float PITCH_E = 0.891f;
    public static final float PITCH_G = 1.059f;
    public static final float PITCH_A = 1.189f;
    public static final float PITCH_HIGH_C = 1.414f;

    /**
     * Arpegio Mayor Ascendente (Brillante, Alegre, Victoria).
     */
    public static void playAscendingMajorArpeggio(Location loc) {
        if (loc.getWorld() == null) return;

        float[] chord = {PITCH_C, PITCH_E, PITCH_G, PITCH_HIGH_C};

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step >= chord.length) {
                    cancel();
                    return;
                }

                loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.2f, chord[step]);
                loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, chord[step] * 1.2f);
                step++;
            }
        }.runTaskTimer(LivingToolsPlugin.getInstance(), 0L, 2L);
    }

    /**
     * Armonía Celestial Divina (Ascensión, Milagro, Luz).
     */
    public static void playDivineChime(Location loc) {
        if (loc.getWorld() == null) return;

        loc.getWorld().playSound(loc, Sound.BLOCK_BELL_USE, 1.2f, 1.4f);
        loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.5f, 1.0f);
        loc.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.8f);
    }

    /**
     * Resonancia Abisal Siniestra (Vacío, Grietas, Sombra).
     */
    public static void playAbyssalDrone(Location loc) {
        if (loc.getWorld() == null) return;

        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.2f, 0.6f);
        loc.getWorld().playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.6f, 0.5f);
    }

    /**
     * Fanfarria Triunfal para el Jugador.
     */
    public static void playVictoryFanfare(Player player) {
        if (player == null || !player.isOnline()) return;

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.4f, 1.0f);
        playAscendingMajorArpeggio(player.getLocation());
    }

    /**
     * Pulso de Advertencia de Peligro Telegrafiado.
     */
    public static void playDangerWarning(Location loc) {
        if (loc.getWorld() == null) return;

        loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1.2f, 0.5f);
        loc.getWorld().playSound(loc, Sound.BLOCK_IRON_DOOR_CLOSE, 0.6f, 0.5f);
    }
}
