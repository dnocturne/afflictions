package com.dnocturne.afflictions.manager;

import com.dnocturne.afflictions.player.AfflictedPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages afflicted player data.
 * <p>
 * This class is thread-safe and can be accessed from async operations
 * (e.g., storage callbacks, async events).
 */
public class PlayerManager {

    private final Map<UUID, AfflictedPlayer> players = new ConcurrentHashMap<>();

    /**
     * Get or create an AfflictedPlayer for a UUID.
     *
     * @param uuid The player UUID
     * @return The AfflictedPlayer instance
     */
    public AfflictedPlayer getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, AfflictedPlayer::new);
    }

    /**
     * Get an AfflictedPlayer if they exist.
     *
     * @param uuid The player UUID
     * @return Optional containing the player, or empty if not tracked
     */
    public Optional<AfflictedPlayer> get(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    /**
     * Check if a player is being tracked.
     *
     * @param uuid The player UUID
     * @return true if tracked
     */
    public boolean isTracked(UUID uuid) {
        return players.containsKey(uuid);
    }

    /**
     * Remove a player from tracking.
     *
     * @param uuid The player UUID
     * @return The removed player data, or empty if not tracked
     */
    public Optional<AfflictedPlayer> remove(UUID uuid) {
        return Optional.ofNullable(players.remove(uuid));
    }

    /**
     * Get all tracked players.
     *
     * @return Unmodifiable collection of all afflicted players
     */
    public Collection<AfflictedPlayer> getAll() {
        return Collections.unmodifiableCollection(players.values());
    }

    /**
     * Get all tracked players that have at least one affliction.
     *
     * @return Collection of players with afflictions
     */
    public Collection<AfflictedPlayer> getAllAfflicted() {
        return players.values().stream()
                .filter(AfflictedPlayer::hasAnyAffliction)
                .toList();
    }

    /**
     * Clear all player data.
     */
    public void clear() {
        players.clear();
    }
}
