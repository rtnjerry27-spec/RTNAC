package com.rtnac.managers;

import com.rtnac.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();

    public PlayerData getData(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(), PlayerData::new);
    }

    public PlayerData getData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void createData(Player player) {
        dataMap.put(player.getUniqueId(), new PlayerData(player.getUniqueId()));
    }

    public void removeData(Player player) {
        dataMap.remove(player.getUniqueId());
    }

    public Map<UUID, PlayerData> getAll() {
        return dataMap;
    }
}
