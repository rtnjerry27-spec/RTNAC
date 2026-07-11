package com.rtnac.managers;

import com.rtnac.RTNAC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public class AlertManager {

    private final RTNAC plugin;
    private final Deque<String> history = new ArrayDeque<>();
    private static final int HISTORY_LIMIT = 200;

    public AlertManager(RTNAC plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(Player cheater, String checkName, String reason, double vl) {
        if (!plugin.getConfig().getBoolean("settings.alerts-enabled", true)) return;

        String prefix = plugin.getConfig().getString("settings.alert-prefix", "&8[&c&lRTNAC&8] &r");
        String raw = prefix + "&f" + cheater.getName() + " &7failed &c" + checkName.toUpperCase(Locale.ROOT)
                + " &7(vl=&e" + String.format(Locale.ROOT, "%.1f", vl) + "&7) &8- &7" + reason;

        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("rtnac.alerts")) {
                staff.sendMessage(message);
            }
        }

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[ALERT] " + cheater.getName() + " -> " + checkName + " vl=" + vl + " (" + reason + ")");
        }

        String histLine = "[" + java.time.LocalDateTime.now() + "] " + cheater.getName() + " failed " + checkName
                + " vl=" + String.format(Locale.ROOT, "%.1f", vl) + " - " + reason;
        history.addLast(histLine);
        if (history.size() > HISTORY_LIMIT) history.removeFirst();
    }

    public Deque<String> getHistory() {
        return history;
    }
}
