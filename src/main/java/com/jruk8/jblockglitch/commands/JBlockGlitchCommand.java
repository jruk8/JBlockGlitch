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
    private final ConfigCommand configCommand;

    JBlockGlitchCommand(HelpCommand helpCommand, ReloadCommand reloadCommand, ConfigCommand configCommand) {
        this.helpCommand = helpCommand;
        this.reloadCommand = reloadCommand;
        this.configCommand = configCommand;
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
            case "config" -> configCommand.onCommand(sender, command, label, subArgs);
            default -> helpCommand.onCommand(sender, command, label, args);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return completeSubcommands(sender, args[0]);
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("config")) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            return configCommand.onTabComplete(sender, command, alias, subArgs);
        }

        return List.of();
    }

    private List<String> completeSubcommands(CommandSender sender, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("jblockglitch.help") && "help".startsWith(normalized)) {
            completions.add("help");
        }
        if (sender.hasPermission("jblockglitch.reload") && "reload".startsWith(normalized)) {
            completions.add("reload");
        }
        if (sender.hasPermission(ConfigCommand.PERMISSION) && "config".startsWith(normalized)) {
            completions.add("config");
        }
        return completions;
    }
}