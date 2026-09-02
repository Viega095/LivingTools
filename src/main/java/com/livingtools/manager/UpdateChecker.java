package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateChecker {

    private final LivingToolsPlugin plugin;
    private final String repoName; // e.g., "Viega095/LivingTools"

    public UpdateChecker(LivingToolsPlugin plugin, String repoName) {
        this.plugin = plugin;
        this.repoName = repoName;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + repoName + "/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Simple JSON parsing to find "tag_name"
                    String json = response.toString();
                    String version = parseVersion(json);

                    if (version != null) {
                        // Remove 'v' prefix if present
                        if (version.startsWith("v")) {
                            version = version.substring(1);
                        }
                        consumer.accept(version);
                    }
                } else {
                    plugin.getLogger()
                            .warning("Failed to check for updates. Response code: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String parseVersion(String json) {
        // Very basic JSON parser to avoid dependencies
        // Looks for "tag_name": "v1.0.0"
        int index = json.indexOf("\"tag_name\"");
        if (index == -1)
            return null;

        int start = json.indexOf(":", index) + 1;
        int end = json.indexOf(",", start);

        String value = json.substring(start, end).trim();
        // Remove quotes
        return value.replaceAll("\"", "");
    }
}
