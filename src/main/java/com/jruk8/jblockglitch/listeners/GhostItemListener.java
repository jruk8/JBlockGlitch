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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Listens for ghost item attempts and blocks them
 * by forcing a client inventory re-sync.
 */
public final class GhostItemListener implements Listener {

    private final JBlockGlitchPlugin plugin;
    private final ModeService modeService;
    private final GhostResyncer ghostResyncer;
    private final Map<UUID, Integer> actionCounts = new HashMap<>();
    private final Set<UUID> pendingBlockResync = new HashSet<>();
    private final CoreEvents coreEvents = new CoreEvents();
    private final InventoryClickEvents inventoryClickEvents = new InventoryClickEvents();
    private final ResyncQueueEvents resyncQueueEvents = new ResyncQueueEvents();
    private BukkitTask tickTask;
    private int blockResyncTicks;

    public GhostItemListener(JBlockGlitchPlugin plugin, ModeService modeService, GhostResyncer ghostResyncer) {
        this.plugin = plugin;
        this.modeService = modeService;
        this.ghostResyncer = ghostResyncer;

        Bukkit.getPluginManager().registerEvents(coreEvents, plugin);
        Bukkit.getPluginManager().registerEvents(inventoryClickEvents, plugin);
        Bukkit.getPluginManager().registerEvents(resyncQueueEvents, plugin);

        startTickLoop();
    }

    /**
     * Single per-tick loop for all modes. The current mode is read from
     * {@link ModeService} on every tick so a live reload takes effect
     * immediately. In brute-force mode it just resyncs everyone
     * unconditionally. Otherwise, it checks each player's registered count
     * accumulated over the previous tick and resyncs only players with at
     * least one action.
     */
    private void startTickLoop() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!modeService.ghostItemEnabled()) {
                actionCounts.clear();
                pendingBlockResync.clear();
                blockResyncTicks = 0;
                return;
            }

            if (isBruteForce()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.updateInventory();
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Integer count = actionCounts.get(player.getUniqueId());

                    if (count != null && count > 0) {
                        handlePotentialGhost(player);
                    }
                }

                actionCounts.clear();
            }

            handleBlockResync();
        }, 1L, 1L);
    }

    /**
     * Resyncs only players with a queued resync. Movement queues a resync
     * (see {@link ResyncQueueEvents}), so a player who moved at any point
     * since the previous resync tick is still resynced on this tick, while
     * stationary players are skipped entirely.
     */
    private void handleBlockResync() {
        int interval = modeService.ghostItemResyncTickInterval();

        if (interval < 0) {
            blockResyncTicks = 0;
            pendingBlockResync.clear();
            return;
        }

        if (++blockResyncTicks < interval) {
            return;
        }

        blockResyncTicks = 0;

        if (pendingBlockResync.isEmpty()) {
            return;
        }

        Set<UUID> queued = new HashSet<>(pendingBlockResync);
        pendingBlockResync.clear();

        for (UUID playerId : queued) {
            Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                ghostResyncer.revalidateNearbyBlocks(player);
            }
        }
    }

    void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        HandlerList.unregisterAll(coreEvents);
        HandlerList.unregisterAll(inventoryClickEvents);
        HandlerList.unregisterAll(resyncQueueEvents);

        actionCounts.clear();
        pendingBlockResync.clear();
        blockResyncTicks = 0;
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
        if (!modeService.ghostItemEnabled() || isBruteForce()) {
            return;
        }

        actionCounts.merge(player.getUniqueId(), 1, Integer::sum);
    }

    private void handlePotentialGhost(Player player) {
        player.updateInventory();
    }

    /**
     * Queues a block resync for this player. The next resync tick consumes
     * the queue, so movement anywhere inside the interval still triggers a
     * resync. Applies to every ghost-item mode, including brute-force.
     */
    private void queueBlockResync(Player player) {
        if (!modeService.ghostItemEnabled()
                || modeService.ghostItemResyncTickInterval() < 0) {
            return;
        }

        pendingBlockResync.add(player.getUniqueId());
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
            if (event.getWhoClicked() instanceof Player player
                    && modeService.ghostItemEnabled()
                    && modeService.ghostItemMode() == ModeService.GhostItemMode.HARD) {

                registerAction(player);
            }
        }
    }

    /**
     * Queues block resyncs on movement so the resync tick only processes
     * players who actually moved. Head-only rotation (unchanged position)
     * does not queue anything. Teleports queue as well since
     * {@link org.bukkit.event.player.PlayerTeleportEvent} extends
     * {@link PlayerMoveEvent}.
     */
    private final class ResyncQueueEvents implements Listener {

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (!hasChangedPosition(event)) {
                return;
            }

            queueBlockResync(event.getPlayer());
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            pendingBlockResync.remove(event.getPlayer().getUniqueId());
        }

        private boolean hasChangedPosition(PlayerMoveEvent event) {
            if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
                return true;
            }

            return event.getFrom().getX() != event.getTo().getX()
                    || event.getFrom().getY() != event.getTo().getY()
                    || event.getFrom().getZ() != event.getTo().getZ();
        }
    }
}