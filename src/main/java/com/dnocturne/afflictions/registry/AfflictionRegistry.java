package com.dnocturne.afflictions.registry;

import com.dnocturne.afflictions.api.affliction.Affliction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for all affliction types.
 */
public class AfflictionRegistry {

    private final Map<String, Affliction> afflictions = new HashMap<>();

    /**
     * Register an affliction type.
     *
     * @param affliction The affliction to register
     * @throws IllegalArgumentException if an affliction with the same ID already exists
     */
    public void register(Affliction affliction) {
        String id = affliction.getId().toLowerCase();
        if (afflictions.containsKey(id)) {
            throw new IllegalArgumentException("Affliction with ID '" + id + "' is already registered");
        }
        afflictions.put(id, affliction);
    }

    /**
     * Unregister an affliction type.
     *
     * @param id The affliction ID to unregister
     * @return true if the affliction was unregistered
     */
    public boolean unregister(String id) {
        return afflictions.remove(id.toLowerCase()) != null;
    }

    /**
     * Get an affliction by ID.
     *
     * @param id The affliction ID
     * @return Optional containing the affliction, or empty if not found
     */
    public Optional<Affliction> get(String id) {
        return Optional.ofNullable(afflictions.get(id.toLowerCase()));
    }

    /**
     * Check if an affliction is registered.
     *
     * @param id The affliction ID
     * @return true if registered
     */
    public boolean isRegistered(String id) {
        return afflictions.containsKey(id.toLowerCase());
    }

    /**
     * Get all registered afflictions.
     *
     * @return Unmodifiable collection of all afflictions
     */
    public Collection<Affliction> getAll() {
        return Collections.unmodifiableCollection(afflictions.values());
    }

    /**
     * Get all affliction IDs.
     *
     * @return Unmodifiable collection of all affliction IDs
     */
    public Collection<String> getAllIds() {
        return Collections.unmodifiableCollection(afflictions.keySet());
    }

    /**
     * Clear all registered afflictions.
     */
    public void clear() {
        afflictions.clear();
    }
}
