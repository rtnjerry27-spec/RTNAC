package com.rtnac.listeners;

import com.rtnac.RTNAC;
import com.rtnac.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Tracks whether a player is currently in a "slowed" client state — eating,
 * drinking, blocking with sword/axe, or drawing a bow — so NoSlowdownCheck
 * can compare actual movement speed against the reduced cap while active.
 */
public class ItemUseListener implements Listener {

    private final RTNAC plugin;

    public ItemUseListener(RTNAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getData(player);
        if (data == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item == null) return;
            Material type = item.getType();

            boolean isFoodOrBlockingItem = item.getType().isEdible()
                    || type == Material.SHIELD
                    || type == Material.BOW
                    || type == Material.CROSSBOW
                    || type.name().endsWith("_SWORD")
                    || type.name().endsWith("_AXE")
                    || type == Material.POTION;

            if (isFoodOrBlockingItem) {
                data.eatingOrBlocking = true;
                // Cleared shortly after via scheduled task in RTNAC's tick loop
                // (see RTNAC#startTickTask) once item use stops registering.
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getData(event.getPlayer());
        if (data != null) data.eatingOrBlocking = false;
    }
}
