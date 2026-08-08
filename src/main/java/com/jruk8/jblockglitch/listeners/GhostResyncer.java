package com.jruk8.jblockglitch.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GhostResyncer {

    private int RESYNC_BELOW = 1; // blocks below the player's feet
    private int RESYNC_ABOVE = 2; // blocks above the player's feet

    /**
     * Sends block change packets to the player for all blocks in a quadrant-
     * pivoted 2x4x2 bounding box around their current location.
     *
     * Determines which quadrant of the player's current block their precise
     * position falls into (based on the fractional part of their X/Z
     * coordinates), then pivots the bounding box's inner corner onto that
     * block and extends it toward the quadrant's two orthogonal neighbors
     * plus the diagonal "other corner" neighbor. Vertically the box spans
     * one block below to two blocks above the player's feet.
     *
     * If the resulting box falls entirely outside the world's height limits
     * (e.g. the player is in the void), this is a no-op.
     */
    public void revalidateNearbyBlocks(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();

        int centerX = location.getBlockX();
        int centerY = location.getBlockY();
        int centerZ = location.getBlockZ();

        // Fractional position within the current block cell (each axis spans
        // [0, 1)). Splitting at the midpoint identifies which of the 4
        // quadrants (NE/NW/SE/SW) the player occupies.
        double fracX = location.getX() - centerX;
        double fracZ = location.getZ() - centerZ;

        // Which neighboring column shares that quadrant on each axis.
        int offsetX = (fracX < 0.5D) ? -1 : 1;
        int offsetZ = (fracZ < 0.5D) ? -1 : 1;

        // Pivot: inner corner = player's own block; outer corner = the
        // diagonal neighbor in the direction of the quadrant.
        int minX = Math.min(centerX, centerX + offsetX);
        int maxX = Math.max(centerX, centerX + offsetX);
        int minZ = Math.min(centerZ, centerZ + offsetZ);
        int maxZ = Math.max(centerZ, centerZ + offsetZ);

        // One block lower, two blocks higher -> 4 blocks tall.
        int minY = centerY - RESYNC_BELOW;
        int maxY = centerY + RESYNC_ABOVE;

        // Guard against the box lying entirely outside the world's height
        // bounds (e.g. player has fallen into the void below the build limit).
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();

        if (maxY < worldMinY || minY >= worldMaxY) {
            return; // Bounding box entirely outside world height bounds — nothing to sync
        }

        // Clamp so a box merely straddling the boundary still syncs its
        // in-bounds portion instead of being skipped entirely.
        minY = Math.max(minY, worldMinY);
        maxY = Math.min(maxY, worldMaxY - 1);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    player.sendBlockChange(block.getLocation(), block.getBlockData());
                }
            }
        }
    }
}
