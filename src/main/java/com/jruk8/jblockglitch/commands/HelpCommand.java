package com.jruk8.jblockglitch.commands;

import java.util.Collections;
import java.util.List;

import com.jruk8.jblockglitch.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

final class HelpCommand implements CommandExecutor, TabCompleter {

    private final MessageService messageService;

    HelpCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jblockglitch.help")) {
            messageService.sendNoPermission(sender);
            return true;
        }

        messageService.sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}