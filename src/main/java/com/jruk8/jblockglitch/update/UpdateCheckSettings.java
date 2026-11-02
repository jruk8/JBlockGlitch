package com.jruk8.jblockglitch.update;

import com.jruk8.jblockglitch.config.PluginConfig;

/**
 * Update checker toggles: master switch plus per-severity switches.
 */
public record UpdateCheckSettings(boolean enabled, boolean major, boolean minor, boolean hotfix) {

    /**
     * Reads the toggles from the update-checker section.
     *
     * @param checker the config section
     * @return the resolved settings
     */
    public static UpdateCheckSettings fromConfig(PluginConfig.UpdateChecker checker) {
        return new UpdateCheckSettings(
                checker.isEnabled(),
                checker.getReleases().isMajor(),
                checker.getReleases().isMinor(),
                checker.getReleases().isHotfix());
    }

    /**
     * Returns whether the severity ("major", "minor", "hotfix") may notify.
     *
     * @param severity the severity name
     * @return {@code true} when that severity notifies
     */
    public boolean allows(String severity) {
        return switch (severity) {
            case "major" -> major;
            case "minor" -> minor;
            case "hotfix" -> hotfix;
            default -> false;
        };
    }
}
