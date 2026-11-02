package com.jruk8.jblockglitch.command.user.help;

import com.jruk8.jblockglitch.command.CommandContext;
import com.jruk8.jblockglitch.command.Permissions;
import com.jruk8.jblockglitch.message.MessagesConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;
import java.util.Map;

/**
 * Help subcommand that lists the plugin's commands, optionally filtered by a search query.
 * <p>
 * Senders with {@code jblockglitch.help} may run it (everyone by default);
 * entries are filtered by permission. Entries use the main {@code /jbg}
 * alias.
 */
public class HelpCommand {

    private final CommandContext ctx;
    private final String version;

    public HelpCommand(CommandContext ctx, String version) {
        this.ctx = ctx;
        this.version = version;
    }

    /**
     * Builds the {@code help} Brigadier node, attached to the {@code jblockglitch} root by the registrar.
     *
     * @return the configured subcommand node
     */
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("help")
                .requires(CommandContext.requiresPermission(Permissions.HELP))
                .executes(context -> {
                    showHelp(context.getSource(), null);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("query", StringArgumentType.greedyString())
                        .executes(context -> {
                            showHelp(context.getSource(), StringArgumentType.getString(context, "query"));
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    /**
     * Shows the help listing, optionally filtered by a search query.
     *
     * @param sender the sender to show the listing to
     * @param query the search query, may be null or blank
     */
    public void showHelp(CommandSourceStack sender, String query) {
        ctx.playHelp(sender);
        ctx.sendMessage(sender, m -> m.getCore().getHelpHeader(), Map.of("version", version));
        List<HelpEntry> matching =
                HelpEntry.matching(entries(ctx.messagesFor(sender.getSender()).getHelp()), query);
        if (matching.isEmpty()) {
            ctx.sendMessage(sender, m -> m.getCore().getHelpNoMatch(), Map.of());
            return;
        }
        List<HelpEntry> visible = matching.stream()
                .filter(entry -> entry.visibleTo(sender.getSender()))
                .toList();
        if (visible.isEmpty()) {
            ctx.sendMessage(sender, m -> m.getCore().getHelpNoCommands(), Map.of());
            return;
        }
        visible.forEach(entry -> ctx.sendMessage(
                sender,
                m -> m.getCore().getHelpEntryFormat(),
                Map.of("syntax", entry.syntax(), "description", entry.description())
        ));
    }

    private List<HelpEntry> entries(MessagesConfig.Help help) {
        return List.of(
                new HelpEntry("/jbg help [query]", help.getHelp()),
                new HelpEntry("/jbg version", help.getVersion(), Permissions.VERSION),
                new HelpEntry("/jbg reload", help.getReload(), Permissions.RELOAD),
                new HelpEntry("/jbg config [key] [value]", help.getConfig(), Permissions.CONFIG));
    }
}
