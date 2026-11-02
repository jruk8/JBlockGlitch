package com.jruk8.jblockglitch.command;

import com.jruk8.jblockglitch.command.admin.AdminCommandsRegistrar;
import com.jruk8.jblockglitch.command.user.UserCommandsRegistrar;
import com.jruk8.jblockglitch.command.user.help.HelpCommand;
import com.jruk8.jblockglitch.config.PluginConfig;
import com.jruk8.jblockglitch.core.Bootstrap;
import com.jruk8.jblockglitch.core.Reloader;
import com.jruk8.jblockglitch.update.UpdateCheckService;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registers all plugin commands through Paper's native Brigadier support.
 * <p>
 * The {@code jblockglitch} root (plus its configured aliases) carries every
 * subcommand. Running the root with no arguments shows the help listing.
 */
public class CommandsRegistrar implements Bootstrap {

    private final LifecycleEventManager<Plugin> lifecycle;
    private final Reloader reloader;
    private final CommandContext commandContext;
    private final PluginMeta pluginMeta;
    private final PluginConfig pluginConfig;
    private final UpdateCheckService updateChecks;

    public CommandsRegistrar(LifecycleEventManager<Plugin> lifecycle,
                             Reloader reloader,
                             CommandContext commandContext,
                             PluginMeta pluginMeta,
                             PluginConfig pluginConfig,
                             UpdateCheckService updateChecks) {
        this.lifecycle = lifecycle;
        this.reloader = reloader;
        this.commandContext = commandContext;
        this.pluginMeta = pluginMeta;
        this.pluginConfig = pluginConfig;
        this.updateChecks = updateChecks;
    }

    @Override
    public void register() {
        lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var root = Commands.literal("jblockglitch");
            var hubAliases = rootAliases();
            var help = new HelpCommand(commandContext, pluginMeta.getVersion());
            root.executes(context -> {
                if (!context.getSource().getSender().hasPermission(Permissions.HELP)) {
                    var tags = Map.of("permission", Permissions.HELP);
                    commandContext.error(context.getSource(), m -> m.getCore().getNoPermission(), tags);
                    return 0;
                }
                help.showHelp(context.getSource(), null);
                return Command.SINGLE_SUCCESS;
            });
            new UserCommandsRegistrar(
                    root, commandContext, pluginMeta, updateChecks, help).register();
            new AdminCommandsRegistrar(root, commandContext, reloader, pluginConfig).register();
            event.registrar().register(root.build(), "JBlockGlitch commands", hubAliases);
        });
    }

    private List<String> rootAliases() {
        return pluginConfig.getAliases().stream()
                .map(String::trim)
                .map(alias -> alias.toLowerCase(Locale.ROOT))
                .filter(alias -> !alias.isEmpty() && !"jblockglitch".equals(alias))
                .distinct()
                .toList();
    }
}
