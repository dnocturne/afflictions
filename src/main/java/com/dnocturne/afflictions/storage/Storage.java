package com.dnocturne.afflictions.storage;

import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Storage abstraction for affliction data persistence.
 */
public interface Storage {

    /**
     * Initialize the storage connection.
     *
     * @return true if successful
     */
    CompletableFuture<Boolean> init();

    /**
     * Shutdown the storage connection.
     */
    CompletableFuture<Void> shutdown();

    /**
     * Load player affliction data by UUID.
     *
     * @param uuid The player UUID
     * @return Optional containing the data, or empty if no data exists
     */
    CompletableFuture<Optional<PlayerAfflictionData>> loadPlayer(UUID uuid);

    /**
     * Load player affliction data by username.
     * Used for offline-mode servers where UUIDs may change.
     *
     * @param username The player username
     * @return Optional containing the data, or empty if no data exists
     */
    CompletableFuture<Optional<PlayerAfflictionData>> loadPlayerByName(String username);

    /**
     * Save player affliction data.
     *
     * @param data The player data to save
     */
    CompletableFuture<Void> savePlayer(PlayerAfflictionData data);

    /**
     * Delete player affliction data.
     *
     * @param uuid The player UUID
     */
    CompletableFuture<Void> deletePlayer(UUID uuid);

    /**
     * Check if player data exists.
     *
     * @param uuid The player UUID
     * @return true if data exists
     */
    CompletableFuture<Boolean> hasPlayer(UUID uuid);

    /**
     * Get the storage type name.
     */
    String getType();
}
