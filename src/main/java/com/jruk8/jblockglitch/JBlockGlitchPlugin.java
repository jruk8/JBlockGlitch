package com.jruk8.jblockglitch;

import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class JBlockGlitchPlugin extends JavaPlugin {

    private MessageManager messageManager;
    private DetectionMode detectionMode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        loadDetectionMode();

        messageManager = new MessageManager(this);
        messageManager.load();

        HelpCommand helpCommand = new HelpCommand(messageManager);
        Objects.requireNonNull(getCommand("help")).setExecutor(helpCommand);
        Objects.requireNonNull(getCommand("help")).setTabCompleter(helpCommand);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        Objects.requireNonNull(getCommand("reload")).setExecutor(reloadCommand);

        getServer().getPluginManager().registerEvents(new BlockGlitchListener(this), this);
    }

    void reloadPluginConfiguration() {
        reloadConfig();
        loadDetectionMode();
        messageManager.load();
    }

    DetectionMode getDetectionMode() {
        return detectionMode;
    }

    private void loadDetectionMode() {
        String configuredMode = getConfig().getString("detection-mode", "strict");
        detectionMode = DetectionMode.parse(configuredMode);
        if (configuredMode == null || !configuredMode.equalsIgnoreCase(detectionMode.name())) {
            getLogger().warning("Unknown detection-mode '" + configuredMode + "'; using strict.");
        }
    }
}
