package com.rtnac.commands;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RTNACCommand implements CommandExecutor {

    private final RTNAC plugin;

    public RTNACCommand(RTNAC plugin) {
        this.plugin = plugin;
    }

    private void msg(CommandSender sender, String raw) {
        Component c = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
        sender.sendMessage(c);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rtnac.admin")) {
            msg(sender, "&cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            msg(sender, "&8[&c&lRTNAC&8] &7v" + plugin.getDescription().getVersion()
                    + " &8- &7Use &f/rtnac <reload|checks|status|alerts|history> <player>");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadConfig();
                msg(sender, "&8[&c&lRTNAC&8] &aConfiguration reloaded.");
            }
            case "checks" -> {
                String checkList = plugin.getCheckManager().getChecks().stream()
                        .map(c -> (c.isEnabled() ? "&a" : "&c") + c.getName())
                        .collect(Collectors.joining("&7, "));
                msg(sender, "&8[&c&lRTNAC&8] &7Loaded checks: " + checkList);
            }
            case "status" -> {
                if (args.length < 2) {
                    msg(sender, "&cUsage: /rtnac status <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    msg(sender, "&cPlayer not found or offline.");
                    return true;
                }
                PlayerData data = plugin.getPlayerDataManager().getData(target);
                if (data == null) {
                    msg(sender, "&cNo data for that player.");
                    return true;
                }
                msg(sender, "&8[&c&lRTNAC&8] &7Violation levels for &f" + target.getName() + "&7:");
                for (Check c : plugin.getCheckManager().getChecks()) {
                    double vl = data.getVl(c.getName());
                    if (vl > 0) {
                        msg(sender, "  &7- &f" + c.getName() + "&7: &e" + String.format("%.1f", vl));
                    }
                }
            }
            case "alerts" -> {
                if (!(sender instanceof Player p)) {
                    msg(sender, "&cOnly players can toggle alerts this way; alerts are permission-gated (rtnac.alerts).");
                    return true;
                }
                msg(sender, "&8[&c&lRTNAC&8] &7Alerts are controlled by the &frtnac.alerts&7 permission node.");
            }
            case "history" -> {
                List<String> hist = plugin.getAlertManager().getHistory().stream().collect(Collectors.toList());
                int show = Math.min(15, hist.size());
                msg(sender, "&8[&c&lRTNAC&8] &7Last " + show + " alerts:");
                for (int i = hist.size() - show; i < hist.size(); i++) {
                    msg(sender, "  &7" + hist.get(i));
                }
            }
            default -> msg(sender, "&cUnknown subcommand. Use reload, checks, status, alerts, or history.");
        }
        return true;
    }
}
