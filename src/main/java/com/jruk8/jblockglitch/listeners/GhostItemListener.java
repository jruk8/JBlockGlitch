package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for ghost item attempts and blocks them
 * by forcing a client inventory re-sync.
 */
public final class GhostItemListener implements Listener {

    private final JBlockGlitchPlugin plugin;
    private final ModeService modeService;
    private final Map<UUID, Integer> actionCounts = new HashMap<>();
    private final CoreEvents coreEvents = new CoreEvents();
    private final InventoryClickEvents inventoryClickEvents = new InventoryClickEvents();
    private BukkitTask tickTask;

    public GhostItemListener(JBlockGlitchPlugin plugin, ModeService modeService) {
        this.plugin = plugin;
        this.modeService = modeService;

        if (!isBruteForce()) {
            // Event-based counting only matters when we're not already
            // resyncing everyone every tick.
            Bukkit.getPluginManager().registerEvents(coreEvents, plugin);
            if (modeService.ghostItemMode() == ModeService.GhostItemMode.HARD) {
                Bukkit.getPluginManager().registerEvents(inventoryClickEvents, plugin);
            }
        }

        startTickLoop();
    }

    /**
     * Single per-tick loop for all modes. The current mode is read from
     * {@link ModeService} on every tick so a live reload takes effect
     * immediately. In brute-force mode it just resyncs everyone
     * unconditionally. Otherwise, it checks each player's registered count
     * accumulated over the previous tick and resyncs only those with more
     * than one.
     */
    private void startTickLoop() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isBruteForce()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.updateInventory();
                }
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                Integer count = actionCounts.get(player.getUniqueId());
                if (count != null && count > 0) {
                    handlePotentialGhost(player);
                }
            }
            actionCounts.clear();
        }, 1L, 1L);
    }

    void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        HandlerList.unregisterAll(coreEvents);
        HandlerList.unregisterAll(inventoryClickEvents);
        actionCounts.clear();
    }

    private boolean isBruteForce() {
        return modeService.ghostItemMode() == ModeService.GhostItemMode.BRUTE_FORCE;
    }

    /**
     * Records an inventory-affecting action for this player this tick.
     * Only relevant for medium/hard; the tick loop reads and resets
     * these counts each tick.
     */
    private void registerAction(Player player) {
        if (isBruteForce()) {
            return;
        }
        actionCounts.merge(player.getUniqueId(), 1, Integer::sum);
    }

    private void handlePotentialGhost(Player player) {
        player.updateInventory();
    }

    /**
     * Well-known pairs: F+Q, hotbar-switch+drop, place+swap.
     * Registered for medium and hard only.
     */
    private final class CoreEvents implements Listener {

        @EventHandler
        public void onSwapHands(PlayerSwapHandItemsEvent event) {
            registerAction(event.getPlayer());
        }

        @EventHandler
        public void onDropItem(PlayerDropItemEvent event) {
            registerAction(event.getPlayer());
        }

        @EventHandler
        public void onHotbarSwitch(PlayerItemHeldEvent event) {
            registerAction(event.getPlayer());
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            registerAction(event.getPlayer());
        }
    }

    /**
     * Creative-mode slot placement (MC-277905: offhand swap and creative slot).
     * Only registered for hard since it can become laggy.
     */
    private final class InventoryClickEvents implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getWhoClicked() instanceof Player player) {
                registerAction(player);
            }
        }
    }
}