package com.rtnac.checks.combat;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * KillAura clients tend to snap the player's view instantly onto a target
 * right before the attack packet, then snap back — producing near-impossible
 * head rotation deltas correlated with attack timing. We track the yaw/pitch
 * at the moment of each attack and flag suspiciously large instantaneous
 * "snap" angles between consecutive attacks that happen in rapid succession
 * (a hallmark of multi-target aura, as opposed to a human manually flicking
 * between mobs, which takes noticeably longer).
 */
public class KillAuraCheck extends Check {

    public KillAuraCheck(RTNAC plugin) {
        super(plugin, "killaura", "killaura");
    }

    public void handleAttack(Player player, PlayerData data, boolean swungArmFirst) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;

        long now = System.currentTimeMillis();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        if (cfgBool("max-attack-range-no-swing", true) && !swungArmFirst) {
            flag(player, data, "attacked without a preceding arm-swing animation", 1.0);
        }

        long msSinceLastAttack = now - data.lastAttackTimeMs;

        if (data.lastAttackTimeMs != 0 && msSinceLastAttack < 300) {
            float yawDelta = angleDelta(data.lastYawBeforeAttack, yaw);
            float pitchDelta = Math.abs(pitch - data.lastPitchBeforeAttack);
            float totalDelta = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);

            double maxSnap = cfgDouble("max-angle-snap-deg", 165.0);
            if (totalDelta > maxSnap) {
                flag(player, data, String.format("view snapped %.1f° in %dms between attacks", totalDelta, msSinceLastAttack), 2.0);
            }
        }

        data.lastAttackTimeMs = now;
        data.lastYawBeforeAttack = yaw;
        data.lastPitchBeforeAttack = pitch;
    }

    private float angleDelta(float a, float b) {
        float delta = Math.abs(a - b) % 360f;
        return delta > 180f ? 360f - delta : delta;
    }
}
