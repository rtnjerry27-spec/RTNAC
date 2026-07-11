package com.rtnac;

import com.rtnac.commands.RTNACCommand;
import com.rtnac.data.PlayerData;
import com.rtnac.listeners.BlockListener;
import com.rtnac.listeners.CombatListener;
import com.rtnac.listeners.ItemUseListener;
import com.rtnac.listeners.MovementListener;
import com.rtnac.listeners.PlayerConnectionListener;
import com.rtnac.managers.AlertManager;
import com.rtnac.managers.CheckManager;
import com.rtnac.managers.PlayerDataManager;
import com.rtnac.managers.PunishmentManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RTNAC extends JavaPlugin {

    private static RTNAC instance;

    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.playerDataManager = new PlayerDataManager();
        this.alertManager = new AlertManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.checkManager = new CheckManager(this);

        // Register any players already online (e.g. /reload)
        for (Player p : getServer().getOnlinePlayers()) {
            playerDataManager.createData(p);
        }

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(this), this);

        var cmd = getCommand("rtnac");
        if (cmd != null) {
            cmd.setExecutor(new RTNACCommand(this));
        }

        startTickTask();
        startDecayTask();

        getLogger().info("RTNAC enabled - " + checkManager.getChecks().size() + " checks loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("RTNAC disabled.");
    }

    /** Runs every server tick: drives tick-based checks (flight, timer-adjacent state, item-use timeout). */
    private void startTickTask() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                PlayerData data = playerDataManager.getData(player);
                if (data == null) continue;
                if (player.hasPermission("rtnac.bypass")) continue;

                for (var check : checkManager.getChecks()) {
                    check.onTick(player, data);
                }

                // Clear "eating/blocking" flag if the player released the button
                // (no reliable release event exists for right-click-hold in Bukkit,
                // so we time it out after a short window instead).
            }
        }, 1L, 1L);
    }

    /** Slowly decays violation levels for players who are behaving, every N seconds. */
    private void startDecayTask() {
        long periodTicks = Math.max(20L, getConfig().getLong("settings.violation-decay-seconds", 10) * 20L);
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : playerDataManager.getAll().values()) {
                data.decayAll(1.0);
            }
        }, periodTicks, periodTicks);
    }

    public static RTNAC getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
}
