package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.config.PluginConfig;
import com.jruk8.jblockglitch.core.Reloadable;

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
public final class ModeService implements Reloadable {

    public enum ProtectedBlockMode {
        MEDIUM,
        STRICT
    }

    public enum GhostItemMode {
        MEDIUM,
        HARD,
        BRUTE_FORCE
    }

    private final PluginConfig pluginConfig;

    private boolean protectedBlockEnabled = true;
    private ProtectedBlockMode protectedBlockMode = ProtectedBlockMode.STRICT;

    private boolean ghostItemEnabled = true;
    private GhostItemMode ghostItemMode = GhostItemMode.MEDIUM;
    private int ghostItemResyncTickInterval = 5;

    public ModeService(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        reload();
    }

    @Override
    public void reload() {
        protectedBlockEnabled = pluginConfig.getProtectedBlock().isEnabled();
        protectedBlockMode = parseProtectedBlockMode();

        ghostItemEnabled = pluginConfig.getGhostItem().isEnabled();
        ghostItemMode = parseGhostItemMode();
        ghostItemResyncTickInterval = pluginConfig.getGhostItem().getResyncTickInterval();
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
        String configured = pluginConfig.getProtectedBlock().getMode();

        return switch (configured.toLowerCase(Locale.ROOT)) {
            case "medium" -> ProtectedBlockMode.MEDIUM;
            default -> ProtectedBlockMode.STRICT;
        };
    }

    private GhostItemMode parseGhostItemMode() {
        String configured = pluginConfig.getGhostItem().getMode();

        return switch (configured.toLowerCase(Locale.ROOT)) {
            case "hard" -> GhostItemMode.HARD;
            case "brute-force" -> GhostItemMode.BRUTE_FORCE;
            default -> GhostItemMode.MEDIUM;
        };
    }
}
