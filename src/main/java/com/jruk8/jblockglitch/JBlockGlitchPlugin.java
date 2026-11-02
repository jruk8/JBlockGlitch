package com.jruk8.jblockglitch;

import com.jruk8.jblockglitch.command.CommandContext;
import com.jruk8.jblockglitch.command.CommandsRegistrar;
import com.jruk8.jblockglitch.config.ConfigRegistrar;
import com.jruk8.jblockglitch.core.MetricsBootstrap;
import com.jruk8.jblockglitch.core.Reloader;
import com.jruk8.jblockglitch.core.StartupBanner;
import com.jruk8.jblockglitch.listeners.ListenerBootstrap;
import com.jruk8.jblockglitch.listeners.ModeService;
import com.jruk8.jblockglitch.message.MessageBootstrap;
import com.jruk8.jblockglitch.message.MessageBundles;
import com.jruk8.jblockglitch.message.Messenger;
import com.jruk8.jblockglitch.sound.SoundPlayer;
import com.jruk8.jblockglitch.update.UpdateCheckerBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class JBlockGlitchPlugin extends JavaPlugin {

    private ListenerBootstrap listenerBootstrap;

    @Override
    public void onEnable() {
        // Set up all core modules
        setupCoreModules();
    }

    @Override
    public void onDisable() {
        if (this.listenerBootstrap != null) {
            this.listenerBootstrap.shutdown();
            this.listenerBootstrap = null;
        }
    }

    private void setupCoreModules() {
        var logger = getLogger();

        // Initialize reloader
        Reloader reloader = new Reloader(logger);

        // Initialize all configs
        var configRegistrar = new ConfigRegistrar(this);
        configRegistrar.register();
        reloader.register(configRegistrar);

        // Initialize detection modes and listeners
        var modeService = new ModeService(configRegistrar.getPluginConfig());
        reloader.register(modeService);
        this.listenerBootstrap = new ListenerBootstrap(this, modeService);
        this.listenerBootstrap.register();
        reloader.register(this.listenerBootstrap);

        // Initialize per-language message bundles and the messenger
        var bundles = messageBundles(reloader, configRegistrar, logger);
        var messenger = messageRegistrar(configRegistrar, bundles).getMessenger();

        // Initialize sound player
        var soundPlayer = new SoundPlayer(logger);

        // Initialize update checker; the banner prints once the first check lands
        var updateChecker = new UpdateCheckerBootstrap(
                this,
                configRegistrar.getPluginConfig(),
                messenger,
                bundles,
                logger,
                this::printBanner
        );
        updateChecker.register();
        reloader.register(updateChecker);

        // Initialize commands
        setupCommands(reloader, configRegistrar, messenger, soundPlayer, updateChecker, bundles);

        // Initialize bStats unless anonymous statistics are disabled
        boolean metricsEnabled = configRegistrar.getPluginConfig().getMetrics().isEnabled();
        var metricsBootstrap = new MetricsBootstrap(this, metricsEnabled);
        metricsBootstrap.register();
    }

    private void printBanner(boolean updateAvailable) {
        StartupBanner.print(
                Bukkit.getConsoleSender()::sendMessage,
                getName(),
                getPluginMeta().getVersion(),
                updateAvailable);
    }

    private MessageBundles messageBundles(
            Reloader reloader,
            ConfigRegistrar configRegistrar,
            Logger logger) {
        // No pins without storage: every player follows the default language
        var bundles = new MessageBundles(
                configRegistrar.getMessagesConfig(),
                uuid -> null,
                new File(getDataFolder(), "lang"),
                logger,
                configRegistrar.getPluginConfig()::getDefaultLanguage);
        reloader.register(bundles);
        return bundles;
    }

    private MessageBootstrap messageRegistrar(ConfigRegistrar configRegistrar, MessageBundles bundles) {
        var messageRegistrar = new MessageBootstrap(configRegistrar, bundles);
        messageRegistrar.register();
        return messageRegistrar;
    }

    private void setupCommands(
            Reloader reloader,
            ConfigRegistrar configRegistrar,
            Messenger messenger,
            SoundPlayer soundPlayer,
            UpdateCheckerBootstrap updateChecker,
            MessageBundles bundles
    ) {
        var context = new CommandContext(
                configRegistrar.getMessagesConfig(),
                configRegistrar.getSoundsConfig(),
                messenger,
                soundPlayer,
                bundles
        );
        var commandRegistrar = new CommandsRegistrar(
                getLifecycleManager(), reloader, context, getPluginMeta(),
                configRegistrar.getPluginConfig(), updateChecker.getService());
        commandRegistrar.register();
    }
}
