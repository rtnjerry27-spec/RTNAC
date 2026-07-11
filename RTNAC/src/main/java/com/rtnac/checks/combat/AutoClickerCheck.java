package com.rtnac.checks.combat;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Two-signal autoclicker detection:
 *  1. Raw CPS ceiling — humans very rarely sustain >16-18 legitimate clicks/sec.
 *  2. Interval consistency — autoclickers (esp. simple ones) produce click
 *     intervals with unnaturally low standard deviation, since humans have
 *     natural micro-jitter in their clicking rhythm.
 */
public class AutoClickerCheck extends Check {

    public AutoClickerCheck(RTNAC plugin) {
        super(plugin, "autoclicker", "autoclicker");
    }

    public void handleClick(Player player, PlayerData data) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;

        long now = System.currentTimeMillis();
        data.recentClickTimestamps.addLast(now);

        long windowStart = now - 1000;
        while (!data.recentClickTimestamps.isEmpty() && data.recentClickTimestamps.peekFirst() < windowStart) {
            data.recentClickTimestamps.removeFirst();
        }

        int cps = data.recentClickTimestamps.size();
        int maxCps = cfgInt("max-cps", 18);

        if (cps > maxCps) {
            flag(player, data, cps + " CPS (cap " + maxCps + ")", 1.5);
        }

        int minSamples = cfgInt("min-samples", 15);
        if (data.recentClickTimestamps.size() >= minSamples) {
            List<Long> deltas = new ArrayList<>();
            Long prev = null;
            for (Long t : data.recentClickTimestamps) {
                if (prev != null) deltas.add(t - prev);
                prev = t;
            }
            double mean = deltas.stream().mapToLong(Long::longValue).average().orElse(0);
            double variance = deltas.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0);
            double stddev = Math.sqrt(variance);

            double maxStddev = cfgDouble("max-cps-stddev", 0.6) * 100; // scale ms domain
            if (stddev < maxStddev && mean < 90) {
                flag(player, data, String.format("click interval stddev %.1fms (too consistent)", stddev), 1.0);
            }
        }
    }
}
