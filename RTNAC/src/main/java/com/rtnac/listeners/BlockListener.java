package com.rtnac.listeners;

import com.rtnac.RTNAC;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockListener implements Listener {

    private final RTNAC plugin;
    // tracks when each player started digging the block they're currently on (for FastBreak)
    private final Map<UUID, Long> breakStartTimes = new HashMap<>();

    public BlockListener(RTNAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getData(player);
        if (data == null) return;

        plugin.getCheckManager().get(com.rtnac.checks.blocks.ScaffoldCheck.class)
                .handlePlace(player, data, event.getBlockPlaced());

        plugin.getCheckManager().get(com.rtnac.checks.blocks.FastPlaceCheck.class)
                .handlePlace(player, data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            breakStartTimes.putIfAbsent(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getData(player);
        if (data == null) return;

        long start = breakStartTimes.getOrDefault(player.getUniqueId(), 0L);
        breakStartTimes.remove(player.getUniqueId());

        plugin.getCheckManager().get(com.rtnac.checks.blocks.FastBreakCheck.class)
                .handleBreak(player, data, event.getBlock(), start);

        data.lastBlockBreakMs = System.currentTimeMillis();
    }
}
