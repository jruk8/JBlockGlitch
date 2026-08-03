package com.jruk8.jblockglitch.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dispatches the {@code /jblockglitch} root command to its subcommands.
 */
public final class JBlockGlitchCommand implements CommandExecutor, TabCompleter {

    private final HelpCommand helpCommand;
    private final ReloadCommand reloadCommand;

    JBlockGlitchCommand(HelpCommand helpCommand, ReloadCommand reloadCommand) {
        this.helpCommand = helpCommand;
        this.reloadCommand = reloadCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return helpCommand.onCommand(sender, command, label, args);
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        return switch (subcommand) {
            case "help" -> helpCommand.onCommand(sender, command, label, subArgs);
            case "reload" -> reloadCommand.onCommand(sender, command, label, subArgs);
            default -> helpCommand.onCommand(sender, command, label, args);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("jblockglitch.help")) {
            completions.add("help");
        }
        if (sender.hasPermission("jblockglitch.reload")) {
            completions.add("reload");
        }
        return completions;
    }
}