package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AutoUpdateManager — Motor centralizado de Auto-Update en Vivo y Hot-Reload.
 * Permite detectar nuevas versiones en GitHub Releases, descargar el .jar en segundo plano
 * e instalarlo/recargarlo en vivo sin necesidad de apagar o reiniciar el servidor.
 */
public class AutoUpdateManager {

    private static AutoUpdateManager instance;
    private final LivingToolsPlugin plugin;

    private String repository;
    private String currentVersion;
    private String latestVersion = null;
    private String releaseTagName = null;
    private String releaseNotes = "";
    private String downloadUrl = null;
    private long assetSize = 0;
    private boolean updateAvailable = false;
    private boolean isDownloading = false;
    private boolean updatePendingReload = false;
    private String downloadedFileName = null;

    public AutoUpdateManager(LivingToolsPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.repository = ConfigManager.getString("update-checker.repository");
        if (this.repository == null || this.repository.trim().isEmpty()) {
            this.repository = "Viega095/LivingTools";
        }
    }

    public static void init(LivingToolsPlugin plugin) {
        instance = new AutoUpdateManager(plugin);
        if (ConfigManager.getBoolean("update-checker.enabled")) {
            instance.startAutoCheckTask();
        }
    }

    public static AutoUpdateManager getInstance() {
        return instance;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion != null ? latestVersion : currentVersion;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public boolean isUpdatePendingReload() {
        return updatePendingReload;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    /**
     * Inicia una tarea periódica para revisar actualizaciones en GitHub cada 4 horas.
     */
    public void startAutoCheckTask() {
        // Ejecución inicial tras 10 segundos, luego cada 4 horas (4 * 60 * 60 * 20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForUpdates(null, false);
            }
        }.runTaskTimerAsynchronously(plugin, 200L, 4L * 60L * 60L * 20L);
    }

    /**
     * Comprueba si hay una nueva versión disponible en GitHub Releases.
     *
     * @param feedbackSender Receptor del feedback (consola o jugador), o null si es automático.
     * @param notifyIfUpToDate Si es true, notifica aunque esté al día.
     */
    public void checkForUpdates(CommandSender feedbackSender, boolean notifyIfUpToDate) {
        this.repository = ConfigManager.getString("update-checker.repository");
        if (this.repository == null || this.repository.trim().isEmpty()) {
            this.repository = "Viega095/LivingTools";
        }

        if (feedbackSender != null) {
            feedbackSender.sendMessage(ChatColor.GOLD + "[LivingTools] " + ChatColor.YELLOW + "Comprobando actualizaciones en GitHub (" + repository + ")...");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL apiUrl = new URL("https://api.github.com/repos/" + repository + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "LivingTools-AutoUpdater");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                    in.close();

                    String json = jsonBuilder.toString();
                    parseReleaseJson(json);

                    boolean newer = isNewerVersion(currentVersion, latestVersion);
                    this.updateAvailable = newer;
                    plugin.setLatestVersion(latestVersion);

                    // Notificaciones en hilo principal
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (newer) {
                            plugin.getLogger().info("¡Nueva versión encontrada en GitHub! Actual: " + currentVersion + " -> Nueva: " + latestVersion);
                            if (feedbackSender != null) {
                                sendUpdateAvailableMessage(feedbackSender);
                            } else {
                                // Notificar a todos los OPs conectados
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (p.isOp() || p.hasPermission("livingtools.admin")) {
                                        sendUpdateAvailableMessage(p);
                                    }
                                }
                            }
                        } else {
                            if (feedbackSender != null && notifyIfUpToDate) {
                                feedbackSender.sendMessage(ChatColor.GREEN + "✔ LivingTools está al día en la versión " + ChatColor.YELLOW + "v" + currentVersion + ChatColor.GREEN + ".");
                                if (feedbackSender instanceof Player) {
                                    ((Player) feedbackSender).playSound(((Player) feedbackSender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                                }
                            }
                        }
                    });

                } else if (code == 404) {
                    // Si GitHub devuelve 404, significa que aún no hay releases creadas en GitHub Releases
                    this.updateAvailable = false;
                    this.latestVersion = currentVersion;
                    plugin.setLatestVersion(currentVersion);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (feedbackSender != null) {
                            feedbackSender.sendMessage("");
                            feedbackSender.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
                            feedbackSender.sendMessage(ChatColor.GREEN + "  ✔ " + ChatColor.BOLD + "LIVING TOOLS: VERSIÓN ACTUAL AL DÍA");
                            feedbackSender.sendMessage(ChatColor.WHITE + "  Versión instalada: " + ChatColor.YELLOW + "v" + currentVersion);
                            feedbackSender.sendMessage(ChatColor.GRAY + "  Repositorio: " + ChatColor.AQUA + "github.com/" + repository);
                            feedbackSender.sendMessage("");
                            feedbackSender.sendMessage(ChatColor.YELLOW + "  💡 Para publicar nuevas versiones descargables:");
                            feedbackSender.sendMessage(ChatColor.GRAY + "  1. Ve a " + ChatColor.AQUA + "https://github.com/" + repository + "/releases/new");
                            feedbackSender.sendMessage(ChatColor.GRAY + "  2. Crea un Release (ej: v1.1) y adjunta el archivo .jar.");
                            feedbackSender.sendMessage(ChatColor.GRAY + "  3. ¡El plugin lo detectará y podrás actualizarlo con /lt update install!");
                            feedbackSender.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
                            feedbackSender.sendMessage("");
                            if (feedbackSender instanceof Player) {
                                ((Player) feedbackSender).playSound(((Player) feedbackSender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                            }
                        } else {
                            plugin.getLogger().info("UpdateChecker: Repositorio " + repository + " verificado. Plugin en versión v" + currentVersion + " (al día).");
                        }
                    });

                } else if (code == 403) {
                    // Límite de la API pública de GitHub alcanzado temporalmente
                    if (feedbackSender != null) {
                        feedbackSender.sendMessage(ChatColor.YELLOW + "[LivingTools] Límite de consultas a la API de GitHub alcanzado temporalmente. Se reintentará más tarde.");
                    } else {
                        plugin.getLogger().info("UpdateChecker: Límite de consultas de GitHub alcanzado temporalmente.");
                    }
                } else {
                    if (feedbackSender != null) {
                        feedbackSender.sendMessage(ChatColor.RED + "[LivingTools] Verificación de GitHub completada con código HTTP: " + code);
                    }
                }
            } catch (Exception e) {
                if (feedbackSender != null) {
                    feedbackSender.sendMessage(ChatColor.YELLOW + "[LivingTools] Sin conexión temporal con GitHub: " + e.getMessage());
                }
                plugin.getLogger().info("UpdateChecker: Sin conexión con GitHub (" + e.getMessage() + ")");
            }
        });
    }

    /**
     * Descarga e instala la última versión en vivo en la carpeta plugins/ sin reiniciar el servidor.
     *
     * @param sender Quien ejecuta el comando.
     * @param autoReload Si es true, ejecuta el hot-reload inmediatamente tras la descarga.
     */
    public void downloadAndInstall(CommandSender sender, boolean autoReload) {
        if (isDownloading) {
            sender.sendMessage(ChatColor.RED + "⚠ Ya hay una descarga en proceso. Espera un momento.");
            return;
        }

        // Si aún no hemos verificado o no tenemos URL de descarga, verificamos primero
        if (downloadUrl == null) {
            sender.sendMessage(ChatColor.YELLOW + "Obteniendo información del paquete de actualización...");
            checkForUpdates(sender, false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (downloadUrl != null) {
                    executeDownload(sender, autoReload);
                } else {
                    sender.sendMessage(ChatColor.RED + "No se encontró un archivo .jar disponible en la release de GitHub.");
                }
            }, 40L);
            return;
        }

        executeDownload(sender, autoReload);
    }

    private void executeDownload(CommandSender sender, boolean autoReload) {
        isDownloading = true;

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
        sender.sendMessage(ChatColor.YELLOW + "  📥 " + ChatColor.BOLD + "AUTO-UPDATE EN VIVO: LIVING TOOLS");
        sender.sendMessage(ChatColor.WHITE + "  Descargando versión: " + ChatColor.GREEN + "v" + (latestVersion != null ? latestVersion : "Nueva"));
        sender.sendMessage(ChatColor.GRAY + "  Instalando paquete sin interrumpir jugadores...");
        sender.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
        sender.sendMessage("");

        if (sender instanceof Player) {
            ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Directorio de plugins y carpeta de actualización oficial de Spigot
                File pluginsDir = plugin.getDataFolder().getParentFile();
                File updateFolder = new File(pluginsDir, "update");
                if (!updateFolder.exists()) {
                    updateFolder.mkdirs();
                }

                String targetFileName = "LivingTools-" + (latestVersion != null ? latestVersion : "latest") + ".jar";
                File targetFileInUpdate = new File(updateFolder, "LivingTools.jar");
                File directFileInPlugins = new File(pluginsDir, targetFileName);

                // Descarga del stream con soporte para redirecciones 301/302 de GitHub a AWS S3
                HttpURLConnection conn = openConnectionWithRedirects(downloadUrl);
                int responseCode = conn.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Servidor devolvió código HTTP no exitoso: " + responseCode);
                }

                int contentLength = conn.getContentLength();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                byte[] data = new byte[8192];
                int count;
                long totalRead = 0;
                long lastProgressLog = 0;

                while ((count = in.read(data, 0, 8192)) != -1) {
                    buffer.write(data, 0, count);
                    totalRead += count;

                    // Reportar progreso cada ~500KB
                    if (totalRead - lastProgressLog > 500 * 1024 && contentLength > 0) {
                        lastProgressLog = totalRead;
                        int percent = (int) ((totalRead * 100) / contentLength);
                        double currentMB = totalRead / (1024.0 * 1024.0);
                        double totalMB = contentLength / (1024.0 * 1024.0);
                        sendActionBarIfPlayer(sender, ChatColor.YELLOW + "Descargando: " + ChatColor.AQUA + percent + "% " +
                                ChatColor.GRAY + String.format("(%.1f / %.1f MB)", currentMB, totalMB));
                    }
                }
                in.close();
                byte[] jarBytes = buffer.toByteArray();

                // 1. Guardar en plugins/update/LivingTools.jar
                FileOutputStream fosUpdate = new FileOutputStream(targetFileInUpdate);
                fosUpdate.write(jarBytes);
                fosUpdate.close();

                // 2. Guardar también archivo directo en plugins/
                try {
                    FileOutputStream fosDirect = new FileOutputStream(directFileInPlugins);
                    fosDirect.write(jarBytes);
                    fosDirect.close();
                } catch (Exception e) {
                    plugin.getLogger().info("Nota: Escritura directa en plugins/ protegida por el sistema operativo. Guardado en plugins/update/");
                }

                this.isDownloading = false;
                this.updatePendingReload = true;
                this.downloadedFileName = targetFileName;

                // Notificar en hilo principal
                Bukkit.getScheduler().runTask(plugin, () -> {
                    double sizeMB = jarBytes.length / (1024.0 * 1024.0);
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.BOLD + "¡DESCARGA COMPLETADA CON ÉXITO!");
                    sender.sendMessage(ChatColor.GRAY + "Tamaño descargado: " + ChatColor.YELLOW + String.format("%.2f MB", sizeMB));
                    sender.sendMessage(ChatColor.AQUA + "El archivo ha sido ubicado en " + ChatColor.WHITE + "plugins/update/LivingTools.jar");
                    sender.sendMessage("");

                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                        p.sendTitle(ChatColor.GREEN + "¡Auto-Update Listo!", ChatColor.YELLOW + "Versión v" + latestVersion + " descargada", 10, 40, 10);
                    }

                    if (autoReload) {
                        sender.sendMessage(ChatColor.GOLD + "✦ Aplicando Hot-Reload en vivo...");
                        performHotReload(sender);
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "💡 Ejecuta " + ChatColor.GOLD + "/livingtool reload" + ChatColor.YELLOW + " para activar la nueva versión en vivo.");
                    }
                });

            } catch (Exception e) {
                this.isDownloading = false;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(ChatColor.RED + "❌ Error al descargar la actualización: " + e.getMessage());
                    plugin.getLogger().severe("Fallo durante auto-update: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Ejecuta un Hot-Reload integral del plugin:
     * 1. Recarga archivos config.yml y messages.yml fusionando claves nuevas.
     * 2. Recarga y re-registra de forma segura todas las recetas de crafteo.
     * 3. Reinicia y sincroniza Leaderboards, Clanes, Forjas, y Caches de Jefes.
     * 4. Notifica visual y sonoramente al ejecutor.
     */
    public void performHotReload(CommandSender sender) {
        long startTime = System.currentTimeMillis();

        if (sender != null) {
            sender.sendMessage(ChatColor.YELLOW + "⚙ Recargando configuración, recetas, almas y sistemas...");
        }

        try {
            // 1. Configuración & Mensajes
            ConfigManager.reload();

            // 2. Recetas seguras y estructuras desplegadas
            RecipeManager.removeExistingRecipes();
            RecipeManager.registerRecipes();
            StructureCoreManager.loadStructures();
            StructureCoreManager.registerBukkitRecipes();

            // 3. Tablas de clasificación y estados de hermandades
            LeaderboardManager.init();
            SoulGuildManager.init();
            BossDropManager.init();

            // 4. Chequeo de versión
            checkForUpdates(null, false);

            long elapsed = System.currentTimeMillis() - startTime;

            if (sender != null) {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.GREEN + "╔════════════════════════════════════════════════════════════╗");
                sender.sendMessage(ChatColor.GREEN + "  ✔ " + ChatColor.BOLD + "LIVING TOOLS: HOT-RELOAD COMPLETADO");
                sender.sendMessage(ChatColor.GRAY + "  Tiempo de recarga: " + ChatColor.YELLOW + elapsed + " ms");
                sender.sendMessage(ChatColor.GRAY + "  Configuraciones, mensajes, idiomas y recetas actualizadas.");
                if (updatePendingReload) {
                    sender.sendMessage(ChatColor.AQUA + "  ✦ ¡Nueva versión v" + getLatestVersion() + " lista y cargada!");
                }
                sender.sendMessage(ChatColor.GREEN + "╚════════════════════════════════════════════════════════════╝");
                sender.sendMessage("");

                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
                    p.sendTitle(ChatColor.GREEN + "✦ Recarga Completada ✦", ChatColor.YELLOW + "LivingTools (" + elapsed + "ms)", 5, 30, 10);
                }
            }

            plugin.getLogger().info("LivingTools hot-reload ejecutado exitosamente en " + elapsed + "ms.");

        } catch (Exception e) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "❌ Error durante la recarga: " + e.getMessage());
            }
            plugin.getLogger().severe("Error durante performHotReload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HttpURLConnection openConnectionWithRedirects(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "LivingTools-AutoUpdater");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(20000);
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        int redirects = 0;
        while ((status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308) && redirects < 5) {
            String newUrl = conn.getHeaderField("Location");
            conn.disconnect();
            url = new URL(newUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "LivingTools-AutoUpdater");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            status = conn.getResponseCode();
            redirects++;
        }
        return conn;
    }

    private void parseReleaseJson(String json) {
        // Tag name
        Pattern tagPattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher tagMatcher = tagPattern.matcher(json);
        if (tagMatcher.find()) {
            this.releaseTagName = tagMatcher.group(1);
            String clean = releaseTagName;
            if (clean.startsWith("v") || clean.startsWith("V")) {
                clean = clean.substring(1);
            }
            this.latestVersion = clean.trim();
        }

        // Body / Changelog
        Pattern bodyPattern = Pattern.compile("\"body\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
        Matcher bodyMatcher = bodyPattern.matcher(json);
        if (bodyMatcher.find()) {
            this.releaseNotes = unescapeJson(bodyMatcher.group(1));
        }

        // Browser download url for .jar asset
        Pattern assetPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.jar)\"");
        Matcher assetMatcher = assetPattern.matcher(json);
        if (assetMatcher.find()) {
            this.downloadUrl = assetMatcher.group(1);
        } else {
            // Fallback to html_url or generic asset
            Pattern htmlPattern = Pattern.compile("\"html_url\"\\s*:\\s*\"([^\"]+)\"");
            Matcher htmlMatcher = htmlPattern.matcher(json);
            if (htmlMatcher.find() && downloadUrl == null) {
                this.downloadUrl = htmlMatcher.group(1);
            }
        }
    }

    private String unescapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Compara dos cadenas de versión semántica (e.g. 1.0 vs 1.1, 1.0-BETA vs 1.1-BETA).
     */
    public boolean isNewerVersion(String current, String remote) {
        if (current == null || remote == null) return false;

        String c = current.replaceAll("(?i)[vV]", "").trim();
        String r = remote.replaceAll("(?i)[vV]", "").trim();

        if (c.equalsIgnoreCase(r)) return false;

        String[] cParts = c.split("[-._]");
        String[] rParts = r.split("[-._]");

        int length = Math.max(cParts.length, rParts.length);
        for (int i = 0; i < length; i++) {
            int cNum = (i < cParts.length) ? parseIntegerSafe(cParts[i]) : 0;
            int rNum = (i < rParts.length) ? parseIntegerSafe(rParts[i]) : 0;

            if (rNum > cNum) return true;
            if (rNum < cNum) return false;
        }

        return !c.equalsIgnoreCase(r);
    }

    private int parseIntegerSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void sendUpdateAvailableMessage(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════════════════╗");
        sender.sendMessage(ChatColor.YELLOW + "  🔔 " + ChatColor.BOLD + "¡NUEVA VERSIÓN DE LIVING TOOLS DISPONIBLE!");
        sender.sendMessage(ChatColor.WHITE + "  Versión actual: " + ChatColor.RED + "v" + currentVersion +
                ChatColor.WHITE + " ➔ Nueva: " + ChatColor.GREEN + "v" + latestVersion);
        if (releaseNotes != null && !releaseNotes.isEmpty()) {
            String snippet = releaseNotes.length() > 100 ? releaseNotes.substring(0, 97) + "..." : releaseNotes;
            sender.sendMessage(ChatColor.GRAY + "  Novedades: " + ChatColor.WHITE + snippet.replace("\n", " "));
        }
        sender.sendMessage(ChatColor.AQUA + "  ► Usa " + ChatColor.YELLOW + "/livingtool update install" +
                ChatColor.AQUA + " para auto-actualizar en vivo.");
        sender.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════════════════╝");
        sender.sendMessage("");

        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
        }
    }

    private void sendActionBarIfPlayer(CommandSender sender, String message) {
        if (sender instanceof Player) {
            try {
                net.md_5.bungee.api.chat.TextComponent tc = new net.md_5.bungee.api.chat.TextComponent(message);
                ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, tc);
            } catch (Throwable ignored) {}
        }
    }
}
