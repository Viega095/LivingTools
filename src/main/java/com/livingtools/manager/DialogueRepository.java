package com.livingtools.manager;

import com.livingtools.mechanics.Personality;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DialogueRepository {

    private static final Map<Personality, Map<PersonalityManager.EventType, List<String>>> dialogueMap = new EnumMap<>(
            Personality.class);
    private static final Random random = new Random();

    static {
        initializeDialogues();
    }

    private static void initializeDialogues() {
        for (Personality p : Personality.values()) {
            dialogueMap.put(p, new EnumMap<>(PersonalityManager.EventType.class));
            for (PersonalityManager.EventType e : PersonalityManager.EventType.values()) {
                dialogueMap.get(p).put(e, new ArrayList<>());
            }
        }

        // --- SARCASTIC ---
        add(Personality.SARCASTIC, PersonalityManager.EventType.LEVEL_UP,
                "Oh, genial. Más poder. ¿Ahora podemos descansar?",
                "Wow, nivel nuevo. ¿Me das un premio? No, espera, soy una herramienta.",
                "Siento el poder... y la pereza de usarlo.",
                "Felicidades, ahora soy más peligroso. Tiembla, mundo.",
                "¿Nivel alto? Compensando algo, ¿eh?");
        add(Personality.SARCASTIC, PersonalityManager.EventType.MINING,
                "¿En serio? ¿Otra piedra? Qué emocionante.",
                "Piedra, piedra, piedra... mi vida es fascinante.",
                "¡Cuidado! ¡Esa piedra te miró mal!",
                "Si me pagaran por esto... ah, cierto, no tengo bolsillos.",
                "¿No tienes nada mejor que hacer?",
                "Romper cosas es mi pasión. Nótese el sarcasmo.");
        add(Personality.SARCASTIC, PersonalityManager.EventType.KILL_MOB,
                "Wow, qué valiente. Mataste a una cosa indefensa.",
                "Uno menos. Faltan... todos los demás.",
                "¿Te sientes poderoso ahora?",
                "Esa cosa era fea de todos modos.",
                "Limpiando el mundo, un mob a la vez. Qué héroe.");
        add(Personality.SARCASTIC, PersonalityManager.EventType.LOW_DURABILITY,
                "Oye, me estoy rompiendo aquí. ¿Te importa?",
                "Si me rompo, te perseguiré como fantasma.",
                "Un poco de cinta adhesiva no vendría mal.",
                "Me duele todo. Literalmente todo.",
                "¿Reparación? ¿Hola? ¿Hay alguien ahí?");
        add(Personality.SARCASTIC, PersonalityManager.EventType.AWAKENING,
                "¿Por qué me despertaste? Estaba teniendo un sueño genial.",
                "Cinco minutos más... ah, eres tú.",
                "Genial, estoy despierto. ¿Ahora qué?",
                "Espero que valga la pena interrumpir mi siesta.");

        // --- HEROIC ---
        add(Personality.HEROIC, PersonalityManager.EventType.LEVEL_UP,
                "¡Siento el poder fluir! ¡Nada nos detendrá!",
                "¡Más fuertes para proteger a los inocentes!",
                "¡La luz de la justicia brilla más fuerte en mí!",
                "¡Un nuevo nivel de poder para combatir la oscuridad!",
                "¡Juntos somos imparables, compañero!");
        add(Personality.HEROIC, PersonalityManager.EventType.MINING,
                "¡Cada bloque roto es un paso hacia la gloria!",
                "¡Forjando el camino hacia la victoria!",
                "¡Con cada golpe, construimos un futuro mejor!",
                "¡El trabajo duro dignifica al héroe!",
                "¡Extraemos los recursos para el bien mayor!");
        add(Personality.HEROIC, PersonalityManager.EventType.KILL_MOB,
                "¡La justicia ha sido servida!",
                "¡El mal no prevalecerá mientras estemos aquí!",
                "¡Por el honor y la gloria!",
                "¡Un enemigo menos, un mundo más seguro!",
                "¡Descansa en paz, criatura de la oscuridad!");
        add(Personality.HEROIC, PersonalityManager.EventType.LOW_DURABILITY,
                "¡Mis heridas son graves, pero seguiré luchando!",
                "¡No me rendiré, incluso si mi cuerpo se quiebra!",
                "¡Necesito recuperarme para seguir la batalla!",
                "¡El dolor es temporal, la gloria es eterna! (Pero repárame por favor)",
                "¡Mi estructura falla, pero mi espíritu no!");
        add(Personality.HEROIC, PersonalityManager.EventType.AWAKENING,
                "¡He despertado! ¡Guíame a la batalla, noble guerrero!",
                "¡La leyenda regresa! ¿Cuál es nuestra misión?",
                "¡Estoy listo para servir a la luz una vez más!",
                "¡El letargo ha terminado! ¡A la aventura!");

        // --- AGGRESSIVE ---
        add(Personality.AGGRESSIVE, PersonalityManager.EventType.LEVEL_UP,
                "¡SÍ! ¡MÁS PODER PARA DESTRUIR!",
                "¡AHORA SOY IMPARABLE!",
                "¡MÁS FUERZA! ¡MÁS SANGRE!",
                "¡TEMEDME, MORTALES! ¡HE ASCENDIDO!",
                "¡EL PODER CORRE POR MIS VENAS... O LO QUE SEA QUE TENGA!");
        add(Personality.AGGRESSIVE, PersonalityManager.EventType.MINING,
                "¡ROMPE TODO!",
                "¡DESTRUYE LA TIERRA!",
                "¡MÁS FUERTE! ¡GOLPEA MÁS FUERTE!",
                "¡NO DEJES NADA EN PIE!",
                "¡ODIO ESTAS PIEDRAS! ¡MUERAN!");
        add(Personality.AGGRESSIVE, PersonalityManager.EventType.KILL_MOB,
                "¡SANGRE PARA EL DIOS DE LA SANGRE!",
                "¡MUERE, INSECTO!",
                "¡APLASTA A TUS ENEMIGOS!",
                "¡JAJAJA! ¡OTRO MÁS!",
                "¡LA MATANZA NUNCA TERMINA!");
        add(Personality.AGGRESSIVE, PersonalityManager.EventType.LOW_DURABILITY,
                "¡NO ME IMPORTA EL DOLOR! ¡SIGUE!",
                "¡ME ESTOY ROMPIENDO PERO ME LLEVARÉ A TODOS CONMIGO!",
                "¡REPÁRAME O TE MUERDO!",
                "¡MI FURIA ME MANTIENE UNIDO!",
                "¡NO PUEDO MORIR AHORA! ¡HAY TANTO QUE DESTRUIR!");
        add(Personality.AGGRESSIVE, PersonalityManager.EventType.AWAKENING,
                "¡¿QUIÉN OSA DESPERTARME?! Espero que valga la pena.",
                "¡HE VUELTO! ¡CORRAN POR SUS VIDAS!",
                "¡DAME ALGO PARA MATAR AHORA MISMO!",
                "¡EL SUEÑO ERA ABURRIDO! ¡QUIERO CAOS!");

        // --- SHY ---
        add(Personality.SHY, PersonalityManager.EventType.LEVEL_UP,
                "Umm... creo que me siento un poco más fuerte...",
                "Oh... gracias por cuidarme tanto...",
                "¿De verdad merezco este nivel?",
                "Me siento... mejor. Gracias.",
                "Espero poder serte útil con este nuevo poder...");
        add(Personality.SHY, PersonalityManager.EventType.MINING,
                "Espero no molestar a los gusanos...",
                "Perdón, piedrita...",
                "Hago lo mejor que puedo...",
                "¿Lo estoy haciendo bien?",
                "Ay, eso sonó fuerte...");
        add(Personality.SHY, PersonalityManager.EventType.KILL_MOB,
                "¡Ay! ¿Tenías que matarlo?",
                "Lo siento... no quería hacerte daño...",
                "Qué miedo daba eso...",
                "Espero que no tenga familia...",
                "¿Ya se acabó? Qué alivio.");
        add(Personality.SHY, PersonalityManager.EventType.LOW_DURABILITY,
                "M-me siento un poco frágil...",
                "Creo que me voy a romper... perdón...",
                "Ay... me duele un poquito...",
                "Por favor, ten cuidado conmigo...",
                "No quiero ser una molestia, pero necesito ayuda...");
        add(Personality.SHY, PersonalityManager.EventType.AWAKENING,
                "H-hola... ¿quién eres tú?",
                "¿Ya es hora? Tengo un poco de miedo...",
                "Oh, eres tú. Qué bueno verte...",
                "Perdón por dormir tanto...");

        // --- GLUTTONOUS ---
        add(Personality.GLUTTONOUS, PersonalityManager.EventType.LEVEL_UP,
                "¡Delicioso poder! ¿Hay más?",
                "¡Me siento lleno de energía! ¡Como si hubiera comido un banquete!",
                "¡Sabor a victoria! ¡Y a XP!",
                "¡Más niveles! ¡Más comida!",
                "¡Crezco fuerte y hambriento!");
        add(Personality.GLUTTONOUS, PersonalityManager.EventType.MINING,
                "¿Este bloque es comestible?",
                "¡Mmm, minerales crujientes!",
                "¡Dame esos recursos!",
                "¡Tengo hambre de destrucción... y de carbón!",
                "¿Podemos comer después de esto?");
        add(Personality.GLUTTONOUS, PersonalityManager.EventType.KILL_MOB,
                "Mmm... carne fresca.",
                "¡Cena servida!",
                "¿Eso se puede cocinar?",
                "¡Espero que suelte comida!",
                "¡Devorador de almas... y de chuletas!");
        add(Personality.GLUTTONOUS, PersonalityManager.EventType.LOW_DURABILITY,
                "Necesito comida... o hierro... ¡reparame!",
                "¡Me muero de hambre! ¡Y de daño!",
                "¡Aliméntame con un yunque, rápido!",
                "¡Estoy tan débil que no puedo ni masticar!",
                "¡Reparación! ¡El plato principal!");
        add(Personality.GLUTTONOUS, PersonalityManager.EventType.AWAKENING,
                "Tengo hambre... ¿tienes diamantes?",
                "¡Soñé con un buffet de netherite!",
                "¿Qué hay de desayuno?",
                "¡Despierto y hambriento!");

        // --- LAZY ---
        add(Personality.LAZY, PersonalityManager.EventType.LEVEL_UP,
                "Ugh, subir de nivel es agotador...",
                "¿Más niveles? ¿Significa más trabajo?",
                "Genial, ahora soy más fuerte. ¿Puedo volver a dormir?",
                "Demasiado esfuerzo para un brillito...",
                "Meh. Está bien, supongo.");
        add(Personality.LAZY, PersonalityManager.EventType.MINING,
                "¿Podemos tomar un descanso ya?",
                "Otra piedra más... qué pereza.",
                "¿No tienes un pico automático o algo?",
                "Trabajar es para los aldeanos...",
                "Me duelen los filos...");
        add(Personality.LAZY, PersonalityManager.EventType.KILL_MOB,
                "Demasiado esfuerzo...",
                "¿Ya está? Bien.",
                "Uff, qué pesado era ese.",
                "Espero que no vengan más.",
                "Me manché. Qué fastidio.");
        add(Personality.LAZY, PersonalityManager.EventType.LOW_DURABILITY,
                "Me voy a romper... qué pena, tendré que dormir.",
                "Estoy demasiado cansado para mantenerme unido.",
                "Si me rompo, no me despiertes.",
                "Necesito una siesta... de reparación.",
                "Ya no aguanto más... buenas noches.");
        add(Personality.LAZY, PersonalityManager.EventType.AWAKENING,
                "Cinco minutos más... por favor...",
                "¿Por qué tanto ruido?",
                "Estaba tan cómodo...",
                "No quiero levantarme...");

        // --- LUCKY ---
        add(Personality.LUCKY, PersonalityManager.EventType.LEVEL_UP,
                "¡Qué suerte! ¡Hemos subido de nivel!",
                "¡Las probabilidades estaban a nuestro favor!",
                "¡Siento que hoy será un gran día!",
                "¡Premio gordo! ¡Nivel nuevo!",
                "¡La fortuna nos sonríe!");
        add(Personality.LUCKY, PersonalityManager.EventType.MINING,
                "¡Siento que encontraremos diamantes pronto!",
                "¡Apuesto a que este bloque tiene algo bueno!",
                "¡Hoy es mi día de suerte!",
                "¡Vamos a hacernos ricos!",
                "¡La fortuna favorece a los audaces!");
        add(Personality.LUCKY, PersonalityManager.EventType.KILL_MOB,
                "¡Seguro que suelta algo bueno!",
                "¡Golpe de suerte!",
                "¡Ni me tocó! ¡Qué suerte!",
                "¡Loot! ¡Loot! ¡Loot!",
                "¡Aposté a que ganábamos y gané!");
        add(Personality.LUCKY, PersonalityManager.EventType.LOW_DURABILITY,
                "Espero no romperme en el peor momento...",
                "¡La suerte se me está acabando!",
                "¡Necesito un golpe de suerte (y un yunque)!",
                "¡Crucemos los dedos para que aguante un poco más!",
                "¡No tientes a la suerte, repárame!");
        add(Personality.LUCKY, PersonalityManager.EventType.AWAKENING,
                "¡Hoy es tu día de suerte! ¡He despertado!",
                "¡Soñé con tréboles de cuatro hojas!",
                "¡Qué afortunado eres de tenerme!",
                "¡Vamos a probar nuestra suerte!");
    }

    private static void add(Personality p, PersonalityManager.EventType e, String... lines) {
        List<String> list = dialogueMap.get(p).get(e);
        for (String line : lines) {
            list.add(line);
        }
    }

    public static String getRandomMessage(Personality p, PersonalityManager.EventType e) {
        List<String> list = dialogueMap.get(p).get(e);
        if (list == null || list.isEmpty())
            return null;
        return list.get(random.nextInt(list.size()));
    }
}
