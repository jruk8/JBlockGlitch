package com.jruk8.jblockglitch.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for block placement events that are denied by the server and prevents
 * players from glitching with the block by moving them back to the ground.
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
        if (!event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        player.sendBlockChange(event.getBlockPlaced().getLocation(), event.getBlockPlaced().getBlockData());
        DeniedPlacement deniedPlacement = new DeniedPlacement(
                event.getBlockPlaced().getLocation().clone(), System.currentTimeMillis());
        deniedPlacements.put(player.getUniqueId(), deniedPlacement);

        if (modeService.protectedBlockMode() == ModeService.ProtectedBlockMode.STRICT) {
            if (isStandingOnDeniedBlock(player.getLocation(), deniedPlacement.blockLocation())) {
                rubberbandToBlockY(player, deniedPlacement.blockLocation());
                deniedPlacements.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
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
                event.setTo(rubberbandLocation(event.getTo(), deniedPlacement.blockLocation()));
                deniedPlacements.remove(playerId);
            }
            return;
        }

        if (event.getTo().getY() > event.getFrom().getY()) {
            event.setTo(event.getFrom());
            deniedPlacements.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        deniedPlacements.remove(event.getPlayer().getUniqueId());
    }

    private boolean isStandingOnDeniedBlock(Location playerLocation, Location blockLocation) {
        // check if player is inside a 3x10x3 box, the bottom of it centered on the block.
        int blockX = blockLocation.getBlockX();
        int blockY = blockLocation.getBlockY();
        int blockZ = blockLocation.getBlockZ();

        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();
        double playerZ = playerLocation.getZ();

        boolean withinX = playerX >= blockX - 1.0 && playerX <= blockX + 2.0;
        boolean withinZ = playerZ >= blockZ - 1.0 && playerZ <= blockZ + 2.0;
        boolean withinY = playerY >= blockY && playerY <= blockY + 10.0;

        return withinX && withinZ && withinY;
    }

    private void rubberbandToBlockY(Player player, Location blockLocation) {
        player.teleport(rubberbandLocation(player.getLocation(), blockLocation));
    }

    private Location rubberbandLocation(Location playerLocation, Location blockLocation) {
        Location rubberband = playerLocation.clone();
        rubberband.setY(blockLocation.getBlockY());
        return rubberband;
    }

    private record DeniedPlacement(Location blockLocation, long deniedAt) {
    }
}