package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class GuideBookManager {

    public static ItemStack getGuideBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) return book;

        meta.setTitle("Guía de Living Tools");
        meta.setAuthor("El Forjador Ancestral");

        // Página 1: Introducción y Primeros Pasos
        TextComponent p1 = new TextComponent("§5§l✦ Living Tools 4.0 ✦\n\n");
        p1.addExtra("§0Tus herramientas poseen un alma viva, personalidad y evolución constante.\n\n");
        p1.addExtra("§0Para iniciar, craftea una herramienta básica y vincúlala:\n\n");
        p1.addExtra(createCommandLink("§d§l[✦ Vincular Alma (/lt bind)]\n", "/livingtool bind", "§7Vincula la herramienta en tu mano a tu alma"));
        p1.addExtra("\n");
        p1.addExtra(createCommandLink("§1§l[📖 Menú de Recetas (/lt recipes)]\n", "/livingtool recipes", "§7Abre la enciclopedia de crafteos y orígenes"));
        p1.addExtra("\n");
        p1.addExtra(createCommandLink("§2§l[⚙ Panel General (/lt menu)]\n", "/livingtool menu", "§7Abre el panel interactivo de tu herramienta"));

        // Página 2: Nutrición y Mantenimiento
        TextComponent p2 = new TextComponent("§5§l🍖 Nutrición & Reparación\n\n");
        p2.addExtra("§0Tu herramienta consume saciedad al trabajar o luchar. Si llega a 0 perderá efectividad.\n\n");
        p2.addExtra(createCommandLink("§4§l[🍖 Alimentar Herramienta]\n", "/livingtool feed", "§7Aliméntala con carbón, madera, minerales o comida"));
        p2.addExtra("\n");
        p2.addExtra(createCommandLink("§8§l[⚒ Forja de Reparación]\n", "/livingtool reforge", "§7Abre la mesa de reparación y mantenimiento"));
        p2.addExtra("\n§0¡Las herramientas de alto nivel se autoreparan lentamente con el tiempo!");

        // Página 3: Progresión, Talentos y Despertar
        TextComponent p3 = new TextComponent("§5§l⚡ Progresión & Poder\n\n");
        p3.addExtra("§0Mina y combate para ganar XP, subir de nivel (1-100) y alcanzar Prestigios.\n\n");
        p3.addExtra(createCommandLink("§9§l[📊 Ver Estadísticas]\n", "/livingtool stats", "§7Consulta daño, XP, nivel y personalidad"));
        p3.addExtra("\n");
        p3.addExtra(createCommandLink("§6§l[🌟 Árbol de Talentos]\n", "/livingtool talents", "§7Asigna puntos de talentos pasivos"));
        p3.addExtra("\n");
        p3.addExtra(createCommandLink("§e§l[⚡ Despertar Definitivo]\n", "/livingtool awaken", "§7Shift + Click Derecho para activar tu habilidad suprema"));
        p3.addExtra("\n");
        p3.addExtra(createCommandLink("§3§l[👑 Títulos de Maestría]\n", "/livingtool titulo", "§7Consulta y equipa títulos honoríficos"));

        // Página 4: Estructuras Místicas
        TextComponent p4 = new TextComponent("§5§l🏛 Estructuras Místicas\n\n");
        p4.addExtra("§0Construye estructuras en el mundo para rituales y forjas avanzadas:\n\n");
        p4.addExtra(createCommandLink("§1§n► Altar Ancestral\n", "/livingtool structure altar", "§7Mesa de encantamientos + redstone + velas"));
        p4.addExtra(createCommandLink("§2§n► Mesa de Ensamblaje (3x3)\n", "/livingtool structure assembly", "§7Mesa de herrería sobre bloque de hierro"));
        p4.addExtra(createCommandLink("§4§n► Forja de Jefes (Cruz)\n", "/livingtool structure bossforge", "§7Plataforma 5x3 blackstone + magma"));
        p4.addExtra(createCommandLink("§5§n► Forja Rúnica\n", "/livingtool structure runeforge", "§7Amatista + velas moradas"));
        p4.addExtra(createCommandLink("§8§n► Forja Maldita\n", "/livingtool structure cursedforge", "§7Obsidiana llorosa + velas rojas"));

        // Página 5: Bestiario y Drops de Jefes
        TextComponent p5 = new TextComponent("§5§l🐉 Bestiario & Jefes\n\n");
        p5.addExtra("§0Enfréntate a criaturas legendarias y sus esbirros subordinados:\n\n");
        p5.addExtra("§4• The Smith §0(Núcleo Magma)\n");
        p5.addExtra("§e• Serafín §0(Pluma & Polvo)\n");
        p5.addExtra("§5• Titán §0(Fragmento & Escamas)\n");
        p5.addExtra("§2• Dríada, Wyrm & Leviatán\n");
        p5.addExtra("§c• Avatar del Génesis (Raid)\n\n");
        p5.addExtra(createCommandLink("§6§l[🐉 Abrir Bestiario Completo]\n", "/livingtool bestiary", "§7Consulta vida, daño, dificultad y drops de cada criatura"));

        // Página 6: Gemas, Runas y Sockets
        TextComponent p6 = new TextComponent("§5§l✧ Runas & Gemas\n\n");
        p6.addExtra("§0Incrusta poder místico en tus herramientas y armaduras:\n\n");
        p6.addExtra(createCommandLink("§b§l[💎 Engarzar Gemas (Sockets)]\n", "/livingtool sockets", "§7Inserta Gemas Celestiales, de Alma o Fénix"));
        p6.addExtra("\n§0• §6Gema Fénix:§0 Te revive al morir.\n");
        p6.addExtra("§0• §dRunas Elementales:§0 Fuego, Hielo, Trueno, Luz y Vacío.\n");
        p6.addExtra("§0• §5Sinergia Rúnica:§0 Combina 3 runas específicas para efectos ocultos.");

        // Página 7: Sendas de Ascensión Divina (4.0)
        TextComponent p7 = new TextComponent("§5§l👑 Ascensión Divina\n\n");
        p7.addExtra("§0Al alcanzar el Nivel 100, consagra tu arma a los Dioses Celestiales:\n\n");
        p7.addExtra("§6• Senda Solar: §0Juicio Ígneo\n");
        p7.addExtra("§b• Senda Lunar: §0Marea Astral\n");
        p7.addExtra("§5• Senda Abisal: §0Devorador\n");
        p7.addExtra("§2• Senda Gaia: §0Regeneración\n");
        p7.addExtra("§9• Senda Tormenta: §0Rayos\n\n");
        p7.addExtra(createCommandLink("§e§l[👑 Altar de Ascensión]\n", "/livingtool ascend", "§7Abre el menú de ascensión de tu arma"));

        // Página 8: Tarot Arcano del Destino
        TextComponent p8 = new TextComponent("§5§l🃏 Tarot del Destino\n\n");
        p8.addExtra("§0Consulta a los arcanos mayores para recibir bendiciones de 30 minutos:\n\n");
        p8.addExtra("§0• §6El Sol:§0 +50% XP y Brillo Solar.\n");
        p8.addExtra("§0• §bEl Mago:§0 Doble activación mágica.\n");
        p8.addExtra("§0• §cLa Muerte:§0 Críticos letales.\n");
        p8.addExtra("§0• §5La Rueda:§0 Botín duplicado.\n\n");
        p8.addExtra(createCommandLink("§d§l[🃏 Robar Carta de Tarot]\n", "/livingtool tarot", "§7Roba tu carta del destino"));

        // Página 9: Hermandades de Almas (Guilds)
        TextComponent p9 = new TextComponent("§5§l🏰 Hermandades (Guilds)\n\n");
        p9.addExtra("§0Únete a otros forjadores y progresen en hermandad:\n\n");
        p9.addExtra("§0• Bóveda comunitaria segura.\n");
        p9.addExtra("§0• Bufos pasivos de clan.\n");
        p9.addExtra("§0• Misiones de clan cooperativas.\n\n");
        p9.addExtra(createCommandLink("§2§l[🏰 Info de mi Hermandad]\n", "/livingtool guild info", "§7Consulta nivel y miembros de tu clan"));
        p9.addExtra("\n");
        p9.addExtra(createSuggestLink("§3§l[📦 Abrir Bóveda de Clan]\n", "/livingtool guild vault", "§7Abre el cofre compartido del clan"));

        // Página 10: Incursiones, Rifts y Minijuegos
        TextComponent p10 = new TextComponent("§5§l⚔ Incursiones & Modos\n\n");
        p10.addExtra("§0Desafíos avanzados de combate y forja:\n\n");
        p10.addExtra(createCommandLink("§4► Rift Abisal (Oleadas)\n", "/livingtool rift", "§7Combate oleadas infinitas de sombras"));
        p10.addExtra(createCommandLink("§5► Mazmorra Incursión\n", "/livingtool incursion", "§7Derrota centinelas y cofres míticos"));
        p10.addExtra(createCommandLink("§6► Minijuego Forja Rítmica\n", "/livingtool ritmo", "§7Golpea al compás para multiplicar XP"));
        p10.addExtra(createCommandLink("§e► Contratos de Caza (Bounties)\n", "/livingtool bounties", "§7Caza monstruos por recompensas diarias"));
        p10.addExtra(createCommandLink("§b► Brújula de Meteoritos\n", "/livingtool compass", "§7Rastrea meteoritos que caen del cielo"));

        // Página 11: Cosméticos y Acompañantes
        TextComponent p11 = new TextComponent("§5§l✨ Cosméticos & Guardián\n\n");
        p11.addExtra("§0Personaliza y embellece tu herramienta:\n\n");
        p11.addExtra(createCommandLink("§d§l[✨ Estelas y Auras]\n", "/livingtool trails", "§7Selecciona efectos de partículas al moverte"));
        p11.addExtra("\n");
        p11.addExtra(createCommandLink("§b§l[🕊 Invocar Guardián Wisp]\n", "/livingtool companion", "§7Invoca un espíritu acompañante con radar de diamantes"));
        p11.addExtra("\n");
        p11.addExtra(createCommandLink("§a§l[🏛 Pedestales 3D (/lt pedestal)]\n", "/livingtool pedestal", "§7Exhibe tu arma en Lodestone y gana XP descansado"));
        p11.addExtra("\n");
        p11.addExtra(createCommandLink("§6§l[🏆 Logros Secretos]\n", "/livingtool logros", "§7Descubre los desafíos ocultos"));

        // Página 12: Atajos y Comandos Rápidos
        TextComponent p12 = new TextComponent("§5§l📋 Atajos en un Click\n\n");
        p12.addExtra(createCommandLink("§1[✦ Menú de Recetas]\n", "/livingtool recipes", "§7Abrir enciclopedia de crafteos"));
        p12.addExtra(createCommandLink("§4[🐉 Bestiario]\n", "/livingtool bestiary", "§7Abrir bestiario"));
        p12.addExtra(createCommandLink("§2[📊 Mis Estadísticas]\n", "/livingtool stats", "§7Ver estadísticas"));
        p12.addExtra(createCommandLink("§6[🏆 Top Jugadores]\n", "/livingtool top", "§7Ver ranking del servidor"));
        p12.addExtra(createCommandLink("§e[👑 Ascensión Divina]\n", "/livingtool ascend", "§7Altar de ascensión"));
        p12.addExtra(createCommandLink("§d[🃏 Tirada de Tarot]\n", "/livingtool tarot", "§7Robar carta"));
        p12.addExtra(createCommandLink("§3[🏰 Mi Hermandad]\n", "/livingtool guild info", "§7Info de clan"));

        meta.spigot().addPage(new TextComponent[]{ p1 });
        meta.spigot().addPage(new TextComponent[]{ p2 });
        meta.spigot().addPage(new TextComponent[]{ p3 });
        meta.spigot().addPage(new TextComponent[]{ p4 });
        meta.spigot().addPage(new TextComponent[]{ p5 });
        meta.spigot().addPage(new TextComponent[]{ p6 });
        meta.spigot().addPage(new TextComponent[]{ p7 });
        meta.spigot().addPage(new TextComponent[]{ p8 });
        meta.spigot().addPage(new TextComponent[]{ p9 });
        meta.spigot().addPage(new TextComponent[]{ p10 });
        meta.spigot().addPage(new TextComponent[]{ p11 });
        meta.spigot().addPage(new TextComponent[]{ p12 });

        book.setItemMeta(meta);
        return book;
    }

    public static void giveBook(Player player) {
        player.getInventory().addItem(getGuideBook());
        player.sendMessage(MessageUtils.color("&dHas recibido la Guía de Living Tools. ¡Ábrela con Click Derecho!"));
    }

    private static TextComponent createCommandLink(String text, String command, String hoverText) {
        TextComponent comp = new TextComponent(text);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        return comp;
    }

    private static TextComponent createSuggestLink(String text, String command, String hoverText) {
        TextComponent comp = new TextComponent(text);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        return comp;
    }
}
