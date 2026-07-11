package com.rtnac.checks.movement;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Flags players who stand/walk on top of water for longer than physically
 * possible (brief "surface tension" from client desync is normal, sustained
 * standing is not, unless riding a boat or having conduit/frost-walker etc.).
 */
public class JesusCheck extends Check {

    public JesusCheck(RTNAC plugin) {
        super(plugin, "jesus", "jesus");
    }

    public void handleMove(Player player, PlayerData data, Location to) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;
        if (player.getGameMode().name().equals("CREATIVE") || player.getGameMode().name().equals("SPECTATOR")) return;
        if (player.isInsideVehicle() || player.isSwimming() || player.isFlying()) return;

        Location below = to.clone().subtract(0, 0.1, 0);
        Material belowType = below.getBlock().getType();
        Material feetType = to.getBlock().getType();

        boolean standingOnWaterSurface = belowType == Material.WATER && feetType != Material.WATER
                && !player.isInWater() && to.getY() - Math.floor(to.getY()) > 0.85;

        // Frost walker creates ice which is a legitimate block underfoot, so it's excluded
        // automatically since belowType would be ICE/FROSTED_ICE, not WATER.

        if (standingOnWaterSurface) {
            data.waterTicks++;
        } else {
            data.waterTicks = 0;
        }

        int maxTicks = cfgInt("max-ticks-on-water", 6);
        if (data.waterTicks > maxTicks) {
            flag(player, data, "stood on water surface for " + data.waterTicks + " ticks", 1.5);
            data.waterTicks = 0;
        }
    }
}
