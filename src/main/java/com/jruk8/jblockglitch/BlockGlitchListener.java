package com.jruk8.jblockglitch;

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

final class BlockGlitchListener implements Listener {

    private static final long MOVEMENT_BACKSTOP_MILLIS = 250L;
    private final JBlockGlitchPlugin plugin;
    private final Map<UUID, DeniedPlacement> deniedPlacements = new HashMap<>();

    BlockGlitchListener(JBlockGlitchPlugin plugin) {
        this.plugin = plugin;
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

        if (plugin.getDetectionMode() == DetectionMode.STRICT) {
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

        if (plugin.getDetectionMode() == DetectionMode.STRICT) {
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
        return playerLocation.getBlockX() == blockLocation.getBlockX()
                && playerLocation.getBlockZ() == blockLocation.getBlockZ()
                && playerLocation.getY() >= blockLocation.getBlockY()
                && playerLocation.getY() <= blockLocation.getBlockY() + 1.0;
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
