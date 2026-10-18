package com.jruk8.jblockglitch;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JBlockGlitchPluginTest {

    private ServerMock server;
    private JBlockGlitchPlugin plugin;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();

        // Load without enabling (unlike MockBukkit.load) so the hermetic
        // config below is in place before onEnable runs: no metrics
        // relocation tripwire, no update HTTP.
        plugin = (JBlockGlitchPlugin) server.getPluginManager().loadPlugin(JBlockGlitchPlugin.class);
        Path dataFolder = plugin.getDataFolder().toPath();
        Files.createDirectories(dataFolder);
        Files.writeString(dataFolder.resolve("config.yml"), """
                update-checker:
                  enabled: false
                toggle-anonymous-statistics: false
                """);

        server.getPluginManager().enablePlugin(plugin);
    }

    @AfterEach
    void tearDown() {
        server.getPluginManager().disablePlugins();
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    private void deleteRecursively(Path path) throws Exception {
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(entry -> entry.toFile().delete());
        }
    }

    @Test
    void reloadRegeneratesDeletedDataFolder() throws Exception {
        Path dataFolder = plugin.getDataFolder().toPath();
        assertTrue(Files.exists(dataFolder.resolve("config.yml")));
        assertTrue(Files.exists(dataFolder.resolve("messages.yml")));

        deleteRecursively(dataFolder);
        assertTrue(Files.notExists(dataFolder));

        // A full reload here would construct Metrics, whose relocation
        // tripwire cannot pass unshaded in tests, so the folder case
        // exercises the extracted step reload() runs first.
        plugin.ensureDefaultFiles();

        assertTrue(Files.exists(dataFolder.resolve("config.yml")));
        assertTrue(Files.exists(dataFolder.resolve("messages.yml")));
        assertEquals("medium", YamlConfiguration
                .loadConfiguration(dataFolder.resolve("config.yml").toFile())
                .getString("ghost-item.mode"));
    }

    @Test
    void reloadRegeneratesSingleMissingFile() throws Exception {
        Path messages = plugin.getDataFolder().toPath().resolve("messages.yml");
        Files.delete(messages);
        assertTrue(Files.notExists(messages));

        plugin.reload();

        assertTrue(Files.exists(messages));
    }
}
