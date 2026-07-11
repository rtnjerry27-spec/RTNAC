package com.rtnac.checks.movement;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Flags players who remain airborne without falling, or ascend, for longer
 * than vanilla jump/gravity physics allow, while lacking elytra, levitation,
 * creative/spectator mode, or a vehicle.
 */
public class FlightCheck extends Check {

    public FlightCheck(RTNAC plugin) {
        super(plugin, "flight", "flight");
    }

    @Override
    public void onTick(Player player, PlayerData data) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return; // elytra
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.isInsideVehicle()) return;
        if (player.isSwimming()) return;
        if (player.getVehicle() != null) return;

        boolean onGround = player.isOnGround();

        if (onGround) {
            data.airTicks = 0;
            return;
        }

        data.airTicks++;

        int maxAirTicks = cfgInt("max-airtime-ticks", 40);

        // Water/lava/climbable negate fall physics legitimately - handled by JesusCheck/NoFall separately,
        // here we just avoid double-flagging obvious liquid/ladder scenarios.
        var blockAt = player.getLocation().getBlock();
        var blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
        if (blockAt.isLiquid() || blockBelow.isLiquid()) return;
        String belowType = blockBelow.getType().name();
        if (belowType.contains("LADDER") || belowType.contains("VINE") || belowType.contains("SCAFFOLDING")) return;

        if (data.airTicks > maxAirTicks) {
            flag(player, data, "airborne for " + data.airTicks + " ticks without falling", 1.5);
        }
    }
}
