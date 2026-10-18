package com.jruk8.jblockglitch.updatechecker.jblockglitch;

import com.jruk8.jblockglitch.updatechecker.ports.UpdateCheckLogger;
import java.util.logging.Logger;

/** Routes engine logging to the plugin logger. */
public final class JBlockGlitchUpdateCheckLogger implements UpdateCheckLogger {
    private final Logger logger;

    public JBlockGlitchUpdateCheckLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warning(message);
    }

    @Override
    public void severe(String message) {
        logger.severe(message);
    }
}
