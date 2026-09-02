package com.livingtools.manager;

import com.livingtools.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class GuideBookManager {

        public static ItemStack getGuideBook() {
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta meta = (BookMeta) book.getItemMeta();
                meta.setTitle("Guía de Living Tools");
                meta.setAuthor("El Forjador Ancestral");

                TextComponent p1 = new TextComponent("§5§lLiving Tools\n\n");
                p1.addExtra("§0Bienvenido a un mundo donde tus herramientas tienen vida.\n\n");
                p1.addExtra("§0Empieza crafteando una herramienta y usa:\n");

                TextComponent cmdBind = new TextComponent("§d/livingtool bind\n");
                cmdBind.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool bind"));
                cmdBind.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text("§7Clic para ejecutar")));
                p1.addExtra(cmdBind);

                p1.addExtra("\n§0¡Cuidado! Necesitas permisos o suerte.");

                TextComponent p2 = new TextComponent("§5§lEstructuras\n\n");
                p2.addExtra("§0Para avanzar, necesitarás construir estructuras especiales.\n\n");

                TextComponent linkAltar = new TextComponent("§1§n[Ver Altar Ancestral]\n");
                linkAltar.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool structure altar"));
                linkAltar.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text(
                                                "§7Mesa encantamientos + redstone (gota) + velas")));
                p2.addExtra(linkAltar);

                p2.addExtra("\n");

                TextComponent linkAssembly = new TextComponent("§2§n[Ver Mesa de Ensamblaje]\n");
                linkAssembly.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool structure assembly"));
                linkAssembly.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text(
                                                "§7Mesa de herrería sobre bloque de hierro")));
                p2.addExtra(linkAssembly);

                p2.addExtra("\n");

                TextComponent linkBossForge = new TextComponent("§4§n[Ver Forja de Jefes]\n");
                linkBossForge.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool structure bossforge"));
                linkBossForge.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text(
                                                "§7Plataforma 5×3 blackstone + magma + mesa")));
                p2.addExtra(linkBossForge);

                p2.addExtra("\n");

                TextComponent linkCursedForge = new TextComponent("§8§n[Ver Forja Maldita]\n");
                linkCursedForge.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool structure cursedforge"));
                linkCursedForge.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text(
                                                "§7Yunque + piso blackstone + obsidiana llorosa + velas rojas")));
                p2.addExtra(linkCursedForge);

                TextComponent linkRuneForge = new TextComponent("§5§n[Ver Forja Rúnica]\n");
                linkRuneForge.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool structure runeforge"));
                linkRuneForge.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text(
                                                "§7Mesa herrería + piso amatista + velas moradas")));
                p2.addExtra(linkRuneForge);

                TextComponent p3 = new TextComponent("§5§lComandos Útiles\n\n");

                TextComponent cmdMenu = new TextComponent("§0§lMENU: §d[Abrir]\n");
                cmdMenu.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool menu"));
                p3.addExtra(cmdMenu);

                TextComponent cmdRecipes = new TextComponent("§0§lRECETAS: §d[Ver]\n");
                cmdRecipes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/living recipes"));
                cmdRecipes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.hover.content.Text("§7Menú unificado de crafteos")));
                p3.addExtra(cmdRecipes);

                TextComponent cmdStats = new TextComponent("§0§lSTATS: §d[Ver]\n");
                cmdStats.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/livingtool stats"));
                p3.addExtra(cmdStats);

                TextComponent cmdTrade = new TextComponent("§0§lTRADE: §d[Comerciar]\n");
                cmdTrade.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/livingtool trade "));
                p3.addExtra(cmdTrade);

                meta.spigot().addPage(new TextComponent[] { p1 });
                meta.spigot().addPage(new TextComponent[] { p2 });
                meta.spigot().addPage(new TextComponent[] { p3 });

                TextComponent p4 = new TextComponent("§5§lAlimentación\n\n");
                p4.addExtra("§0Tu herramienta tiene hambre. Si llega a 0, se quejará.\n\n");
                p4.addExtra("§0Usa §d/livingtool feed§0 con el material adecuado en tu inventario.\n\n");
                p4.addExtra("§0Si se rompe, aliméntala para repararla.");
                meta.spigot().addPage(new TextComponent[] { p4 });

                TextComponent p5 = new TextComponent("§5§lRituales\n\n");
                p5.addExtra("§0Usa el Altar Ancestral para mejorar tu herramienta.\n\n");
                p5.addExtra("§0Necesitas:\n");
                p5.addExtra("§1- Catalizador (Centro)\n");
                p5.addExtra("§1- Sacrificios (Alrededor)\n\n");
                p5.addExtra("§0¡Experimenta para descubrir recetas!");
                meta.spigot().addPage(new TextComponent[] { p5 });

                TextComponent p6 = new TextComponent("§5§lJefes Vivientes\n\n");
                p6.addExtra("§0Desafía a los avatares de poder:\n\n");
                p6.addExtra("§4- Living Boss (Volcánico)\n");
                p6.addExtra("§e- Seraphim (Celestial)\n");
                p6.addExtra("§5- Titan (Vacío)\n");
                p6.addExtra("§2- Dríade Corrupta (Bosque)\n");
                p6.addExtra("§6- Wyrm de las Arenas\n");
                p6.addExtra("§3- Leviatán Abisal\n\n");
                p6.addExtra("§0Los jefes clásicos dropean bolsas de botín.\n");
                p6.addExtra("§0Ítems clásicos (Expansor, Alas…) → §4Forja de Jefes§0 (cruz).\n");
                p6.addExtra("§0Reliquias Dryad/Wyrm/Leviatán → §2Mesa de Ensamblaje§0 (3×3).\n");
                meta.spigot().addPage(new TextComponent[] { p6 });

                book.setItemMeta(meta);
                return book;
        }

        public static void giveBook(Player player) {
                if (!player.hasPlayedBefore()) {
                        player.getInventory().addItem(getGuideBook());
                        player.sendMessage(MessageUtils.color("&dHas recibido la Guía de Living Tools."));
                }
        }
}
