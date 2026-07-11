package com.rtnac.listeners;

import com.rtnac.RTNAC;
import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

public class CombatListener implements Listener {

    private final RTNAC plugin;

    public CombatListener(RTNAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        PlayerData data = plugin.getPlayerDataManager().getData(attacker);
        if (data == null) return;

        boolean swungFirst = data.swungBeforeLastDamage;
        data.swungBeforeLastDamage = false; // reset for next attack

        plugin.getCheckManager().get(com.rtnac.checks.combat.KillAuraCheck.class)
                .handleAttack(attacker, data, swungFirst);

        plugin.getCheckManager().get(com.rtnac.checks.combat.ReachCheck.class)
                .handleAttack(attacker, data, event.getEntity());

        plugin.getCheckManager().get(com.rtnac.checks.combat.AutoClickerCheck.class)
                .handleClick(attacker, data);
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getData(event.getPlayer());
        if (data != null) data.swungBeforeLastDamage = true;
    }
}
