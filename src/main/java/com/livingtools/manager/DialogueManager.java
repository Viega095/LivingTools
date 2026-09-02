package com.livingtools.manager;

import com.livingtools.mechanics.Personality;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class DialogueManager {

    private static final Map<Personality, Map<DialogueCategory, List<String>>> dialogues = new HashMap<>();
    private static final Random random = new Random();

    public enum DialogueCategory {
        KILL, MINE, LEVEL_UP, LOW_DURABILITY, BROKEN, IDLE
    }

    static {
        initializeDialogues();
    }

    private static void initializeDialogues() {
        for (Personality p : Personality.values()) {
            dialogues.put(p, new HashMap<>());
            for (DialogueCategory c : DialogueCategory.values()) {
                dialogues.get(p).put(c, new ArrayList<>());
            }
        }

        // --- AGGRESSIVE ---
        add(Personality.AGGRESSIVE, DialogueCategory.KILL,
                "¡SÍ! ¡MÁS SANGRE!", "¡DESTRUCCIÓN TOTAL!", "¡Nadie puede detenernos!",
                "¡Mira cómo caen ante nosotros!", "¡Su miedo me alimenta!", "¡Jajaja! ¡Patético!",
                "¡Otra víctima para la colección!", "¡No dejes a ninguno con vida!", "¡El caos es hermoso!",
                "¡Siento su fuerza vital desvanecerse!", "¡Más! ¡NECESITO MÁS!", "¡Soy imparable en tus manos!",
                "¡Que tiemblen ante mi filo!", "¡La piedad es para los débiles!", "¡Aplástalos a todos!",
                "¡Esto es lo que nací para hacer!", "¡Sangre para el dios de la sangre!", "¡Corta! ¡Rasga! ¡Destruye!",
                "¡Sus gritos son música para mis oídos!", "¡No pares ahora, cobarde!", "¡La victoria es nuestra!",
                "¡Soy la muerte encarnada!", "¡Tiemblan al verme!", "¡Nadie escapa de mi ira!",
                "¡Rompe sus huesos!", "¡Desgarra su carne!", "¡Aniquilación absoluta!",
                "¡Soy el fin de todas las cosas!", "¡Tu ira me fortalece!", "¡Vamos, busca otro!",
                "¡No hay descanso para los malvados!", "¡Mátalos antes de que huyan!", "¡Soy tu mejor arma!",
                "¡Juntos somos invencibles!", "¡El mundo arderá a nuestro paso!", "¡Gloria en la batalla!",
                "¡La paz es una mentira!", "¡Solo existe el poder!", "¡Muere, insecto!",
                "¡No merecen vivir!", "¡Limpia tu espada con su sangre!", "¡Brutalidad sin fin!",
                "¡Soy la pesadilla de tus enemigos!", "¡El dolor es su único destino!", "¡Cosecha sus almas!",
                "¡Devora su esencia!", "¡Soy el castigo divino!", "¡Que sufran!",
                "¡No tengas piedad!", "¡Mátalos a todos y que Dios los clasifique!", "¡Furia ciega!",
                "¡Desata el infierno!", "¡Soy la tormenta!", "¡Soy el cataclismo!");

        add(Personality.AGGRESSIVE, DialogueCategory.MINE,
                "¡Destruye esa roca!", "¡Rompe la tierra!", "¡Nada se interpone en mi camino!",
                "¡Haz pedazos este mundo!", "¡Siente cómo se quiebra!", "¡Fuerza bruta!",
                "¡No hay bloque que resista!", "¡Golpea más fuerte!", "¡Demuéstrales tu poder!",
                "¡Arranca los tesoros de la tierra!", "¡La piedra tiembla ante mí!", "¡Soy el destructor de mundos!",
                "¡Abre camino a la fuerza!", "¡Nada es sólido para mí!", "¡Rompe! ¡Rompe! ¡Rompe!",
                "¡Incluso la obsidiana cede!", "¡Soy imparable!", "¡La tierra sangra minerales!",
                "¡Saca todo lo que tenga!", "¡No dejes nada atrás!", "¡Destrucción geológica!",
                "¡El subsuelo es nuestro!", "¡Haz un túnel al infierno!", "¡Golpea con odio!",
                "¡Que la tierra se parta!", "¡Soy el taladro del destino!", "¡Nada sobrevive a mi toque!",
                "¡Desgarra la realidad!", "¡Rompe la base del mundo!", "¡Poder ilimitado!");

        add(Personality.AGGRESSIVE, DialogueCategory.LOW_DURABILITY,
                "¡Me estoy rompiendo! ¡Arréglame!", "¡Necesito reparaciones, inútil!", "¡No puedo seguir así!",
                "¡Mi filo se desafila! ¡Haz algo!", "¡Me siento débil! ¡Aliméntame!", "¡No me dejes romperme!",
                "¡Maldita sea, repárame!", "¡Estoy perdiendo integridad!", "¡Cuidado, idiota!",
                "¡Si me rompo, te mato!", "¡Necesito materiales, AHORA!", "¡Mi cuerpo no aguanta más!",
                "¡Reparación urgente requerida!", "¡No me uses si no me cuidas!", "¡Me estoy desmoronando!");

        // --- LAZY ---
        add(Personality.LAZY, DialogueCategory.KILL,
                "Ugh, qué esfuerzo...", "¿Era necesario?", "Me has manchado de sangre...",
                "Qué pereza limpiar esto...", "¿Ya terminamos?", "Demasiado movimiento...",
                "Podríamos estar durmiendo...", "Ay, qué pesado eres...", "Otro más... qué aburrido.",
                "¿No puedes dialogar?", "Me duele el filo...", "Hazlo rápido, por favor.",
                "Zzz... ¿eh? ¿Ya murió?", "No me despiertes para esto.", "Qué desperdicio de energía.",
                "Me pesan los encantamientos...", "Quiero volver al cofre.", "¿Falta mucho?",
                "No me agites tanto.", "Cuidado con los movimientos bruscos.", "Me mareo...",
                "Qué vida tan dura...", "Prefiero ser un marco.", "Déjame descansar un rato.",
                "¿Otra vez peleando?", "Qué estrés...", "Me voy a echar una siesta mental.",
                "Avísame cuando acabes.", "No cuentes conmigo para el próximo.", "Qué fiaca...");

        add(Personality.LAZY, DialogueCategory.MINE,
                "¿Otra vez picando? Quiero dormir.", "La piedra está dura...", "No quiero trabajar hoy.",
                "¿No tienes un esclavo para esto?", "Me duele la cabeza...", "Pica tú con la mano.",
                "Qué aburrido es esto...", "Solo 5 minutos más...", "No me obligues...",
                "¿Es necesario este bloque?", "Déjalo ahí, está bien.", "Me estoy desgastando...",
                "Qué trabajo tan sucio.", "Lleno de polvo otra vez...", "Quiero un baño de lava.",
                "¿Podemos irnos a casa?", "Me pesan los átomos.", "La gravedad está fuerte hoy.",
                "No tengo ganas...", "Hazlo suavemente...", "No golpees tan fuerte.",
                "Me vibra todo el cuerpo...", "Qué molestia...", "Odio la minería.",
                "¿Y si lo dejamos para mañana?", "Hoy es día de descanso.", "Soy alérgico al trabajo.",
                "Me duele el mango...", "Qué vida tan injusta...", "Quiero ser de aire.");

        add(Personality.LAZY, DialogueCategory.LOW_DURABILITY,
                "Me siento frágil...", "Creo que me voy a romper...", "Ay, mi espalda...",
                "Necesito un descanso eterno...", "Me estoy deshaciendo...", "Cuidado, estoy sensible.",
                "No me presiones...", "Me voy a desmayar...", "Necesito comida...",
                "Tengo hambre y sueño...", "Repárame o me duermo para siempre.", "Estoy en las últimas...",
                "Qué debilidad...", "No puedo más...", "Me voy a romper de cansancio...");

        // --- SARCASTIC ---
        add(Personality.SARCASTIC, DialogueCategory.KILL,
                "Wow, qué héroe eres.", "Impresionante, mataste a una mosca.", "¿Te sientes poderoso?",
                "Qué técnica tan... interesante.", "Seguro que eso le dolió.", "Un aplauso para el genio.",
                "¿Ese era el plan?", "Qué limpieza, eh.", "Casi me rompo de la risa.",
                "¿Te costó mucho?", "Qué valiente eres con un arma mágica.", "El terror de los pollos.",
                "¿Vas a poner eso en tu currículum?", "Qué gran hazaña.", "Deberían darte una medalla.",
                "Cuidado, no te lastimes.", "¿Eso era un enemigo?", "Parecía inofensivo.",
                "Qué crueldad... me encanta.", "Sigue así, campeón.", "Nadie te gana... hoy.",
                "Qué estilo tan peculiar.", "¿Aprendiste eso en un tutorial?", "Me aburro...",
                "¿Podemos buscar un reto real?", "Eso fue patético.", "Hasta yo lo hacía mejor.",
                "¿Necesitas ayuda?", "No te emociones tanto.", "Fue suerte.");

        add(Personality.SARCASTIC, DialogueCategory.MINE,
                "Qué emocionante es tu vida.", "Rompiendo piedras, qué original.", "El pináculo de la diversión.",
                "¿Te diviertes?", "Wow, un bloque de tierra.", "Qué tesoro tan increíble.",
                "Vas a ser rico con esto.", "Sigue así, llegarás lejos.", "¿No tienes nada mejor que hacer?",
                "Qué habilidad con el pico.", "Maestro de la minería.", "Cuidado, no te canses.",
                "Qué profundidad...", "Me siento realizado.", "Gracias por usarme para esto.",
                "Qué honor picar piedra.", "El sueño de toda herramienta.", "Estoy emocionado. De verdad.",
                "¿Y ahora qué?", "Otro bloque igual al anterior.", "Qué variedad.",
                "Me encanta el olor a polvo.", "Qué paisaje tan gris.", "No te pierdas.",
                "¿Sabes a dónde vas?", "Parece que damos vueltas.", "Qué gran estrategia.",
                "Rompiendo récords de aburrimiento.", "Eres todo un profesional.", "Fascinante.");

        add(Personality.SARCASTIC, DialogueCategory.LOW_DURABILITY,
                "Oye, genio, me estoy rompiendo.", "¿Te has fijado en mi salud?", "Casi me muero, pero tú a lo tuyo.",
                "Un golpe más y adiós.", "Qué buen dueño eres.", "Me siento tan seguro contigo...",
                "¿Sabes lo que es reparar?", "Se me cae un pedazo...", "Qué bien me cuidas.",
                "Voy a explotar en cualquier momento.", "Tic, tac...", "Despídete de mí.",
                "Fue un placer... o no.", "Arréglame, por favor. Nótese la ironía.", "Estoy en las últimas, crack.");

        // --- HEROIC ---
        add(Personality.HEROIC, DialogueCategory.KILL,
                "¡La justicia prevalece!", "¡Por el honor!", "¡El mal ha sido purgado!",
                "¡Defendiendo a los inocentes!", "¡Tu valor es legendario!", "¡Una victoria para la luz!",
                "¡Que su oscuridad se disipe!", "¡Juntos venceremos!", "¡No temas, estoy contigo!",
                "¡La espada de la verdad!", "¡Golpe de justicia!", "¡El destino nos guía!",
                "¡Protege este mundo!", "¡Eres el elegido!", "¡Lucha con honor!",
                "¡Nunca te rindas!", "¡La esperanza brilla!", "¡Derrota a la oscuridad!",
                "¡Un golpe certero!", "¡Gloria al héroe!", "¡Tu leyenda crecerá!",
                "¡El mal tiembla!", "¡Luz divina!", "¡Purificación!",
                "¡Justicia ciega!", "¡Por el rey!", "¡Por la reina!",
                "¡Por el pueblo!", "¡Salva el reino!", "¡Eres nuestra última esperanza!");

        add(Personality.HEROIC, DialogueCategory.MINE,
                "¡Forjando el futuro!", "¡Construyendo un mundo mejor!", "¡Cada bloque cuenta!",
                "¡Cimientos de gloria!", "¡Descubriendo secretos antiguos!", "¡La tierra nos provee!",
                "¡Trabajo honesto!", "¡Con el sudor de tu frente!", "¡Hacia las profundidades!",
                "¡Buscando la luz en la oscuridad!", "¡El progreso requiere sacrificio!", "¡Pica con valor!",
                "¡Encuentra los tesoros perdidos!", "¡Recursos para la causa!", "¡Construye tu legado!",
                "¡La piedra cede ante la justicia!", "¡Fuerza y honor!", "¡No desistas!",
                "¡El camino es duro pero justo!", "¡La recompensa espera!", "¡Sigue adelante, héroe!",
                "¡Tu esfuerzo será recordado!", "¡Levanta castillos!", "¡Crea maravillas!",
                "¡La arquitectura divina!", "¡Moldea el mundo!", "¡Eres un creador!",
                "¡La naturaleza nos ayuda!", "¡Bendiciones de la tierra!", "¡Adelante!");

        add(Personality.HEROIC, DialogueCategory.LOW_DURABILITY,
                "¡Mi luz se desvanece...", "¡Necesito recuperar fuerzas!", "¡No puedo caer aquí!",
                "¡Ayúdame a seguir luchando!", "¡Mis heridas son profundas!", "¡La batalla me ha desgastado!",
                "¡Necesito un herrero!", "¡No dejes que mi leyenda termine!", "¡Aún tengo mucho que dar!",
                "¡Repárame para la batalla final!", "¡Mi integridad está comprometida!", "¡Resistiré... un poco más!",
                "¡Por favor, cuida tu arma!", "¡El héroe necesita su espada!", "¡No me abandones ahora!");

        // --- GLUTTONOUS ---
        add(Personality.GLUTTONOUS, DialogueCategory.KILL,
                "¿Se puede comer eso?", "¡Tengo hambre!", "¡Carne fresca!",
                "¡Delicioso!", "¡Ñam ñam!", "¡Quiero su alma!",
                "¡Aliméntame con su XP!", "¡Más comida!", "¡Qué festín!",
                "¡Me rugen las entrañas!", "¡Sabía a pollo!", "¡Tráeme otro!",
                "¡Devorar!", "¡Tengo un hueco en el estómago!", "¡Qué rico!",
                "¡Dame sus drops!", "¡Quiero postre!", "¡No dejes ni los huesos!",
                "¡Comida rápida!", "¡Sacia mi apetito!", "¡Tengo sed de sangre!",
                "¡Un bocado más!", "¡Estoy famélico!", "¡Qué banquete!",
                "¡Cocínalo bien!", "¡Crudo también me gusta!", "¡Todo para mí!",
                "¡No comparto!", "¡Mi tesoro!", "¡Glotonería pura!");

        add(Personality.GLUTTONOUS, DialogueCategory.MINE,
                "¡Tengo hambre de minerales!", "¡Piedra crujiente!", "¡Diamantes deliciosos!",
                "¡Oro sabroso!", "¡Carbón para la barbacoa!", "¡Come tierra!",
                "¡Mastica la roca!", "¡Quiero hierro!", "¡Aliméntame con bloques!",
                "¡Qué rico subsuelo!", "¡Encuentra una mena!", "¡Tengo antojo de esmeraldas!",
                "¡La redstone pica!", "¡El lapislázuli es dulce!", "¡Obsidiana dura de roer!",
                "¡Tengo hambre!", "¡Dame materiales!", "¡Llena mi inventario!",
                "¡Quiero más!", "¡No pares de comer!", "¡El mundo es un pastel!",
                "¡Corta una rebanada!", "¡Mmm... grava!", "¡Tierra fértil!",
                "¡Raíces jugosas!", "¡Magma picante!", "¡Hielo refrescante!",
                "¡Todo es comestible!", "¡Tengo un agujero negro en el estómago!", "¡Más recursos!");

        add(Personality.GLUTTONOUS, DialogueCategory.LOW_DURABILITY,
                "¡Tengo tanta hambre que me duele!", "¡Aliméntame o me muero!", "¡Me estoy consumiendo!",
                "¡Dame de comer ítems!", "¡Necesito reparación urgente!", "¡Me voy a comer a mí mismo!",
                "¡Tengo el estómago vacío!", "¡Me desmayo del hambre!", "¡Comida! ¡Comida!",
                "¡Repárame con algo rico!", "¡No me dejes morir de hambre!", "¡Soy un saco de huesos!",
                "¡Me estoy rompiendo de debilidad!", "¡Dame madera! ¡Piedra! ¡Algo!", "¡Ayuda!");

        // --- LUCKY ---
        add(Personality.LUCKY, DialogueCategory.KILL,
                "¡Seguro soltó algo bueno!", "¡Hoy es tu día de suerte!", "¡Mira qué loot!",
                "¡Premio gordo!", "¡Qué afortunado eres!", "¡El destino nos sonríe!",
                "¡Probabilidad crítica!", "¡Ha sido un golpe de suerte!", "¡Increíble!",
                "¡Qué casualidad!", "¡Justo en el blanco!", "¡La fortuna favorece a los audaces!",
                "¡Lluvia de objetos!", "¡Siento buenas vibraciones!", "¡Qué racha!",
                "¡Aposté por ti y gané!", "¡El azar está de nuestro lado!", "¡Bingo!",
                "¡Jackpot!", "¡Qué suerte tienes de tenerme!", "¡Todo sale bien!",
                "¡Ni yo me lo creo!", "¡Milagro!", "¡Bendecido por el RNG!",
                "¡Tira los dados!", "¡Doble o nada!", "¡Ganamos!",
                "¡Qué botín!", "¡Tesoro!", "¡La suerte es loca!");

        add(Personality.LUCKY, DialogueCategory.MINE,
                "Siento que hoy es mi día de suerte.", "¡Aquí hay diamantes, lo presiento!",
                "¡Pica aquí, tengo un pálpito!",
                "¡La fortuna nos espera!", "¡Encontraremos algo grande!", "¡Qué buena veta!",
                "¡Suerte de principiante!", "¡El azar nos guía!", "¡Sigue tu instinto!",
                "¡Hoy nos haremos ricos!", "¡Siento el oro cerca!", "¡La suerte está echada!",
                "¡Qué buen pico tienes!", "¡Todo brilla!", "¡Descubrimiento afortunado!",
                "¡Qué casualidad encontrar esto!", "¡Somos un imán de tesoros!", "¡La suerte nunca falla!",
                "¡Confía en el destino!", "¡Algo bueno va a pasar!", "¡Me siento afortunado!",
                "¡Qué buena racha minera!", "¡No pares, la suerte sigue!", "¡Fortuna III se queda corto!",
                "¡Bendición minera!", "¡Qué suerte la mía!", "¡El destino es generoso!",
                "¡Riquezas inesperadas!", "¡Sorpresa!", "¡Éxito rotundo!");

        add(Personality.LUCKY, DialogueCategory.LOW_DURABILITY,
                "Se me está acabando la suerte...", "¡Cuidado, no tientes al destino!",
                "¡Me voy a romper, qué mala suerte!",
                "¡Necesito una reparación afortunada!", "¡La racha se acaba!", "¡Ayuda, la suerte me abandona!",
                "¡No apuestes mi vida!", "¡Repárame por si acaso!", "¡Espero no romperme ahora!",
                "¡Qué mala pata!", "¡Necesito un amuleto... o un lingote!", "¡La fortuna es voluble!",
                "¡No juegues con fuego!", "¡Sálvame!", "¡Espero tener suerte y aguantar!");

        // --- SHY ---
        add(Personality.SHY, DialogueCategory.KILL,
                "Ay... eso fue violento.", "No me gusta pelear...", "Me da miedo...",
                "¿Tenías que matarlo?", "Pobrecito...", "Me quiero esconder.",
                "No me mires...", "Qué ruido...", "Hay mucha sangre...",
                "Me tiembla la hoja...", "Quiero irme a casa.", "No me obligues a esto.",
                "Perdón...", "Lo siento...", "No quería hacerlo...",
                "Qué susto...", "Me tapo los ojos.", "Es muy agresivo...",
                "¿Podemos parar?", "Me siento mal...", "No soy un arma...",
                "Solo quiero paz...", "Qué miedo me das...", "No me gusta el conflicto.",
                "Me voy a desmayar...", "Ay...", "Uff...",
                "Qué horror...", "No me uses para el mal...", "Soy sensible...");

        add(Personality.SHY, DialogueCategory.MINE,
                "Espero no molestar a las piedras.", "Hago mucho ruido...", "Perdón, bloque.",
                "Me da vergüenza picar.", "¿Me están mirando?", "Quiero ser invisible.",
                "Pico despacito...", "No quiero romper nada... bueno, sí.", "Ay, qué duro.",
                "Me siento observado.", "Qué oscuridad...", "Me da miedo la lava.",
                "¿Y si hay monstruos?", "No te alejes mucho.", "Me siento pequeño.",
                "Qué cueva tan grande...", "Me da claustrofobia.", "Quiero salir...",
                "No hagas tanto ruido.", "Shhh...", "Despacito...",
                "Con cuidado...", "No me golpees fuerte.", "Soy frágil...",
                "Me da pena la tierra.", "Perdón por molestar.", "Soy muy tímido...",
                "Me sonrojo...", "Ay...", "Uhm...");

        add(Personality.SHY, DialogueCategory.LOW_DURABILITY,
                "Me duele...", "Me estoy rompiendo...", "Ayuda, por favor...",
                "No quiero molestar, pero...", "Me siento muy débil...", "Tengo miedo de romperme...",
                "Cuídame...", "Soy muy frágil...", "No me dejes morir...",
                "Me estoy desvaneciendo...", "Perdón por ser débil...", "Necesito un abrazo... y materiales.",
                "Me da miedo desaparecer...", "Ay...", "Sálvame...");

        // --- NORMAL ---
        add(Personality.NORMAL, DialogueCategory.KILL,
                "Buen trabajo.", "Objetivo eliminado.", "Sigamos adelante.",
                "Uno menos.", "Eficaz.", "Bien hecho.",
                "Misión cumplida.", "Enemigo abatido.", "Correcto.",
                "Sin problemas.", "Ejecución limpia.", "Buen golpe.",
                "Siguiente.", "Despejado.", "Área segura.",
                "Buen combate.", "Técnica sólida.", "Así se hace.",
                "Neutralizado.", "Victoria.", "Adelante.",
                "Buen rendimiento.", "Funciono correctamente.", "Listo para más.",
                "Sin daños críticos.", "Procediendo.", "Afirmativo.",
                "Buen uso.", "Herramienta lista.", "Esperando órdenes.");

        add(Personality.NORMAL, DialogueCategory.MINE,
                "Sigamos trabajando.", "Bloque extraído.", "Recurso obtenido.",
                "Buen ritmo.", "Zona despejada.", "Continuemos.",
                "Minería eficiente.", "Buen material.", "Progreso constante.",
                "Sin novedades.", "Todo en orden.", "Funcionando al 100%.",
                "Pico afilado.", "Buen ángulo.", "Extracción exitosa.",
                "Inventario llenándose.", "Buen hallazgo.", "Trabajo completado.",
                "Operación minera.", "Excavando.", "Profundizando.",
                "Recogiendo muestras.", "Análisis completo.", "Estructura estable.",
                "Buen día de trabajo.", "Cumpliendo objetivos.", "Productividad alta.",
                "Herramienta operativa.", "Listo.", "Vamos.");

        add(Personality.NORMAL, DialogueCategory.LOW_DURABILITY,
                "Durabilidad baja.", "Necesito reparaciones.", "Integridad comprometida.",
                "Recomiendo mantenimiento.", "Desgaste detectado.", "Por favor, repárame.",
                "Rendimiento disminuyendo.", "Peligro de rotura.", "Estado crítico.",
                "Solicito materiales.", "Mantenimiento requerido.", "Atención al estado.",
                "No puedo continuar mucho más.", "Reparación necesaria.", "Alerta de durabilidad.");
    }

    private static void add(Personality p, DialogueCategory c, String... lines) {
        dialogues.get(p).get(c).addAll(Arrays.asList(lines));
    }

    public static String getLine(Personality personality, DialogueCategory category) {
        if (personality == null || category == null)
            return null;
        List<String> lines = dialogues.get(personality).get(category);
        if (lines == null || lines.isEmpty())
            return null;
        return lines.get(random.nextInt(lines.size()));
    }
}
