package com.dnocturne.afflictions.storage.data;

import com.dnocturne.basalt.storage.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Immutable data transfer object for player affliction storage.
 * Represents serializable player data for persistence.
 *
 * @param uuid        The player's UUID
 * @param username    The player's username (for offline mode support)
 * @param afflictions The list of affliction data
 */
public record PlayerAfflictionData(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull List<AfflictionData> afflictions
) implements PlayerData {
    /**
     * Compact constructor that ensures afflictions list is immutable.
     */
    public PlayerAfflictionData {
        afflictions = List.copyOf(afflictions);
    }

    /**
     * Create player data with no afflictions.
     */
    public PlayerAfflictionData(@NotNull UUID uuid, @NotNull String username) {
        this(uuid, username, List.of());
    }

    /**
     * Check if the player has a specific affliction.
     *
     * @param afflictionId The affliction ID to check
     * @return true if the player has this affliction
     */
    public boolean hasAffliction(@NotNull String afflictionId) {
        return afflictions.stream()
                .anyMatch(a -> a.afflictionId().equalsIgnoreCase(afflictionId));
    }

    @Override
    public @NotNull UUID getUuid() {
        return uuid;
    }

    @Override
    public @NotNull String getUsername() {
        return username;
    }
}
