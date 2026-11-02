package com.jruk8.jblockglitch.command.admin.config;

import com.jruk8.jblockglitch.command.CommandContext;
import com.jruk8.jblockglitch.config.PluginConfig;
import com.jruk8.jblockglitch.core.Reloader;
import com.jruk8.jblockglitch.message.MessagesConfig;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConfigCommandTest {

    private static ServerMock server;

    private final MessagesConfig messages = new MessagesConfig();

    private Reloader reloader;
    private CommandContext ctx;
    private CommandSourceStack source;
    private PluginConfig pluginConfig;
    private ConfigCommand command;

    @BeforeAll
    static void startServer() {
        server = MockBukkit.mock();
    }

    @AfterAll
    static void stopServer() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUp(@TempDir Path dir) {
        pluginConfig = ConfigManager.create(PluginConfig.class, it -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(dir.resolve("config.yml").toFile());
            it.saveDefaults();
            it.load(true);
        });
        reloader = mock(Reloader.class);
        ctx = mock(CommandContext.class);
        source = mock(CommandSourceStack.class);
        command = new ConfigCommand(reloader, ctx, pluginConfig);
    }

    @Test
    @DisplayName("should build the Brigadier node without errors")
    void shouldBuildNode() {
        assertDoesNotThrow(() -> command.node());
    }

    @Test
    @DisplayName("should list the header plus every key")
    void shouldListAllKeys() {
        command.showAll(source);

        var selectorCaptor = ArgumentCaptor.forClass(Function.class);
        var tagsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx, times(11)).send(eq(source), selectorCaptor.capture(), tagsCaptor.capture());
        List<String> resolved = selectorCaptor.getAllValues().stream()
                .map(selector -> ((Function<MessagesConfig, String>) selector).apply(messages))
                .toList();
        assertEquals(messages.getConfig().getHeader(), resolved.get(0));
        assertEquals(10, resolved.stream()
                .filter(messages.getConfig().getEntry()::equals)
                .count());
        List<Map<String, String>> tags = tagsCaptor.getAllValues().stream()
                .map(map -> (Map<String, String>) map)
                .toList();
        assertEquals("medium", tags.stream()
                .filter(entry -> "ghostItem.mode".equals(entry.get("key")))
                .findFirst().orElseThrow().get("value"));
        assertEquals("5", tags.stream()
                .filter(entry -> "ghostItem.resyncTickInterval".equals(entry.get("key")))
                .findFirst().orElseThrow().get("value"));
    }

    @Test
    @DisplayName("should show one value")
    void shouldShowValue() {
        command.showValue(source, "ghostItem.mode");

        var tagsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).send(eq(source), selectorCaptor().capture(), tagsCaptor.capture());
        Map<String, String> tags = tagsCaptor.getValue();
        assertEquals("ghostItem.mode", tags.get("key"));
        assertEquals("medium", tags.get("value"));
    }

    @Test
    @DisplayName("should report an unknown key without touching the config")
    void shouldReportUnknownKey() {
        command.showValue(source, "nope");

        verify(ctx).error(eq(source), selectorCaptor().capture(), anyMap());
        verify(ctx, never()).send(eq(source), selectorCaptor().capture(), anyMap());
        verify(reloader, never()).reloadAll();
    }

    @Test
    @DisplayName("should save and apply a boolean change")
    void shouldSaveAndApplyBoolean() {
        command.setValue(source, "ghostItem.enabled", "false");

        assertFalse(pluginConfig.getGhostItem().isEnabled());
        verify(reloader).reloadAll();
        var tagsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).send(eq(source), selectorCaptor().capture(), tagsCaptor.capture());
        Map<String, String> tags = tagsCaptor.getValue();
        assertEquals("ghostItem.enabled", tags.get("key"));
        assertEquals("false", tags.get("value"));
    }

    @Test
    @DisplayName("should reject an invalid value without saving or reloading")
    void shouldRejectInvalidValue() {
        pluginConfig.getGhostItem().setMode("medium");

        command.setValue(source, "ghostItem.mode", "extreme");

        verify(ctx).error(eq(source), selectorCaptor().capture(), anyMap());
        verify(reloader, never()).reloadAll();
        assertEquals("medium", pluginConfig.getGhostItem().getMode());
    }

    @Test
    @DisplayName("should reject intervals below minus one")
    void shouldRejectLowInterval() {
        command.setValue(source, "ghostItem.resyncTickInterval", "-2");

        verify(ctx).error(eq(source), selectorCaptor().capture(), anyMap());
        verify(reloader, never()).reloadAll();

        command.setValue(source, "ghostItem.resyncTickInterval", "10");

        assertEquals(10, pluginConfig.getGhostItem().getResyncTickInterval());
        verify(reloader).reloadAll();
    }

    @Test
    @DisplayName("should match keys case-insensitively")
    void shouldMatchKeysCaseInsensitively() {
        assertEquals("ghostItem.mode", ConfigCommand.findKey("GHOSTITEM.MODE").path());
        assertNull(ConfigCommand.findKey("nope"));
    }

    @Test
    @DisplayName("should suggest keys then values by prefix")
    void shouldSuggestKeysThenValues() {
        List<String> keys = ConfigCommand.completeKeys("ghost");

        assertTrue(keys.contains("ghostItem.enabled"), keys.toString());
        assertTrue(keys.contains("ghostItem.mode"), keys.toString());
        assertTrue(keys.contains("ghostItem.resyncTickInterval"), keys.toString());

        assertEquals(
                List.of("medium", "hard", "brute-force"),
                ConfigCommand.completeValues(ConfigCommand.findKey("ghostItem.mode"), ""));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Function<MessagesConfig, String>> selectorCaptor() {
        return ArgumentCaptor.forClass(Function.class);
    }
}
