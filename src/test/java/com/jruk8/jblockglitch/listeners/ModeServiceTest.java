package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.config.PluginConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeServiceTest {

    private PluginConfig pluginConfig;
    private ModeService modes;

    @BeforeEach
    void setUp() {
        pluginConfig = new PluginConfig();
        modes = new ModeService(pluginConfig);
    }

    @Test
    @DisplayName("should read the schema defaults")
    void shouldReadDefaults() {
        assertTrue(modes.protectedBlockEnabled());
        assertEquals(ModeService.ProtectedBlockMode.STRICT, modes.protectedBlockMode());
        assertTrue(modes.ghostItemEnabled());
        assertEquals(ModeService.GhostItemMode.MEDIUM, modes.ghostItemMode());
        assertEquals(5, modes.ghostItemResyncTickInterval());
    }

    @Test
    @DisplayName("should parse protected-block modes case-insensitively")
    void shouldParseProtectedBlockModes() {
        pluginConfig.getProtectedBlock().setMode("MEDIUM");
        modes.reload();

        assertEquals(ModeService.ProtectedBlockMode.MEDIUM, modes.protectedBlockMode());
    }

    @Test
    @DisplayName("should fall back to strict on unknown protected-block modes")
    void shouldFallBackToStrict() {
        pluginConfig.getProtectedBlock().setMode("extreme");
        modes.reload();

        assertEquals(ModeService.ProtectedBlockMode.STRICT, modes.protectedBlockMode());
    }

    @Test
    @DisplayName("should parse every ghost-item mode")
    void shouldParseGhostItemModes() {
        pluginConfig.getGhostItem().setMode("hard");
        modes.reload();
        assertEquals(ModeService.GhostItemMode.HARD, modes.ghostItemMode());

        pluginConfig.getGhostItem().setMode("brute-force");
        modes.reload();
        assertEquals(ModeService.GhostItemMode.BRUTE_FORCE, modes.ghostItemMode());
    }

    @Test
    @DisplayName("should fall back to medium on unknown ghost-item modes")
    void shouldFallBackToMedium() {
        pluginConfig.getGhostItem().setMode("extreme");
        modes.reload();

        assertEquals(ModeService.GhostItemMode.MEDIUM, modes.ghostItemMode());
    }

    @Test
    @DisplayName("should follow toggles and the resync interval on reload")
    void shouldFollowTogglesOnReload() {
        pluginConfig.getProtectedBlock().setEnabled(false);
        pluginConfig.getGhostItem().setEnabled(false);
        pluginConfig.getGhostItem().setResyncTickInterval(-1);
        modes.reload();

        assertEquals(false, modes.protectedBlockEnabled());
        assertEquals(false, modes.ghostItemEnabled());
        assertEquals(-1, modes.ghostItemResyncTickInterval());
    }
}
