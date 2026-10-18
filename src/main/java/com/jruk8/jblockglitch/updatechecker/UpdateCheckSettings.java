package com.jruk8.jblockglitch.updatechecker;

import org.bukkit.configuration.file.FileConfiguration;

/** Update checker toggles: master switch plus per-severity switches. */
public record UpdateCheckSettings(boolean enabled, boolean major, boolean minor, boolean hotfix) {
    /** Reads the toggles from the update-checker section. */
    public static UpdateCheckSettings fromConfig(FileConfiguration config) {
        return new UpdateCheckSettings(
                config.getBoolean("update-checker.enabled", true),
                config.getBoolean("update-checker.releases.major", true),
                config.getBoolean("update-checker.releases.minor", true),
                config.getBoolean("update-checker.releases.hotfix", true));
    }

    /** True when the severity ("major", "minor", "hotfix") may notify. */
    public boolean allows(String severity) {
        return switch (severity) {
            case "major" -> major;
            case "minor" -> minor;
            case "hotfix" -> hotfix;
            default -> false;
        };
    }
}
