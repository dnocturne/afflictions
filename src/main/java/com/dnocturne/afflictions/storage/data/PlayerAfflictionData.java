package com.dnocturne.afflictions.storage.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object for player affliction storage.
 * Represents serializable player data for persistence.
 */
public class PlayerAfflictionData {

    private final UUID uuid;
    private final String username;
    private final List<AfflictionData> afflictions;

    public PlayerAfflictionData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.afflictions = new ArrayList<>();
    }

    public PlayerAfflictionData(UUID uuid, String username, List<AfflictionData> afflictions) {
        this.uuid = uuid;
        this.username = username;
        this.afflictions = new ArrayList<>(afflictions);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public List<AfflictionData> getAfflictions() {
        return afflictions;
    }

    public void addAffliction(AfflictionData affliction) {
        afflictions.add(affliction);
    }

    public void removeAffliction(String afflictionId) {
        afflictions.removeIf(a -> a.getAfflictionId().equalsIgnoreCase(afflictionId));
    }

    public boolean hasAffliction(String afflictionId) {
        return afflictions.stream()
                .anyMatch(a -> a.getAfflictionId().equalsIgnoreCase(afflictionId));
    }
}
