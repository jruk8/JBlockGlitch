package com.jruk8.jblockglitch.updatechecker.jblockglitch;

import com.jruk8.jblockglitch.MessageService;
import com.jruk8.jblockglitch.updatechecker.ports.UpdateCheckNotifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Sends update notices to permission holders. */
public final class JBlockGlitchUpdateCheckNotifier implements UpdateCheckNotifier {
    /** Admins holding this permission hear about updates. */
    public static final String ADMIN_PERMISSION = "jblockglitch.admin";

    private final MessageService messages;

    public JBlockGlitchUpdateCheckNotifier(MessageService messages) {
        this.messages = messages;
    }

    @Override
    public void notifyAdmins(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            notifyPlayer(player, message);
        }
    }

    @Override
    public void notifyPlayer(Player player, String message) {
        if (player.hasPermission(ADMIN_PERMISSION)) {
            messages.sendUpdateAvailable(player, message);
        }
    }
}
