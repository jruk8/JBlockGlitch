package com.jruk8.jblockglitch.commands;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import com.jruk8.jblockglitch.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Views and changes config.yml values in-game. {@code /jbg config} lists
 * every tunable key, {@code /jbg config <key>} shows one value, and
 * {@code /jbg config <key> <value>} validates, saves, and applies it live.
 */
final class ConfigCommand implements CommandExecutor, TabCompleter {

    static final String PERMISSION = "jblockglitch.config";

    private static final List<String> BOOLEAN_VALUES = List.of("true", "false");
    private static final List<String> INTERVAL_SUGGESTIONS = List.of("-1", "1", "5", "10", "20");

    private final JBlockGlitchPlugin plugin;
    private final MessageService messageService;

    ConfigCommand(JBlockGlitchPlugin plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            messageService.sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            showAll(sender);
            return true;
        }

        if (args.length > 2) {
            messageService.sendConfigUsage(sender);
            return true;
        }

        ConfigKey key = findKey(args[0]);
        if (key == null) {
            messageService.sendConfigUnknownKey(sender, args[0], validKeys());
            return true;
        }

        if (args.length == 1) {
            messageService.sendConfigValue(sender, key.path(), currentValue(key));
            return true;
        }

        setValue(sender, key, args[1]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return completeKeys(args[0]);
        }

        if (args.length == 2) {
            ConfigKey key = findKey(args[0]);
            return key == null ? List.of() : completeValues(key, args[1]);
        }

        return List.of();
    }

    private void showAll(CommandSender sender) {
        Map<String, String> entries = new LinkedHashMap<>();
        for (ConfigKey key : ConfigKeys.ALL) {
            entries.put(key.path(), currentValue(key));
        }
        messageService.sendConfigList(sender, entries);
    }

    private void setValue(CommandSender sender, ConfigKey key, String rawValue) {
        Object parsed = parse(key, rawValue);
        if (parsed == null) {
            messageService.sendConfigInvalidValue(sender, key.path(), rawValue, key.allowedText());
            return;
        }

        plugin.getConfig().set(key.path(), parsed);
        plugin.saveConfig();
        plugin.reload();
        messageService.sendConfigUpdated(sender, key.path(), String.valueOf(parsed));
    }

    private String currentValue(ConfigKey key) {
        Object value = plugin.getConfig().get(key.path());
        return value == null ? "<unset>" : value.toString();
    }

    private String validKeys() {
        return String.join(", ", ConfigKeys.ALL.stream().map(ConfigKey::path).toList());
    }

    private List<String> completeKeys(String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (ConfigKey key : ConfigKeys.ALL) {
            if (key.path().startsWith(normalized)) {
                matches.add(key.path());
            }
        }
        return matches;
    }

    private List<String> completeValues(ConfigKey key, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String candidate : key.suggestions()) {
            if (candidate.startsWith(normalized)) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    private ConfigKey findKey(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT).trim();
        for (ConfigKey key : ConfigKeys.ALL) {
            if (key.path().equals(normalized)) {
                return key;
            }
        }
        return null;
    }

    private Object parse(ConfigKey key, String rawValue) {
        return switch (key.kind()) {
            case BOOLEAN -> parseBoolean(rawValue);
            case ENUM -> parseEnum(key, rawValue);
            case INTEGER -> parseInterval(rawValue);
        };
    }

    private Boolean parseBoolean(String rawValue) {
        if (rawValue.equalsIgnoreCase("true")) {
            return true;
        }
        if (rawValue.equalsIgnoreCase("false")) {
            return false;
        }
        return null;
    }

    private String parseEnum(ConfigKey key, String rawValue) {
        String normalized = rawValue.toLowerCase(Locale.ROOT);
        return key.suggestions().contains(normalized) ? normalized : null;
    }

    private Integer parseInterval(String rawValue) {
        try {
            int value = Integer.parseInt(rawValue);
            return value >= -1 ? value : null;
        } catch (NumberFormatException invalid) {
            return null;
        }
    }

    private enum Kind {
        BOOLEAN,
        ENUM,
        INTEGER
    }

    private record ConfigKey(String path, Kind kind, List<String> suggestions, String allowedText) {
    }

    private static final class ConfigKeys {
        private static final List<ConfigKey> ALL = List.of(
                new ConfigKey("text-format", Kind.ENUM,
                        List.of("minimessage", "legacy"), "minimessage, legacy"),
                new ConfigKey("toggle-anonymous-statistics", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"),
                new ConfigKey("protected-block.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"),
                new ConfigKey("protected-block.mode", Kind.ENUM,
                        List.of("medium", "strict"), "medium, strict"),
                new ConfigKey("ghost-item.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"),
                new ConfigKey("ghost-item.mode", Kind.ENUM,
                        List.of("medium", "hard", "brute-force"), "medium, hard, brute-force"),
                new ConfigKey("ghost-item.resync-tick-interval", Kind.INTEGER,
                        INTERVAL_SUGGESTIONS, "an integer >= -1 (-1 disables)"),
                new ConfigKey("update-checker.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"),
                new ConfigKey("update-checker.releases.major", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"),
                new ConfigKey("update-checker.releases.minor", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"),
                new ConfigKey("update-checker.releases.hotfix", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false"));

        private ConfigKeys() {
        }
    }
}
