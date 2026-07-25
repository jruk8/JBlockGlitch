package com.jruk8.jblockglitch;

import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class JBlockGlitchPlugin extends JavaPlugin {

    private MessageManager messageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        messageManager = new MessageManager(this);
        messageManager.load();

        HelpCommand helpCommand = new HelpCommand(messageManager);
        Objects.requireNonNull(getCommand("help")).setExecutor(helpCommand);
        Objects.requireNonNull(getCommand("help")).setTabCompleter(helpCommand);

        getServer().getPluginManager().registerEvents(new BlockGlitchListener(), this);
    }
}
