package com.jruk8.jblockglitch.commands;

import com.jruk8.jblockglitch.Bootstrap;
import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import com.jruk8.jblockglitch.MessageService;

import java.util.Objects;

/**
 * Registers all plugin commands and their executors/tab-completers.
 */
public final class CommandBootstrap implements Bootstrap {

    private final JBlockGlitchPlugin plugin;
    private final MessageService messageService;

    public CommandBootstrap(JBlockGlitchPlugin plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
    }

    @Override
    public void register() {
        HelpCommand helpCommand = new HelpCommand(messageService);
        ReloadCommand reloadCommand = new ReloadCommand(plugin, messageService);
        ConfigCommand configCommand = new ConfigCommand(plugin, messageService);

        JBlockGlitchCommand jBlockGlitchCommand = new JBlockGlitchCommand(helpCommand, reloadCommand, configCommand);
        var pluginJBlockGlitchCommand = plugin.getCommand("jblockglitch");
        Objects.requireNonNull(pluginJBlockGlitchCommand).setExecutor(jBlockGlitchCommand);
        Objects.requireNonNull(pluginJBlockGlitchCommand).setTabCompleter(jBlockGlitchCommand);
    }
}