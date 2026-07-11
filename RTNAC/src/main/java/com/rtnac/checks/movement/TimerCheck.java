package com.rtnac.checks.movement;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * Timer hacks make the client send movement packets faster than 20/sec to
 * simulate a faster game tick. We approximate detection by counting how many
 * PlayerMoveEvents arrive per real-world second and flagging sustained excess.
 * This is a coarse approximation without a packet-level library (e.g. ProtocolLib);
 * for production-grade timer detection, pairing RTNAC with ProtocolLib packet
 * timestamps is recommended (see README).
 */
public class TimerCheck extends Check {

    public TimerCheck(RTNAC plugin) {
        super(plugin, "timer", "timer");
    }

    public void handlePacket(Player player, PlayerData data) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;

        long now = System.currentTimeMillis();
        long delta = now - data.lastTickTimestampMs;
        data.lastTickTimestampMs = now;

        if (delta <= 0) return;

        data.recentTickDeltas.addLast(delta);
        while (data.recentTickDeltas.size() > 40) {
            data.recentTickDeltas.removeFirst();
        }
        if (data.recentTickDeltas.size() < 20) return;

        long sum = 0;
        for (long d : data.recentTickDeltas) sum += d;
        double avgDeltaMs = sum / (double) data.recentTickDeltas.size();
        double packetsPerSecond = 1000.0 / avgDeltaMs;

        double maxAllowed = cfgDouble("max-ticks-per-second", 22.5);

        if (packetsPerSecond > maxAllowed) {
            flag(player, data, String.format("packet rate %.1f/s (cap %.1f/s)", packetsPerSecond, maxAllowed), 1.0);
        }
    }
}
