package com.jruk8.jblockglitch.command;

/**
 * Permission nodes used by the JBlockGlitch commands, declared in
 * {@code paper-plugin.yml}.
 */
public final class Permissions {

    /**
     * View or change the plugin settings in-game.
     */
    public static final String CONFIG = "jblockglitch.config";

    /**
     * Reload the plugin configuration. Also receives update notices.
     */
    public static final String RELOAD = "jblockglitch.reload";

    /**
     * View the plugin help listing. Entries are filtered by permission.
     */
    public static final String HELP = "jblockglitch.help";

    /**
     * View the plugin version.
     */
    public static final String VERSION = "jblockglitch.version";

    private Permissions() {
    }
}
