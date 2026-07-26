package com.jruk8.jblockglitch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

final class ReloadCommand implements CommandExecutor {

    private final JBlockGlitchPlugin plugin;

    ReloadCommand(JBlockGlitchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jblockglitch.reload")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        plugin.reloadPluginConfiguration();
        sender.sendMessage("JBlockGlitch configuration and messages reloaded.");
        return true;
    }
}
