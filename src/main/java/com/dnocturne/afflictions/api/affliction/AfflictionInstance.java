package com.dnocturne.afflictions.api.affliction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents an active affliction on a specific player.
 * Holds runtime state and per-player data.
 */
public class AfflictionInstance {

    private final UUID playerUuid;
    private final Affliction affliction;
    private final long contractedAt;

    private int level;
    private long duration; // -1 for permanent
    private final Map<String, Object> data;

    public AfflictionInstance(UUID playerUuid, Affliction affliction) {
        this(playerUuid, affliction, 1, -1);
    }

    public AfflictionInstance(UUID playerUuid, Affliction affliction, int level, long duration) {
        this.playerUuid = playerUuid;
        this.affliction = affliction;
        this.level = level;
        this.duration = duration;
        this.contractedAt = System.currentTimeMillis();
        this.data = new HashMap<>();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(playerUuid));
    }

    public Affliction getAffliction() {
        return affliction;
    }

    public String getAfflictionId() {
        return affliction.getId();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.min(level, affliction.getMaxLevel());
    }

    public void incrementLevel() {
        setLevel(level + 1);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isPermanent() {
        return duration < 0;
    }

    public long getContractedAt() {
        return contractedAt;
    }

    public long getTimeAfflicted() {
        return System.currentTimeMillis() - contractedAt;
    }

    /**
     * Get custom data stored with this instance.
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }

    /**
     * Get custom data with a default value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Store custom data with this instance.
     */
    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public boolean hasData(String key) {
        return data.containsKey(key);
    }

    public void removeData(String key) {
        data.remove(key);
    }

    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
}
