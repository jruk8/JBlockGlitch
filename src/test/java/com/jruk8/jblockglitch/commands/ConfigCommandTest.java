package com.jruk8.jblockglitch.commands;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import com.jruk8.jblockglitch.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigCommandTest {

    private JBlockGlitchPlugin plugin;
    private MessageService messages;
    private ConfigCommand command;
    private CommandSender sender;
    private Command bukkitCommand;
    private YamlConfiguration config;

    @BeforeEach
    void setUp() {
        config = new YamlConfiguration();
        config.set("text-format", "minimessage");
        config.set("toggle-anonymous-statistics", true);
        config.set("protected-block.enabled", true);
        config.set("protected-block.mode", "strict");
        config.set("ghost-item.enabled", true);
        config.set("ghost-item.mode", "medium");
        config.set("ghost-item.resync-tick-interval", 5);
        config.set("update-checker.enabled", true);
        config.set("update-checker.releases.major", true);
        config.set("update-checker.releases.minor", true);
        config.set("update-checker.releases.hotfix", true);

        plugin = mock(JBlockGlitchPlugin.class);
        when(plugin.getConfig()).thenReturn(config);
        messages = mock(MessageService.class);
        command = new ConfigCommand(plugin, messages);

        sender = mock(CommandSender.class);
        when(sender.hasPermission(ConfigCommand.PERMISSION)).thenReturn(true);
        bukkitCommand = mock(Command.class);
    }

    @Test
    void noArgsListsAllKeys() {
        command.onCommand(sender, bukkitCommand, "config", new String[0]);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(messages).sendConfigList(eq(sender), captor.capture());
        assertEquals(11, captor.getValue().size());
        assertEquals("medium", captor.getValue().get("ghost-item.mode"));
        assertEquals("5", captor.getValue().get("ghost-item.resync-tick-interval"));
    }

    @Test
    void oneArgShowsValue() {
        command.onCommand(sender, bukkitCommand, "config", new String[]{"ghost-item.mode"});

        verify(messages).sendConfigValue(sender, "ghost-item.mode", "medium");
    }

    @Test
    void unknownKeySendsError() {
        command.onCommand(sender, bukkitCommand, "config", new String[]{"nope"});

        verify(messages).sendConfigUnknownKey(eq(sender), eq("nope"), any(String.class));
        verify(plugin, never()).saveConfig();
    }

    @Test
    void setBooleanSavesAndApplies() {
        command.onCommand(sender, bukkitCommand, "config", new String[]{"ghost-item.enabled", "false"});

        assertEquals(false, config.getBoolean("ghost-item.enabled"));
        verify(plugin).saveConfig();
        verify(plugin).reload();
        verify(messages).sendConfigUpdated(sender, "ghost-item.enabled", "false");
    }

    @Test
    void setInvalidValueSendsErrorWithoutSaving() {
        command.onCommand(sender, bukkitCommand, "config", new String[]{"ghost-item.mode", "extreme"});

        verify(messages).sendConfigInvalidValue(eq(sender), eq("ghost-item.mode"), eq("extreme"), any(String.class));
        verify(plugin, never()).saveConfig();
        verify(plugin, never()).reload();
        assertEquals("medium", config.getString("ghost-item.mode"));
    }

    @Test
    void setIntervalRejectsBelowMinusOne() {
        command.onCommand(sender, bukkitCommand, "config",
                new String[]{"ghost-item.resync-tick-interval", "-2"});

        verify(messages).sendConfigInvalidValue(any(), any(), any(), any());
        verify(plugin, never()).saveConfig();

        command.onCommand(sender, bukkitCommand, "config",
                new String[]{"ghost-item.resync-tick-interval", "10"});

        assertEquals(10, config.getInt("ghost-item.resync-tick-interval"));
        verify(plugin).saveConfig();
    }

    @Test
    void tabCompleteSuggestsKeysThenValues() {
        List<String> keys = command.onTabComplete(sender, bukkitCommand, "config", new String[]{"ghost"});

        assertTrue(keys.contains("ghost-item.enabled"), keys.toString());
        assertTrue(keys.contains("ghost-item.mode"), keys.toString());
        assertTrue(keys.contains("ghost-item.resync-tick-interval"), keys.toString());

        List<String> modes = command.onTabComplete(sender, bukkitCommand, "config",
                new String[]{"ghost-item.mode", ""});

        assertEquals(List.of("medium", "hard", "brute-force"), modes);
    }

    @Test
    void withoutPermissionSendsNoPermission() {
        when(sender.hasPermission(ConfigCommand.PERMISSION)).thenReturn(false);

        command.onCommand(sender, bukkitCommand, "config", new String[0]);

        verify(messages).sendNoPermission(sender);
        assertTrue(command.onTabComplete(sender, bukkitCommand, "config", new String[]{""}).isEmpty());
    }
}
