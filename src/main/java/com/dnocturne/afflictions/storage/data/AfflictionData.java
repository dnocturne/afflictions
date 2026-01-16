package com.dnocturne.afflictions.storage.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Immutable data transfer object for a single affliction instance.
 * Represents serializable affliction data for persistence.
 *
 * @param afflictionId The affliction type ID
 * @param level        The affliction level
 * @param duration     The duration in ticks (-1 for permanent)
 * @param contractedAt The timestamp when contracted
 * @param data         Additional custom data
 */
public record AfflictionData(
        @NotNull String afflictionId,
        int level,
        long duration,
        long contractedAt,
        @NotNull Map<String, String> data
) {
    /**
     * Compact constructor that ensures data map is immutable.
     */
    public AfflictionData {
        data = Map.copyOf(data);
    }

    /**
     * Create affliction data without custom data.
     */
    public AfflictionData(@NotNull String afflictionId, int level, long duration, long contractedAt) {
        this(afflictionId, level, duration, contractedAt, Map.of());
    }

    /**
     * Get a specific data value by key.
     *
     * @param key The data key
     * @return The value, or null if not present
     */
    public @Nullable String getData(@NotNull String key) {
        return data.get(key);
    }
}
