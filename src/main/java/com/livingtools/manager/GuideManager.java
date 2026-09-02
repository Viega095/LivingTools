package com.livingtools.manager;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GuideManager {

    public enum TutorialStep {
        NONE(0),
        INTRO(1),
        FIRST_XP(2),
        LEVEL_5(3),
        GUI_OPENED(4),
        LEVEL_25(5),
        LEVEL_50(6),
        CORRUPTION_START(7),
        FULL_SET(8),
        SLEEPING_FOUND(9),
        COMPLETED(99);

        private final int id;

        TutorialStep(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static TutorialStep fromId(int id) {
            for (TutorialStep step : values()) {
                if (step.id == id) {
                    return step;
                }
            }
            return NONE;
        }
    }

    public static void triggerStep(Player player, LivingTool tool, TutorialStep step) {
        TutorialStep current = tool.getData().getTutorialStep();

        // Only proceed if we are at the correct stage (sequential) or if it's a
        // specific trigger that hasn't been seen
        // For simplicity, we'll enforce sequential for now, but allow skipping if logic
        // demands
        if (current.getId() >= step.getId()) {
            return; // Already done
        }

        // Update step
        tool.getData().setTutorialStep(step);

        // Send Message
        sendWhisper(player, tool, step);
    }

    private static void sendWhisper(Player player, LivingTool tool, TutorialStep step) {
        String personalityName = tool.getData().getPersonality();
        com.livingtools.mechanics.Personality personality = com.livingtools.mechanics.Personality.NORMAL;

        if (personalityName != null) {
            try {
                personality = com.livingtools.mechanics.Personality.valueOf(personalityName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        String message = getMessageForPersonality(personality, step);
        if (message == null || message.isEmpty())
            return;

        // Delay slightly for effect
        final String finalMessage = message;
        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.5f);
                player.sendMessage(
                        ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "[Susurro] " + ChatColor.GRAY + finalMessage);
            }
        }.runTaskLater(com.livingtools.LivingToolsPlugin.getInstance(), 20L);
    }

    private static String getMessageForPersonality(com.livingtools.mechanics.Personality personality,
            TutorialStep step) {
        switch (personality) {
            case AGGRESSIVE:
                switch (step) {
                    case INTRO:
                        return "¡SANGRE! ¡Despiértame con sangre y XP!";
                    case FIRST_XP:
                        return "¡SÍ! ¡MÁS PODER! ¡MÁS DESTRUCCIÓN!";
                    case LEVEL_5:
                        return "Me fortalezco... Usa /livingtool para ver cómo acabar con ellos.";
                    case GUI_OPENED:
                        return "Mira mis estadísticas. Mira cuán letales somos.";
                    case LEVEL_25:
                        return "¡Evolución! Busca Geodas Rúnicas. ¡Rómpelas!";
                    case LEVEL_50:
                        return "¡Santuarios! ¡Desafíalos! ¡Demostremos nuestra furia!";
                    case CORRUPTION_START:
                        return "La oscuridad... me gusta. ¡Mátalos a todos!";
                    case FULL_SET:
                        return "¡Armadura completa! ¡Somos invencibles! ¡A LA GUERRA!";
                    case SLEEPING_FOUND:
                        return "¡Despierta esa cosa! ¡Necesitamos más armas para la matanza!";
                    default:
                        return "";
                }
            case LAZY:
                switch (step) {
                    case INTRO:
                        return "*Bostezo*... ¿Ya es hora? 5 minutos más...";
                    case FIRST_XP:
                        return "Ugh, trabajo... supongo que la XP está bien.";
                    case LEVEL_5:
                        return "Nivel 5... ¿podemos descansar ya? Mira /livingtool si quieres.";
                    case GUI_OPENED:
                        return "Demasiados números... despiértame cuando terminemos.";
                    case LEVEL_25:
                        return "Geodas... suenan pesadas. Pero si insistes...";
                    case LEVEL_50:
                        return "Santuarios... mucho caminar. ¿No podemos quedarnos aquí?";
                    case CORRUPTION_START:
                        return "Esta oscuridad pesa... me da sueño.";
                    case FULL_SET:
                        return "Armadura cómoda... buena para una siesta.";
                    case SLEEPING_FOUND:
                        return "Oh no, otra herramienta... más trabajo. Despiértala si quieres.";
                    default:
                        return "";
                }
            case SHY:
                switch (step) {
                    case INTRO:
                        return "H-hola... ¿eres mi dueño? Prometo esforzarme.";
                    case FIRST_XP:
                        return "¡Oh! ¿Eso fue XP? Gracias...";
                    case LEVEL_5:
                        return "Creo que mejoré un poco... revisa /livingtool, por favor.";
                    case GUI_OPENED:
                        return "No mires mucho mis stats... me da vergüenza.";
                    case LEVEL_25:
                        return "Um... he oído de Geodas Rúnicas... ¿podríamos buscar una?";
                    case LEVEL_50:
                        return "Santuarios Ancestrales... dan miedo, pero iré contigo.";
                    case CORRUPTION_START:
                        return "Tengo miedo... algo oscuro está entrando en mí...";
                    case FULL_SET:
                        return "Me siento segura con esta armadura... gracias.";
                    case SLEEPING_FOUND:
                        return "Hay alguien dormido ahí... ¿podemos ayudarle en el Altar?";
                    default:
                        return "";
                }
            case SARCASTIC:
                switch (step) {
                    case INTRO:
                        return "Genial, otro dueño. Trata de no romperme esta vez.";
                    case FIRST_XP:
                        return "Wow, XP. Qué logro impresionante. Aplausos.";
                    case LEVEL_5:
                        return "Nivel 5. No te emociones, aún somos débiles. Usa /livingtool.";
                    case GUI_OPENED:
                        return "¿Admirando mi grandeza? Tómate tu tiempo.";
                    case LEVEL_25:
                        return "Geodas Rúnicas. Brillantes. Útiles. Búscalas.";
                    case LEVEL_50:
                        return "Santuarios. Supongo que crees que estás listo. Qué tierno.";
                    case CORRUPTION_START:
                        return "Oh, matando inocentes. Qué original. Qué 'edgy'.";
                    case FULL_SET:
                        return "Conjunto completo. Al menos tienes sentido de la moda.";
                    case SLEEPING_FOUND:
                        return "Mira, otro trasto inútil. Llévalo al Altar a ver si sirve de algo.";
                    default:
                        return "";
                }
            case GLUTTONOUS:
                switch (step) {
                    case INTRO:
                        return "¡Tengo HAMBRE! ¡Dame XP! ¡Dame almas!";
                    case FIRST_XP:
                        return "¡Delicioso! ¡Más! ¡Quiero más!";
                    case LEVEL_5:
                        return "¡Crezco! ¡Aliméntame más! Revisa el menú /livingtool.";
                    case GUI_OPENED:
                        return "¿Hay comida aquí? No, solo números. Aburrido.";
                    case LEVEL_25:
                        return "¡Geodas! ¡Dicen que saben a caramelo mágico! ¡Busca!";
                    case LEVEL_50:
                        return "Santuarios... ¿tienen banquetes? Vamos a ver.";
                    case CORRUPTION_START:
                        return "Esta energía... sabe picante. Extraño, pero sabroso.";
                    case FULL_SET:
                        return "Me siento lleno... de poder. ¡Pero sigo con hambre!";
                    case SLEEPING_FOUND:
                        return "¿Eso se come? No, está dormido. ¡Despiértalo en el Altar para que cace con nosotros!";
                    default:
                        return "";
                }
            case HEROIC:
                switch (step) {
                    case INTRO:
                        return "¡Saludos, noble aventurero! ¡Juntos venceremos al mal!";
                    case FIRST_XP:
                        return "¡Fortaleza! Cada paso nos acerca a la gloria.";
                    case LEVEL_5:
                        return "¡Nivel 5! Nuestro poder crece. Consulta /livingtool, héroe.";
                    case GUI_OPENED:
                        return "Aquí se registra nuestra leyenda. ¡Adelante!";
                    case LEVEL_25:
                        return "¡Geodas Rúnicas! Herramientas de los antiguos. ¡Debemos hallarlas!";
                    case LEVEL_50:
                        return "¡El Santuario aguarda! ¡Una prueba digna de nosotros!";
                    case CORRUPTION_START:
                        return "¡Cuidado! ¡La oscuridad acecha! No te desvíes del camino.";
                    case FULL_SET:
                        return "¡Armadura radiante! ¡La luz nos protege! ¡A la batalla!";
                    case SLEEPING_FOUND:
                        return "¡Un alma perdida! Debemos llevarla al Altar Sagrado y restaurar su gloria.";
                    default:
                        return "";
                }
            case LUCKY:
                switch (step) {
                    case INTRO:
                        return "¡Hoy es tu día de suerte! ¡Me has encontrado!";
                    case FIRST_XP:
                        return "¡Premio! Un poco de XP para empezar la racha.";
                    case LEVEL_5:
                        return "¡Nivel 5! ¡Qué suerte tenemos! Mira /livingtool.";
                    case GUI_OPENED:
                        return "¿Ves esos números? Apuesto a que subirán rápido.";
                    case LEVEL_25:
                        return "¡Geodas! Siento que encontraremos una pronto. ¡Lo presiento!";
                    case LEVEL_50:
                        return "Santuarios... ¡seguro hay tesoros increíbles allí!";
                    case CORRUPTION_START:
                        return "Uh oh... se me acabó la suerte. Esto se siente mal.";
                    case FULL_SET:
                        return "¡Bingo! ¡Set completo! Nada puede salir mal ahora.";
                    case SLEEPING_FOUND:
                        return "¡Mira qué hallazgo! Seguro que si lo llevas al Altar sale algo bueno.";
                    default:
                        return "";
                }
            case NORMAL:
            default:
                switch (step) {
                    case INTRO:
                        return "Me has despertado... Soy tu herramienta viviente. Aliméntame con XP.";
                    case FIRST_XP:
                        return "¡Eso es! Siento el poder. Sigue usándome.";
                    case LEVEL_5:
                        return "Me fortalezco. Usa /livingtool para ver mis habilidades.";
                    case GUI_OPENED:
                        return "Aquí puedes ver mi progreso. Revisa el árbol de habilidades.";
                    case LEVEL_25:
                        return "He crecido. Busca Geodas Rúnicas minando piedra profunda.";
                    case LEVEL_50:
                        return "Estamos listos. Busca un Santuario Ancestral (Lodestone).";
                    case CORRUPTION_START:
                        return "Siento... oscuridad. Ten cuidado, o me perderé.";
                    case FULL_SET:
                        return "¡La resonancia es perfecta! Siente el poder de la Sinergia.";
                    case SLEEPING_FOUND:
                        return "Siento un poder dormido cerca... Llévalo al Altar de Rituales para despertarlo.";
                    default:
                        return "";
                }
        }
    }
}
