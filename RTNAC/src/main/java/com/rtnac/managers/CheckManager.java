package com.rtnac.managers;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.checks.blocks.FastBreakCheck;
import com.rtnac.checks.blocks.FastPlaceCheck;
import com.rtnac.checks.blocks.ScaffoldCheck;
import com.rtnac.checks.combat.AutoClickerCheck;
import com.rtnac.checks.combat.KillAuraCheck;
import com.rtnac.checks.combat.ReachCheck;
import com.rtnac.checks.movement.FlightCheck;
import com.rtnac.checks.movement.JesusCheck;
import com.rtnac.checks.movement.NoFallCheck;
import com.rtnac.checks.movement.NoSlowdownCheck;
import com.rtnac.checks.movement.SpeedCheck;
import com.rtnac.checks.movement.TimerCheck;

import java.util.ArrayList;
import java.util.List;

public class CheckManager {

    private final List<Check> checks = new ArrayList<>();

    public CheckManager(RTNAC plugin) {
        checks.add(new SpeedCheck(plugin));
        checks.add(new FlightCheck(plugin));
        checks.add(new NoFallCheck(plugin));
        checks.add(new JesusCheck(plugin));
        checks.add(new NoSlowdownCheck(plugin));
        checks.add(new TimerCheck(plugin));
        checks.add(new KillAuraCheck(plugin));
        checks.add(new ReachCheck(plugin));
        checks.add(new AutoClickerCheck(plugin));
        checks.add(new ScaffoldCheck(plugin));
        checks.add(new FastPlaceCheck(plugin));
        checks.add(new FastBreakCheck(plugin));
    }

    public List<Check> getChecks() {
        return checks;
    }

    public <T extends Check> T get(Class<T> clazz) {
        for (Check c : checks) {
            if (clazz.isInstance(c)) return clazz.cast(c);
        }
        return null;
    }
}
