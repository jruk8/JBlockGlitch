package com.jruk8.jblockglitch.listeners;

import com.jruk8.jblockglitch.JBlockGlitchPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GhostItemListenerTest {

    private ServerMock server;
    private WorldMock world;
    private GhostItemListener listener;
    private GhostResyncer resyncer;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");

        JBlockGlitchPlugin plugin = mock(JBlockGlitchPlugin.class);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getName()).thenReturn("JBlockGlitch");
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getPluginLoader())
                .thenReturn(MockBukkit.createMockPlugin("JBlockGlitch").getPluginLoader());

        ModeService modes = mock(ModeService.class);
        when(modes.ghostItemEnabled()).thenReturn(true);
        when(modes.ghostItemMode()).thenReturn(ModeService.GhostItemMode.MEDIUM);
        when(modes.ghostItemResyncTickInterval()).thenReturn(5);

        resyncer = mock(GhostResyncer.class);
        listener = new GhostItemListener(plugin, modes, resyncer);
    }

    @AfterEach
    void tearDown() {
        if (listener != null) {
            listener.shutdown();
            listener = null;
        }
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    private Location location(double x, double y, double z) {
        return new Location(world, x, y, z);
    }

    private void move(Player player, Location from, Location to) {
        server.getPluginManager().callEvent(new PlayerMoveEvent(player, from, to));
    }

    @Test
    void resyncTickOnlyProcessesMovedPlayers() {
        PlayerMock mover = server.addPlayer();
        PlayerMock stayer = server.addPlayer();

        move(mover, location(0, 64, 0), location(1, 64, 0));

        server.getScheduler().performTicks(5);

        verify(resyncer, times(1)).revalidateNearbyBlocks(mover);
        verify(resyncer, never()).revalidateNearbyBlocks(stayer);
    }

    @Test
    void headOnlyRotationQueuesNothing() {
        PlayerMock player = server.addPlayer();
        Location to = location(0, 64, 0);
        to.setYaw(90.0F);
        move(player, location(0, 64, 0), to);

        server.getScheduler().performTicks(5);

        verify(resyncer, never()).revalidateNearbyBlocks(any(Player.class));
    }

    @Test
    void quitClearsQueuedResync() {
        PlayerMock player = server.addPlayer();
        move(player, location(0, 64, 0), location(1, 64, 0));
        server.getPluginManager().callEvent(new PlayerQuitEvent(player, "bye"));

        server.getScheduler().performTicks(5);

        verify(resyncer, never()).revalidateNearbyBlocks(any(Player.class));
    }
}
