package com.rtnac.listeners;

import com.rtnac.RTNAC;
import com.rtnac.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class MovementListener implements Listener {

    private final RTNAC plugin;

    public MovementListener(RTNAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return; // pure look, no positional change
        }

        PlayerData data = plugin.getPlayerDataManager().getData(player);
        if (data == null) return;

        Material below = event.getTo().clone().subtract(0, 1, 0).getBlock().getType();
        data.onIce = below == Material.ICE || below == Material.PACKED_ICE
                || below == Material.BLUE_ICE || below == Material.FROSTED_ICE;

        // track fall distance server-side for NoFall comparisons
        if (!player.isOnGround() && event.getTo().getY() < event.getFrom().getY()) {
            data.lastFallDistance = Math.max(data.lastFallDistance, player.getFallDistance());
        }
        if (player.isOnGround()) {
            // landed - let NoFallCheck compare client-reported fall distance
            plugin.getCheckManager().get(com.rtnac.checks.movement.NoFallCheck.class)
                    .handleLand(player, data, player.getFallDistance());
            data.lastFallDistance = 0;
        }

        plugin.getCheckManager().get(com.rtnac.checks.movement.SpeedCheck.class)
                .handleMove(player, data, event.getFrom(), event.getTo());

        plugin.getCheckManager().get(com.rtnac.checks.movement.JesusCheck.class)
                .handleMove(player, data, event.getTo());

        plugin.getCheckManager().get(com.rtnac.checks.movement.NoSlowdownCheck.class)
                .handleMove(player, data, event.getFrom(), event.getTo());

        plugin.getCheckManager().get(com.rtnac.checks.movement.TimerCheck.class)
                .handlePacket(player, data);

        data.lastLocation = event.getFrom();
        data.currentLocation = event.getTo();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getData(event.getPlayer());
        if (data != null) data.sneaking = event.isSneaking();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            PlayerData data = plugin.getPlayerDataManager().getData(player);
            if (data != null) data.tookFallDamageRecently = true;
        }
    }
}
