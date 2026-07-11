package com.rtnac.checks;

import com.rtnac.RTNAC;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Base class for every detection check in RTNAC.
 * Each concrete check is responsible for:
 *   1. Reading config values relevant to it (via configPath)
 *   2. Deciding when to flag a violation (call flag())
 *   3. Naming itself uniquely (used as config key + punishment key)
 */
public abstract class Check {

    protected final RTNAC plugin;
    private final String name;
    private final String configPath;

    protected Check(RTNAC plugin, String name, String configPath) {
        this.plugin = plugin;
        this.name = name;
        this.configPath = configPath;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("checks." + configPath + ".enabled", true);
    }

    protected double cfgDouble(String key, double def) {
        return plugin.getConfig().getDouble("checks." + configPath + "." + key, def);
    }

    protected int cfgInt(String key, int def) {
        return plugin.getConfig().getInt("checks." + configPath + "." + key, def);
    }

    protected boolean cfgBool(String key, boolean def) {
        return plugin.getConfig().getBoolean("checks." + configPath + "." + key, def);
    }

    /**
     * Raise a violation for the given player with a human-readable reason.
     * Handles VL accumulation, alerting staff, and triggering punishments.
     */
    protected void flag(Player player, PlayerData data, String reason, double vlAmount) {
        if (player.hasPermission("rtnac.bypass")) return;
        data.addVl(name, vlAmount);
        plugin.getAlertManager().sendAlert(player, name, reason, data.getVl(name));
        plugin.getPunishmentManager().evaluate(player, name, data.getVl(name));
    }

    /** Called every server tick for every online player who isn't bypassing. */
    public void onTick(Player player, PlayerData data) {
        // default no-op; overridden by tick-based checks (flight, timer, jesus...)
    }
}
