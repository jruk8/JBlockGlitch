package com.jruk8.jblockglitch.updatechecker;

/**
 * A found update: the plain console-friendly message, the new and running
 * versions, and the release page URL for link rendering.
 */
public record UpdateAvailable(String message, String newVersion, String currentVersion, String url) {
}
