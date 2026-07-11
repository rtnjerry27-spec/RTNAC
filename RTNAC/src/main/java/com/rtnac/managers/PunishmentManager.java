package com.rtnac.managers;

import com.rtnac.RTNAC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads punishments.actions.<check> from config.yml, a list of {vl, command}
 * entries. When a player's violation level for a check crosses a threshold
 * for the first time, the associated command is dispatched from console.
 */
public class PunishmentManager {

    private final RTNAC plugin;
    // playerUUID -> checkName -> set of thresholds already triggered
    private final Map<String, Set<Integer>> triggered = new ConcurrentHashMap<>();

    public PunishmentManager(RTNAC plugin) {
        this.plugin = plugin;
    }

    public void evaluate(Player player, String checkName, double vl) {
        if (!plugin.getConfig().getBoolean("punishments.enabled", true)) return;

        List<Map<?, ?>> actions = plugin.getConfig().getMapList("punishments.actions." + checkName);
        if (actions == null || actions.isEmpty()) return;

        String key = player.getUniqueId() + ":" + checkName;
        Set<Integer> done = triggered.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());

        for (Map<?, ?> action : actions) {
            Object vlObj = action.get("vl");
            Object cmdObj = action.get("command");
            if (vlObj == null || cmdObj == null) continue;

            int threshold = (int) Double.parseDouble(vlObj.toString());
            if (vl >= threshold && !done.contains(threshold)) {
                done.add(threshold);
                String command = cmdObj.toString().replace("%player%", player.getName());
                Bukkit.getScheduler().runTask(plugin, () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        }
    }

    public void clearForPlayer(Player player) {
        triggered.keySet().removeIf(k -> k.startsWith(player.getUniqueId().toString()));
    }
}
