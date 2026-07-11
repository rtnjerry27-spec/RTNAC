package com.rtnac.checks.combat;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Flags melee attacks landed from beyond vanilla's ~3.0 block interaction
 * range (creative allows slightly further).
 */
public class ReachCheck extends Check {

    public ReachCheck(RTNAC plugin) {
        super(plugin, "reach", "reach");
    }

    public void handleAttack(Player attacker, PlayerData data, Entity target) {
        if (!isEnabled() || attacker.hasPermission("rtnac.bypass")) return;

        double distance = attacker.getEyeLocation().distance(target.getLocation());
        double max = attacker.getGameMode() == GameMode.CREATIVE
                ? cfgDouble("max-reach-creative", 5.1)
                : cfgDouble("max-reach-survival", 3.1);

        if (distance > max) {
            flag(attacker, data, String.format("hit target from %.2f blocks (cap %.2f)", distance, max), 2.0);
        }
    }
}
