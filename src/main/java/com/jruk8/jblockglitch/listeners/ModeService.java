package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;

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
    private ProtectedBlockMode protectedBlockMode = ProtectedBlockMode.STRICT;
    private GhostItemMode ghostItemMode = GhostItemMode.MEDIUM;

    public ModeService(JBlockGlitchPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        protectedBlockMode = parseProtectedBlockMode();
        ghostItemMode = parseGhostItemMode();
    }

    public ProtectedBlockMode protectedBlockMode() {
        return protectedBlockMode;
    }

    public GhostItemMode ghostItemMode() {
        return ghostItemMode;
    }

    private ProtectedBlockMode parseProtectedBlockMode() {
        String configured = plugin.getConfig().getString("protected-block-mode", "strict");
        return switch (configured.toLowerCase(java.util.Locale.ROOT)) {
            case "medium" -> ProtectedBlockMode.MEDIUM;
            default -> ProtectedBlockMode.STRICT;
        };
    }

    private GhostItemMode parseGhostItemMode() {
        String configured = plugin.getConfig().getString("ghost-item-mode", "medium");
        return switch (configured.toLowerCase(java.util.Locale.ROOT)) {
            case "hard" -> GhostItemMode.HARD;
            case "brute-force" -> GhostItemMode.BRUTE_FORCE;
            default -> GhostItemMode.MEDIUM;
        };
    }
}