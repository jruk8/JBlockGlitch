package com.jruk8.jblockglitch.core;

/**
 * Common contract for plugin subsystems that need to register
 * their components (commands, listeners, etc.) on enable.
 */
public interface Bootstrap {

    void register();
}
