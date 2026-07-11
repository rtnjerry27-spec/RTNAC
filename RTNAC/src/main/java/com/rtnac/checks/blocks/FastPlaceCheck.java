package com.rtnac.checks.blocks;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Vanilla clients are rate-limited to placing one block roughly every
 * ~2.5-4 client ticks depending on version. FastPlace hacks bypass this
 * client-side cooldown. We track server-side timestamps between placements.
 */
public class FastPlaceCheck extends Check {

    public FastPlaceCheck(RTNAC plugin) {
        super(plugin, "fastplace", "fastplace");
    }

    public void handlePlace(Player player, PlayerData data) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;

        long now = System.currentTimeMillis();
        long delta = now - data.lastBlockPlaceMs;
        data.lastBlockPlaceMs = now;

        if (delta <= 0) return;

        double perSecond = 1000.0 / delta;
        double maxAllowed = cfgDouble("max-blocks-per-second", 12);

        if (perSecond > maxAllowed && delta < 1000) {
            flag(player, data, String.format("placed blocks at %.1f/s (cap %.1f/s)", perSecond, maxAllowed), 1.0);
        }
    }
}
