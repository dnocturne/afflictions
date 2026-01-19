package com.dnocturne.afflictions.api.affliction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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

    public AfflictionInstance(@NotNull UUID playerUuid, @NotNull Affliction affliction) {
        this(playerUuid, affliction, 1, -1);
    }

    public AfflictionInstance(@NotNull UUID playerUuid, @NotNull Affliction affliction, int level, long duration) {
        this(playerUuid, affliction, level, duration, System.currentTimeMillis());
    }

    public AfflictionInstance(@NotNull UUID playerUuid, @NotNull Affliction affliction, int level, long duration, long contractedAt) {
        this.playerUuid = playerUuid;
        this.affliction = affliction;
        this.level = level;
        this.duration = duration;
        this.contractedAt = contractedAt;
        this.data = new HashMap<>();
    }

    public @NotNull UUID getPlayerUuid() {
        return playerUuid;
    }

    public @NotNull Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(playerUuid));
    }

    public @NotNull Affliction getAffliction() {
        return affliction;
    }

    public @NotNull String getAfflictionId() {
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
     *
     * @param key  The data key
     * @param type The expected type class
     * @return The value cast to the expected type, or null if not present or wrong type
     */
    public <T> @Nullable T getData(@NotNull String key, @NotNull Class<T> type) {
        Object value = data.get(key);
        if (value == null || !type.isInstance(value)) {
            return null;
        }
        return type.cast(value);
    }

    /**
     * Get custom data with a default value.
     *
     * @param key          The data key
     * @param type         The expected type class
     * @param defaultValue The default value if not present or wrong type
     * @return The value cast to the expected type, or defaultValue
     */
    public <T> T getData(@NotNull String key, @NotNull Class<T> type, T defaultValue) {
        Object value = data.get(key);
        if (value == null || !type.isInstance(value)) {
            return defaultValue;
        }
        return type.cast(value);
    }

    /**
     * Get custom data stored with this instance as Object.
     * Prefer {@link #getData(String, Class)} for type-safe access.
     *
     * @param key The data key
     * @return The raw value, or null if not present
     */
    public @Nullable Object getData(@NotNull String key) {
        return data.get(key);
    }

    /**
     * Store custom data with this instance.
     */
    public void setData(@NotNull String key, @Nullable Object value) {
        data.put(key, value);
    }

    public boolean hasData(@NotNull String key) {
        return data.containsKey(key);
    }

    public void removeData(@NotNull String key) {
        data.remove(key);
    }

    public @NotNull Map<String, Object> getAllData() {
        return Collections.unmodifiableMap(data);
    }
}
