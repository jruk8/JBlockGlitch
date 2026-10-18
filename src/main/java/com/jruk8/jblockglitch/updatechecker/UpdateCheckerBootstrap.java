package com.jruk8.jblockglitch.updatechecker;

import com.jruk8.jblockglitch.Bootstrap;
import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import com.jruk8.jblockglitch.MessageService;
import com.jruk8.jblockglitch.updatechecker.jblockglitch.JBlockGlitchUpdateCheckHttp;
import com.jruk8.jblockglitch.updatechecker.jblockglitch.JBlockGlitchUpdateCheckLogger;
import com.jruk8.jblockglitch.updatechecker.jblockglitch.JBlockGlitchUpdateCheckNotifier;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Wires the update checker: builds the engine with JBlockGlitch adapters,
 * registers the join listener, and runs the first check asynchronously.
 * Versions resolve from this repository's own GitHub releases.
 */
public final class UpdateCheckerBootstrap implements Bootstrap {

    private final JBlockGlitchPlugin plugin;
    private final MessageService messageService;
    private UpdateCheckService service;
    private UpdateCheckJoinListener joinListener;

    public UpdateCheckerBootstrap(JBlockGlitchPlugin plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
    }

    @Override
    public void register() {
        JBlockGlitchUpdateCheckNotifier notifier = new JBlockGlitchUpdateCheckNotifier(messageService);
        service = new UpdateCheckService(
                new JBlockGlitchUpdateCheckHttp("jruk8", "JBlockGlitch"),
                notifier,
                new JBlockGlitchUpdateCheckLogger(plugin.getLogger()));
        joinListener = new UpdateCheckJoinListener(service, notifier);
        Bukkit.getPluginManager().registerEvents(joinListener, plugin);
        checkNow();
    }

    /**
     * Runs one check against the live config, off the main thread. Safe to
     * call on reload so update-checker toggles apply immediately.
     */
    public void checkNow() {
        if (service == null) {
            return;
        }

        UpdateCheckSettings settings = UpdateCheckSettings.fromConfig(plugin.getConfig());
        String version = plugin.getPluginMeta().getVersion();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> service.checkNow(settings, version));
    }

    public void shutdown() {
        if (joinListener != null) {
            HandlerList.unregisterAll(joinListener);
            joinListener = null;
        }
        service = null;
    }
}
