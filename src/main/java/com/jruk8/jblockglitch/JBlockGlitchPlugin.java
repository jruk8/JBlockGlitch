package com.jruk8.jblockglitch;

import com.jruk8.jblockglitch.commands.CommandBootstrap;
import com.jruk8.jblockglitch.listeners.ListenerBootstrap;
import com.jruk8.jblockglitch.listeners.ModeService;
import com.jruk8.jblockglitch.updatechecker.UpdateCheckerBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public class JBlockGlitchPlugin extends JavaPlugin {

    private MessageService messageService;
    private ModeService modeService;
    private ListenerBootstrap listenerBootstrap;
    private MetricsBootstrap metricsBootstrap;
    private UpdateCheckerBootstrap updateCheckerBootstrap;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        messageService = new MessageService(this);
        messageService.load();

        modeService = new ModeService(this);

        var commandBootstrap = new CommandBootstrap(this, messageService);
        commandBootstrap.register();

        listenerBootstrap = new ListenerBootstrap(this, modeService);
        listenerBootstrap.register();

        metricsBootstrap = new MetricsBootstrap(this);
        metricsBootstrap.register();

        updateCheckerBootstrap = new UpdateCheckerBootstrap(this, messageService);
        updateCheckerBootstrap.register();
    }

    @Override
    public void onDisable() {
        if (listenerBootstrap != null) {
            listenerBootstrap.shutdown();
            listenerBootstrap = null;
        }
        if (metricsBootstrap != null) {
            metricsBootstrap.shutdown();
            metricsBootstrap = null;
        }
        if (updateCheckerBootstrap != null) {
            updateCheckerBootstrap.shutdown();
            updateCheckerBootstrap = null;
        }
    }

    public void reload() {
        ensureDefaultFiles();

        reloadConfig();
        messageService.load();
        modeService.reload();
        metricsBootstrap.reload();
        listenerBootstrap.reload();
        updateCheckerBootstrap.checkNow();
    }

    /**
     * Recreates the data folder and any missing default files so a
     * deleted data folder (or a single deleted file) heals on reload
     * instead of only on restart.
     */
    void ensureDefaultFiles() {
        getDataFolder().mkdirs();
        saveDefaultConfig();
        saveResource("messages.yml", false);
    }
}