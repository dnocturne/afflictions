package com.dnocturne.afflictions.player;

import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a player's affliction state.
 */
public class AfflictedPlayer {

    private final UUID uuid;
    private final Map<String, AfflictionInstance> activeAfflictions = new HashMap<>();

    public AfflictedPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the Bukkit player if online.
     */
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }

    /**
     * Check if the player is online.
     */
    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    /**
     * Add an affliction to this player.
     *
     * @param instance The affliction instance
     * @return true if added, false if already has this affliction
     */
    public boolean addAffliction(AfflictionInstance instance) {
        String id = instance.getAfflictionId().toLowerCase();
        if (activeAfflictions.containsKey(id)) {
            return false;
        }
        activeAfflictions.put(id, instance);
        return true;
    }

    /**
     * Remove an affliction from this player.
     *
     * @param afflictionId The affliction ID
     * @return The removed affliction instance, or empty if not found
     */
    public Optional<AfflictionInstance> removeAffliction(String afflictionId) {
        return Optional.ofNullable(activeAfflictions.remove(afflictionId.toLowerCase()));
    }

    /**
     * Check if player has a specific affliction.
     *
     * @param afflictionId The affliction ID
     * @return true if afflicted
     */
    public boolean hasAffliction(String afflictionId) {
        return activeAfflictions.containsKey(afflictionId.toLowerCase());
    }

    /**
     * Get affliction instance for a specific affliction.
     *
     * @param afflictionId The affliction ID
     * @return Optional containing the instance, or empty if not afflicted
     */
    public Optional<AfflictionInstance> getAffliction(String afflictionId) {
        return Optional.ofNullable(activeAfflictions.get(afflictionId.toLowerCase()));
    }

    /**
     * Get all active afflictions.
     *
     * @return Unmodifiable collection of affliction instances
     */
    public Collection<AfflictionInstance> getAfflictions() {
        return Collections.unmodifiableCollection(activeAfflictions.values());
    }

    /**
     * Get the number of active afflictions.
     */
    public int getAfflictionCount() {
        return activeAfflictions.size();
    }

    /**
     * Check if the player has any afflictions.
     */
    public boolean hasAnyAffliction() {
        return !activeAfflictions.isEmpty();
    }

    /**
     * Clear all afflictions from this player.
     */
    public void clearAfflictions() {
        activeAfflictions.clear();
    }

    /**
     * Get all afflictions of a specific category.
     *
     * @param category The category to filter by
     * @return List of affliction instances in that category
     */
    public List<AfflictionInstance> getAfflictionsByCategory(AfflictionCategory category) {
        return activeAfflictions.values().stream()
                .filter(instance -> instance.getAffliction().getCategory() == category)
                .toList();
    }

    /**
     * Check if player has any affliction of a specific category.
     *
     * @param category The category to check
     * @return true if player has at least one affliction in that category
     */
    public boolean hasAfflictionInCategory(AfflictionCategory category) {
        return activeAfflictions.values().stream()
                .anyMatch(instance -> instance.getAffliction().getCategory() == category);
    }

    /**
     * Get the player's supernatural affliction (if any).
     * Since players can normally only have one supernatural affliction,
     * this returns the first one found.
     *
     * @return Optional containing the supernatural affliction instance
     */
    public Optional<AfflictionInstance> getSupernaturalAffliction() {
        return activeAfflictions.values().stream()
                .filter(instance -> instance.getAffliction().getCategory() == AfflictionCategory.SUPERNATURAL)
                .findFirst();
    }

    /**
     * Get all curses on this player.
     *
     * @return List of curse affliction instances
     */
    public List<AfflictionInstance> getCurses() {
        return getAfflictionsByCategory(AfflictionCategory.CURSE);
    }

    /**
     * Get the number of curses on this player.
     */
    public int getCurseCount() {
        return (int) activeAfflictions.values().stream()
                .filter(instance -> instance.getAffliction().getCategory() == AfflictionCategory.CURSE)
                .count();
    }
}
