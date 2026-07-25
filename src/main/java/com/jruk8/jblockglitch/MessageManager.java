package com.jruk8.jblockglitch;

import java.io.File;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

final class MessageManager {

    private final JavaPlugin plugin;
    private List<String> helpLines = List.of();
    private TextFormat textFormat = TextFormat.MINIMESSAGE;

    MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void load() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        helpLines = messages.getStringList("help-message");

        String configuredFormat = plugin.getConfig().getString("text-format", "minimessage");
        try {
            textFormat = TextFormat.valueOf(configuredFormat.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            textFormat = TextFormat.MINIMESSAGE;
            plugin.getLogger().warning("Unknown text-format '" + configuredFormat
                    + "'; using minimessage.");
        }
    }

    void sendHelp(org.bukkit.command.CommandSender sender) {
        for (String line : helpLines) {
            sender.sendMessage(deserialize(line));
        }
    }

    private Component deserialize(String message) {
        if (textFormat == TextFormat.LEGACY) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        }
        return MiniMessage.miniMessage().deserialize(message);
    }

    private enum TextFormat {
        LEGACY,
        MINIMESSAGE
    }
}
