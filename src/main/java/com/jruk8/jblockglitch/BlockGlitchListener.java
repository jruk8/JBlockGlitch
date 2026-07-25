package com.jruk8.jblockglitch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class BlockGlitchListener implements Listener {

    private static final long MOVEMENT_BACKSTOP_MILLIS = 250L;
    private final Map<UUID, Long> deniedPlacements = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        player.sendBlockChange(event.getBlockPlaced().getLocation(), event.getBlockPlaced().getBlockData());
        deniedPlacements.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Long deniedAt = deniedPlacements.get(playerId);
        if (deniedAt == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - deniedAt;
        if (elapsed > MOVEMENT_BACKSTOP_MILLIS) {
            deniedPlacements.remove(playerId);
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
}
