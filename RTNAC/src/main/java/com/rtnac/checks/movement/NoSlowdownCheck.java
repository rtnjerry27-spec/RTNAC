package com.rtnac.checks.movement;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Vanilla forces reduced movement speed while using items (eating, blocking
 * with a sword/shield, drawing a bow) and while sneaking. Hacked clients
 * often ignore these slowdowns to keep full speed. This check compares
 * actual movement speed against the reduced-speed cap while such a state
 * is active.
 */
public class NoSlowdownCheck extends Check {

    public NoSlowdownCheck(RTNAC plugin) {
        super(plugin, "noslow", "noslowdown");
    }

    public void handleMove(Player player, PlayerData data, Location from, Location to) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;
        if (player.getGameMode().name().equals("CREATIVE") || player.getGameMode().name().equals("SPECTATOR")) return;

        boolean shouldBeSlowed = data.eatingOrBlocking;
        if (!shouldBeSlowed) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        double maxAllowed = cfgDouble("max-speed-while-active", 0.15);

        if (dist > maxAllowed) {
            flag(player, data, String.format("moved %.3f while using item/blocking (cap %.3f)", dist, maxAllowed), 1.0);
        }
    }
}
