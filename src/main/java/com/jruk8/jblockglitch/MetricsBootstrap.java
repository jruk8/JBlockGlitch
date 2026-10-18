package com.jruk8.jblockglitch;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class MetricsBootstrap implements Bootstrap {

    private static final int PLUGIN_ID = 33146;
    private final JavaPlugin plugin;
    private Metrics metrics;

    public MetricsBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        if (!statisticsEnabled()) {
            plugin.getLogger().info("Anonymous statistics are disabled (toggle-anonymous-statistics: false).");
            return;
        }

        metrics = new Metrics(plugin, PLUGIN_ID);
    }

    /**
     * Applies a live reload: starts statistics when newly enabled, stops
     * them when newly disabled.
     */
    public void reload() {
        if (statisticsEnabled() && metrics == null) {
            metrics = new Metrics(plugin, PLUGIN_ID);
        } else if (!statisticsEnabled() && metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
    }

    public void shutdown() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
    }

    private boolean statisticsEnabled() {
        return plugin.getConfig().getBoolean("toggle-anonymous-statistics", true);
    }
}
