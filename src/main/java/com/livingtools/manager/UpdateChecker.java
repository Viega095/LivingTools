package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;

import java.util.function.Consumer;

/**
 * UpdateChecker — Módulo compatible con versiones previas que delega al AutoUpdateManager.
 */
public class UpdateChecker {

    private final LivingToolsPlugin plugin;
    private final String repoName;

    public UpdateChecker(LivingToolsPlugin plugin, String repoName) {
        this.plugin = plugin;
        this.repoName = repoName;
    }

    public void getVersion(final Consumer<String> consumer) {
        AutoUpdateManager manager = AutoUpdateManager.getInstance();
        if (manager != null) {
            manager.checkForUpdates(null, false);
            if (consumer != null && manager.getLatestVersion() != null) {
                consumer.accept(manager.getLatestVersion());
            }
        }
    }
}
