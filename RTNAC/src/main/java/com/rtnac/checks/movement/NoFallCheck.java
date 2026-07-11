package com.rtnac.checks.movement;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Flags players who accrue significant fall distance but land without taking
 * (or reporting) fall damage — the classic NoFall hack signature.
 */
public class NoFallCheck extends Check {

    public NoFallCheck(RTNAC plugin) {
        super(plugin, "nofall", "nofall");
    }

    public void handleLand(Player player, PlayerData data, float clientFallDistance) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;
        if (player.getGameMode().name().equals("CREATIVE") || player.getGameMode().name().equals("SPECTATOR")) return;

        double minFall = cfgDouble("min-fall-distance-for-damage", 3.5);

        // Server-tracked fall distance vs what the client reports; large legitimate
        // gaps (feather falling, water landing, slime block) are excluded by the
        // event caller before this is invoked (see PlayerMoveListener).
        if (clientFallDistance <= 0.05 && data.lastFallDistance >= minFall) {
            flag(player, data, String.format("landed after %.1f blocks fall with no damage report", data.lastFallDistance), 2.0);
        }
    }
}
