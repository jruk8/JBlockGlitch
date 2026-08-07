package com.jruk8.jblockglitch.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for block placement events that are denied by the server and prevents
 * players from glitching with the block.
 *
 * MEDIUM mode simply rubberbands the player back to their last-tick location
 * on all axes with no exceptions.
 *
 * STRICT mode only intervenes when the player
 * is actually standing inside the denied block's column and snaps them to
 * the exact center (x.5, z.5) of that block.
 */
public final class ProtectedBlockListener implements Listener {

    private static final long MOVEMENT_BACKSTOP_MILLIS = 250L;

    private final ModeService modeService;
    private final Map<UUID, DeniedPlacement> deniedPlacements = new HashMap<>();

    public ProtectedBlockListener(ModeService modeService) {
        this.modeService = modeService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!modeService.protectedBlockEnabled()) {
            return;
        }

        if (!event.isCancelled()) {
            return;
        }

        handleBlockEvent(
                event.getPlayer(),
                event.getBlockPlaced().getLocation(),
                event.getBlockPlaced().getBlockData()
        );
    }

    /**
     * Handles the case where a player attempts to place a block using a bucket
     * (water, lava, or powder snow) and the placement is denied by the server.
     *
     * For some reason, this event does NOT fire in WorldGuard regions specifically.
     * WorldGuard's bucket event is handled through PlayerInteractEvent instead.
     * Thus, it is here for other plugins that may deny placement.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!modeService.protectedBlockEnabled()) {
            return;
        }

        if (!event.isCancelled()) {
            return;
        }

        BlockData attemptedBlockData = switch (event.getBucket()) {
            case WATER_BUCKET -> Material.WATER.createBlockData();
            case LAVA_BUCKET -> Material.LAVA.createBlockData();
            case POWDER_SNOW_BUCKET -> Material.POWDER_SNOW.createBlockData();
            default -> null;
        };

        if (attemptedBlockData == null) {
            return;
        }

        handleBlockEvent(
                event.getPlayer(),
                event.getBlock().getLocation(),
                attemptedBlockData
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!modeService.protectedBlockEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.useInteractedBlock() != Event.Result.DENY) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Material material = item.getType();

        if (material != Material.WATER_BUCKET
                && material != Material.LAVA_BUCKET
                && material != Material.POWDER_SNOW_BUCKET) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Block target = clicked.getRelative(event.getBlockFace());

        BlockData attemptedData = switch (material) {
            case WATER_BUCKET -> Material.WATER.createBlockData();
            case LAVA_BUCKET -> Material.LAVA.createBlockData();
            case POWDER_SNOW_BUCKET -> Material.POWDER_SNOW.createBlockData();
            default -> null;
        };

        handleBlockEvent(
                event.getPlayer(),
                target.getLocation(),
                attemptedData
        );
    }

    private void handleBlockEvent(
            Player player,
            Location blockLocation,
            BlockData attemptedBlockData
    ) {
        player.sendBlockChange(blockLocation, attemptedBlockData);

        DeniedPlacement deniedPlacement = new DeniedPlacement(
                blockLocation.clone(),
                System.currentTimeMillis()
        );

        deniedPlacements.put(player.getUniqueId(), deniedPlacement);

        if (modeService.protectedBlockMode() == ModeService.ProtectedBlockMode.STRICT
                && isStandingOnDeniedBlock(player.getLocation(), blockLocation)) {

            rubberbandToBlockCenter(player, blockLocation);
            deniedPlacements.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!modeService.protectedBlockEnabled()) {
            return;
        }

        UUID playerId = event.getPlayer().getUniqueId();
        DeniedPlacement deniedPlacement = deniedPlacements.get(playerId);

        if (deniedPlacement == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - deniedPlacement.deniedAt();

        if (elapsed > MOVEMENT_BACKSTOP_MILLIS) {
            deniedPlacements.remove(playerId);
            return;
        }

        if (modeService.protectedBlockMode() == ModeService.ProtectedBlockMode.STRICT) {
            if (isStandingOnDeniedBlock(event.getTo(), deniedPlacement.blockLocation())) {
                event.setTo(blockCenterLocation(
                        event.getTo(),
                        deniedPlacement.blockLocation()
                ));
                deniedPlacements.remove(playerId);
            }
            return;
        }

        event.setTo(event.getFrom());
        deniedPlacements.remove(playerId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        deniedPlacements.remove(event.getPlayer().getUniqueId());
    }

    private boolean isStandingOnDeniedBlock(
            Location playerLocation,
            Location blockLocation
    ) {
        int blockX = blockLocation.getBlockX();
        int blockY = blockLocation.getBlockY();
        int blockZ = blockLocation.getBlockZ();

        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();
        double playerZ = playerLocation.getZ();

        boolean withinX = playerX >= blockX - 0.3 && playerX <= blockX + 1.3;
        boolean withinZ = playerZ >= blockZ - 0.3 && playerZ <= blockZ + 1.3;
        boolean withinY = playerY >= blockY && playerY <= blockY + 3.25;

        return withinX && withinY && withinZ;
    }

    private void rubberbandToBlockCenter(Player player, Location blockLocation) {
        player.teleport(blockCenterLocation(
                player.getLocation(),
                blockLocation
        ));
    }

    private Location blockCenterLocation(
            Location playerLocation,
            Location blockLocation
    ) {
        Location rubberband = playerLocation.clone();
        rubberband.setX(rubberband.getBlockX() + 0.5);
        rubberband.setY(blockLocation.getBlockY());
        rubberband.setZ(rubberband.getBlockZ() + 0.5);
        return rubberband;
    }

    private record DeniedPlacement(Location blockLocation, long deniedAt) {
    }
}