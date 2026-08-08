package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.Bootstrap;
import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Registers all event listeners with the Bukkit plugin manager.
 * Also tracks them so they can be shut down cleanly on plugin disable.
 */
public final class ListenerBootstrap implements Bootstrap {

    private final JBlockGlitchPlugin plugin;
    private final ModeService modeService;
    private GhostItemListener ghostItemListener;
    private ProtectedBlockListener protectedBlockListener;

    public ListenerBootstrap(JBlockGlitchPlugin plugin, ModeService modeService) {
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