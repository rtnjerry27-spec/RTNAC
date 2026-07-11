package com.rtnac.checks.blocks;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Scaffold hacks place blocks beneath the player's feet while running,
 * typically without the player looking down to target the block face
 * (real bridging requires looking down-ish, or at minimum a consistent,
 * human-plausible placement angle). This check flags rapid successive
 * under-feet placements where look angle deviates from the expected
 * downward range in a suspiciously consistent (bot-like) way.
 */
public class ScaffoldCheck extends Check {

    public ScaffoldCheck(RTNAC plugin) {
        super(plugin, "scaffold", "scaffold");
    }

    public void handlePlace(Player player, PlayerData data, Block placed) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;

        Location playerLoc = player.getLocation();
        boolean underFeet = placed.getY() < playerLoc.getY() && placed.getX() == Math.floor(playerLoc.getX())
                && placed.getZ() == Math.floor(playerLoc.getZ());

        float pitch = player.getLocation().getPitch();

        if (underFeet) {
            // Legit bridging: player looks down (pitch roughly > 40deg) before placing.
            // A pitch near 0 (looking straight ahead) while placing under-feet blocks
            // while sprinting is a strong scaffold signature.
            data.recentPlaceYaws.addLast(playerLoc.getYaw());
            while (data.recentPlaceYaws.size() > 10) data.recentPlaceYaws.removeFirst();

            if (player.isSprinting() && pitch < 35f) {
                data.placesInWindow++;
                int minPattern = cfgInt("min-placements-for-pattern", 5);
                if (data.placesInWindow >= minPattern) {
                    flag(player, data, "sprint-bridging without looking down (pitch " + String.format("%.1f", pitch) + "°)", 1.5);
                    data.placesInWindow = 0;
                }
            } else {
                data.placesInWindow = Math.max(0, data.placesInWindow - 1);
            }
        }
    }
}
