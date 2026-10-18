package com.jruk8.jblockglitch;

import java.io.File;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads and sends player-facing messages from messages.yml.
 * Supports both MiniMessage and legacy {@code &} color codes.
 */
public final class MessageService {

    private static final String DEFAULT_PREFIX = "<dark_gray>[<aqua>JBlockGlitch<dark_gray>] ";

    private final JavaPlugin plugin;
    private String prefix = DEFAULT_PREFIX;
    private List<String> helpLines = List.of();
    private String noPermissionMessage = "";
    private String reloadSuccessMessage = "";
    private TextFormat textFormat = TextFormat.MINIMESSAGE;

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** The raw configured prefix, before text-format parsing. */
    public String prefix() {
        return prefix;
    }

    public void sendHelp(CommandSender sender) {
        for (String line : helpLines) {
            sender.sendMessage(deserialize(line));
        }
    }

    public void sendNoPermission(CommandSender sender) {
        sender.sendMessage(deserialize(noPermissionMessage));
    }

    public void sendReloadSuccess(CommandSender sender) {
        sender.sendMessage(deserialize(reloadSuccessMessage));
    }

    public void sendUpdateAvailable(CommandSender sender, String detail) {
        sender.sendMessage(deserialize(prefix + detail));
    }

    public void load() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        prefix = messages.getString("prefix", DEFAULT_PREFIX);
        helpLines = messages.getStringList("help-message");
        noPermissionMessage = messages.getString("no-permission", "");
        reloadSuccessMessage = messages.getString("reload-success", "");

        String configuredFormat = plugin.getConfig().getString("text-format", "minimessage");
        try {
            textFormat = TextFormat.valueOf(configuredFormat.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            textFormat = TextFormat.MINIMESSAGE;
            plugin.getLogger().warning("Unknown text-format '" + configuredFormat
                    + "'; using minimessage.");
        }
    }

    private Component deserialize(String message) {
        String rendered = message.replace("{prefix}", prefix);
        if (textFormat == TextFormat.LEGACY) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(rendered);
        }
        return MiniMessage.miniMessage().deserialize(rendered);
    }

    private enum TextFormat {
        LEGACY,
        MINIMESSAGE
    }
}