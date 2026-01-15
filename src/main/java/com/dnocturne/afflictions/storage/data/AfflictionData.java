package com.dnocturne.afflictions.storage.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for a single affliction instance.
 * Represents serializable affliction data for persistence.
 */
public class AfflictionData {

    private final String afflictionId;
    private final int level;
    private final long duration;
    private final long contractedAt;
    private final Map<String, String> data;

    public AfflictionData(String afflictionId, int level, long duration, long contractedAt) {
        this(afflictionId, level, duration, contractedAt, new HashMap<>());
    }

    public AfflictionData(String afflictionId, int level, long duration, long contractedAt, Map<String, String> data) {
        this.afflictionId = afflictionId;
        this.level = level;
        this.duration = duration;
        this.contractedAt = contractedAt;
        this.data = new HashMap<>(data);
    }

    public String getAfflictionId() {
        return afflictionId;
    }

    public int getLevel() {
        return level;
    }

    public long getDuration() {
        return duration;
    }

    public long getContractedAt() {
        return contractedAt;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void setData(String key, String value) {
        data.put(key, value);
    }
}
