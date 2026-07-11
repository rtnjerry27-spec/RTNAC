package com.rtnac.listeners;

import com.rtnac.RTNAC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerConnectionListener implements Listener {

    private final RTNAC plugin;

    public PlayerConnectionListener(RTNAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().createData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().removeData(event.getPlayer());
        plugin.getPunishmentManager().clearForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        // Reset movement-sensitive state on teleport to avoid false positives
        var data = plugin.getPlayerDataManager().getData(event.getPlayer());
        if (data != null) {
            data.airTicks = 0;
            data.waterTicks = 0;
            data.lastLocation = event.getTo();
        }
    }
}
