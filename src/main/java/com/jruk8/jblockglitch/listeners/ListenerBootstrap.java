package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.core.Bootstrap;
import com.jruk8.jblockglitch.core.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers all event listeners with the Bukkit plugin manager.
 * Also tracks them so they can be shut down cleanly on plugin disable.
 */
public final class ListenerBootstrap implements Bootstrap, Reloadable {

    private final JavaPlugin plugin;
    private final ModeService modeService;
    private GhostItemListener ghostItemListener;
    private ProtectedBlockListener protectedBlockListener;

    public ListenerBootstrap(JavaPlugin plugin, ModeService modeService) {
        this.plugin = plugin;
        this.modeService = modeService;
    }

    @Override
    public void register() {
        protectedBlockListener = new ProtectedBlockListener(modeService);
        Bukkit.getPluginManager().registerEvents(protectedBlockListener, plugin);
        ghostItemListener = new GhostItemListener(plugin, modeService, new GhostResyncer());
    }

    /**
     * Tears down and re-registers all listeners. This cancels the ghost item
     * tick scheduler and unregisters its events before rebuilding them from
     * the current mode, so switching between medium/hard/brute-force on a
     * live reload takes effect without leaking tasks or event handlers.
     */
    @Override
    public void reload() {
        shutdown();
        register();
    }

    public void shutdown() {
        if (ghostItemListener != null) {
            ghostItemListener.shutdown();
            ghostItemListener = null;
        }
        if (protectedBlockListener != null) {
            HandlerList.unregisterAll(protectedBlockListener);
            protectedBlockListener = null;
        }
    }
}
