package com.jruk8.jblockglitch.command.admin.config;

import com.jruk8.jblockglitch.command.CommandContext;
import com.jruk8.jblockglitch.command.Permissions;
import com.jruk8.jblockglitch.config.PluginConfig;
import com.jruk8.jblockglitch.core.Reloader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Views and changes config.yml values in-game. {@code /jbg config} lists
 * every tunable key, {@code /jbg config <key>} shows one value, and
 * {@code /jbg config <key> <value>} validates, saves, and applies it live.
 */
public class ConfigCommand {

    private static final List<String> BOOLEAN_VALUES = List.of("true", "false");
    private static final List<String> INTERVAL_SUGGESTIONS = List.of("-1", "1", "5", "10", "20");

    private final Reloader reloader;
    private final CommandContext ctx;
    private final PluginConfig pluginConfig;

    public ConfigCommand(Reloader reloader, CommandContext ctx, PluginConfig pluginConfig) {
        this.reloader = reloader;
        this.ctx = ctx;
        this.pluginConfig = pluginConfig;
    }

    /**
     * Builds the {@code config} Brigadier node, attached to the {@code jblockglitch} root by the registrar.
     *
     * @return the configured subcommand node
     */
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("config")
                .requires(CommandContext.requiresPermission(Permissions.CONFIG))
                .executes(context -> {
                    showAll(context.getSource());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("key", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (String key : completeKeys(builder.getRemainingLowerCase())) {
                                builder.suggest(key);
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            showValue(context.getSource(), StringArgumentType.getString(context, "key"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    for (String value : completeValues(context, builder.getRemainingLowerCase())) {
                                        builder.suggest(value);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    setValue(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "key"),
                                            StringArgumentType.getString(context, "value"));
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    public void showAll(CommandSourceStack sender) {
        ctx.playNeutral(sender);
        ctx.sendMessage(sender, m -> m.getConfig().getHeader(), Map.of());
        for (ConfigKey key : ConfigKeys.ALL) {
            var tags = Map.of("key", key.path(), "value", currentValue(key));
            ctx.sendMessage(sender, m -> m.getConfig().getEntry(), tags);
        }
    }

    public void showValue(CommandSourceStack sender, String rawKey) {
        ConfigKey key = findKey(rawKey);
        if (key == null) {
            ctx.error(sender, m -> m.getConfig().getUnknownKey(),
                    Map.of("key", rawKey, "keys", validKeys()));
            return;
        }
        ctx.send(sender, m -> m.getConfig().getEntry(),
                Map.of("key", key.path(), "value", currentValue(key)));
    }

    public void setValue(CommandSourceStack sender, String rawKey, String rawValue) {
        ConfigKey key = findKey(rawKey);
        if (key == null) {
            ctx.error(sender, m -> m.getConfig().getUnknownKey(),
                    Map.of("key", rawKey, "keys", validKeys()));
            return;
        }
        Object parsed = parse(key, rawValue.strip());
        if (parsed == null) {
            ctx.error(sender, m -> m.getConfig().getInvalidValue(),
                    Map.of("key", key.path(), "value", rawValue.strip(), "allowed", key.allowedText()));
            return;
        }

        key.write().accept(pluginConfig, parsed);
        pluginConfig.save();
        reloader.reloadAll();
        ctx.send(sender, m -> m.getConfig().getUpdated(),
                Map.of("key", key.path(), "value", String.valueOf(parsed)));
    }

    private String currentValue(ConfigKey key) {
        Object value = key.read().apply(pluginConfig);
        return value == null ? "<unset>" : value.toString();
    }

    private static List<String> completeValues(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, String prefix) {
        ConfigKey key;
        try {
            key = findKey(StringArgumentType.getString(context, "key"));
        } catch (IllegalArgumentException missing) {
            return List.of();
        }
        if (key == null) {
            return List.of();
        }
        return completeValues(key, prefix);
    }

    static List<String> completeKeys(String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return ConfigKeys.ALL.stream()
                .map(ConfigKey::path)
                .filter(path -> path.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    static List<String> completeValues(ConfigKey key, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return key.suggestions().stream()
                .filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    static String validKeys() {
        return String.join(", ", ConfigKeys.ALL.stream().map(ConfigKey::path).toList());
    }

    static ConfigKey findKey(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT).trim();
        for (ConfigKey key : ConfigKeys.ALL) {
            if (key.path().toLowerCase(Locale.ROOT).equals(normalized)) {
                return key;
            }
        }
        return null;
    }

    static Object parse(ConfigKey key, String rawValue) {
        return switch (key.kind()) {
            case BOOLEAN -> parseBoolean(rawValue);
            case ENUM -> parseEnum(key, rawValue);
            case INTEGER -> parseInterval(rawValue);
        };
    }

    private static Boolean parseBoolean(String rawValue) {
        if (rawValue.equalsIgnoreCase("true")) {
            return true;
        }
        if (rawValue.equalsIgnoreCase("false")) {
            return false;
        }
        return null;
    }

    private static String parseEnum(ConfigKey key, String rawValue) {
        String normalized = rawValue.toLowerCase(Locale.ROOT);
        return key.suggestions().contains(normalized) ? normalized : null;
    }

    private static Integer parseInterval(String rawValue) {
        try {
            int value = Integer.parseInt(rawValue);
            return value >= -1 ? value : null;
        } catch (NumberFormatException invalid) {
            return null;
        }
    }

    enum Kind {
        BOOLEAN,
        ENUM,
        INTEGER
    }

    record ConfigKey(
            String path,
            Kind kind,
            List<String> suggestions,
            String allowedText,
            Function<PluginConfig, Object> read,
            BiConsumer<PluginConfig, Object> write
    ) {
    }

    private static final class ConfigKeys {
        private static final List<ConfigKey> ALL = List.of(
                new ConfigKey("metrics.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getMetrics().isEnabled(),
                        (config, value) -> config.getMetrics().setEnabled((Boolean) value)),
                new ConfigKey("protectedBlock.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getProtectedBlock().isEnabled(),
                        (config, value) -> config.getProtectedBlock().setEnabled((Boolean) value)),
                new ConfigKey("protectedBlock.mode", Kind.ENUM,
                        List.of("medium", "strict"), "medium, strict",
                        config -> config.getProtectedBlock().getMode(),
                        (config, value) -> config.getProtectedBlock().setMode((String) value)),
                new ConfigKey("ghostItem.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getGhostItem().isEnabled(),
                        (config, value) -> config.getGhostItem().setEnabled((Boolean) value)),
                new ConfigKey("ghostItem.mode", Kind.ENUM,
                        List.of("medium", "hard", "brute-force"), "medium, hard, brute-force",
                        config -> config.getGhostItem().getMode(),
                        (config, value) -> config.getGhostItem().setMode((String) value)),
                new ConfigKey("ghostItem.resyncTickInterval", Kind.INTEGER,
                        INTERVAL_SUGGESTIONS, "an integer >= -1 (-1 disables)",
                        config -> config.getGhostItem().getResyncTickInterval(),
                        (config, value) -> config.getGhostItem().setResyncTickInterval((Integer) value)),
                new ConfigKey("updateChecker.enabled", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getUpdateChecker().isEnabled(),
                        (config, value) -> config.getUpdateChecker().setEnabled((Boolean) value)),
                new ConfigKey("updateChecker.releases.major", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getUpdateChecker().getReleases().isMajor(),
                        (config, value) -> config.getUpdateChecker().getReleases().setMajor((Boolean) value)),
                new ConfigKey("updateChecker.releases.minor", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getUpdateChecker().getReleases().isMinor(),
                        (config, value) -> config.getUpdateChecker().getReleases().setMinor((Boolean) value)),
                new ConfigKey("updateChecker.releases.hotfix", Kind.BOOLEAN,
                        BOOLEAN_VALUES, "true, false",
                        config -> config.getUpdateChecker().getReleases().isHotfix(),
                        (config, value) -> config.getUpdateChecker().getReleases().setHotfix((Boolean) value)));

        private ConfigKeys() {
        }
    }
}
