package com.jruk8.jblockglitch;

import com.jruk8.jblockglitch.commands.CommandBootstrap;
import com.jruk8.jblockglitch.listeners.ListenerBootstrap;
import com.jruk8.jblockglitch.listeners.ModeService;
import org.bukkit.plugin.java.JavaPlugin;

public final class JBlockGlitchPlugin extends JavaPlugin {

    private MessageService messageService;
    private ModeService modeService;
    private ListenerBootstrap listenerBootstrap;

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
    }

    @Override
    public void onDisable() {
        if (listenerBootstrap != null) {
            listenerBootstrap.shutdown();
            listenerBootstrap = null;
        }
    }

    public void reload() {
        reloadConfig();
        messageService.load();
        modeService.reload();
        listenerBootstrap.reload();
    }
}