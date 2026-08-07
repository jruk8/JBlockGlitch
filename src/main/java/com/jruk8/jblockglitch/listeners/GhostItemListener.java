package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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

    private static final int BLOCK_RESYNC_RADIUS = 1;

    private final JBlockGlitchPlugin plugin;
    private final ModeService modeService;
    private final Map<UUID, Integer> actionCounts = new HashMap<>();
    private final CoreEvents coreEvents = new CoreEvents();
    private final InventoryClickEvents inventoryClickEvents = new InventoryClickEvents();
    private BukkitTask tickTask;
    private int blockResyncTicks;

    public GhostItemListener(JBlockGlitchPlugin plugin, ModeService modeService) {
        this.plugin = plugin;
        this.modeService = modeService;

        Bukkit.getPluginManager().registerEvents(coreEvents, plugin);
        Bukkit.getPluginManager().registerEvents(inventoryClickEvents, plugin);

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
            if (!modeService.ghostItemEnabled()) {
                actionCounts.clear();
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

    private void handleBlockResync() {
        int interval = modeService.ghostItemResyncTickInterval();

        if (interval < 0) {
            blockResyncTicks = 0;
            return;
        }

        if (++blockResyncTicks < interval) {
            return;
        }

        blockResyncTicks = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            revalidateNearbyBlocks(player);
        }
    }

    /**
     * Sends block change packets to the player for all blocks in a 3x3x2
     * area around their current location. This is a brute-force way to
     * ensure the client has the correct block state and can help prevent
     * ghost item issues that arise from desyncs between client and server.
     */
    private void revalidateNearbyBlocks(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();

        int centerX = location.getBlockX();
        int centerY = location.getBlockY();
        int centerZ = location.getBlockZ();

        for (int x = centerX - BLOCK_RESYNC_RADIUS;
             x <= centerX + BLOCK_RESYNC_RADIUS;
             x++) {

            for (int y = centerY;
                 y <= centerY + 1;
                 y++) {

                for (int z = centerZ - BLOCK_RESYNC_RADIUS;
                     z <= centerZ + BLOCK_RESYNC_RADIUS;
                     z++) {

                    Block block = world.getBlockAt(x, y, z);

                    player.sendBlockChange(
                            block.getLocation(),
                            block.getBlockData()
                    );
                }
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

        actionCounts.clear();
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
}