package com.jruk8.jblockglitch;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads and sends player-facing messages from messages.yml.
 * Supports both MiniMessage and legacy {@code &} color codes.
 */
public final class MessageService {

    private static final String DEFAULT_PREFIX = "<dark_gray>[<{brand-col}>JBlockGlitch<dark_gray>] ";
    private static final String DEFAULT_BRAND_COLOR = "#5adbc8";
    private static final String DEFAULT_CONFIG_HEADER = "{prefix}<{brand-col}>Current configuration:";
    private static final String DEFAULT_CONFIG_ENTRY = "\n{prefix}<gray>» {key}<dark_gray>: <white>{value}";
    private static final String DEFAULT_CONFIG_UPDATED =
            "{prefix}<green>Set <white>{key}<green> to <white>{value}<green>.";
    private static final String DEFAULT_CONFIG_UNKNOWN_KEY =
            "{prefix}<red>Unknown config key '<white>{key}<red>'. Valid keys: <white>{keys}<red>.";
    private static final String DEFAULT_CONFIG_INVALID_VALUE =
            "{prefix}<red>Invalid value '<white>{value}<red>' for <white>{key}<red>. Allowed: <white>{allowed}<red>.";
    private static final String DEFAULT_CONFIG_USAGE = "{prefix}<gray>» <white>/jbg config [key] [value]";

    private final JavaPlugin plugin;
    private String prefix = DEFAULT_PREFIX;
    private String brandColor = DEFAULT_BRAND_COLOR;
    private List<String> helpLines = List.of();
    private String noPermissionMessage = "";
    private String reloadSuccessMessage = "";
    private String configHeaderMessage = DEFAULT_CONFIG_HEADER;
    private String configEntryMessage = DEFAULT_CONFIG_ENTRY;
    private String configUpdatedMessage = DEFAULT_CONFIG_UPDATED;
    private String configUnknownKeyMessage = DEFAULT_CONFIG_UNKNOWN_KEY;
    private String configInvalidValueMessage = DEFAULT_CONFIG_INVALID_VALUE;
    private String configUsageMessage = DEFAULT_CONFIG_USAGE;
    private TextFormat textFormat = TextFormat.MINIMESSAGE;
    private SoundConfig neutralSound = defaultNeutralSound();
    private SoundConfig angrySound = defaultAngrySound();

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
        playSound(sender, neutralSound);
    }

    public void sendNoPermission(CommandSender sender) {
        sender.sendMessage(deserialize(noPermissionMessage));
        playSound(sender, angrySound);
    }

    public void sendReloadSuccess(CommandSender sender) {
        sender.sendMessage(deserialize(reloadSuccessMessage));
        playSound(sender, neutralSound);
    }

    public void sendUpdateAvailable(CommandSender sender, String detail) {
        sender.sendMessage(deserialize(prefix + detail));
        playSound(sender, neutralSound);
    }

    public void sendConfigList(CommandSender sender, Map<String, String> entries) {
        sender.sendMessage(deserialize(configHeaderMessage));
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            sender.sendMessage(deserialize(fillPlaceholders(configEntryMessage,
                    Map.of("key", entry.getKey(), "value", entry.getValue()))));
        }
        playSound(sender, neutralSound);
    }

    public void sendConfigValue(CommandSender sender, String key, String value) {
        sender.sendMessage(deserialize(fillPlaceholders(configEntryMessage,
                Map.of("key", key, "value", value))));
        playSound(sender, neutralSound);
    }

    public void sendConfigUpdated(CommandSender sender, String key, String value) {
        sender.sendMessage(deserialize(fillPlaceholders(configUpdatedMessage,
                Map.of("key", key, "value", value))));
        playSound(sender, neutralSound);
    }

    public void sendConfigUnknownKey(CommandSender sender, String key, String validKeys) {
        sender.sendMessage(deserialize(fillPlaceholders(configUnknownKeyMessage,
                Map.of("key", key, "keys", validKeys))));
        playSound(sender, angrySound);
    }

    public void sendConfigInvalidValue(CommandSender sender, String key, String value, String allowed) {
        sender.sendMessage(deserialize(fillPlaceholders(configInvalidValueMessage,
                Map.of("key", key, "value", value, "allowed", allowed))));
        playSound(sender, angrySound);
    }

    public void sendConfigUsage(CommandSender sender) {
        sender.sendMessage(deserialize(configUsageMessage));
        playSound(sender, angrySound);
    }

    public void load() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        prefix = messages.getString("prefix", DEFAULT_PREFIX);
        brandColor = messages.getString("brand-color", DEFAULT_BRAND_COLOR);
        helpLines = messages.getStringList("help-message");
        noPermissionMessage = messages.getString("no-permission", "");
        reloadSuccessMessage = messages.getString("reload-success", "");
        configHeaderMessage = messages.getString("config-header", DEFAULT_CONFIG_HEADER);
        configEntryMessage = messages.getString("config-entry", DEFAULT_CONFIG_ENTRY);
        configUpdatedMessage = messages.getString("config-updated", DEFAULT_CONFIG_UPDATED);
        configUnknownKeyMessage = messages.getString("config-unknown-key", DEFAULT_CONFIG_UNKNOWN_KEY);
        configInvalidValueMessage = messages.getString("config-invalid-value", DEFAULT_CONFIG_INVALID_VALUE);
        configUsageMessage = messages.getString("config-usage", DEFAULT_CONFIG_USAGE);

        neutralSound = loadSound("sounds.neutral", defaultNeutralSound());
        angrySound = loadSound("sounds.angry", defaultAngrySound());

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
        // Prefix first: the prefix itself may carry {brand-col}.
        String rendered = message.replace("{prefix}", prefix).replace("{brand-col}", brandColor);
        if (textFormat == TextFormat.LEGACY) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(rendered);
        }
        return MiniMessage.miniMessage().deserialize(rendered);
    }

    private String fillPlaceholders(String message, Map<String, String> values) {
        String rendered = message;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return rendered;
    }

    private SoundConfig loadSound(String path, SoundConfig fallback) {
        String name = plugin.getConfig().getString(path + ".sound", fallback.sound());
        float volume = (float) plugin.getConfig().getDouble(path + ".volume", fallback.volume());
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", fallback.pitch());
        return new SoundConfig(name, volume, pitch);
    }

    private static SoundConfig defaultNeutralSound() {
        return new SoundConfig("block.chain.step", 0.9F, 1.2F);
    }

    private static SoundConfig defaultAngrySound() {
        return new SoundConfig("block.chain.break", 0.7F, 0.8F);
    }

    private void playSound(CommandSender sender, SoundConfig sound) {
        if (!(sender instanceof Player player)) {
            return;
        }

        try {
            player.playSound(Sound.sound(
                    Key.key(sound.sound()), Sound.Source.MASTER, sound.volume(), sound.pitch()));
        } catch (IllegalArgumentException invalid) {
            plugin.getLogger().warning("Unknown sound '" + sound.sound() + "'; skipping.");
        }
    }

    private record SoundConfig(String sound, float volume, float pitch) {
    }

    private enum TextFormat {
        LEGACY,
        MINIMESSAGE
    }
}