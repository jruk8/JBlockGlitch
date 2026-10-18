package com.jruk8.jblockglitch.updatechecker.ports;

import com.jruk8.jblockglitch.updatechecker.UpdateAvailable;
import org.bukkit.entity.Player;

/** Delivers update notices to admins. Adapted per platform. */
public interface UpdateCheckNotifier {
    /** Notices every online admin. */
    void notifyAdmins(UpdateAvailable update);

    /** Notices one joining admin when an update is pending. */
    void notifyPlayer(Player player, UpdateAvailable update);
}
