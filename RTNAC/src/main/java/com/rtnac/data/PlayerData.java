package com.rtnac.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds all rolling state RTNAC needs to evaluate a single player across ticks.
 * One instance per online player, created on join, destroyed on quit.
 */
public class PlayerData {

    private final UUID uuid;

    // ---- movement tracking ----
    public Location lastLocation;
    public Location currentLocation;
    public long lastMoveTimeMs = System.currentTimeMillis();
    public int airTicks = 0;
    public int groundTicks = 0;
    public double lastFallDistance = 0;
    public boolean tookFallDamageRecently = false;
    public int waterTicks = 0;
    public boolean onIce = false;

    // ---- combat tracking ----
    public long lastAttackTimeMs = 0;
    public float lastYawBeforeAttack = 0f;
    public float lastPitchBeforeAttack = 0f;
    public boolean swungBeforeLastDamage = false;
    public final Deque<Long> recentClickTimestamps = new ArrayDeque<>();
    public final Deque<Float> recentAttackAngles = new ArrayDeque<>();

    // ---- timer tracking ----
    public long lastTickTimestampMs = System.currentTimeMillis();
    public final Deque<Long> recentTickDeltas = new ArrayDeque<>();

    // ---- block tracking ----
    public long lastBlockPlaceMs = 0;
    public long lastBlockBreakMs = 0;
    public Location lastBlockPlaceLoc;
    public final Deque<Float> recentPlaceYaws = new ArrayDeque<>();
    public int placesInWindow = 0;

    // ---- state flags ----
    public boolean eatingOrBlocking = false;
    public boolean sneaking = false;

    // ---- violation levels, keyed by check name ----
    public final Map<String, Double> violationLevels = new HashMap<>();
    public final Map<String, Long> lastViolationTimestamp = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getVl(String checkName) {
        return violationLevels.getOrDefault(checkName, 0.0);
    }

    public void addVl(String checkName, double amount) {
        double newVl = getVl(checkName) + amount;
        violationLevels.put(checkName, newVl);
        lastViolationTimestamp.put(checkName, System.currentTimeMillis());
    }

    public void resetVl(String checkName) {
        violationLevels.put(checkName, 0.0);
    }

    public void decayAll(double amount) {
        for (Map.Entry<String, Double> entry : violationLevels.entrySet()) {
            double v = entry.getValue() - amount;
            entry.setValue(Math.max(0, v));
        }
    }
}
