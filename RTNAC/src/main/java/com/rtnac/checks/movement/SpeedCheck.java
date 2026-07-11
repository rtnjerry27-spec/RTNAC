package com.rtnac.checks.movement;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Flags players moving horizontally faster than vanilla physics allow,
 * accounting for sprint, jump boost, speed potions, and ice friction.
 * Evaluated on every PlayerMoveEvent.
 */
public class SpeedCheck extends Check {

    public SpeedCheck(RTNAC plugin) {
        super(plugin, "speed", "speed");
    }

    public void handleMove(Player player, PlayerData data, Location from, Location to) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;
        if (player.getGameMode().name().equals("CREATIVE") || player.getGameMode().name().equals("SPECTATOR")) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horizontalDistSq = dx * dx + dz * dz;
        double horizontalDist = Math.sqrt(horizontalDistSq);

        // Only evaluate roughly once per real second's worth of movement noise;
        // per-move-event deltas are noisy so we compare against a per-tick cap.
        double baseCap = cfgDouble("max-blocks-per-second", 5.6) / 20.0; // convert to per-tick

        double multiplier = 1.0;
        if (data.onIce) {
            multiplier *= cfgDouble("ice-multiplier", 1.9);
        }
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            multiplier *= 1.0 + (amplifier * cfgDouble("buff-per-speed-level", 0.18));
        }
        if (player.isSprinting()) {
            multiplier *= 1.3;
        }

        double allowed = baseCap * multiplier;

        // Grace period right after teleport / respawn / velocity knockback isn't tracked
        // here in detail; a simple buffer keeps false positives low.
        double buffer = allowed * 1.35;

        if (horizontalDist > buffer) {
            double excess = horizontalDist - buffer;
            flag(player, data, String.format("moved %.2f blocks (limit ~%.2f)", horizontalDist, buffer),
                    Math.min(3.0, 1.0 + excess));
        } else {
            data.resetVl(getName());
        }
    }
}
