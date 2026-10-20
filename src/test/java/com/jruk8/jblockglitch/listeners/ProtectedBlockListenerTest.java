package com.jruk8.jblockglitch.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtectedBlockListenerTest {

    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        player = server.addPlayer();
        player.teleport(new Location(world, 0.5, 65.0, 0.5));

        ModeService modes = mock(ModeService.class);
        when(modes.protectedBlockEnabled()).thenReturn(true);
        when(modes.protectedBlockMode()).thenReturn(ModeService.ProtectedBlockMode.MEDIUM);

        server.getPluginManager().registerEvents(
                new ProtectedBlockListener(modes), MockBukkit.createMockPlugin());
    }

    @AfterEach
    void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    private Block blockAt(int x, int y, int z, Material type) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(type);
        return block;
    }

    private PlayerInteractEvent deniedInteract(Material held, Block clicked) {
        PlayerInteractEvent event = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_BLOCK, new ItemStack(held), clicked, BlockFace.UP);
        event.setCancelled(true);
        return event;
    }

    private Location moveTo(double x, double y, double z) {
        PlayerMoveEvent move = new PlayerMoveEvent(
                player, player.getLocation(), new Location(world, x, y, z));
        server.getPluginManager().callEvent(move);
        return move.getTo();
    }

    private void cancelledPlace(Material held, Block target, Block against) {
        BlockPlaceEvent event = new BlockPlaceEvent(target, target.getState(), against,
                new ItemStack(held), player, true, EquipmentSlot.HAND);
        event.setCancelled(true);
        server.getPluginManager().callEvent(event);
    }

    private void standInside(int x, int y, int z) {
        player.teleport(new Location(world, x + 0.5, y, z + 0.5));
    }

    @Test
    void deniedSolidPlacementRubberbandsNextMove() {
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        server.getPluginManager().callEvent(deniedInteract(Material.DIRT, clicked));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(0.5, after.getX());
        assertEquals(0.5, after.getZ());
    }

    private void stampIntercept(long atMillis) {
        player.setMetadata("jmanhunt:intercept-consumed",
                new FixedMetadataValue(MockBukkit.createMockPlugin(), atMillis));
    }

    @Test
    void freshInterceptStampSkipsHandling() {
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        stampIntercept(System.currentTimeMillis());
        server.getPluginManager().callEvent(deniedInteract(Material.DIRT, clicked));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
        assertEquals(65.0, after.getY());
        assertEquals(10.5, after.getZ());
    }

    @Test
    void staleInterceptStampStillHandled() {
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        stampIntercept(System.currentTimeMillis() - 5000);
        server.getPluginManager().callEvent(deniedInteract(Material.DIRT, clicked));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(0.5, after.getX());
        assertEquals(0.5, after.getZ());
    }

    @Test
    void interactableClickWithoutSneakingIsIgnored() {
        Block chest = blockAt(10, 64, 10, Material.CHEST);
        player.setSneaking(false);
        server.getPluginManager().callEvent(deniedInteract(Material.DIRT, chest));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
        assertEquals(10.5, after.getZ());
    }

    @Test
    void sneakingPastInteractableIsHandled() {
        Block chest = blockAt(10, 64, 10, Material.CHEST);
        player.setSneaking(true);
        server.getPluginManager().callEvent(deniedInteract(Material.DIRT, chest));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(0.5, after.getX());
        assertEquals(0.5, after.getZ());
    }

    @Test
    void nonBlockItemIsIgnored() {
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        server.getPluginManager().callEvent(deniedInteract(Material.COMPASS, clicked));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
    }

    @Test
    void allowedItemUseIsIgnored() {
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        PlayerInteractEvent event = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.DIRT), clicked, BlockFace.UP);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.ALLOW);
        server.getPluginManager().callEvent(event);

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
    }

    @Test
    void deniedBucketStillHandled() {
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        server.getPluginManager().callEvent(deniedInteract(Material.WATER_BUCKET, clicked));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(0.5, after.getX());
        assertEquals(0.5, after.getZ());
    }

    @Test
    void obstructedPlaceSkipsHandling() {
        standInside(10, 65, 10);
        Block target = world.getBlockAt(10, 65, 10);
        cancelledPlace(Material.DIRT, target, blockAt(10, 64, 10, Material.STONE));

        Location after = moveTo(11.5, 65.0, 10.5);

        assertEquals(11.5, after.getX());
        assertEquals(10.5, after.getZ());
    }

    @Test
    void unobstructedPlaceStillRubberbands() {
        Block target = world.getBlockAt(10, 65, 10);
        cancelledPlace(Material.DIRT, target, blockAt(10, 64, 10, Material.STONE));

        Location after = moveTo(10.5, 65.0, 10.5);

        assertEquals(0.5, after.getX());
        assertEquals(0.5, after.getZ());
    }

    @Test
    void creativePlaceNeverSkips() {
        player.setGameMode(GameMode.CREATIVE);
        standInside(10, 65, 10);
        Block target = world.getBlockAt(10, 65, 10);
        cancelledPlace(Material.DIRT, target, blockAt(10, 64, 10, Material.STONE));

        Location after = moveTo(11.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
        assertEquals(10.5, after.getZ());
    }

    @Test
    void nonOccludingPlaceNeverSkips() {
        standInside(10, 65, 10);
        Block target = world.getBlockAt(10, 65, 10);
        cancelledPlace(Material.TORCH, target, blockAt(10, 64, 10, Material.STONE));

        Location after = moveTo(11.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
        assertEquals(10.5, after.getZ());
    }

    @Test
    void obstructedInteractSkipsHandling() {
        standInside(10, 65, 10);
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        server.getPluginManager().callEvent(deniedInteract(Material.DIRT, clicked));

        Location after = moveTo(11.5, 65.0, 10.5);

        assertEquals(11.5, after.getX());
        assertEquals(10.5, after.getZ());
    }

    @Test
    void obstructedBucketStillHandled() {
        standInside(10, 65, 10);
        Block clicked = blockAt(10, 64, 10, Material.STONE);
        server.getPluginManager().callEvent(deniedInteract(Material.WATER_BUCKET, clicked));

        Location after = moveTo(11.5, 65.0, 10.5);

        assertEquals(10.5, after.getX());
        assertEquals(10.5, after.getZ());
    }
}
