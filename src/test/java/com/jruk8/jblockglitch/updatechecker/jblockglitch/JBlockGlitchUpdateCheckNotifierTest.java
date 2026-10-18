package com.jruk8.jblockglitch.updatechecker.jblockglitch;

import com.jruk8.jblockglitch.MessageService;
import com.jruk8.jblockglitch.updatechecker.UpdateAvailable;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JBlockGlitchUpdateCheckNotifierTest {

    private static final UpdateAvailable UPDATE = new UpdateAvailable(
            "plain", "3.2.0", "3.1.0", "https://example.invalid/r");

    @Test
    void permissionHolderReceivesNotice() {
        MessageService messages = mock(MessageService.class);
        JBlockGlitchUpdateCheckNotifier notifier = new JBlockGlitchUpdateCheckNotifier(messages);
        Player admin = mock(Player.class);
        when(admin.hasPermission(JBlockGlitchUpdateCheckNotifier.ADMIN_PERMISSION)).thenReturn(true);

        notifier.notifyPlayer(admin, UPDATE);

        verify(messages).sendUpdateAvailable(admin, UPDATE);
    }

    @Test
    void withoutPermissionStaysSilent() {
        MessageService messages = mock(MessageService.class);
        JBlockGlitchUpdateCheckNotifier notifier = new JBlockGlitchUpdateCheckNotifier(messages);
        Player player = mock(Player.class);
        when(player.hasPermission(JBlockGlitchUpdateCheckNotifier.ADMIN_PERMISSION)).thenReturn(false);

        notifier.notifyPlayer(player, UPDATE);

        verify(messages, never()).sendUpdateAvailable(player, UPDATE);
    }
}
