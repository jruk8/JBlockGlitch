package com.jruk8.jblockglitch.command.admin;

import com.jruk8.jblockglitch.command.CommandContext;
import com.jruk8.jblockglitch.command.admin.config.ConfigCommand;
import com.jruk8.jblockglitch.command.admin.reload.ReloadCommand;
import com.jruk8.jblockglitch.config.PluginConfig;
import com.jruk8.jblockglitch.core.Bootstrap;
import com.jruk8.jblockglitch.core.Reloader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * Registers all admin subcommands on the plugin's root command node.
 */
public class AdminCommandsRegistrar implements Bootstrap {

    private final LiteralArgumentBuilder<CommandSourceStack> root;
    private final CommandContext ctx;
    private final Reloader reloader;
    private final PluginConfig pluginConfig;

    public AdminCommandsRegistrar(LiteralArgumentBuilder<CommandSourceStack> root,
                                  CommandContext ctx,
                                  Reloader reloader,
                                  PluginConfig pluginConfig) {
        this.root = root;
        this.ctx = ctx;
        this.reloader = reloader;
        this.pluginConfig = pluginConfig;
    }

    @Override
    public void register() {
        root.then(new ReloadCommand(reloader, ctx, pluginConfig).node());
        root.then(new ConfigCommand(reloader, ctx, pluginConfig).node());
    }
}
