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
        var pluginHelpCommand = plugin.getCommand("help");
        Objects.requireNonNull(pluginHelpCommand).setExecutor(helpCommand);
        Objects.requireNonNull(pluginHelpCommand).setTabCompleter(helpCommand);

        ReloadCommand reloadCommand = new ReloadCommand(plugin, messageService);
        var pluginReloadCommand = plugin.getCommand("reload");
        Objects.requireNonNull(pluginReloadCommand).setExecutor(reloadCommand);
        Objects.requireNonNull(pluginReloadCommand).setTabCompleter(reloadCommand);

        JBlockGlitchCommand jBlockGlitchCommand = new JBlockGlitchCommand(helpCommand, reloadCommand);
        var pluginJBlockGlitchCommand = plugin.getCommand("jblockglitch");
        Objects.requireNonNull(pluginJBlockGlitchCommand).setExecutor(jBlockGlitchCommand);
        Objects.requireNonNull(pluginJBlockGlitchCommand).setTabCompleter(jBlockGlitchCommand);
    }
}