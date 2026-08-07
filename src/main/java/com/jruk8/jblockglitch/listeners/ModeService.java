package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;

import java.util.Locale;

/**
 * Reads and caches the detection modes from config.yml.
 * <p>
 * Two independent modes exist:
 * <ul>
 *   <li>{@link ProtectedBlockMode} — how denied block placements are handled.</li>
 *   <li>{@link GhostItemMode} — how ghost item attempts are detected.</li>
 * </ul>
 */
public final class ModeService {

    public enum ProtectedBlockMode {
        MEDIUM,
        STRICT
    }

    public enum GhostItemMode {
        MEDIUM,
        HARD,
        BRUTE_FORCE
    }

    private final JBlockGlitchPlugin plugin;

    private boolean protectedBlockEnabled = true;
    private ProtectedBlockMode protectedBlockMode = ProtectedBlockMode.STRICT;

    private boolean ghostItemEnabled = true;
    private GhostItemMode ghostItemMode = GhostItemMode.MEDIUM;
    private int ghostItemResyncTickInterval = 5;

    public ModeService(JBlockGlitchPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        protectedBlockEnabled = plugin.getConfig().getBoolean(
                "protected-block.enabled",
                true
        );
        protectedBlockMode = parseProtectedBlockMode();

        ghostItemEnabled = plugin.getConfig().getBoolean(
                "ghost-item.enabled",
                true
        );
        ghostItemMode = parseGhostItemMode();
        ghostItemResyncTickInterval = parseGhostItemResyncTickInterval();
    }

    public boolean protectedBlockEnabled() {
        return protectedBlockEnabled;
    }

    public ProtectedBlockMode protectedBlockMode() {
        return protectedBlockMode;
    }

    public boolean ghostItemEnabled() {
        return ghostItemEnabled;
    }

    public GhostItemMode ghostItemMode() {
        return ghostItemMode;
    }

    public int ghostItemResyncTickInterval() {
        return ghostItemResyncTickInterval;
    }

    private ProtectedBlockMode parseProtectedBlockMode() {
        String configured = plugin.getConfig().getString(
                "protected-block.mode",
                "strict"
        );

        return switch (configured.toLowerCase(Locale.ROOT)) {
            case "medium" -> ProtectedBlockMode.MEDIUM;
            default -> ProtectedBlockMode.STRICT;
        };
    }

    private GhostItemMode parseGhostItemMode() {
        String configured = plugin.getConfig().getString(
                "ghost-item.mode",
                "medium"
        );

        return switch (configured.toLowerCase(Locale.ROOT)) {
            case "hard" -> GhostItemMode.HARD;
            case "brute-force" -> GhostItemMode.BRUTE_FORCE;
            default -> GhostItemMode.MEDIUM;
        };
    }

    private int parseGhostItemResyncTickInterval() {
        return plugin.getConfig().getInt(
                "ghost-item.resync-tick-interval",
                5
        );
    }
}